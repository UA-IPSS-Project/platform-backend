package pt.florinhas.requisicoes.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

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

        interceptor =
                new JwtWebSocketInterceptor();
    }

    @Test
    void preSend_DeveUsarGatewayUser() {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.create(
                        StompCommand.CONNECT);

        Map<String, Object> session =
                new HashMap<>();

        session.put(
                "gatewayUser",
                "nuno@test.com");

        session.put(
                "gatewayRoles",
                List.of("ADMIN"));

        accessor.setSessionAttributes(session);

        accessor.setLeaveMutable(true);

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

    @Test
    void preSend_DeveRetornarNullNoConnectSemUser() {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.create(
                        StompCommand.CONNECT);

        accessor.setLeaveMutable(true);

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
    void preSend_DeveAceitarMensagemNormal() {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.create(
                        StompCommand.SEND);

        accessor.setLeaveMutable(true);

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