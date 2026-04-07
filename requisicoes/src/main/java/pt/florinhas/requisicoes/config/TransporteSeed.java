package pt.florinhas.requisicoes.config;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import pt.florinhas.requisicoes.domain.Transporte;
import pt.florinhas.requisicoes.domain.TransporteCategoria;
import pt.florinhas.requisicoes.repository.TransporteRepository;

@Slf4j
@Component
@Order(2)
public class TransporteSeed implements CommandLineRunner {

    private final TransporteRepository transporteRepository;

    public TransporteSeed(TransporteRepository transporteRepository) {
        this.transporteRepository = transporteRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("--- STARTING TRANSPORTE SEED ---");

        // 1. Seed transport catalog
        record TransporteSeedItem(
                String codigo,
                String tipo,
                TransporteCategoria categoria,
                String marca,
                String modelo,
                String matricula,
                LocalDate dataMatricula,
                Integer lotacao) {
        }

        List<TransporteSeedItem> transportesBase = List.of(
                new TransporteSeedItem("V01", "Mini Autocarro", TransporteCategoria.PESADO_DE_PASSAGEIROS, "Iveco", "70c18", "32-TS-44",
                        LocalDate.of(2017, 10, 26), 31),
                new TransporteSeedItem("V02", "Carrinha", TransporteCategoria.LIGEIRO_DE_PASSAGEIROS, "Renault", "Master", "61-PX-87",
                        LocalDate.of(2015, 5, 26), 9),
                new TransporteSeedItem("V03", "Carrinha", TransporteCategoria.LIGEIRO_DE_MERCADORIAS, "Renault", "Kangoo", "36-OU-67",
                        LocalDate.of(2014, 6, 25), 3),
                new TransporteSeedItem("V04", "Carrinha", TransporteCategoria.LIGEIRO_DE_MERCADORIAS, "Renault", "Trafic", "79-NV-51",
                        LocalDate.of(2013, 7, 19), 2),
                new TransporteSeedItem("V05", "Carrinha", TransporteCategoria.LIGEIRO_DE_PASSAGEIROS, "Ford", "Transit", "75-HJ-95",
                        LocalDate.of(2009, 3, 18), 9),
                new TransporteSeedItem("V06", "Carrinha", TransporteCategoria.LIGEIRO_ESPECIAL, "Mercedes", "215 CDI", "43-HD-54",
                        LocalDate.of(2009, 1, 13), 6),
                new TransporteSeedItem("V07", "Carro", TransporteCategoria.LIGEIRO_DE_PASSAGEIROS, "Skoda", "Fabia", "68-ED-26",
                        LocalDate.of(2007, 7, 31), 5),
                new TransporteSeedItem("V08", "Carrinha", TransporteCategoria.LIGEIRO_DE_PASSAGEIROS, "Renault", "Kangoo-Al", "90-43-LJ",
                        LocalDate.of(1998, 7, 1), 6),
                new TransporteSeedItem("V09", "Carrinha", TransporteCategoria.LIGEIRO_DE_PASSAGEIROS, "Mercedes", "208 D/30", "54-95-GE",
                        LocalDate.of(1996, 1, 19), 9));

        Map<String, Transporte> transportesPorCodigo = transporteRepository.findAll().stream()
                .filter(transporte -> transporte.getCodigo() != null && !transporte.getCodigo().isBlank())
                .collect(Collectors.toMap(
                        transporte -> transporte.getCodigo().toUpperCase(Locale.ROOT),
                        Function.identity(),
                        (existing, replacement) -> existing));

        Map<String, Transporte> transportesPorMatricula = transporteRepository.findAll().stream()
                .filter(transporte -> transporte.getMatricula() != null && !transporte.getMatricula().isBlank())
                .collect(Collectors.toMap(
                        transporte -> transporte.getMatricula().toUpperCase(Locale.ROOT),
                        Function.identity(),
                        (existing, replacement) -> existing));

        int count = 0;
        for (TransporteSeedItem seed : transportesBase) {
            Transporte transporte = transportesPorCodigo.get(seed.codigo().toUpperCase(Locale.ROOT));
            if (transporte == null) {
                transporte = transportesPorMatricula.get(seed.matricula().toUpperCase(Locale.ROOT));
            }
            if (transporte == null) {
                transporte = new Transporte();
            }

            transporte.setCodigo(seed.codigo());
            transporte.setTipo(seed.tipo());
            transporte.setCategoria(seed.categoria());
            transporte.setMarca(seed.marca());
            transporte.setModelo(seed.modelo());
            transporte.setMatricula(seed.matricula());
            transporte.setDataMatricula(seed.dataMatricula());
            transporte.setLotacao(seed.lotacao());

            try {
                Transporte persisted = transporteRepository.save(transporte);
                transportesPorCodigo.put(seed.codigo().toUpperCase(Locale.ROOT), persisted);
                transportesPorMatricula.put(seed.matricula().toUpperCase(Locale.ROOT), persisted);
                count++;
            } catch (Exception e) {
                log.error("--- Error saving vehicle {}: {} ---", seed.matricula(), e.getMessage());
            }
        }
        log.info("--- TRANSPORTE SEED COMPLETED: {} vehicles processed ---", count);
    }
}
