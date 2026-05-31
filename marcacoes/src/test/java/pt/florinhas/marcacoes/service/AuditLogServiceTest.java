package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.http.HttpServletRequest;
import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.common_data.repository.UtilizadorRepository;
import pt.florinhas.marcacoes.domain.AuditLog;
import pt.florinhas.marcacoes.repository.AuditLogRepository;

class AuditLogServiceTest {

    private AuditLogRepository auditLogRepository;
    private UtilizadorRepository utilizadorRepository;
    private HttpServletRequest request;

    private AuditLogService service;

    @BeforeEach
    void setUp() throws Exception {

        auditLogRepository = mock(AuditLogRepository.class);
        utilizadorRepository = mock(UtilizadorRepository.class);
        request = new MockHttpServletRequest();

        service = new AuditLogService();

        setField("auditLogRepository", auditLogRepository);
        setField("utilizadorRepository", utilizadorRepository);
        setField("request", request);

        SecurityContextHolder.clearContext();
    }

    @Test
    void log_DeveGuardarLogComUtilizador() {

        Utilizador utilizador = new Utilizador();

        utilizador.setId(1L);
        utilizador.setNome("Nuno");

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        utilizador,
                        null,
                        List.of());

        SecurityContextHolder.getContext()
                .setAuthentication(auth);

        service.log(
                "LOGIN",
                "USER",
                1L,
                "Teste");

        verify(auditLogRepository)
                .save(any(AuditLog.class));
    }

    @Test
    void log_DeveResolverUtilizadorPorEmail() {

        Utilizador utilizador = new Utilizador();

        utilizador.setId(2L);
        utilizador.setNome("Teste");

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        "teste@test.com",
                        null,
                        List.of());

        SecurityContextHolder.getContext()
                .setAuthentication(auth);

        when(utilizadorRepository.findByEmail("teste@test.com"))
                .thenReturn(List.of(utilizador));

        service.log(
                "LOGIN",
                "USER",
                1L,
                "Teste");

        verify(auditLogRepository)
                .save(any(AuditLog.class));
    }

    @Test
    void log_DeveFuncionarSemAutenticacao() {

        service.log(
                "LOGIN",
                "USER",
                1L,
                "Teste");

        verify(auditLogRepository)
                .save(any(AuditLog.class));
    }

    @Test
    void log_NaoDeveLancarErro() {

        when(auditLogRepository.save(any()))
                .thenThrow(new RuntimeException());

        assertDoesNotThrow(() ->
                service.log(
                        "LOGIN",
                        "USER",
                        1L,
                        "Teste"));
    }

    @Test
    void findAll_DeveRetornarPagina() {

        when(auditLogRepository.findAllByOrderByTimestampDesc(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        assertEquals(0, service.findAll(Pageable.unpaged()).getTotalElements());
    }

    @Test
    void findWithFilters_DeveRetornarPagina() {

        when(auditLogRepository.findWithFilters(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()))
                .thenReturn(new PageImpl<>(List.of()));

        assertEquals(
                0,
                service.findWithFilters(
                        null,
                        null,
                        null,
                        null,
                        null,
                        Pageable.unpaged())
                        .getTotalElements());
    }

    private void setField(String field, Object value) throws Exception {

        Field f = AuditLogService.class.getDeclaredField(field);

        f.setAccessible(true);

        f.set(service, value);
    }
}