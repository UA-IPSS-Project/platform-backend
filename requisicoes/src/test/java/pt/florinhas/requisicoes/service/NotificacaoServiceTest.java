package pt.florinhas.requisicoes.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NotificacaoServiceTest {

    private NotificacaoService service;

    @BeforeEach
    void setUp() {
        service = new NotificacaoService();
    }

    @Test
    void service_DeveSerInstanciado() {
        assertNotNull(service);
    }
}