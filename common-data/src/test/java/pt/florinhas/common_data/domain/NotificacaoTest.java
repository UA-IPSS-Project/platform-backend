package pt.florinhas.common_data.domain;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class NotificacaoTest {

    @Test
    void onCreate_DeveDefinirDataCriacao() {

        Notificacao notificacao =
                new Notificacao();

        notificacao.onCreate();

        LocalDateTime data =
                notificacao.getDataCriacao();

        assertNotNull(data);
    }
}