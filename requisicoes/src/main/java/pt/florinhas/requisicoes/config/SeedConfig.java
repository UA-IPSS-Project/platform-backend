package pt.florinhas.requisicoes.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import pt.florinhas.requisicoes.domain.ManutencaoItem;
import pt.florinhas.requisicoes.repository.ManutencaoItemRepository;

@Configuration
public class SeedConfig implements CommandLineRunner {

    private final ManutencaoItemRepository manutencaoItemRepository;

    public SeedConfig(ManutencaoItemRepository manutencaoItemRepository) {
        this.manutencaoItemRepository = manutencaoItemRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (manutencaoItemRepository.count() == 0) {
            // Seed existing items if needed (simulated based on previous context)
            // But the focus is on VEICULOS
        }

        // Check if VEICULOS items already exist
        if (manutencaoItemRepository.findByCategoria("VEICULOS").isEmpty()) {
            List<ManutencaoItem> items = List.of(
                new ManutencaoItem(null, "VEICULOS", "Geral", "Óleo e Filtros"),
                new ManutencaoItem(null, "VEICULOS", "Geral", "Travões"),
                new ManutencaoItem(null, "VEICULOS", "Geral", "Pneus"),
                new ManutencaoItem(null, "VEICULOS", "Geral", "Luzes"),
                new ManutencaoItem(null, "VEICULOS", "Geral", "Exterior (Escovas/Vidros)"),
                new ManutencaoItem(null, "VEICULOS", "Geral", "Inspeção (IPO)"),
                new ManutencaoItem(null, "VEICULOS", "Geral", "Seguro / IUC"),
                new ManutencaoItem(null, "VEICULOS", "Geral", "Bateria")
            );
            manutencaoItemRepository.saveAll(items);
        }
    }
}
