package pt.florinhas.notificacoes;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class NotificacoesApplicationTest {

    @Test
    void main_NaoDeveFalhar() {

        assertDoesNotThrow(() ->
                NotificacoesApplication.main(
                        new String[] {}));
    }
}