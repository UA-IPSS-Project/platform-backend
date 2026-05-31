package pt.florinhas.api_gateway.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import io.jsonwebtoken.Claims;
import pt.florinhas.common_data.domain.Utilizador;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtService();

        setField("secret", "12345678901234567890123456789012");
        setField("jwtExpiration", 86400000L);

        jwtService.init();
    }

    @Test
    void generateToken_DeveGerarToken() {
        String token = jwtService.generateToken(
                new JwtService.AuthUserClaims(
                        1L,
                        "teste@teste.com",
                        "Teste",
                        "UTENTE",
                        "123456789",
                        "912345678",
                        List.of(new SimpleGrantedAuthority("ROLE_UTENTE"))));

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void parseClaims_DeveExtrairClaims() {
        String token = jwtService.generateToken(
                new JwtService.AuthUserClaims(
                        1L,
                        "teste@teste.com",
                        "Teste",
                        "UTENTE",
                        "123456789",
                        "912345678",
                        List.of(new SimpleGrantedAuthority("ROLE_UTENTE"))));

        Claims claims = jwtService.parseClaims(token);

        assertEquals("teste@teste.com", claims.getSubject());
        assertEquals("Teste", claims.get("nome"));
        assertEquals("UTENTE", claims.get("role"));
    }

    @Test
    void generateToken_DeveUsarNifQuandoEmailNull() {
        String token = jwtService.generateToken(
                new JwtService.AuthUserClaims(
                        1L,
                        null,
                        "Teste",
                        "UTENTE",
                        "123456789",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_UTENTE"))));

        Claims claims = jwtService.parseClaims(token);

        assertEquals("123456789", claims.getSubject());
    }

    @Test
    void generateToken_DeveGuardarRoles() {
        String token = jwtService.generateToken(
                new JwtService.AuthUserClaims(
                        1L,
                        "teste@teste.com",
                        "Teste",
                        "UTENTE",
                        "123456789",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_UTENTE"))));

        Claims claims = jwtService.parseClaims(token);

        List<?> roles = claims.get("roles", List.class);

        assertEquals(1, roles.size());
        assertEquals("ROLE_UTENTE", roles.get(0));
    }

    private void setField(String fieldName, Object value)
            throws Exception {

        Field field =
                JwtService.class.getDeclaredField(fieldName);

        field.setAccessible(true);
        field.set(jwtService, value);
    }
    @Test
        void generateToken_DeveFallbackSubjectParaNifQuandoEmailEmBranco() {

        JwtService.AuthUserClaims claimsData =
                new JwtService.AuthUserClaims(
                        1L,
                        "   ",
                        "Nuno",
                        "USER",
                        "123456789",
                        "912345678",
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_USER")));

        String token = jwtService.generateToken(claimsData);

        Claims claims = jwtService.parseClaims(token);

        assertEquals("123456789", claims.getSubject());
        }

       @Test
        void generateToken_DeveConterClaimsImportantes() {

        JwtService.AuthUserClaims claimsData =
                new JwtService.AuthUserClaims(
                        10L,
                        "user@example.com",
                        "Nuno",
                        "ADMIN",
                        "123456789",
                        "912345678",
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_ADMIN")));

        String token =
                jwtService.generateToken(
                        claimsData);

        Claims claims =
                jwtService.parseClaims(token);

        assertEquals(
                10,
                ((Number) claims.get("userId"))
                        .intValue());

        assertEquals(
                "123456789",
                claims.get("nif"));

        assertEquals(
                "912345678",
                claims.get("telefone"));

        assertTrue(
                ((List<?>) claims.get("roles"))
                        .contains("ROLE_ADMIN"));
        }

        @Test
        void generateToken_DeveDefinirExpirationFutura() {

            JwtService.AuthUserClaims claimsData =
                    new JwtService.AuthUserClaims(
                            1L,
                            "user@example.com",
                            "Nuno",
                            "USER",
                            "123456789",
                            "912345678",
                            List.of(
                                    new SimpleGrantedAuthority(
                                            "ROLE_USER")));

            String token =
                    jwtService.generateToken(
                            claimsData);

            Claims claims =
                    jwtService.parseClaims(token);

            assertNotNull(
                    claims.getExpiration());

            assertTrue(
                    claims.getExpiration()
                            .getTime() >
                            System.currentTimeMillis());
        }
}