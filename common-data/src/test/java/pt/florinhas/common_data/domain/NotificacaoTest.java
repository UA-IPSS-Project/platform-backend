package pt.florinhas.common_data.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

class NotificacaoTest {

    @Test
    void onCreate_DeveDefinirDataCriacao()
            throws Exception {

        Notificacao notificacao =
                new Notificacao();

        Method method =
                Notificacao.class
                        .getDeclaredMethod("onCreate");

        method.setAccessible(true);

        method.invoke(notificacao);

        assertNotNull(
                notificacao.getDataCriacao()
        );
    }

    @Test
    void gettersAndSetters_DeveFuncionar() {

        Notificacao notificacao =
                new Notificacao();

        notificacao.setTitulo("Titulo");
        notificacao.setMensagem("Mensagem");
        notificacao.setTipo("INFO");
        notificacao.setLida(true);

        assertEquals(
                "Titulo",
                notificacao.getTitulo()
        );

        assertEquals(
                "Mensagem",
                notificacao.getMensagem()
        );

        assertEquals(
                "INFO",
                notificacao.getTipo()
        );

        assertTrue(
                notificacao.isLida()
        );
    }
}