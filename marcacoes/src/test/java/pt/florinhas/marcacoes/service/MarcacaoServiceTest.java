package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.dto.CriarMarcacaoRequest;
import pt.florinhas.marcacoes.exception.ConflictException;
import pt.florinhas.marcacoes.repository.FuncionarioRepository;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;
import pt.florinhas.marcacoes.repository.UtenteRepository;
import pt.florinhas.marcacoes.repository.UtilizadorRepository;
import pt.florinhas.marcacoes.service.email.EmailService;

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
    void criarMarcacaoPresencial_DeveLancarConflictException_QuandoHorarioOcupado() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        CriarMarcacaoRequest request = new CriarMarcacaoRequest();
        request.setData(now);
        request.setUtenteId(1L);

        // Mock lock returning conflict
        when(marcacaoRepository.findConflictingWithLock(now))
                .thenReturn(List.of(new Marcacao())); // Not empty

        // Act & Assert
        assertThrows(ConflictException.class, () -> marcacaoService.criarMarcacaoPresencial(request));

        verify(marcacaoRepository).findConflictingWithLock(now);
        // Ensure we didn't proceed to save
        verify(marcacaoRepository, never()).save(any());
    }

    @Test
    void criarMarcacaoRemota_DeveLancarConflictException_QuandoHorarioOcupado() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        CriarMarcacaoRequest request = new CriarMarcacaoRequest();
        request.setData(now);
        request.setUtenteId(1L);

        // Mock lock returning conflict
        when(marcacaoRepository.findConflictingWithLock(now))
                .thenReturn(List.of(new Marcacao())); // Not empty

        // Act & Assert
        assertThrows(ConflictException.class, () -> marcacaoService.criarMarcacaoRemota(request));

        verify(marcacaoRepository).findConflictingWithLock(now);
        // Ensure we didn't proceed to save
        verify(marcacaoRepository, never()).save(any());
    }
}
