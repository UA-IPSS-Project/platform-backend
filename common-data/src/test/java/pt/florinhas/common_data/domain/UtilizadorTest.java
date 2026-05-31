package pt.florinhas.common_data.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;

import java.time.LocalDateTime;
import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import pt.florinhas.common_data.security.CryptoUtils;

class UtilizadorTest {

    @Test
    void getAuthorities_DeveRetornarRoleUtente() {

        Utilizador utilizador =
                new Utilizador();

        Collection<? extends GrantedAuthority> authorities =
                utilizador.getAuthorities();

        assertEquals(1, authorities.size());

        assertEquals(
                "ROLE_UTENTE",
                authorities.iterator()
                        .next()
                        .getAuthority());
    }

    @Test
    void getUsername_DeveRetornarEmail() {

        Utilizador utilizador =
                new Utilizador();

        utilizador.setEmail(
                "teste@teste.com");

        assertEquals(
                "teste@teste.com",
                utilizador.getUsername());
    }

    @Test
    void getPassword_DeveRetornarPassHash() {

        Utilizador utilizador =
                new Utilizador();

        utilizador.setPassHash("hash");

        assertEquals(
                "hash",
                utilizador.getPassword());
    }

    @Test
    void isEnabled_DeveRetornarTrue() {

        Utilizador utilizador =
                new Utilizador();

        assertTrue(
                utilizador.isEnabled());
    }

    @Test
    void onCreate_DeveDefinirCreatedAt() {

        Utilizador utilizador =
                new Utilizador();

        utilizador.setNif("123456789");

        assertThrows(
                IllegalStateException.class,
                utilizador::onCreate);
    }

    @Test
    void onUpdate_DeveFalharSemCryptoUtils() {

        Utilizador utilizador =
                new Utilizador();

        utilizador.setNif("123456789");

        assertThrows(
                IllegalStateException.class,
                utilizador::onUpdate);
    }

    @Test
    void deleteRequested_DeveTerDefaultFalse() {

        Utilizador utilizador =
                Utilizador.builder()
                        .build();

        assertEquals(
                false,
                utilizador.getDeleteRequested());
    }

    @Test
    void createdAt_DeveAceitarValor() {

        Utilizador utilizador =
                new Utilizador();

        LocalDateTime now =
                LocalDateTime.now();

        utilizador.setCreatedAt(now);

        assertEquals(
                now,
                utilizador.getCreatedAt());
    }

    @Test
        void onCreate_DeveDefinirCreatedAtENifHash() throws Exception {

        CryptoUtils cryptoUtils = new CryptoUtils();

        Field encryptionField =
                CryptoUtils.class.getDeclaredField("encryptionKeyHex");

        encryptionField.setAccessible(true);

        encryptionField.set(
                cryptoUtils,
                "00112233445566778899AABBCCDDEEFF");

        Field blindField =
                CryptoUtils.class.getDeclaredField("blindIndexKey");

        blindField.setAccessible(true);

        blindField.set(
                cryptoUtils,
                "blind-key");

        cryptoUtils.init();

        Utilizador.setCryptoUtils(cryptoUtils);

        Utilizador utilizador = new Utilizador();

        utilizador.setNif("123456789");

        utilizador.onCreate();

        assertNotNull(utilizador.getCreatedAt());

        assertNotNull(utilizador.getNifHash());
        }

        @Test
        void onUpdate_DeveAtualizarNifHash() throws Exception {

        CryptoUtils cryptoUtils = new CryptoUtils();

        Field encryptionField =
                CryptoUtils.class.getDeclaredField("encryptionKeyHex");

        encryptionField.setAccessible(true);

        encryptionField.set(
                cryptoUtils,
                "00112233445566778899AABBCCDDEEFF");

        Field blindField =
                CryptoUtils.class.getDeclaredField("blindIndexKey");

        blindField.setAccessible(true);

        blindField.set(
                cryptoUtils,
                "blind-key");

        cryptoUtils.init();
        Utilizador.setCryptoUtils(cryptoUtils);
        Utilizador utilizador = new Utilizador();
        utilizador.setNif("123456789");
        utilizador.onUpdate();
        String hash1 = utilizador.getNifHash();
        utilizador.setNif("987654321");
        utilizador.onUpdate();
        String hash2 = utilizador.getNifHash();
        assertNotEquals(hash1, hash2);
        }
        
}