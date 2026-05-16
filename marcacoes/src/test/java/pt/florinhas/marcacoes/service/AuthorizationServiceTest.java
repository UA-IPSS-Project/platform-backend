package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.common_data.repository.UtilizadorRepository;

class AuthorizationServiceTest {

        private UtilizadorRepository utilizadorRepository;
        private AuthorizationService authorizationService;

        @BeforeEach
        void setup() {

                utilizadorRepository = mock(UtilizadorRepository.class);

                authorizationService = new AuthorizationService(utilizadorRepository);
        }

        @AfterEach
        void cleanup() {

                SecurityContextHolder.clearContext();
        }

        @Test
        void getCurrentUserId_DeveRetornarId() {

                Utilizador user = new Utilizador();
                user.setId(1L);

                TestingAuthenticationToken auth = new TestingAuthenticationToken(
                                user,
                                null,
                                "ROLE_USER");

                auth.setAuthenticated(true);

                SecurityContextHolder.getContext()
                                .setAuthentication(auth);

                Long resultado = authorizationService.getCurrentUserId();

                assertNotNull(resultado);
                assertEquals(1L, resultado);
        }

        @Test
        void hasAnyRole_DeveRetornarTrue() {

                var auth = new TestingAuthenticationToken(
                                "user",
                                null,
                                "ROLE_SECRETARIA");

                SecurityContextHolder.getContext()
                                .setAuthentication(auth);

                assertTrue(
                                authorizationService.hasAnyRole(
                                                "ROLE_SECRETARIA"));
        }

        @Test
        void isAdmin_DeveRetornarTrue() {

                var auth = new TestingAuthenticationToken(
                                "user",
                                null,
                                "ROLE_SECRETARIA");

                SecurityContextHolder.getContext()
                                .setAuthentication(auth);

                assertTrue(
                                authorizationService.isAdmin());
        }

        @Test
        void checkPermission_DevePermitirAdmin() {

                var auth = new TestingAuthenticationToken(
                                "user",
                                null,
                                "ROLE_SECRETARIA");

                SecurityContextHolder.getContext()
                                .setAuthentication(auth);

                assertDoesNotThrow(() -> authorizationService.checkPermission(
                                1L,
                                "recurso"));
        }

        @Test
        void checkPermission_DeveLancarErro() {

                Utilizador user = new Utilizador();
                user.setId(2L);

                var auth = new TestingAuthenticationToken(
                                user,
                                null);

                SecurityContextHolder.getContext()
                                .setAuthentication(auth);

                assertThrows(
                                Exception.class,
                                () -> authorizationService.checkPermission(
                                                1L,
                                                "recurso"));
        }
}