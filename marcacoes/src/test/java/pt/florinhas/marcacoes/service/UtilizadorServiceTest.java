package pt.florinhas.marcacoes.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import pt.florinhas.common_data.domain.Funcionario;
import pt.florinhas.common_data.domain.Utente;
import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.common_data.dto.UtilizadorInfoDTO;
import pt.florinhas.common_data.repository.FuncionarioRepository;
import pt.florinhas.common_data.repository.UtenteRepository;
import pt.florinhas.common_data.repository.UtilizadorRepository;
import pt.florinhas.marcacoes.dto.CreateUserRequestDTO;
import pt.florinhas.marcacoes.dto.RecoverAccountDTO;
import pt.florinhas.marcacoes.service.email.EmailService;
import pt.florinhas.marcacoes.validation.NifValidator;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UtilizadorServiceTest {

    @Mock private UtilizadorRepository utilizadorRepository;
    @Mock private UtenteRepository utenteRepository;
    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private EmailService emailService;
    @Mock private NifValidator nifValidator;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UtilizadorService utilizadorService;

    @Test
    void shouldReturnUserByEmail() {
        Utilizador user = new Utente();
        user.setEmail("a@b.com");

        when(utilizadorRepository.findByEmail("a@b.com")).thenReturn(List.of(user));

        assertEquals(user, utilizadorService.buscarPorEmail("a@b.com"));
    }

    @Test
    void shouldThrowWhenEmailNotFound() {
        when(utilizadorRepository.findByEmail("x@y.com")).thenReturn(List.of());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> utilizadorService.buscarPorEmail("x@y.com")
        );

        assertTrue(ex.getMessage().contains("Utilizador não encontrado"));
    }

    @Test
    void shouldReturnEmptyWhenSearchingBlankNif() {
        assertTrue(utilizadorService.buscarPorNif(" ").isEmpty());
    }

    @Test
    void shouldReturnUserWhenSearchingNif() {
        Utilizador user = new Utente();
        user.setId(1L);
        when(utilizadorRepository.findByNif("100000002")).thenReturn(List.of(user));

        Optional<Utilizador> result = utilizadorService.buscarPorNif("100000002");

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void shouldReturnExistingUtenteWhenNifAlreadyBelongsToUtente() {
        Utente utente = new Utente();
        utente.setNif("100000002");

        when(utilizadorRepository.findByNif("100000002")).thenReturn(List.of(utente));

        Utente result = utilizadorService.obterOuCriarUtente("100000002", "Nome", "u@x.com", "999");

        assertSame(utente, result);
    }

    @Test
    void shouldThrowWhenNifAlreadyBelongsToFuncionario() {
        Funcionario funcionario = new Funcionario();
        funcionario.setNif("100000002");

        when(utilizadorRepository.findByNif("100000002")).thenReturn(List.of(funcionario));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> utilizadorService.obterOuCriarUtente("100000002", "Nome", "u@x.com", "999")
        );

        assertTrue(ex.getMessage().contains("já está registado como Funcionário"));
    }

    @Test
    void shouldThrowWhenCreatingUtenteWithoutRequiredName() {
        when(utilizadorRepository.findByNif("100000002")).thenReturn(List.of());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> utilizadorService.obterOuCriarUtente("100000002", " ", "u@x.com", "999")
        );

        assertEquals("Nome do utente é obrigatório para criar novo registo", ex.getMessage());
    }

    @Test
    void shouldThrowWhenEmailAlreadyExistsWhileCreatingUtente() {
        when(utilizadorRepository.findByNif("100000002")).thenReturn(List.of());
        when(utenteRepository.existsByEmail("u@x.com")).thenReturn(true);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> utilizadorService.obterOuCriarUtente("100000002", "Nome", "u@x.com", "999")
        );

        assertEquals("Email já está registado no sistema", ex.getMessage());
    }

    @Test
    void shouldCreateUtenteSuccessfully() {
        when(utilizadorRepository.findByNif("100000002")).thenReturn(List.of());
        when(utenteRepository.existsByEmail("u@x.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("HASH");
        when(utenteRepository.save(any(Utente.class))).thenAnswer(inv -> {
            Utente u = inv.getArgument(0);
            u.setId(10L);
            return u;
        });

        Utente result = utilizadorService.obterOuCriarUtente("100000002", "Nome", "u@x.com", "999");

        assertEquals("100000002", result.getNif());
        assertFalse(result.isActivo());
        verify(emailService).sendPassword(eq("u@x.com"), anyString());
        verify(nifValidator).validateRequiredOrThrow("100000002");
    }

    @Test
    void shouldGetUserById() {
        Utilizador user = new Utente();
        user.setId(1L);

        when(utilizadorRepository.findById(1L)).thenReturn(Optional.of(user));

        assertEquals(user, utilizadorService.obterUtilizadorPorId(1L));
    }

    @Test
    void shouldThrowWhenUserIdNotFound() {
        when(utilizadorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> utilizadorService.obterUtilizadorPorId(1L));
    }

    @Test
    void shouldUpdateUtilizadorFields() {
        Utilizador user = new Utente();
        user.setId(1L);
        user.setNome("Antigo");
        user.setEmail("old@x.com");

        UtilizadorInfoDTO dto = new UtilizadorInfoDTO();
        dto.setNome("Novo Nome");
        dto.setEmail("new@x.com");
        dto.setTelefone("999");
        dto.setDataNasc("2000-01-01");

        when(utilizadorRepository.findById(1L)).thenReturn(Optional.of(user));
        when(utilizadorRepository.findByEmail("new@x.com")).thenReturn(List.of());
        when(utilizadorRepository.save(user)).thenReturn(user);

        Utilizador result = utilizadorService.atualizarUtilizador(1L, dto);

        assertEquals("Novo Nome", result.getNome());
        assertEquals("new@x.com", result.getEmail());
        assertEquals("999", result.getTelefone());
        assertEquals(LocalDate.of(2000, 1, 1), result.getDataNasc());
    }

    @Test
    void shouldThrowWhenUpdatingEmailAlreadyUsedByAnotherUser() {
        Utilizador current = new Utente();
        current.setId(1L);

        Utilizador other = new Utente();
        other.setId(2L);

        UtilizadorInfoDTO dto = new UtilizadorInfoDTO();
        dto.setEmail("dup@x.com");

        when(utilizadorRepository.findById(1L)).thenReturn(Optional.of(current));
        when(utilizadorRepository.findByEmail("dup@x.com")).thenReturn(List.of(other));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> utilizadorService.atualizarUtilizador(1L, dto)
        );

        assertEquals("Email já está em uso por outro utilizador", ex.getMessage());
    }

    @Test
    void shouldThrowWhenBirthDateInvalid() {
        Utilizador user = new Utente();
        user.setId(1L);

        UtilizadorInfoDTO dto = new UtilizadorInfoDTO();
        dto.setDataNasc("31-12-2000");

        when(utilizadorRepository.findById(1L)).thenReturn(Optional.of(user));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> utilizadorService.atualizarUtilizador(1L, dto)
        );

        assertEquals("Formato de data inválido. Use YYYY-MM-DD", ex.getMessage());
    }

    @Test
    void shouldApproveFuncionario() {
        Funcionario funcionario = new Funcionario();
        funcionario.setId(1L);
        funcionario.setActivo(false);

        when(funcionarioRepository.findById(1L)).thenReturn(Optional.of(funcionario));

        utilizadorService.aprovarFuncionario(1L);

        assertTrue(funcionario.isActivo());
        verify(funcionarioRepository).save(funcionario);
    }

    @Test
    void shouldCreateUserBySecretariaAsUtente() {
        CreateUserRequestDTO dto = new CreateUserRequestDTO();
        dto.setNif("100000002");
        dto.setName("Nome");
        dto.setContact("999");
        dto.setEmail("u@x.com");
        dto.setBirthDate("2000-01-01");
        dto.setEmployee(false);
        dto.setRole("UTENTE");

        when(utilizadorRepository.existsByNif("100000002")).thenReturn(false);
        when(utilizadorRepository.existsByEmail("u@x.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("HASH");
        when(utilizadorRepository.save(any(Utilizador.class))).thenAnswer(inv -> {
            Utilizador u = inv.getArgument(0);
            u.setId(3L);
            return u;
        });

        Utilizador result = utilizadorService.criarUtilizadorPelaSecretaria(dto);

        assertTrue(result instanceof Utente);
        verify(emailService).sendPassword(eq("u@x.com"), anyString());
    }

    @Test
    void shouldCreateUserBySecretariaAsFuncionario() {
        CreateUserRequestDTO dto = new CreateUserRequestDTO();
        dto.setNif("100000002");
        dto.setName("Nome");
        dto.setContact("999");
        dto.setEmail("f@x.com");
        dto.setBirthDate("2000-01-01");
        dto.setEmployee(true);
        dto.setRole("SECRETARIA");

        when(utilizadorRepository.existsByNif("100000002")).thenReturn(false);
        when(utilizadorRepository.existsByEmail("f@x.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("HASH");
        when(utilizadorRepository.save(any(Utilizador.class))).thenAnswer(inv -> {
            Utilizador u = inv.getArgument(0);
            u.setId(4L);
            return u;
        });

        Utilizador result = utilizadorService.criarUtilizadorPelaSecretaria(dto);

        assertTrue(result instanceof Funcionario);
        assertFalse(((Funcionario) result).isActivo());
    }

    @Test
    void shouldRecoverAccountAndSendNewPassword() {
        Utente utente = new Utente();
        utente.setId(1L);
        utente.setNif("100000002");
        utente.setEmail("old@x.com");
        utente.setTelefone("111");
        utente.setActivo(true);

        RecoverAccountDTO dto = new RecoverAccountDTO();
        dto.setNif("100000002");
        dto.setUpdatedEmail("new@x.com");
        dto.setUpdatedContact("999");

        when(utilizadorRepository.findByNif("100000002")).thenReturn(List.of(utente));
        when(utilizadorRepository.existsByEmail("new@x.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("HASH");

        utilizadorService.recuperarConta(dto);

        assertEquals("new@x.com", utente.getEmail());
        assertEquals("999", utente.getTelefone());
        assertFalse(utente.isActivo());
        verify(utilizadorRepository).save(utente);
        verify(emailService).sendPassword(eq("new@x.com"), anyString());
    }

    @Test
    void shouldCountActiveUtentes() {
        when(utenteRepository.countByActivo(true)).thenReturn(12L);
        assertEquals(12L, utilizadorService.contarUtentesAtivos());
    }
}