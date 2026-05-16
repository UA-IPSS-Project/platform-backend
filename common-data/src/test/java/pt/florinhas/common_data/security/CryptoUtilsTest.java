package pt.florinhas.common_data.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pt.florinhas.common_data.TestUtils;
import pt.florinhas.common_data.exception.CryptoException;

class CryptoUtilsTest {

    private CryptoUtils cryptoUtils;

    @BeforeEach
    void setUp() {
        cryptoUtils = new CryptoUtils();
        TestUtils.setField(
                cryptoUtils,
                "encryptionKeyHex",
                "0123456789ABCDEF0123456789ABCDEF");
        TestUtils.setField(
                cryptoUtils,
                "blindIndexKey",
                "blind-key");
        cryptoUtils.init();
    }

    @Test
    @DisplayName("Criptografia com sucesso")
    void encryptDecrypt_DeveFuncionar() {
        String encrypted = cryptoUtils.encrypt("123456789");
        assertNotEquals("123456789", encrypted);

        String decrypted = cryptoUtils.decrypt(encrypted);
        assertEquals("123456789", decrypted);
    }

    @Test
    @DisplayName("Cifrar valor nulo retorna nulo")
    void encrypt_DeveAceitarNull() {
        assertNull(cryptoUtils.encrypt(null));
    }

    @Test
    @DisplayName("Decifrar valor nulo retorna nulo")
    void decrypt_DeveAceitarNull() {
        assertNull(cryptoUtils.decrypt(null));
    }

    @Test
    @DisplayName("Gera blind index para NIF válido")
    void generateBlindIndex_DeveGerarHash() {
        String hash = cryptoUtils.generateBlindIndex("123456789");
        assertNotNull(hash);

        // NIFs diferentes devem gerar hashes diferentes
        String otherHash = cryptoUtils.generateBlindIndex("987654321");
        assertNotEquals(hash, otherHash);
    }

    @Test
    @DisplayName("Blind index com valor nulo retorna nulo")
    void generateBlindIndex_DeveRetornarNull_QuandoValorNulo() {
        assertNull(cryptoUtils.generateBlindIndex(null));
    }

    @Test
    @DisplayName("Blind index com valor que normaliza para nulo retorna nulo")
    void generateBlindIndex_DeveRetornarNull_QuandoNormalizaParaNulo() {
        // "abc" não contém dígitos e por isso normaliza para null em NifValidator
        assertNull(cryptoUtils.generateBlindIndex("abc"));
    }

    @Test
    @DisplayName("Decifrar texto inválido sem delimitador deve lançar exceção")
    void decrypt_DeveLancarExcecaoFormatoInvalido() {
        assertThrows(CryptoException.class, () -> cryptoUtils.decrypt("invalido"));
    }

    @Test
    @DisplayName("Decifrar formato com delimitador incorreto deve lançar CryptoException")
    void decrypt_DelimitadorIncorreto_DeveLancarCryptoException() {
        CryptoException exception = assertThrows(CryptoException.class, () -> cryptoUtils.decrypt("invalido:formato"));
        assertTrue(exception.getMessage().contains("Erro ao decifrar dados"));
    }

    @Test
    @DisplayName("Inicialização sem chave de criptografia deve lançar exceção")
    void init_DeveLancarExcecaoQuandoChaveNulaOuVazia() {
        CryptoUtils utils = new CryptoUtils();
        TestUtils.setField(utils, "blindIndexKey", "blind");

        TestUtils.setField(utils, "encryptionKeyHex", null);
        assertThrows(IllegalStateException.class, utils::init);

        TestUtils.setField(utils, "encryptionKeyHex", "   ");
        assertThrows(IllegalStateException.class, utils::init);
    }

    @Test
    @DisplayName("Inicialização sem blind index deve lançar exceção")
    void init_DeveLancarExcecaoQuandoBlindKeyNullOuVazia() {
        CryptoUtils utils = new CryptoUtils();
        TestUtils.setField(utils, "encryptionKeyHex", "0123456789ABCDEF0123456789ABCDEF");

        TestUtils.setField(utils, "blindIndexKey", null);
        assertThrows(IllegalStateException.class, utils::init);

        TestUtils.setField(utils, "blindIndexKey", "   ");
        assertThrows(IllegalStateException.class, utils::init);
    }

    @Test
    @DisplayName("Inicialização com chave de tamanho ímpar deve lançar exceção")
    void init_DeveLancarExcecaoQuandoComprimentoImpar() {
        CryptoUtils utils = new CryptoUtils();
        TestUtils.setField(utils, "encryptionKeyHex", "12345"); // comprimento ímpar
        TestUtils.setField(utils, "blindIndexKey", "blind");

        IllegalStateException ex = assertThrows(IllegalStateException.class, utils::init);
        assertTrue(ex.getMessage().contains("hex inválido (comprimento ímpar)"));
    }

    @Test
    @DisplayName("Inicialização com chave contendo caracteres não-hex deve lançar exceção")
    void init_DeveLancarExcecaoQuandoNaoHex() {
        CryptoUtils utils = new CryptoUtils();
        TestUtils.setField(utils, "encryptionKeyHex", "0123456789ABCDEZ0123456789ABCDEZ"); // 'Z' não é hex
        TestUtils.setField(utils, "blindIndexKey", "blind");

        IllegalStateException ex = assertThrows(IllegalStateException.class, utils::init);
        assertTrue(ex.getMessage().contains("contém caracteres não-hex"));
    }

    @Test
    @DisplayName("Inicialização com chave de tamanho inválido (nem 16, 24 ou 32 bytes) deve lançar exceção")
    void init_DeveLancarExcecaoQuandoTamanhoNaoAES() {
        CryptoUtils utils = new CryptoUtils();
        TestUtils.setField(utils, "encryptionKeyHex", "0123456789ABCDEF"); // 8 bytes
        TestUtils.setField(utils, "blindIndexKey", "blind");

        IllegalStateException ex = assertThrows(IllegalStateException.class, utils::init);
        assertTrue(ex.getMessage().contains("deve ser hex de 32, 48 ou 64 chars"));
    }

    @Test
    @DisplayName("Inicialização com chaves válidas de 16, 24 e 32 bytes")
    void init_DeveAceitarTamanhosValidosAES() {
        // 16 bytes (32 caracteres hex)
        CryptoUtils utils16 = new CryptoUtils();
        TestUtils.setField(utils16, "encryptionKeyHex", "0123456789ABCDEF0123456789ABCDEF");
        TestUtils.setField(utils16, "blindIndexKey", "blind");
        assertDoesNotThrow(utils16::init);

        // 24 bytes (48 caracteres hex)
        CryptoUtils utils24 = new CryptoUtils();
        TestUtils.setField(utils24, "encryptionKeyHex", "0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF");
        TestUtils.setField(utils24, "blindIndexKey", "blind");
        assertDoesNotThrow(utils24::init);

        // 32 bytes (64 caracteres hex)
        CryptoUtils utils32 = new CryptoUtils();
        TestUtils.setField(utils32, "encryptionKeyHex", "0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF");
        TestUtils.setField(utils32, "blindIndexKey", "blind");
        assertDoesNotThrow(utils32::init);
    }
}