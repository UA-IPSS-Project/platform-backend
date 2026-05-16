package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

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
        void setup() {
                auditLogRepository = mock(AuditLogRepository.class);
                utilizadorRepository = mock(UtilizadorRepository.class);
                request = mock(HttpServletRequest.class);

                @SuppressWarnings("unchecked")
                ObjectProvider<HttpServletRequest> requestProvider = mock(ObjectProvider.class);
                when(requestProvider.getIfAvailable()).thenReturn(request);

                service = new AuditLogService(auditLogRepository, utilizadorRepository, requestProvider);
        }

        @AfterEach
        void cleanup() {
                SecurityContextHolder.clearContext();
        }

        @Test
        @DisplayName("Deve guardar auditoria sem autenticacao")
        void log_DeveGuardarAuditoriaSemAutenticacao() {

                when(request.getHeader("X-Forwarded-For"))
                                .thenReturn("127.0.0.1");

                assertDoesNotThrow(() -> service.log(
                                "CREATE",
                                "UTILIZADOR",
                                1L,
                                "Detalhes"));

                verify(auditLogRepository)
                                .save(any(AuditLog.class));
        }

        @Test
        @DisplayName("Deve guardar auditoria com utilizador autenticado")
        void log_DeveGuardarAuditoriaComUtilizadorAutenticado() {

                Utilizador utilizador = new Utilizador();

                utilizador.setId(10L);
                utilizador.setNome("Joao");

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                utilizador,
                                null,
                                List.of());

                SecurityContextHolder
                                .getContext()
                                .setAuthentication(auth);

                when(request.getHeader("X-Forwarded-For"))
                                .thenReturn("192.168.1.1");

                service.log(
                                "UPDATE",
                                "DOCUMENTO",
                                5L,
                                "Documento atualizado");

                verify(auditLogRepository)
                                .save(argThat(log -> log.getUserId().equals(10L)
                                                && log.getUserName().equals("Joao")
                                                && log.getAction().equals("UPDATE")));
        }

        @Test
        @DisplayName("Deve buscar utilizador por email quando autenticado via principal String")
        void log_DeveBuscarUtilizadorPorEmail() {

                Utilizador utilizador = new Utilizador();

                utilizador.setId(20L);
                utilizador.setNome("Maria");
                utilizador.setEmail("maria@test.com");

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                "maria@test.com",
                                null,
                                List.of());

                SecurityContextHolder
                                .getContext()
                                .setAuthentication(auth);

                when(utilizadorRepository.findByEmail("maria@test.com"))
                                .thenReturn(List.of(utilizador));

                when(request.getHeader("X-Forwarded-For"))
                                .thenReturn("10.0.0.1");

                service.log(
                                "DELETE",
                                "UTENTE",
                                7L,
                                "Removido");

                verify(auditLogRepository)
                                .save(argThat(log -> log.getUserId().equals(20L)
                                                && log.getUserName().equals("Maria")));
        }

        @Test
        @DisplayName("Deve manter userId nulo se o utilizador autenticado por email nao for encontrado na base de dados")
        void log_DeveManterUserIdNulo_QuandoUtilizadorPorEmailNaoEncontrado() {
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                "unknown@test.com",
                                null,
                                List.of());

                SecurityContextHolder.getContext().setAuthentication(auth);
                when(utilizadorRepository.findByEmail("unknown@test.com")).thenReturn(List.of());
                when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1");

                service.log("DELETE", "UTENTE", 7L, "Removido");

                verify(auditLogRepository).save(argThat(log -> log.getUserId() == null && "Sistema".equals(log.getUserName())));
        }

        @Test
        @DisplayName("Deve ignorar autenticacao anonima")
        void log_DeveIgnorarAutenticacaoAnonima() {
                UsernamePasswordAuthenticationToken anonAuth = new UsernamePasswordAuthenticationToken(
                                "anonymousUser",
                                null,
                                List.of());
                SecurityContextHolder.getContext().setAuthentication(anonAuth);
                when(request.getHeader("X-Forwarded-For")).thenReturn("127.0.0.1");

                service.log("CREATE", "UTENTE", 1L, "Anon");
                verify(auditLogRepository).save(argThat(log -> log.getUserId() == null && "Sistema".equals(log.getUserName())));
        }

        @Test
        @DisplayName("Deve ignorar autenticacao nao autenticada")
        void log_DeveIgnorarAutenticacaoNaoAutenticada() {
                UsernamePasswordAuthenticationToken unauth = new UsernamePasswordAuthenticationToken("user", null);
                unauth.setAuthenticated(false);
                SecurityContextHolder.getContext().setAuthentication(unauth);
                when(request.getHeader("X-Forwarded-For")).thenReturn("127.0.0.1");

                service.log("CREATE", "UTENTE", 2L, "Unauth");
                verify(auditLogRepository).save(argThat(log -> log.getUserId() == null && "Sistema".equals(log.getUserName())));
        }

        @Test
        @DisplayName("Deve continuar mesmo com erro ao buscar utilizador")
        void log_DeveContinuarMesmoComErroAoBuscarUtilizador() {

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                "erro@test.com",
                                null,
                                List.of());

                SecurityContextHolder
                                .getContext()
                                .setAuthentication(auth);

                when(utilizadorRepository.findByEmail("erro@test.com"))
                                .thenThrow(new RuntimeException("Erro"));

                when(request.getHeader("X-Forwarded-For"))
                                .thenReturn("127.0.0.1");

                assertDoesNotThrow(() -> service.log(
                                "TEST",
                                "ENTITY",
                                1L,
                                "Teste"));

                verify(auditLogRepository)
                                .save(any(AuditLog.class));
        }

        @Test
        @DisplayName("Deve ignorar erro ao guardar audit log")
        void log_DeveIgnorarErroAoGuardar() {

                when(request.getHeader("X-Forwarded-For"))
                                .thenReturn("127.0.0.1");

                doThrow(new RuntimeException("DB Error"))
                                .when(auditLogRepository)
                                .save(any(AuditLog.class));

                assertDoesNotThrow(() -> service.log(
                                "CREATE",
                                "UTILIZADOR",
                                1L,
                                "Teste"));
        }

        @Test
        @DisplayName("findAll deve retornar pagina ordenada")
        void findAll_DeveRetornarPagina() {

                Pageable pageable = PageRequest.of(0, 10);

                Page<AuditLog> pagina = new PageImpl<>(List.of(
                                new AuditLog()));

                when(auditLogRepository
                                .findAllByOrderByTimestampDesc(pageable))
                                .thenReturn(pagina);

                Page<AuditLog> resultado = service.findAll(pageable);

                assertEquals(
                                1,
                                resultado.getContent().size());
        }

        @Test
        @DisplayName("findWithFilters deve retornar pagina com filtros")
        void findWithFilters_DeveRetornarPagina() {

                Pageable pageable = PageRequest.of(0, 10);

                Page<AuditLog> pagina = new PageImpl<>(List.of(
                                new AuditLog()));

                LocalDateTime inicio = LocalDateTime.now().minusDays(1);

                LocalDateTime fim = LocalDateTime.now();

                when(auditLogRepository.findWithFilters(
                                1L,
                                "CREATE",
                                "UTILIZADOR",
                                inicio,
                                fim,
                                pageable)).thenReturn(pagina);

                Page<AuditLog> resultado = service.findWithFilters(
                                1L,
                                "CREATE",
                                "UTILIZADOR",
                                inicio,
                                fim,
                                pageable);

                assertEquals(
                                1,
                                resultado.getContent().size());
        }

        @Test
        @DisplayName("getClientIp deve usar o primeiro IP do cabecalho X-Forwarded-For")
        void getClientIp_DeveUsarXForwardedFor() throws Exception {

                when(request.getHeader("X-Forwarded-For"))
                                .thenReturn("192.168.1.100, 10.0.0.1");

                String resultado = invokeGetClientIp();

                assertEquals(
                                "192.168.1.100",
                                resultado);
        }

        @Test
        @DisplayName("getClientIp deve usar X-Real-Ip se X-Forwarded-For for unknown ou nulo")
        void getClientIp_DeveUsarXRealIp() throws Exception {

                when(request.getHeader("X-Forwarded-For"))
                                .thenReturn("unknown");

                when(request.getHeader("X-Real-IP"))
                                .thenReturn("10.0.0.1");

                String resultado = invokeGetClientIp();

                assertEquals(
                                "10.0.0.1",
                                resultado);
        }

        @Test
        @DisplayName("getClientIp deve usar RemoteAddr se os outros cabecalhos forem unknown")
        void getClientIp_DeveUsarRemoteAddr() throws Exception {

                when(request.getHeader("X-Forwarded-For"))
                                .thenReturn(null);

                when(request.getHeader("X-Real-IP"))
                                .thenReturn("unknown");

                when(request.getRemoteAddr())
                                .thenReturn("127.0.0.1");

                String resultado = invokeGetClientIp();

                assertEquals(
                                "127.0.0.1",
                                resultado);
        }

        @Test
        @DisplayName("getClientIp deve retornar unknown quando requestProvider retornar nulo")
        void getClientIp_DeveRetornarUnknownQuandoRequestNull() throws Exception {

                @SuppressWarnings("unchecked")
                ObjectProvider<HttpServletRequest> emptyProvider = (ObjectProvider<HttpServletRequest>) mock(
                                ObjectProvider.class);
                when(emptyProvider.getIfAvailable()).thenReturn(null);

                ReflectionTestUtils.setField(service, "requestProvider", emptyProvider);

                String resultado = invokeGetClientIp();
                assertEquals("unknown", resultado);
        }

        private String invokeGetClientIp() throws Exception {

                var method = AuditLogService.class
                                .getDeclaredMethod("getClientIp");

                method.setAccessible(true);

                return (String) method.invoke(service);
        }
}