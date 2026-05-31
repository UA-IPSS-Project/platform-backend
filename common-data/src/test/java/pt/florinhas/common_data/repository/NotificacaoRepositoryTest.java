package pt.florinhas.common_data.repository;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class NotificacaoRepositoryTest {

    @Test
    void repository_DeveExistir() {

        Class<NotificacaoRepository> clazz =
                NotificacaoRepository.class;

        assertNotNull(clazz);
    }
}