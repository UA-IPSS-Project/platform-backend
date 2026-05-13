package pt.florinhas.common_data.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import pt.florinhas.common_data.security.CryptoUtils;

class UtilizadorTest {

    @BeforeEach
    void setUp() {

        CryptoUtils cryptoUtils =
                new CryptoUtils();

        Utilizador.setCryptoUtils(
                cryptoUtils
        );
    }

    @Test
    void getAuthorities_Utente() {

        Utilizador utilizador =
                new Utilizador();

        Collection<? extends GrantedAuthority>
                authorities =
                utilizador.getAuthorities();

        assertEquals(
                "ROLE_UTENTE",
                authorities.iterator()
                        .next()
                        .getAuthority()
        );
    }

    @Test
    void getUsername_DeveRetornarEmail() {

        Utilizador utilizador =
                new Utilizador();

        utilizador.setEmail(
                "teste@teste.com"
        );

        assertEquals(
                "teste@teste.com",
                utilizador.getUsername()
        );
    }

    @Test
    void isEnabled_DeveRetornarTrue() {

        Utilizador utilizador =
                new Utilizador();

        assertTrue(
                utilizador.isEnabled()
        );
    }

    @Test
        void onCreate_DeveDefinirCreatedAt()
                throws Exception {

        Utilizador utilizador =
                new Utilizador();

        Method method =
                Utilizador.class
                        .getDeclaredMethod(
                                "onCreate"
                        );

        method.setAccessible(true);

        method.invoke(utilizador);

        assertNotNull(
                utilizador.getCreatedAt()
        );
        }
}