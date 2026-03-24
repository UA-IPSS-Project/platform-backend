package pt.florinhas.api_gateway.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Set secret and expiration via reflection for test
        TestUtils.setField(jwtService, "secret", "test-secret-key-12345678901234567890123456789012");
        TestUtils.setField(jwtService, "jwtExpiration", 3600000L);
    }

    @Test
    void generateAndParseToken_ShouldContainAllClaims() {
        JwtService.AuthUserClaims claims = new JwtService.AuthUserClaims(
                1L,
                "user@example.com",
                "Test User",
                "ADMIN",
                "123456789",
                "912345678",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        String token = jwtService.generateToken(claims);
        assertNotNull(token);

        Claims parsed = jwtService.parseClaims(token);
        assertEquals(1, parsed.get("userId", Integer.class));
        assertEquals("user@example.com", parsed.get("email"));
        assertEquals("Test User", parsed.get("nome"));
        assertEquals("ADMIN", parsed.get("role"));
        assertEquals("123456789", parsed.get("nif"));
        assertEquals("912345678", parsed.get("telefone"));
        assertTrue(((List<?>) parsed.get("roles")).contains("ROLE_ADMIN"));
    }

    @Test
    void tokenShouldExpireCorrectly() {
        JwtService.AuthUserClaims claims = new JwtService.AuthUserClaims(
                2L,
                "other@example.com",
                "Other User",
                "UTENTE",
                "987654321",
                "987654321",
                List.of(new SimpleGrantedAuthority("ROLE_UTENTE"))
        );
        String token = jwtService.generateToken(claims);
        Claims parsed = jwtService.parseClaims(token);
        assertNotNull(parsed.getExpiration());
        assertTrue(parsed.getExpiration().getTime() > System.currentTimeMillis());
    }
}
