package pt.florinhas.requisicoes.config;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import pt.florinhas.requisicoes.domain.ManutencaoItem;
import pt.florinhas.requisicoes.repository.ManutencaoItemRepository;

@Slf4j
@Component
@Order(3)
public class ManutencaoItemSeed implements CommandLineRunner {

    private final ManutencaoItemRepository manutencaoItemRepository;

    public ManutencaoItemSeed(ManutencaoItemRepository manutencaoItemRepository) {
        this.manutencaoItemRepository = manutencaoItemRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("--- STARTING COMPREHENSIVE MANUTENCAO ITEM SEED ---");

        // 1. Cleanup Legacy/Incorrect categories used in previous refactor
        // (Daycare/Preschool)
        cleanupLegacyCategories();

        // 2. Client-Approved Base Lists from merged historic sources (Consolidated)
        Map<String, List<String>> espacosPorCategoria = Map.of(
                "CATL", List.of(
                        "WC masculino", "WC feminino", "Salão", "Salão (palco)"),
                "RC", List.of(
                        "Parque exterior", "Relvado", "Acolhimento pré", "Acolhimento creche",
                        "Gabinete", "WC deficientes", "WC Rosa", "WC azul", "Gabinete médico",
                        "Oficina", "Corredor + WC", "Biblioteca", "Refeitório", "Lavatórios + Hall",
                        "Elevador", "Escadas acesso 1º"),
                "PRE_ESCOLAR", List.of(
                        "Sala acolhimento", "Sala de educadoras", "WC deficientes", "WC azul",
                        "WC cor de rosa", "Hall", "Escadas acesso 2º", "Corredor", "Sala Amarela",
                        "Sala Azul", "Sala Verde", "Sala Arco-Íris", "WC", "Parque exterior"),
                "CRECHE", List.of(
                        "Parque ext. 3º andar", "S. Acolhimento grande", "S. Acollhimento peq.",
                        "WC", "WC azul", "Corredor e hall", "Escadas acesso sotão",
                        "Sala Amarela limão", "Sala Verde Alface", "Sala Vermelha", "WC",
                        "Refeitório", "Copa", "Fraldário", "Sala azul turquesa", "Berçário"));

        // Standard verification items from historic code
        List<String> verificacoesPadrao = List.of(
                "Alumínios (janelas)", "Blackouts", "Caixilhos (madeiras)", "Armários",
                "Aquecedores", "Torneiras", "Eletricidade", "Cabides", "Paredes", "Tetos", "Chão");

        // Map current records BEFORE adding new ones (to handle changes/reseed
        // gracefully)
        Map<String, ManutencaoItem> existentesPorChave = manutencaoItemRepository.findAll().stream()
                .collect(Collectors.toMap(
                        item -> (item.getCategoria() + "|" + item.getEspaco() + "|" + item.getItemVerificacao())
                                .toLowerCase(),
                        Function.identity(),
                        (existing, replacement) -> existing));

        int addedCount = 0;
        for (Map.Entry<String, List<String>> entry : espacosPorCategoria.entrySet()) {
            String categoria = entry.getKey();
            for (String espaco : entry.getValue()) {
                for (String verificacao : verificacoesPadrao) {
                    String chave = (categoria + "|" + espaco + "|" + verificacao).toLowerCase();
                    if (!existentesPorChave.containsKey(chave)) {
                        ManutencaoItem item = new ManutencaoItem();
                        item.setCategoria(categoria);
                        item.setEspaco(espaco);
                        item.setItemVerificacao(verificacao);
                        manutencaoItemRepository.save(item);
                        existentesPorChave.put(chave, item);
                        addedCount++;
                    }
                }
            }
        }

        // 3. VEICULOS category items (preserved legacy logic for vehicles added
        // post-monolith)
        List<String> vehicleItems = List.of(
                "Óleo e Filtros", "Travões", "Pneus", "Luzes", "Exterior (Escovas/Vidros)",
                "Inspeção (IPO)", "Seguro / IUC", "Bateria");

        for (String vItem : vehicleItems) {
            String chave = ("VEICULOS|Geral|" + vItem).toLowerCase();
            if (!existentesPorChave.containsKey(chave)) {
                ManutencaoItem item = new ManutencaoItem();
                item.setCategoria("VEICULOS");
                item.setEspaco("Geral");
                item.setItemVerificacao(vItem);
                manutencaoItemRepository.save(item);
                existentesPorChave.put(chave, item);
                addedCount++;
            }
        }

        log.info("--- MANUTENCAO ITEM SEED COMPLETED: {} NEW items processed ---", addedCount);
        log.info("--- TOTAL MANUTENCAO ITEMS IN DB: {} ---", manutencaoItemRepository.count());
    }

    private void cleanupLegacyCategories() {
        // Fix data inserted with English names in the previous turn
        List<ManutencaoItem> itemsToFix = manutencaoItemRepository.findAll().stream()
                .filter(i -> "Daycare".equalsIgnoreCase(i.getCategoria())
                        || "Preschool".equalsIgnoreCase(i.getCategoria()))
                .collect(Collectors.toList());

        if (!itemsToFix.isEmpty()) {
            log.info("--- CLEANING UP {} LEGACY MAINTENANCE ITEMS ---", itemsToFix.size());
            for (ManutencaoItem item : itemsToFix) {
                if ("Daycare".equalsIgnoreCase(item.getCategoria())) {
                    item.setCategoria("CRECHE");
                } else {
                    item.setCategoria("PRE_ESCOLAR");
                }
                manutencaoItemRepository.save(item);
            }
        }
    }
}
