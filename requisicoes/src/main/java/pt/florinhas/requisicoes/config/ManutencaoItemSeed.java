package pt.florinhas.requisicoes.config;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import pt.florinhas.requisicoes.domain.ManutencaoItem;
import pt.florinhas.requisicoes.repository.ManutencaoItemRepository;

@Component
@Order(3)
public class ManutencaoItemSeed implements CommandLineRunner {

    private final ManutencaoItemRepository manutencaoItemRepository;

    public ManutencaoItemSeed(ManutencaoItemRepository manutencaoItemRepository) {
        this.manutencaoItemRepository = manutencaoItemRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        record ManutencaoItemSeedItem(String categoria, String espaco, String itemVerificacao) {
        }

        // 1. Base list from original code
        List<ManutencaoItemSeedItem> itemsBase = List.of(
                // CATL
                new ManutencaoItemSeedItem("CATL", "WC (Masc/Fem)", "Alumínios"),
                new ManutencaoItemSeedItem("CATL", "WC (Masc/Fem)", "Blackouts"),
                new ManutencaoItemSeedItem("CATL", "WC (Masc/Fem)", "Madeiras"),
                new ManutencaoItemSeedItem("CATL", "WC (Masc/Fem)", "Armários"),
                new ManutencaoItemSeedItem("CATL", "WC (Masc/Fem)", "Aquecedores"),
                new ManutencaoItemSeedItem("CATL", "WC (Masc/Fem)", "Torneiras"),
                new ManutencaoItemSeedItem("CATL", "WC (Masc/Fem)", "Eletricidade"),
                new ManutencaoItemSeedItem("CATL", "WC (Masc/Fem)", "Cabides"),
                new ManutencaoItemSeedItem("CATL", "WC (Masc/Fem)", "Paredes"),
                new ManutencaoItemSeedItem("CATL", "WC (Masc/Fem)", "Tetos"),
                new ManutencaoItemSeedItem("CATL", "WC (Masc/Fem)", "Chão"),
                new ManutencaoItemSeedItem("CATL", "Salão e Palco", "Alumínios"),
                new ManutencaoItemSeedItem("CATL", "Salão e Palco", "Blackouts"),
                new ManutencaoItemSeedItem("CATL", "Salão e Palco", "Madeiras"),
                new ManutencaoItemSeedItem("CATL", "Salão e Palco", "Armários"),
                new ManutencaoItemSeedItem("CATL", "Salão e Palco", "Aquecedores"),
                new ManutencaoItemSeedItem("CATL", "Salão e Palco", "Torneiras"),
                new ManutencaoItemSeedItem("CATL", "Salão e Palco", "Eletricidade"),
                new ManutencaoItemSeedItem("CATL", "Salão e Palco", "Cabides"),
                new ManutencaoItemSeedItem("CATL", "Salão e Palco", "Paredes"),
                new ManutencaoItemSeedItem("CATL", "Salão e Palco", "Tetos"),
                new ManutencaoItemSeedItem("CATL", "Salão e Palco", "Chão"),
                // RC
                new ManutencaoItemSeedItem("RC", "Parque", "Alumínios"),
                new ManutencaoItemSeedItem("RC", "Parque", "Blackouts"),
                new ManutencaoItemSeedItem("RC", "Parque", "Madeiras"),
                new ManutencaoItemSeedItem("RC", "Parque", "Armários"),
                new ManutencaoItemSeedItem("RC", "Parque", "Aquecedores"),
                new ManutencaoItemSeedItem("RC", "Parque", "Torneiras"),
                new ManutencaoItemSeedItem("RC", "Parque", "Eletricidade"),
                new ManutencaoItemSeedItem("RC", "Parque", "Cabides"),
                new ManutencaoItemSeedItem("RC", "Parque", "Paredes"),
                new ManutencaoItemSeedItem("RC", "Parque", "Tetos"),
                new ManutencaoItemSeedItem("RC", "Parque", "Chão"),
                new ManutencaoItemSeedItem("RC", "Relvado", "Alumínios"),
                new ManutencaoItemSeedItem("RC", "Relvado", "Blackouts"),
                new ManutencaoItemSeedItem("RC", "Relvado", "Madeiras"),
                new ManutencaoItemSeedItem("RC", "Relvado", "Armários"),
                new ManutencaoItemSeedItem("RC", "Relvado", "Aquecedores"),
                new ManutencaoItemSeedItem("RC", "Relvado", "Torneiras"),
                new ManutencaoItemSeedItem("RC", "Relvado", "Eletricidade"),
                new ManutencaoItemSeedItem("RC", "Relvado", "Cabides"),
                new ManutencaoItemSeedItem("RC", "Relvado", "Paredes"),
                new ManutencaoItemSeedItem("RC", "Relvado", "Tetos"),
                new ManutencaoItemSeedItem("RC", "Relvado", "Chão"),
                new ManutencaoItemSeedItem("RC", "Acolhimento", "Alumínios"),
                new ManutencaoItemSeedItem("RC", "Acolhimento", "Blackouts"),
                new ManutencaoItemSeedItem("RC", "Acolhimento", "Madeiras"),
                new ManutencaoItemSeedItem("RC", "Acolhimento", "Armários"),
                new ManutencaoItemSeedItem("RC", "Acolhimento", "Aquecedores"),
                new ManutencaoItemSeedItem("RC", "Acolhimento", "Torneiras"),
                new ManutencaoItemSeedItem("RC", "Acolhimento", "Eletricidade"),
                new ManutencaoItemSeedItem("RC", "Acolhimento", "Cabides"),
                new ManutencaoItemSeedItem("RC", "Acolhimento", "Paredes"),
                new ManutencaoItemSeedItem("RC", "Acolhimento", "Tetos"),
                new ManutencaoItemSeedItem("RC", "Acolhimento", "Chão"),
                new ManutencaoItemSeedItem("RC", "Gabinetes", "Alumínios"),
                new ManutencaoItemSeedItem("RC", "Gabinetes", "Blackouts"),
                new ManutencaoItemSeedItem("RC", "Gabinetes", "Madeiras"),
                new ManutencaoItemSeedItem("RC", "Gabinetes", "Armários"),
                new ManutencaoItemSeedItem("RC", "Gabinetes", "Aquecedores"),
                new ManutencaoItemSeedItem("RC", "Gabinetes", "Torneiras"),
                new ManutencaoItemSeedItem("RC", "Gabinetes", "Eletricidade"),
                new ManutencaoItemSeedItem("RC", "Gabinetes", "Cabides"),
                new ManutencaoItemSeedItem("RC", "Gabinetes", "Paredes"),
                new ManutencaoItemSeedItem("RC", "Gabinetes", "Tetos"),
                new ManutencaoItemSeedItem("RC", "Gabinetes", "Chão"),
                new ManutencaoItemSeedItem("RC", "WCs", "Alumínios"),
                new ManutencaoItemSeedItem("RC", "WCs", "Blackouts"),
                new ManutencaoItemSeedItem("RC", "WCs", "Madeiras"),
                new ManutencaoItemSeedItem("RC", "WCs", "Armários"),
                new ManutencaoItemSeedItem("RC", "WCs", "Aquecedores"),
                new ManutencaoItemSeedItem("RC", "WCs", "Torneiras"),
                new ManutencaoItemSeedItem("RC", "WCs", "Eletricidade"),
                new ManutencaoItemSeedItem("RC", "WCs", "Cabides"),
                new ManutencaoItemSeedItem("RC", "WCs", "Paredes"),
                new ManutencaoItemSeedItem("RC", "WCs", "Tetos"),
                new ManutencaoItemSeedItem("RC", "WCs", "Chão"),
                new ManutencaoItemSeedItem("RC", "Oficina", "Alumínios"),
                new ManutencaoItemSeedItem("RC", "Oficina", "Blackouts"),
                new ManutencaoItemSeedItem("RC", "Oficina", "Madeiras"),
                new ManutencaoItemSeedItem("RC", "Oficina", "Armários"),
                new ManutencaoItemSeedItem("RC", "Oficina", "Aquecedores"),
                new ManutencaoItemSeedItem("RC", "Oficina", "Torneiras"),
                new ManutencaoItemSeedItem("RC", "Oficina", "Eletricidade"),
                new ManutencaoItemSeedItem("RC", "Oficina", "Cabides"),
                new ManutencaoItemSeedItem("RC", "Oficina", "Paredes"),
                new ManutencaoItemSeedItem("RC", "Oficina", "Tetos"),
                new ManutencaoItemSeedItem("RC", "Oficina", "Chão"),
                new ManutencaoItemSeedItem("RC", "Biblioteca", "Alumínios"),
                new ManutencaoItemSeedItem("RC", "Biblioteca", "Blackouts"),
                new ManutencaoItemSeedItem("RC", "Biblioteca", "Madeiras"),
                new ManutencaoItemSeedItem("RC", "Biblioteca", "Armários"),
                new ManutencaoItemSeedItem("RC", "Biblioteca", "Aquecedores"),
                new ManutencaoItemSeedItem("RC", "Biblioteca", "Torneiras"),
                new ManutencaoItemSeedItem("RC", "Biblioteca", "Eletricidade"),
                new ManutencaoItemSeedItem("RC", "Biblioteca", "Cabides"),
                new ManutencaoItemSeedItem("RC", "Biblioteca", "Paredes"),
                new ManutencaoItemSeedItem("RC", "Biblioteca", "Tetos"),
                new ManutencaoItemSeedItem("RC", "Biblioteca", "Chão"),
                new ManutencaoItemSeedItem("RC", "Refeitório", "Alumínios"),
                new ManutencaoItemSeedItem("RC", "Refeitório", "Blackouts"),
                new ManutencaoItemSeedItem("RC", "Refeitório", "Madeiras"),
                new ManutencaoItemSeedItem("RC", "Refeitório", "Armários"),
                new ManutencaoItemSeedItem("RC", "Refeitório", "Aquecedores"),
                new ManutencaoItemSeedItem("RC", "Refeitório", "Torneiras"),
                new ManutencaoItemSeedItem("RC", "Refeitório", "Eletricidade"),
                new ManutencaoItemSeedItem("RC", "Refeitório", "Cabides"),
                new ManutencaoItemSeedItem("RC", "Refeitório", "Paredes"),
                new ManutencaoItemSeedItem("RC", "Refeitório", "Tetos"),
                new ManutencaoItemSeedItem("RC", "Refeitório", "Chão"),
                new ManutencaoItemSeedItem("RC", "Elevador", "Alumínios"),
                new ManutencaoItemSeedItem("RC", "Elevador", "Blackouts"),
                new ManutencaoItemSeedItem("RC", "Elevador", "Madeiras"),
                new ManutencaoItemSeedItem("RC", "Elevador", "Armários"),
                new ManutencaoItemSeedItem("RC", "Elevador", "Aquecedores"),
                new ManutencaoItemSeedItem("RC", "Elevador", "Torneiras"),
                new ManutencaoItemSeedItem("RC", "Elevador", "Eletricidade"),
                new ManutencaoItemSeedItem("RC", "Elevador", "Cabides"),
                new ManutencaoItemSeedItem("RC", "Elevador", "Paredes"),
                new ManutencaoItemSeedItem("RC", "Elevador", "Tetos"),
                new ManutencaoItemSeedItem("RC", "Elevador", "Chão"),
                // PRE_ESCOLAR
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Salas (Amarela, Azul, Verde, Arco-Íris)", "Alumínios"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Salas (Amarela, Azul, Verde, Arco-Íris)", "Blackouts"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Salas (Amarela, Azul, Verde, Arco-Íris)", "Madeiras"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Salas (Amarela, Azul, Verde, Arco-Íris)", "Armários"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Salas (Amarela, Azul, Verde, Arco-Íris)", "Aquecedores"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Salas (Amarela, Azul, Verde, Arco-Íris)", "Torneiras"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Salas (Amarela, Azul, Verde, Arco-Íris)", "Eletricidade"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Salas (Amarela, Azul, Verde, Arco-Íris)", "Cabides"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Salas (Amarela, Azul, Verde, Arco-Íris)", "Paredes"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Salas (Amarela, Azul, Verde, Arco-Íris)", "Tetos"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Salas (Amarela, Azul, Verde, Arco-Íris)", "Chão"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "WCs", "Alumínios"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "WCs", "Blackouts"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "WCs", "Madeiras"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "WCs", "Armários"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "WCs", "Aquecedores"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "WCs", "Torneiras"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "WCs", "Eletricidade"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "WCs", "Cabides"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "WCs", "Paredes"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "WCs", "Tetos"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "WCs", "Chão"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Hall", "Alumínios"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Hall", "Blackouts"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Hall", "Madeiras"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Hall", "Armários"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Hall", "Aquecedores"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Hall", "Torneiras"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Hall", "Eletricidade"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Hall", "Cabides"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Hall", "Paredes"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Hall", "Tetos"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Hall", "Chão"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Corredor", "Alumínios"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Corredor", "Blackouts"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Corredor", "Madeiras"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Corredor", "Armários"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Corredor", "Aquecedores"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Corredor", "Torneiras"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Corredor", "Eletricidade"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Corredor", "Cabides"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Corredor", "Paredes"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Corredor", "Tetos"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Corredor", "Chão"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Parque exterior", "Alumínios"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Parque exterior", "Blackouts"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Parque exterior", "Madeiras"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Parque exterior", "Armários"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Parque exterior", "Aquecedores"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Parque exterior", "Torneiras"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Parque exterior", "Eletricidade"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Parque exterior", "Cabides"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Parque exterior", "Paredes"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Parque exterior", "Tetos"),
                new ManutencaoItemSeedItem("PRE_ESCOLAR", "Parque exterior", "Chão"),
                // CRECHE
                new ManutencaoItemSeedItem("CRECHE", "Berçário", "Alumínios"),
                new ManutencaoItemSeedItem("CRECHE", "Berçário", "Blackouts"),
                new ManutencaoItemSeedItem("CRECHE", "Berçário", "Madeiras"),
                new ManutencaoItemSeedItem("CRECHE", "Berçário", "Armários"),
                new ManutencaoItemSeedItem("CRECHE", "Berçário", "Aquecedores"),
                new ManutencaoItemSeedItem("CRECHE", "Berçário", "Torneiras"),
                new ManutencaoItemSeedItem("CRECHE", "Berçário", "Eletricidade"),
                new ManutencaoItemSeedItem("CRECHE", "Berçário", "Cabides"),
                new ManutencaoItemSeedItem("CRECHE", "Berçário", "Paredes"),
                new ManutencaoItemSeedItem("CRECHE", "Berçário", "Tetos"),
                new ManutencaoItemSeedItem("CRECHE", "Berçário", "Chão"),
                new ManutencaoItemSeedItem("CRECHE", "Salas (Limão, Alface, Vermelha, Turquesa)", "Alumínios"),
                new ManutencaoItemSeedItem("CRECHE", "Salas (Limão, Alface, Vermelha, Turquesa)", "Blackouts"),
                new ManutencaoItemSeedItem("CRECHE", "Salas (Limão, Alface, Vermelha, Turquesa)", "Madeiras"),
                new ManutencaoItemSeedItem("CRECHE", "Salas (Limão, Alface, Vermelha, Turquesa)", "Armários"),
                new ManutencaoItemSeedItem("CRECHE", "Salas (Limão, Alface, Vermelha, Turquesa)", "Aquecedores"),
                new ManutencaoItemSeedItem("CRECHE", "Salas (Limão, Alface, Vermelha, Turquesa)", "Torneiras"),
                new ManutencaoItemSeedItem("CRECHE", "Salas (Limão, Alface, Vermelha, Turquesa)", "Eletricidade"),
                new ManutencaoItemSeedItem("CRECHE", "Salas (Limão, Alface, Vermelha, Turquesa)", "Cabides"),
                new ManutencaoItemSeedItem("CRECHE", "Salas (Limão, Alface, Vermelha, Turquesa)", "Paredes"),
                new ManutencaoItemSeedItem("CRECHE", "Salas (Limão, Alface, Vermelha, Turquesa)", "Tetos"),
                new ManutencaoItemSeedItem("CRECHE", "Salas (Limão, Alface, Vermelha, Turquesa)", "Chão"),
                new ManutencaoItemSeedItem("CRECHE", "Fraldário", "Alumínios"),
                new ManutencaoItemSeedItem("CRECHE", "Fraldário", "Blackouts"),
                new ManutencaoItemSeedItem("CRECHE", "Fraldário", "Madeiras"),
                new ManutencaoItemSeedItem("CRECHE", "Fraldário", "Armários"),
                new ManutencaoItemSeedItem("CRECHE", "Fraldário", "Aquecedores"),
                new ManutencaoItemSeedItem("CRECHE", "Fraldário", "Torneiras"),
                new ManutencaoItemSeedItem("CRECHE", "Fraldário", "Eletricidade"),
                new ManutencaoItemSeedItem("CRECHE", "Fraldário", "Cabides"),
                new ManutencaoItemSeedItem("CRECHE", "Fraldário", "Paredes"),
                new ManutencaoItemSeedItem("CRECHE", "Fraldário", "Tetos"),
                new ManutencaoItemSeedItem("CRECHE", "Fraldário", "Chão"),
                new ManutencaoItemSeedItem("CRECHE", "Copa", "Alumínios"),
                new ManutencaoItemSeedItem("CRECHE", "Copa", "Blackouts"),
                new ManutencaoItemSeedItem("CRECHE", "Copa", "Madeiras"),
                new ManutencaoItemSeedItem("CRECHE", "Copa", "Armários"),
                new ManutencaoItemSeedItem("CRECHE", "Copa", "Aquecedores"),
                new ManutencaoItemSeedItem("CRECHE", "Copa", "Torneiras"),
                new ManutencaoItemSeedItem("CRECHE", "Copa", "Eletricidade"),
                new ManutencaoItemSeedItem("CRECHE", "Copa", "Cabides"),
                new ManutencaoItemSeedItem("CRECHE", "Copa", "Paredes"),
                new ManutencaoItemSeedItem("CRECHE", "Copa", "Tetos"),
                new ManutencaoItemSeedItem("CRECHE", "Copa", "Chão"),
                new ManutencaoItemSeedItem("CRECHE", "Refeitório", "Alumínios"),
                new ManutencaoItemSeedItem("CRECHE", "Refeitório", "Blackouts"),
                new ManutencaoItemSeedItem("CRECHE", "Refeitório", "Madeiras"),
                new ManutencaoItemSeedItem("CRECHE", "Refeitório", "Armários"),
                new ManutencaoItemSeedItem("CRECHE", "Refeitório", "Aquecedores"),
                new ManutencaoItemSeedItem("CRECHE", "Refeitório", "Torneiras"),
                new ManutencaoItemSeedItem("CRECHE", "Refeitório", "Eletricidade"),
                new ManutencaoItemSeedItem("CRECHE", "Refeitório", "Cabides"),
                new ManutencaoItemSeedItem("CRECHE", "Refeitório", "Paredes"),
                new ManutencaoItemSeedItem("CRECHE", "Refeitório", "Tetos"),
                new ManutencaoItemSeedItem("CRECHE", "Refeitório", "Chão"),
                new ManutencaoItemSeedItem("CRECHE", "Parque 3º andar", "Alumínios"),
                new ManutencaoItemSeedItem("CRECHE", "Parque 3º andar", "Blackouts"),
                new ManutencaoItemSeedItem("CRECHE", "Parque 3º andar", "Madeiras"),
                new ManutencaoItemSeedItem("CRECHE", "Parque 3º andar", "Armários"),
                new ManutencaoItemSeedItem("CRECHE", "Parque 3º andar", "Aquecedores"),
                new ManutencaoItemSeedItem("CRECHE", "Parque 3º andar", "Torneiras"),
                new ManutencaoItemSeedItem("CRECHE", "Parque 3º andar", "Eletricidade"),
                new ManutencaoItemSeedItem("CRECHE", "Parque 3º andar", "Cabides"),
                new ManutencaoItemSeedItem("CRECHE", "Parque 3º andar", "Paredes"),
                new ManutencaoItemSeedItem("CRECHE", "Parque 3º andar", "Tetos"),
                new ManutencaoItemSeedItem("CRECHE", "Parque 3º andar", "Chão"));

        // 2. Initial batch save if empty
        if (manutencaoItemRepository.count() == 0) {
            for (ManutencaoItemSeedItem seed : itemsBase) {
                ManutencaoItem item = new ManutencaoItem();
                item.setCategoria(seed.categoria());
                item.setEspaco(seed.espaco());
                item.setItemVerificacao(seed.itemVerificacao());
                manutencaoItemRepository.save(item);
            }
        }

        // 3. Dynamic generation of items for obligatory spaces
        List<String> verificacoes = List.of(
                "Alumínios", "Blackouts", "Madeiras", "Armários", "Aquecedores",
                "Torneiras", "Eletricidade", "Cabides", "Paredes", "Tetos", "Chão");

        Map<String, List<String>> espacosObrigatorios = Map.of(
                "CATL", List.of("WC masculino", "WC feminino", "Salão", "Salão (palco)"),
                "RC", List.of("Parque exterior", "Relvado", "Acolhimento pré", "Acolhimento creche",
                        "Gabinete", "WC deficientes", "WC Rosa", "WC azul", "Gabinete médico",
                        "Oficina", "Corredor + WC", "Biblioteca", "Refeitório", "Lavatórios + Hall",
                        "Elevador", "Escadas acesso 1º"),
                "PRE_ESCOLAR", List.of("Sala acolhimento", "Sala de educadoras", "WC deficientes",
                        "WC azul", "WC cor de rosa", "Hall", "Escadas acesso 2º", "Corredor",
                        "Sala Amarela", "Sala Azul", "Sala Verde", "Sala Arco-Íris", "WC", "Parque exterior"),
                "CRECHE", List.of("Parque ext. 3º andar", "S. Acolhimento grande", "S. Acollhimento peq.",
                        "WC", "WC azul", "Corredor e hall", "Escadas acesso sotão", "Sala Amarela limão",
                        "Sala Verde Alface", "Sala Vermelha", "Refeitório", "Copa", "Fraldário",
                        "Sala azul turquesa", "Berçário"));

        Map<String, ManutencaoItem> existentesPorChave = manutencaoItemRepository.findAll().stream()
                .collect(Collectors.toMap(
                        item -> item.getCategoria() + "|" + item.getEspaco() + "|" + item.getItemVerificacao(),
                        Function.identity(),
                        (existing, ignored) -> existing));

        espacosObrigatorios.forEach((categoria, espacos) -> {
            for (String espaco : espacos) {
                for (String verificacao : verificacoes) {
                    String chave = categoria + "|" + espaco + "|" + verificacao;
                    if (!existentesPorChave.containsKey(chave)) {
                        ManutencaoItem item = new ManutencaoItem();
                        item.setCategoria(categoria);
                        item.setEspaco(espaco);
                        item.setItemVerificacao(verificacao);
                        manutencaoItemRepository.save(item);
                    }
                }
            }
        });

        // 4. Add VEICULOS category items
        if (manutencaoItemRepository.findByCategoria("VEICULOS").isEmpty()) {
            List<ManutencaoItem> vehicleItems = List.of(
                    new ManutencaoItem(null, "VEICULOS", "Geral", "Óleo e Filtros"),
                    new ManutencaoItem(null, "VEICULOS", "Geral", "Travões"),
                    new ManutencaoItem(null, "VEICULOS", "Geral", "Pneus"),
                    new ManutencaoItem(null, "VEICULOS", "Geral", "Luzes"),
                    new ManutencaoItem(null, "VEICULOS", "Geral", "Exterior (Escovas/Vidros)"),
                    new ManutencaoItem(null, "VEICULOS", "Geral", "Inspeção (IPO)"),
                    new ManutencaoItem(null, "VEICULOS", "Geral", "Seguro / IUC"),
                    new ManutencaoItem(null, "VEICULOS", "Geral", "Bateria"));
            manutencaoItemRepository.saveAll(vehicleItems);
        }
    }
}
