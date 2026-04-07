package pt.florinhas.requisicoes.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import pt.florinhas.requisicoes.domain.ManutencaoItem;
import pt.florinhas.requisicoes.domain.Transporte;
import pt.florinhas.requisicoes.repository.ManutencaoItemRepository;
import pt.florinhas.requisicoes.repository.TransporteRepository;

@Configuration
public class SeedConfig implements CommandLineRunner {

    private final ManutencaoItemRepository manutencaoItemRepository;
    private final TransporteRepository transporteRepository;

    public SeedConfig(ManutencaoItemRepository manutencaoItemRepository, TransporteRepository transporteRepository) {
        this.manutencaoItemRepository = manutencaoItemRepository;
        this.transporteRepository = transporteRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Seed Vehicles if empty
        if (transporteRepository.count() == 0) {
            List<Transporte> transportes = List.of(
                createTransporte("V-01", "Ligeiro passageiros", "PT-10-AA", "Mercedes", "Sprinter", 9),
                createTransporte("V-02", "Ligeiro passageiros", "PT-20-BB", "Volkswagen", "Crafter", 16),
                createTransporte("V-03", "Ligeiro passageiros", "PT-30-CC", "Ford", "Transit", 9),
                createTransporte("V-04", "Adaptado", "PT-40-DD", "Renault", "Master", 7)
            );
            transporteRepository.saveAll(transportes);
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

    private Transporte createTransporte(String codigo, String categoria, String matricula, String marca, String modelo, Integer lotacao) {
        Transporte t = new Transporte();
        t.setCodigo(codigo);
        t.setCategoria(categoria);
        t.setMatricula(matricula);
        t.setMarca(marca);
        t.setModelo(modelo);
        t.setLotacao(lotacao);
        return t;
    }
}
