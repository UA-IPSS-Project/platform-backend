package pt.florinhas.marcacoes.config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import pt.florinhas.marcacoes.domain.Assunto;
import pt.florinhas.marcacoes.repository.AssuntoRepository;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class AssuntoSeed implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssuntoSeed.class);
    private final AssuntoRepository assuntoRepository;

    @Override
    public void run(String... args) {
        List<String> assuntosBase = List.of(
                "Pagar mensalidade",
                "Entregar documentos",
                "Reunião presencial",
                "Outro"
        );

        for (String nome : assuntosBase) {
            if (assuntoRepository.findByNome(nome).isEmpty()) {
                Assunto assunto = new Assunto(nome);
                assuntoRepository.save(assunto);
                LOGGER.info(">>> Assunto base '{}' criado.", nome);
            }
        }
    }
}
