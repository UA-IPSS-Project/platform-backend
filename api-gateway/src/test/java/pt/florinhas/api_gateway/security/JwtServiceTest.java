package pt.florinhas.api_gateway.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import io.jsonwebtoken.Claims;

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
}