package pt.florinhas.common_data.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.Test;

class NotificacaoTest {

    @Test
    void construtorVazio_DeveCriarObjeto() {

        Notificacao notificacao =
                new Notificacao();

        assertNotNull(notificacao);
    }

    @Test
    void construtorCompleto_DeveDefinirCampos() {

        Utilizador utilizador =
                new Utilizador();

        LocalDateTime data =
                LocalDateTime.now();

        Map<String, Object> metadata =
                Map.of("id", 1);

        Notificacao notificacao =
                new Notificacao(
                        1L,
                        utilizador,
                        "Titulo",
                        "Mensagem",
                        "INFO",
                        true,
                        metadata,
                        data);

        assertEquals(
                1L,
                notificacao.getId());

        assertEquals(
                utilizador,
                notificacao.getUtilizador());

        assertEquals(
                "Titulo",
                notificacao.getTitulo());

        assertEquals(
                "Mensagem",
                notificacao.getMensagem());

        assertEquals(
                "INFO",
                notificacao.getTipo());

        assertEquals(
                metadata,
                notificacao.getMetadata());

        assertEquals(
                data,
                notificacao.getDataCriacao());
    }

    @Test
    void onCreate_DeveDefinirDataCriacao()
            throws Exception {

        Notificacao notificacao =
                new Notificacao();

        Method method =
                Notificacao.class.getDeclaredMethod(
                        "onCreate");

        method.setAccessible(true);

        method.invoke(notificacao);

        assertNotNull(
                notificacao.getDataCriacao());
    }

    @Test
    void lida_DeveSerFalsePorDefeito() {

        Notificacao notificacao =
                new Notificacao();

        assertFalse(
                notificacao.isLida());
    }
}