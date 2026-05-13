package pt.florinhas.common_data.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class NotificacaoRepositoryTest {

    @Test
    void interface_DeveExistir() {

        assertNotNull(
                NotificacaoRepository.class
        );

        assertTrue(
                NotificacaoRepository.class
                        .isInterface()
        );
    }
}