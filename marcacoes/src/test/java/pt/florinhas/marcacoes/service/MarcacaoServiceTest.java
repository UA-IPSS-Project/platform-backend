package pt.florinhas.marcacoes.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import pt.florinhas.marcacoes.domain.Funcionario;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.domain.MarcacaoBalneario;
import pt.florinhas.marcacoes.domain.Utente;
import pt.florinhas.marcacoes.domain.Valencia;
import pt.florinhas.marcacoes.repository.FuncionarioRepository;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;
import pt.florinhas.marcacoes.repository.UtenteRepository;
import pt.florinhas.marcacoes.repository.ValenciaRepository;

@ExtendWith(MockitoExtension.class)
class MarcacaoServiceTest{

    @Mock
    private MarcacaoRepository marcacaoRepository;

    @Mock
    private UtenteRepository utenteRepository;

    @Mock
    private FuncionarioRepository funcionarioRepository;

    @Mock
    private ValenciaRepository valenciaRepository;

    //@Mock
    //private EmailService emailService;

    @InjectMocks
    private MarcacaoServiceImpl marcacaoService;

    private Utente utente;
    private Funcionario funcionario;
    private Valencia valencia;
    private Funcionario secretaria;

    @BeforeEach
    void setUp() {
        utente = new Utente();
        utente.setId(1L);
        utente.setNome("João Silva");
        utente.setNif("123456789");
        utente.setTelefone("912345678");
        utente.setEmail("joao@email.com");

        funcionario = new Funcionario();
        funcionario.setId(1L);
        funcionario.setNome("Dr. Maria Santos");
        funcionario.setTipo("SECRETARIA");

        valencia = new Valencia();
        valencia.setId(1L);
        valencia.setNome("Serviço Social");

        secretaria = new Funcionario();
        secretaria.setId(2L);
        secretaria.setNome("Ana Costa");
        secretaria.setTipo("SECRETARIA");
    }

