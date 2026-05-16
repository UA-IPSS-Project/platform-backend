package pt.florinhas.notificacoes.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

class JwtWebSocketInterceptorTest {

    private WsJwtService wsJwtService;

    private JwtWebSocketInterceptor interceptor;

    @BeforeEach
    void setUp() {

        wsJwtService =
                org.mockito.Mockito.mock(
                        WsJwtService.class);

        interceptor =
                new JwtWebSocketInterceptor(
                        wsJwtService);
    }

    @Test
    void preSend_DeveAutenticarViaSessionAttributes() {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.create(
                        StompCommand.CONNECT);

        HashMap<String, Object> sessionAttributes =
                new HashMap<>();

        sessionAttributes.put(
                "gatewayUser",
                "teste");

        sessionAttributes.put(
                "gatewayRoles",
                List.of("ROLE_USER"));

        accessor.setSessionAttributes(
                sessionAttributes);

        accessor.setLeaveMutable(true);

        Message<byte[]> message =
                MessageBuilder.createMessage(
                        new byte[0],
                        accessor.getMessageHeaders());

        Message<?> result =
                interceptor.preSend(
                        message,
                        org.mockito.Mockito.mock(
                                MessageChannel.class));

        assertNotNull(result);
    }
}