package pt.florinhas.marcacoes.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import pt.florinhas.marcacoes.domain.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class MarcacaoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MarcacaoRepository marcacaoRepository;

    private Funcionario funcionario;
    private Utente utente;
    private Marcacao marcacao;

    @BeforeEach
    void setUp() {
        // Setup Funcionario
        funcionario = new Funcionario();
        funcionario.setNome("João Silva");
        funcionario.setEmail("joao@test.com");
        funcionario.setNif("123456789");
        funcionario.setTipo(FuncionarioTipo.SECRETARIA);
        funcionario = entityManager.persist(funcionario);

        // Setup Utente
        utente = new Utente();
        utente.setNome("Maria Santos");
        utente.setEmail("maria@test.com");
        utente.setNif("987654321");
        utente = entityManager.persist(utente);

        // Setup Marcacao
        marcacao = new Marcacao();
        marcacao.setData(LocalDateTime.now().plusDays(1).truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
        marcacao.setEstado(EventoEstado.AGENDADO);
        marcacao.setCriadoPor(funcionario);
        marcacao = entityManager.persist(marcacao);

        // Setup MarcacaoSecretaria
        MarcacaoSecretaria marcacaoSecretaria = new MarcacaoSecretaria();
        marcacaoSecretaria.setMarcacao(marcacao);
        marcacaoSecretaria.setAssunto("Consulta");
        marcacaoSecretaria.setTipoAtendimento(AtendimentoTipo.PRESENCIAL);
        marcacaoSecretaria.setUtente(utente);
        entityManager.persist(marcacaoSecretaria);

        entityManager.flush();
    }

    @Test
    void findByUtente_DeveRetornarMarcacoesDoUtente() {
        // Act
        List<Marcacao> resultado = marcacaoRepository.findByUtente(utente);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
    }

    @Test
    void findByCriadoPor_DeveRetornarMarcacoesCriadasPorUtilizador() {
        // Act
        List<Marcacao> resultado = marcacaoRepository.findByCriadoPor(funcionario);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
    }

    @Test
    void findByEstado_DeveRetornarMarcacoesPorEstado() {
        // Act
        List<Marcacao> resultado = marcacaoRepository.findByEstado(EventoEstado.AGENDADO);

        // Assert
        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
        assertTrue(resultado.stream().allMatch(m -> m.getEstado() == EventoEstado.AGENDADO));
    }

    @Test
    void findMarcacoesBetweenDates_DeveRetornarMarcacoesNoPeriodo() {
        // Arrange
        LocalDateTime inicio = LocalDateTime.now();
        LocalDateTime fim = LocalDateTime.now().plusDays(7);

        // Act
        List<Marcacao> resultado = marcacaoRepository.findMarcacoesBetweenDates(inicio, fim, null);

        // Assert
        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
    }

    @Test
    void existsByDataAndEstadoNot_DeveRetornarTrueSeExiste() {
        // Arrange
        LocalDateTime data = marcacao.getData();

        // Act
        boolean existe = marcacaoRepository.existsByDataAndEstadoNot(data, EventoEstado.CANCELADO);

        // Assert
        assertTrue(existe, "Deveria existir marcação na data " + data + " com estado diferente de CANCELADO");
    }

    @Test
    void existsByDataAndEstadoNot_DeveRetornarFalseSeNaoExiste() {
        // Arrange
        LocalDateTime dataInexistente = LocalDateTime.now().plusYears(1);

        // Act
        boolean existe = marcacaoRepository.existsByDataAndEstadoNot(dataInexistente, EventoEstado.CANCELADO);

        // Assert
        assertFalse(existe);
    }
}
