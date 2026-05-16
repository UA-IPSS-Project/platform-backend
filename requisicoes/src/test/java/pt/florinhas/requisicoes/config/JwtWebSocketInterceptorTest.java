package pt.florinhas.requisicoes.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

class JwtWebSocketInterceptorTest {

    private JwtWebSocketInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new JwtWebSocketInterceptor();
    }

    @Test
    void preSend_DeveRetornarMensagemQuandoUserJaExiste() {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.create(StompCommand.SEND);

        Principal principal = mock(Principal.class);

        accessor.setUser(principal);

        Message<byte[]> message =
                MessageBuilder.createMessage(
                        new byte[0],
                        accessor.getMessageHeaders());

        Message<?> result =
                interceptor.preSend(
                        message,
                        mock(MessageChannel.class));

        assertNotNull(result);

        assertEquals(message, result);
    }

    @Test
    void preSend_DeveRetornarMensagemQuandoGatewayUserExiste() {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.create(StompCommand.CONNECT);

        Map<String, Object> sessionAttributes =
                new HashMap<>();

        sessionAttributes.put(
                "gatewayUser",
                "ana");

        sessionAttributes.put(
                "gatewayRoles",
                List.of("ADMIN"));

        accessor.setSessionAttributes(sessionAttributes);

        Message<byte[]> message =
                MessageBuilder.createMessage(
                        new byte[0],
                        accessor.getMessageHeaders());

        Message<?> result =
                interceptor.preSend(
                        message,
                        mock(MessageChannel.class));

        assertNotNull(result);

        assertEquals(message, result);
    }

    @Test
    void preSend_DeveRetornarNullQuandoConnectSemUser() {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.create(StompCommand.CONNECT);

        Message<byte[]> message =
                MessageBuilder.createMessage(
                        new byte[0],
                        accessor.getMessageHeaders());

        Message<?> result =
                interceptor.preSend(
                        message,
                        mock(MessageChannel.class));

        assertNull(result);
    }

    @Test
    void preSend_DeveRetornarMensagemQuandoNaoEConnect() {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.create(StompCommand.SEND);

        Message<byte[]> message =
                MessageBuilder.createMessage(
                        new byte[0],
                        accessor.getMessageHeaders());

        Message<?> result =
                interceptor.preSend(
                        message,
                        mock(MessageChannel.class));

        assertNotNull(result);
    }
}