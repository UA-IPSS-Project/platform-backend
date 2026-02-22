package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import pt.florinhas.marcacoes.domain.AtendimentoTipo;
import pt.florinhas.marcacoes.domain.EventoEstado;
import pt.florinhas.marcacoes.domain.Funcionario;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.domain.Utente;
import pt.florinhas.marcacoes.dto.CriarMarcacaoRequest;
import pt.florinhas.marcacoes.repository.FuncionarioRepository;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;
import pt.florinhas.marcacoes.repository.UtenteRepository;
import pt.florinhas.marcacoes.repository.UtilizadorRepository;
import pt.florinhas.marcacoes.service.email.EmailService;
import pt.florinhas.marcacoes.validation.MarcacaoValidator;

@ExtendWith(MockitoExtension.class)
class MarcacaoServiceTest {

    @Mock
    private MarcacaoRepository marcacaoRepository;
    @Mock
    private UtenteRepository utenteRepository;
    @Mock
    private FuncionarioRepository funcionarioRepository;
    @Mock
    private UtilizadorRepository utilizadorRepository;
    @Mock
    private NotificacaoService notificacaoService;
    @Mock
    private MarcacaoValidator marcacaoValidator;
    @Mock
    private EmailService emailService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MarcacaoService marcacaoService;

    @Test
    void criarMarcacaoPresencial_DeveLancarIllegalArgumentException_QuandoValidacaoFalha() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        CriarMarcacaoRequest request = new CriarMarcacaoRequest();
        request.setData(now);
        request.setUtenteId(1L);

        // Mock validator para lançar exceção (simula conflito ou validação falha)
        doThrow(new IllegalArgumentException("Horário já ocupado ou inválido"))
                .when(marcacaoValidator).validarCriacao(request);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> marcacaoService.criarMarcacaoPresencial(request));

        // Ensure validacao was called
        verify(marcacaoValidator).validarCriacao(request);
        // Ensure we didn't proceed to save
        verify(marcacaoRepository, never()).save(any());
    }

    @Test
    void criarMarcacaoRemota_DeveLancarIllegalArgumentException_QuandoValidacaoFalha() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        CriarMarcacaoRequest request = new CriarMarcacaoRequest();
        request.setData(now);
        request.setUtenteId(1L);

        // Mock validator para lançar exceção (simula conflito ou validação falha)
        doThrow(new IllegalArgumentException("Horário já ocupado ou inválido"))
                .when(marcacaoValidator).validarCriacao(request);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> marcacaoService.criarMarcacaoRemota(request));

        // Ensure validacao was called
        verify(marcacaoValidator).validarCriacao(request);
        // Ensure we didn't proceed to save
        verify(marcacaoRepository, never()).save(any());
    }

    @Test
    void criarMarcacaoPresencial_ComSucesso() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        CriarMarcacaoRequest request = new CriarMarcacaoRequest();
        request.setData(now);
        request.setUtenteId(1L);
        request.setCriadoPorId(2L);
        request.setAssunto("Consulta");
        request.setDescricao("Dor de cabeça");

        Utente utente = new Utente();
        utente.setId(1L);

        Funcionario funcionario = new Funcionario();
        funcionario.setId(2L);

        // Mock validator para não lançar exceção (validação passa)
        doNothing().when(marcacaoValidator).validarCriacao(request);
        
        when(utenteRepository.findById(1L)).thenReturn(Optional.of(utente));
        when(funcionarioRepository.findById(2L)).thenReturn(Optional.of(funcionario));
        when(marcacaoRepository.save(any(Marcacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Marcacao resultado = marcacaoService.criarMarcacaoPresencial(request);

        // Assert
        assertNotNull(resultado);
        assertEquals(now, resultado.getData());
        assertEquals(EventoEstado.AGENDADO, resultado.getEstado());
        assertEquals(funcionario, resultado.getCriadoPor());
        assertNotNull(resultado.getMarcacaoSecretaria());
        assertEquals(AtendimentoTipo.PRESENCIAL, resultado.getMarcacaoSecretaria().getTipoAtendimento());
        assertEquals(utente, resultado.getMarcacaoSecretaria().getUtente());
        assertEquals("Consulta", resultado.getMarcacaoSecretaria().getAssunto());
    }

    @Test
    void criarMarcacaoRemota_ComSucesso() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        CriarMarcacaoRequest request = new CriarMarcacaoRequest();
        request.setData(now);
        request.setUtenteId(1L);
        request.setAssunto("Consulta Remota");
        request.setDescricao("Follow up");

        Utente utente = new Utente();
        utente.setId(1L);

        // Mock validator para não lançar exceção (validação passa)
        doNothing().when(marcacaoValidator).validarCriacao(request);

        when(utenteRepository.findById(1L)).thenReturn(Optional.of(utente));
        when(marcacaoRepository.save(any(Marcacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Marcacao resultado = marcacaoService.criarMarcacaoRemota(request);

        // Assert
        assertNotNull(resultado);
        assertEquals(now, resultado.getData());
        assertEquals(EventoEstado.AGENDADO, resultado.getEstado());
        // Na remota, criadoPor pode ser null ou não especificado na lógica atual quando
        // feito por utente
        assertNotNull(resultado.getMarcacaoSecretaria());
        assertEquals(AtendimentoTipo.REMOTO, resultado.getMarcacaoSecretaria().getTipoAtendimento());
        assertEquals(utente, resultado.getMarcacaoSecretaria().getUtente());
        assertEquals("Consulta Remota", resultado.getMarcacaoSecretaria().getAssunto());
    }
}