    @Test
    void testCriarMarcacaoPresencial_Success() {
        // Arrange
        LocalDate data = LocalDate.now().plusDays(1);
        LocalTime hora = LocalTime.of(10, 0);
        
        when(marcacaoRepository.existsByDataAndHoraAndFuncionarioAndEstadoNot(any(), any(), any(), any())).thenReturn(false);
        when(marcacaoRepository.save(any(Marcacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Marcacao result = marcacaoService.criarMarcacaoPresencial(
                data, hora, "PRESENCIAL", utente, funcionario, valencia, secretaria);

        // Assert
        assertNotNull(result);
        assertEquals(data, result.getData());
        assertEquals(hora, result.getHora());
        assertEquals("PRESENCIAL", result.getTipoAtendimento());
        assertEquals("AGENDADO", result.getEstado());
        assertEquals(utente, result.getUtente());
        assertEquals(funcionario, result.getFuncionario());
        assertEquals(valencia, result.getValencia());
        
        verify(marcacaoRepository, times(1)).save(any(Marcacao.class));
    }

    @Test
    void testCriarMarcacaoRemota_Success() {
        // Arrange
        LocalDate data = LocalDate.now().plusDays(2);
        LocalTime hora = LocalTime.of(14, 30);
        
        when(marcacaoRepository.existsByDataAndHoraAndFuncionarioAndEstadoNot(any(), any(), any(), any())).thenReturn(false);
        when(marcacaoRepository.save(any(Marcacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Marcacao result = marcacaoService.criarMarcacaoRemota(
                data, hora, "REMOTO", utente, funcionario, valencia);

        // Assert
        assertNotNull(result);
        assertEquals(data, result.getData());
        assertEquals(hora, result.getHora());
        assertEquals("REMOTO", result.getTipoAtendimento());
        assertEquals("AGENDADO", result.getEstado());
        assertFalse(result.getPresencaConfirmada());
        assertFalse(result.getDocumentosInvalidos());
        
        verify(marcacaoRepository, times(1)).save(any(Marcacao.class));
    }

    @Test
    void testCancelarMarcacao_Success() {
        // Arrange
        Long marcacaoId = 1L;
        Marcacao marcacao = new Marcacao();
        marcacao.setId(marcacaoId);
        marcacao.setEstado("AGENDADO");
        marcacao.setUtente(utente);
        marcacao.setFuncionario(funcionario);

        when(marcacaoRepository.findById(marcacaoId)).thenReturn(Optional.of(marcacao));
        when(marcacaoRepository.save(any(Marcacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        marcacaoService.cancelarMarcacao(marcacaoId, "Utente não compareceu", secretaria);

        // Assert
        assertEquals("CANCELADO", marcacao.getEstado());
        assertNotNull(marcacao.getDataAtualizacao());
        verify(marcacaoRepository, times(1)).save(marcacao);
    }

    @Test
    void testCriarMarcacaoBalnearioTecnico_Success() {
        // Arrange
        LocalDate data = LocalDate.now().plusDays(1);
        LocalTime hora = LocalTime.of(9, 0);
        
        funcionario.setTipo("TECNICO");
        
        when(marcacaoRepository.existsByDataAndHoraAndFuncionarioAndEstadoNot(any(), any(), any(), any())).thenReturn(false);
        when(marcacaoRepository.save(any(MarcacaoBalneario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        MarcacaoBalneario result = marcacaoService.criarMarcacaoBalnearioTecnico(
                data, hora, utente, funcionario, true, false, "Camisola e calças");

        // Assert
        assertNotNull(result);
        assertEquals(MarcacaoBalneario.class, result.getClass());
        assertEquals(data, result.getData());
        assertEquals(hora, result.getHora());
        assertTrue(result.getProdutosHigiene());
        assertFalse(result.getLavagemRoupa());
        assertEquals("Camisola e calças", result.getRoupaDescricao());
        assertFalse(result.getConsumoRegistado());
        
        verify(marcacaoRepository, times(1)).save(any(MarcacaoBalneario.class));
    }

    @Test
    void testAtualizarEstadoMarcacao_Success() {
        // Arrange
        Long marcacaoId = 1L;
        Marcacao marcacao = new Marcacao();
        marcacao.setId(marcacaoId);
        marcacao.setEstado("AGENDADO");

        when(marcacaoRepository.findById(marcacaoId)).thenReturn(Optional.of(marcacao));
        when(marcacaoRepository.save(any(Marcacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Marcacao result = marcacaoService.atualizarEstadoMarcacao(marcacaoId, "CONCLUIDO", secretaria);

        // Assert
        assertEquals("CONCLUIDO", result.getEstado());
        assertNotNull(result.getDataAtualizacao());
        verify(marcacaoRepository, times(1)).save(marcacao);
    }

    @Test
    void testConfirmarPresencaBalneario_Success() {
        // Arrange
        Long marcacaoId = 1L;
        MarcacaoBalneario marcacao = new MarcacaoBalneario();
        marcacao.setId(marcacaoId);
        marcacao.setEstado("AGENDADO");
        marcacao.setPresencaConfirmada(false);

        funcionario.setTipo("BALNEARIO");

        when(marcacaoRepository.findById(marcacaoId)).thenReturn(Optional.of(marcacao));
        when(marcacaoRepository.save(any(MarcacaoBalneario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        marcacaoService.confirmarPresencaBalneario(marcacaoId, true, funcionario);

        // Assert
        assertTrue(marcacao.getPresencaConfirmada());
        assertEquals("CONFIRMADO", marcacao.getEstado());
        assertNotNull(marcacao.getDataAtualizacao());
        verify(marcacaoRepository, times(1)).save(marcacao);
    }

    //@Test
    //void testNotificarDocumentosInvalidos_Success() {
    //    // Arrange
    //    Long marcacaoId = 1L;
    //    Marcacao marcacao = new Marcacao();
    //    marcacao.setId(marcacaoId);
    //    marcacao.setDocumentosInvalidos(false);
    //
    //    when(marcacaoRepository.findById(marcacaoId)).thenReturn(Optional.of(marcacao));
    //    when(marcacaoRepository.save(any(Marcacao.class))).thenAnswer(invocation -> invocation.getArgument(0));
    //
    //    // Act
    //    marcacaoService.notificarDocumentosInvalidos(marcacaoId, "NIF inválido", secretaria);
    //
    //    // Assert
    //    assertTrue(marcacao.getDocumentosInvalidos());
    //    assertEquals("NIF inválido", marcacao.getDocumentosObservacoes());
    //    assertNotNull(marcacao.getDataAtualizacao());
    //    verify(marcacaoRepository, times(1)).save(marcacao);
    //}

    @Test
    void testFindById_Success() {
        // Arrange
        Long marcacaoId = 1L;
        Marcacao marcacao = new Marcacao();
        marcacao.setId(marcacaoId);

        when(marcacaoRepository.findById(marcacaoId)).thenReturn(Optional.of(marcacao));

        // Act
        Optional<Marcacao> result = marcacaoService.findById(marcacaoId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(marcacaoId, result.get().getId());
        verify(marcacaoRepository, times(1)).findById(marcacaoId);
    }

    @Test
    void testFindById_NotFound() {
        // Arrange
        Long marcacaoId = 999L;
        when(marcacaoRepository.findById(marcacaoId)).thenReturn(Optional.empty());

        // Act
        Optional<Marcacao> result = marcacaoService.findById(marcacaoId);

        // Assert
        assertFalse(result.isPresent());
        verify(marcacaoRepository, times(1)).findById(marcacaoId);
    }
}