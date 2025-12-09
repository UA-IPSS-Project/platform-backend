package pt.florinhas.marcacoes.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pt.florinhas.marcacoes.domain.*;
import pt.florinhas.marcacoes.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarcacaoServiceTest {

    @Mock
    private MarcacaoRepository marcacaoRepository;

    @Mock
    private MarcacaoSecretariaRepository marcacaoSecretariaRepository;

    @Mock
    private UtenteRepository utenteRepository;

    @Mock
    private FuncionarioRepository funcionarioRepository;

    @InjectMocks
    private MarcacaoServiceImpl marcacaoService;

    private Funcionario funcionarioSecretaria;
    private Utente utente;
    private Marcacao marcacao;
    private MarcacaoSecretaria marcacaoSecretaria;

    @BeforeEach
    void setUp() {
        // Setup Funcionario
        funcionarioSecretaria = new Funcionario();
        funcionarioSecretaria.setId(1L);
        funcionarioSecretaria.setNome("João Silva");
        funcionarioSecretaria.setTipo(FuncionarioTipo.SECRETARIA);
        funcionarioSecretaria.setEmail("joao@test.com");

        // Setup Utente
        utente = new Utente();
        utente.setId(2L);
        utente.setNome("Maria Santos");
        utente.setEmail("maria@test.com");
        utente.setNif("123456789");

        // Setup Marcacao
        marcacao = new Marcacao();
        marcacao.setId(1L);
        marcacao.setData(LocalDateTime.now().plusDays(1));
        marcacao.setEstado(EventoEstado.AGENDADO);
        marcacao.setCriadoPor(funcionarioSecretaria);

        // Setup MarcacaoSecretaria
        marcacaoSecretaria = new MarcacaoSecretaria();
        marcacaoSecretaria.setMarcacaoId(1L);
        marcacaoSecretaria.setMarcacao(marcacao);
        marcacaoSecretaria.setAssunto("Consulta");
        marcacaoSecretaria.setTipoAtendimento(AtendimentoTipo.PRESENCIAL);
        marcacaoSecretaria.setUtente(utente);

        marcacao.setMarcacaoSecretaria(marcacaoSecretaria);
    }

    @Test
    void criarMarcacaoPresencial_DevecriarComSucesso() {
        // Arrange
        LocalDateTime dataFutura = LocalDateTime.now().plusDays(1);
        when(marcacaoRepository.save(any(Marcacao.class))).thenReturn(marcacao);
        when(marcacaoSecretariaRepository.save(any(MarcacaoSecretaria.class))).thenReturn(marcacaoSecretaria);

        // Act
        Marcacao resultado = marcacaoService.criarMarcacaoPresencial(dataFutura, "Consulta", utente, funcionarioSecretaria);

        // Assert
        assertNotNull(resultado);
        verify(marcacaoRepository, times(1)).save(any(Marcacao.class));
        verify(marcacaoSecretariaRepository, times(1)).save(any(MarcacaoSecretaria.class));
    }

    @Test
    void criarMarcacaoPresencial_DeveFalharSeNaoForSecretaria() {
        // Arrange
        Funcionario funcionarioBalneario = new Funcionario();
        funcionarioBalneario.setTipo(FuncionarioTipo.BALNEARIO);
        LocalDateTime dataFutura = LocalDateTime.now().plusDays(1);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            marcacaoService.criarMarcacaoPresencial(dataFutura, "Consulta", utente, funcionarioBalneario)
        );
    }

    @Test
    void criarMarcacaoRemota_DeveCriarComSucesso() {
        // Arrange
        LocalDateTime dataFutura = LocalDateTime.now().plusDays(1);
        when(marcacaoRepository.save(any(Marcacao.class))).thenReturn(marcacao);
        when(marcacaoSecretariaRepository.save(any(MarcacaoSecretaria.class))).thenReturn(marcacaoSecretaria);

        // Act
        Marcacao resultado = marcacaoService.criarMarcacaoRemota(dataFutura, "Consulta", utente);

        // Assert
        assertNotNull(resultado);
        verify(marcacaoRepository, times(1)).save(any(Marcacao.class));
        verify(marcacaoSecretariaRepository, times(1)).save(any(MarcacaoSecretaria.class));
    }

    @Test
    void cancelarMarcacao_DeveCancelarComSucesso() {
        // Arrange
        when(marcacaoRepository.findById(1L)).thenReturn(Optional.of(marcacao));
        when(marcacaoRepository.save(any(Marcacao.class))).thenReturn(marcacao);

        // Act
        marcacaoService.cancelarMarcacao(1L, "Motivo teste", funcionarioSecretaria);

        // Assert
        assertEquals(EventoEstado.CANCELADO, marcacao.getEstado());
        verify(marcacaoRepository, times(1)).save(marcacao);
    }

    @Test
    void cancelarMarcacao_DeveFalharSeMarcacaoNaoExiste() {
        // Arrange
        when(marcacaoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            marcacaoService.cancelarMarcacao(999L, "Motivo", funcionarioSecretaria)
        );
    }

    @Test
    void consultarAgenda_DeveRetornarMarcacoes() {
        // Arrange
        LocalDateTime inicio = LocalDateTime.now();
        LocalDateTime fim = LocalDateTime.now().plusDays(7);
        when(marcacaoRepository.findMarcacoesBetweenDates(inicio, fim))
            .thenReturn(List.of(marcacao));

        // Act
        List<Marcacao> resultado = marcacaoService.consultarAgenda(inicio, fim);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(marcacaoRepository, times(1)).findMarcacoesBetweenDates(inicio, fim);
    }

    @Test
    void procurarAgenda_DeveFiltrarPorEstado() {
        // Arrange
        LocalDateTime inicio = LocalDateTime.now();
        LocalDateTime fim = LocalDateTime.now().plusDays(7);
        when(marcacaoRepository.findMarcacoesBetweenDates(inicio, fim))
            .thenReturn(List.of(marcacao));

        // Act
        List<Marcacao> resultado = marcacaoService.procurarAgenda(inicio, fim, null, null, EventoEstado.AGENDADO);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(EventoEstado.AGENDADO, resultado.get(0).getEstado());
    }

    @Test
    void atualizarEstadoMarcacao_DeveAtualizarComSucesso() {
        // Arrange
        when(marcacaoRepository.findById(1L)).thenReturn(Optional.of(marcacao));
        when(marcacaoRepository.save(any(Marcacao.class))).thenReturn(marcacao);

        // Act
        Marcacao resultado = marcacaoService.atualizarEstadoMarcacao(1L, EventoEstado.CONFIRMADO, funcionarioSecretaria);

        // Assert
        assertNotNull(resultado);
        assertEquals(EventoEstado.CONFIRMADO, marcacao.getEstado());
        verify(marcacaoRepository, times(1)).save(marcacao);
    }

    @Test
    void atualizarEstadoMarcacao_DeveFalharSeNaoForFuncionario() {
        // Arrange
        when(marcacaoRepository.findById(1L)).thenReturn(Optional.of(marcacao));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            marcacaoService.atualizarEstadoMarcacao(1L, EventoEstado.CONFIRMADO, utente)
        );
    }

    @Test
    void criarUtenteAutomatico_DeveCriarComSucesso() {
        // Arrange
        when(utenteRepository.existsByNif("123456789")).thenReturn(false);
        when(utenteRepository.save(any(Utente.class))).thenReturn(utente);

        // Act
        Utente resultado = marcacaoService.criarUtenteAutomatico("Maria Santos", "123456789", "912345678", "maria@test.com");

        // Assert
        assertNotNull(resultado);
        verify(utenteRepository, times(1)).save(any(Utente.class));
    }

    @Test
    void criarUtenteAutomatico_DeveFalharSeNifInvalido() {
        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            marcacaoService.criarUtenteAutomatico("Nome", "123", "912345678", "email@test.com")
        );
    }

    @Test
    void criarUtenteAutomatico_DeveFalharSeNifJaExiste() {
        // Arrange
        when(utenteRepository.existsByNif("123456789")).thenReturn(true);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            marcacaoService.criarUtenteAutomatico("Nome", "123456789", "912345678", "email@test.com")
        );
    }

    @Test
    void findById_DeveRetornarMarcacao() {
        // Arrange
        when(marcacaoRepository.findById(1L)).thenReturn(Optional.of(marcacao));

        // Act
        Optional<Marcacao> resultado = marcacaoService.findById(1L);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getId());
    }

    @Test
    void findById_DeveFalharSeIdNulo() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            marcacaoService.findById(null)
        );
    }

    @Test
    void findAll_DeveRetornarTodasMarcacoes() {
        // Arrange
        when(marcacaoRepository.findAll()).thenReturn(List.of(marcacao));

        // Act
        List<Marcacao> resultado = marcacaoService.findAll();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
    }

    @Test
    void deleteById_DeveRemoverMarcacao() {
        // Arrange
        doNothing().when(marcacaoRepository).deleteById(1L);

        // Act
        marcacaoService.deleteById(1L);

        // Assert
        verify(marcacaoRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteById_DeveFalharSeIdNulo() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            marcacaoService.deleteById(null)
        );
    }

    @Test
    void notificarDocumentosInvalidos_DeveNotificarComSucesso() {
        // Arrange
        when(marcacaoRepository.findById(1L)).thenReturn(Optional.of(marcacao));

        // Act
        marcacaoService.notificarDocumentosInvalidos(1L, "Documentos incompletos", funcionarioSecretaria);

        // Assert
        verify(marcacaoRepository, times(1)).findById(1L);
    }

    @Test
    void notificarDocumentosInvalidos_DeveFalharSeArgumentoNulo() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            marcacaoService.notificarDocumentosInvalidos(null, "Obs", funcionarioSecretaria)
        );
    }
}
