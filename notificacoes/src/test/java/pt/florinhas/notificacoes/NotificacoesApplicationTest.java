package pt.florinhas.notificacoes;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class NotificacoesApplicationTest {

    @Test
    void main_NaoDeveFalhar() {
        assertDoesNotThrow(() ->
                NotificacoesApplication.main(
                        new String[] {}));
    }
}