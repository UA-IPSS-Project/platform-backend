package pt.florinhas.candidaturas.initiliazer;

// Java
import java.time.Instant;
import java.util.Map;

// Spring
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// Logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// From this project
import pt.florinhas.candidaturas.domain.Candidatura;
import pt.florinhas.candidaturas.domain.CandidaturaEstado;
import pt.florinhas.candidaturas.domain.Form;
import pt.florinhas.candidaturas.repository.CandidaturaRepository;
import pt.florinhas.candidaturas.repository.FormRepository;

@Component
public class DataInitializer implements CommandLineRunner {

        private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

        private final FormRepository formRepository;
        private final CandidaturaRepository candidaturaRepository;

        public DataInitializer(FormRepository formRepository,
                        CandidaturaRepository candidaturaRepository) {
                this.formRepository = formRepository;
                this.candidaturaRepository = candidaturaRepository;
        }

        @Override
        public void run(String... args) {
                if (formRepository.count() > 0 || candidaturaRepository.count() > 0) {
                        log.info("[DataInitializer] Data already exists — seed skipped.");
                        return;
                }

                log.info("[DataInitializer] Initializing sample data...");

                // ----------------------------------------------------------------
                // Form 1 — Research Grant Application
                // ----------------------------------------------------------------
                Form form1 = new Form();
                form1.setName("Candidatura a Bolsa de Investigação");
                form1.setSchema(Map.of(
                                "type", "object",
                                "title", "Bolsa de Investigação",
                                "required", new String[] { "nome", "email", "motivacao", "areaInvestigacao" },
                                "properties", Map.of(
                                                "nome", Map.of("type", "string", "title", "Nome completo"),
                                                "email", Map.of("type", "string", "title", "E-mail", "format", "email"),
                                                "areaInvestigacao",
                                                Map.of("type", "string", "title", "Área de Investigação"),
                                                "motivacao", Map.of("type", "string", "title", "Motivação",
                                                                "description",
                                                                "Descreva a sua motivação para esta bolsa."))));
                form1.setUiSchema(Map.of(
                                "motivacao", Map.of("ui:widget", "textarea"),
                                "email", Map.of("ui:placeholder", "exemplo@email.com")));
                form1.setCriadoPor(1L);
                form1.setCriadoEm(Instant.parse("2025-01-10T09:00:00Z"));
                form1 = formRepository.save(form1);
                log.info("[DataInitializer] Form 1 criado: id={}", form1.getId());

                // ----------------------------------------------------------------
                // Form 2 — Candidatura a Estágio Profissional
                // ----------------------------------------------------------------
                Form form2 = new Form();
                form2.setName("Candidatura a Estágio Profissional");
                form2.setSchema(Map.of(
                                "type", "object",
                                "title", "Estágio Profissional",
                                "required", new String[] { "nome", "email", "curso", "disponibilidade" },
                                "properties", Map.of(
                                                "nome", Map.of("type", "string", "title", "Nome completo"),
                                                "email", Map.of("type", "string", "title", "E-mail", "format", "email"),
                                                "curso", Map.of("type", "string", "title", "Curso / Licenciatura"),
                                                "disponibilidade",
                                                Map.of("type", "string", "title", "Data de disponibilidade",
                                                                "format", "date"))));
                form2.setUiSchema(Map.of(
                                "disponibilidade", Map.of("ui:widget", "date"),
                                "email", Map.of("ui:placeholder", "exemplo@email.com")));
                form2.setCriadoPor(1L);
                form2.setCriadoEm(Instant.parse("2025-02-05T10:30:00Z"));
                form2 = formRepository.save(form2);
                log.info("[DataInitializer] Form 2 criado: id={}", form2.getId());

                // ----------------------------------------------------------------
                // Candidatura 1 — ao Form 1 (Bolsa), estado PENDENTE
                // ----------------------------------------------------------------
                Candidatura c1 = new Candidatura();
                c1.setFormId(form1.getId());
                c1.setRespostas(Map.of(
                                "nome", "Ana Ferreira",
                                "email", "ana.ferreira@universidade.pt",
                                "areaInvestigacao", "Inteligência Artificial",
                                "motivacao", "Pretendo aprofundar os meus conhecimentos em IA aplicada à saúde."));
                c1.setEstado(CandidaturaEstado.PENDENTE);
                c1.setCriadoPor(10L);
                c1.setCriadoEm(Instant.parse("2025-03-01T08:00:00Z"));
                candidaturaRepository.save(c1);
                log.info("[DataInitializer] Candidatura 1 criada (Form1 / PENDENTE)");

                // ----------------------------------------------------------------
                // Candidatura 2 — ao Form 1 (Bolsa), estado APROVADA
                // ----------------------------------------------------------------
                Candidatura c2 = new Candidatura();
                c2.setFormId(form1.getId());
                c2.setRespostas(Map.of(
                                "nome", "Bruno Silva",
                                "email", "bruno.silva@universidade.pt",
                                "areaInvestigacao", "Engenharia de Software",
                                "motivacao", "Quero investigar metodologias ágeis em contextos críticos de software."));
                c2.setEstado(CandidaturaEstado.APROVADA);
                c2.setCriadoPor(11L);
                c2.setCriadoEm(Instant.parse("2025-03-05T14:20:00Z"));
                c2.setAtualizadoPor(1L);
                c2.setAtualizadoEm(Instant.parse("2025-03-10T09:45:00Z"));
                candidaturaRepository.save(c2);
                log.info("[DataInitializer] Candidatura 2 criada (Form1 / APROVADA)");

                // ----------------------------------------------------------------
                // Candidatura 3 — ao Form 2 (Estágio), estado REJEITADA
                // ----------------------------------------------------------------
                Candidatura c3 = new Candidatura();
                c3.setFormId(form2.getId());
                c3.setRespostas(Map.of(
                                "nome", "Catarina Lopes",
                                "email", "catarina.lopes@email.com",
                                "curso", "Engenharia Informática",
                                "disponibilidade", "2025-07-01"));
                c3.setEstado(CandidaturaEstado.REJEITADA);
                c3.setCriadoPor(12L);
                c3.setCriadoEm(Instant.parse("2025-04-01T11:00:00Z"));
                c3.setAtualizadoPor(1L);
                c3.setAtualizadoEm(Instant.parse("2025-04-08T15:30:00Z"));
                candidaturaRepository.save(c3);
                log.info("[DataInitializer] Candidatura 3 criada (Form2 / REJEITADA)");

                log.info("[DataInitializer] Seed concluído — 2 forms e 3 candidaturas inseridos.");
        }
}
