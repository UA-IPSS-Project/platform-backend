package pt.florinhas.candidaturas.initiliazer;

// Java
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.bson.Document;

// Spring
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// Logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// From this project
import pt.florinhas.candidaturas.domain.Candidatura;
import pt.florinhas.candidaturas.domain.CandidaturaEstado;
import pt.florinhas.candidaturas.domain.FieldAudience;
import pt.florinhas.candidaturas.domain.FieldDefinition;
import pt.florinhas.candidaturas.domain.Form;
import pt.florinhas.candidaturas.domain.FormPage;
import pt.florinhas.candidaturas.domain.FormStatus;
import pt.florinhas.candidaturas.repository.CandidaturaRepository;
import pt.florinhas.candidaturas.repository.FormRepository;

@Component
public class DataInitializer implements CommandLineRunner {

        private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

        private final FormRepository formRepository;
        private final CandidaturaRepository candidaturaRepository;

        @org.springframework.beans.factory.annotation.Value("${INITIALIZE_DATA:false}")
        private boolean forceInitialize;

        public DataInitializer(FormRepository formRepository,
                        CandidaturaRepository candidaturaRepository) {
                this.formRepository = formRepository;
                this.candidaturaRepository = candidaturaRepository;
        }

        @Override
        public void run(String... args) {
                if (forceInitialize) {
                        log.info("[DataInitializer] Force initialize is TRUE — clearing existing data...");
                        formRepository.deleteAll();
                        candidaturaRepository.deleteAll();
                } else if (formRepository.count() > 0 || candidaturaRepository.count() > 0) {
                        log.info("[DataInitializer] Data already exists — seed skipped.");
                        return;
                }

                log.info("[DataInitializer] Initializing sample data...");

                // ----------------------------------------------------------------
                // Form 1 — Research Grant Application
                // ----------------------------------------------------------------
                Form form1 = new Form();
                form1.setName("Candidatura a Bolsa de Investigação");
                form1.setStatus(FormStatus.ATIVO);

                FormPage page1 = new FormPage();
                page1.setId("page-1");
                page1.setTitle("Bolsa de Investigação");
                page1.setDescription("Preencha os dados da candidatura");
                page1.setOrder(1);

                FieldDefinition fNome = new FieldDefinition("nome", "text", 1, new Document(Map.of("label", "Nome completo", "required", true)), FieldAudience.PUBLIC);
                FieldDefinition fEmail = new FieldDefinition("email", "email", 2, new Document(Map.of("label", "E-mail", "required", true, "placeholder", "exemplo@email.com")), FieldAudience.PUBLIC);
                FieldDefinition fArea = new FieldDefinition("areaInvestigacao", "text", 3, new Document(Map.of("label", "Área de Investigação", "required", true)), FieldAudience.PUBLIC);
                FieldDefinition fMotivacao = new FieldDefinition("motivacao", "textarea", 4, new Document(Map.of("label", "Motivação", "required", true, "description", "Descreva a sua motivação para esta bolsa.")), FieldAudience.PUBLIC);

                page1.setFields(List.of(fNome, fEmail, fArea, fMotivacao));
                form1.setPages(List.of(page1));

                form1.setCriadoPor(1L);
                form1.setCriadoEm(Instant.parse("2025-01-10T09:00:00Z"));
                form1 = formRepository.save(form1);
                log.info("[DataInitializer] Form 1 criado: id={}", form1.getId());

                // ----------------------------------------------------------------
                // Form 2 — Candidatura a Estágio Profissional
                // ----------------------------------------------------------------
                Form form2 = new Form();
                form2.setName("Candidatura a Estágio Profissional");
                form2.setStatus(FormStatus.ATIVO);

                FormPage page2 = new FormPage();
                page2.setId("page-1");
                page2.setTitle("Estágio Profissional");
                page2.setDescription("Preencha os dados da candidatura ao estágio");
                page2.setOrder(1);

                FieldDefinition f2Nome = new FieldDefinition("nome", "text", 1, new Document(Map.of("label", "Nome completo", "required", true)), FieldAudience.PUBLIC);
                FieldDefinition f2Email = new FieldDefinition("email", "email", 2, new Document(Map.of("label", "E-mail", "required", true, "placeholder", "exemplo@email.com")), FieldAudience.PUBLIC);
                FieldDefinition f2Curso = new FieldDefinition("curso", "text", 3, new Document(Map.of("label", "Curso / Licenciatura", "required", true)), FieldAudience.PUBLIC);
                FieldDefinition f2Disp = new FieldDefinition("disponibilidade", "date", 4, new Document(Map.of("label", "Data de disponibilidade", "required", true)), FieldAudience.PUBLIC);

                page2.setFields(List.of(f2Nome, f2Email, f2Curso, f2Disp));
                form2.setPages(List.of(page2));

                form2.setCriadoPor(1L);
                form2.setCriadoEm(Instant.parse("2025-02-05T10:30:00Z"));
                form2 = formRepository.save(form2);
                log.info("[DataInitializer] Form 2 criado: id={}", form2.getId());

                // ----------------------------------------------------------------
                // Candidatura 1 — ao Form 1 (Bolsa), estado PENDENTE
                // ----------------------------------------------------------------
                Candidatura c1 = new Candidatura();
                c1.setFormId(form1.getId());
                c1.setNif("123456789");
                c1.setNome("Ana Ferreira");
                c1.setAssinado(false);
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
                c2.setNif("234567899");
                c2.setNome("Bruno Silva");
                c2.setAssinado(true);
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
                c3.setNif("501234560");
                c3.setNome("Catarina Lopes");
                c3.setAssinado(false);
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

                // ----------------------------------------------------------------
                // Candidatura 4 — ao Form 2 (Estágio), estado RASCUNHO (Campos em falta)
                // ----------------------------------------------------------------
                Candidatura c4 = new Candidatura();
                c4.setFormId(form2.getId());
                c4.setNif("999999999");
                c4.setNome("Daniel Rascunho");
                c4.setAssinado(false);
                c4.setRespostas(Map.of(
                                "nome", "Daniel Rascunho"
                                // email, curso e disponibilidade em falta de propósito
                                ));
                c4.setEstado(CandidaturaEstado.RASCUNHO);
                c4.setCriadoPor(13L);
                c4.setCriadoEm(Instant.parse("2025-05-01T10:00:00Z"));
                candidaturaRepository.save(c4);
                log.info("[DataInitializer] Candidatura 4 criada (Form2 / RASCUNHO)");

                log.info("[DataInitializer] Seed concluído — 2 forms e 4 candidaturas inseridos.");
        }
}
