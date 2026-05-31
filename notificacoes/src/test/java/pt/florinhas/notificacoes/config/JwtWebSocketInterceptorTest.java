package pt.florinhas.notificacoes.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

class JwtWebSocketInterceptorTest {

    private WsJwtService wsJwtService;
    private JwtWebSocketInterceptor interceptor;

    @BeforeEach
    void setUp() {

        wsJwtService =
                mock(WsJwtService.class);

        interceptor =
                new JwtWebSocketInterceptor(
                        wsJwtService);
    }

    @Test
    void preSend_DeveIgnorarQuandoAccessorNull() {

        Message<String> message =
                MessageBuilder.withPayload("x")
                        .build();

        Message<?> result =
                interceptor.preSend(
                        message,
                        mock(MessageChannel.class));

        assertNotNull(result);
    }

    @Test
    void preSend_DeveUsarUserSessao() {

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

        Principal user =
                StompHeaderAccessor
                        .wrap(result)
                        .getUser();

        assertNotNull(user);

        assertEquals(
                "nuno@test.com",
                user.getName());
    }

    @Test
    void preSend_DeveUsarJwtAuthorization() {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.create(
                        StompCommand.CONNECT);

        accessor.addNativeHeader(
                "Authorization",
                "Bearer token");

        Claims claims =
            Jwts.claims()
                    .subject("nuno@test.com")
                    .add("roles", List.of("ADMIN"))
                    .build();

        when(wsJwtService.parseClaims("token"))
                .thenReturn(claims);

        accessor.setLeaveMutable(true);

        Message<byte[]> message =
                MessageBuilder.createMessage(
                        new byte[0],
                        accessor.getMessageHeaders());

        Message<?> result =
                interceptor.preSend(
                        message,
                        mock(MessageChannel.class));

        Principal user =
                StompHeaderAccessor
                        .wrap(result)
                        .getUser();

        assertNotNull(user);

        assertEquals(
                "nuno@test.com",
                user.getName());
    }

    @Test
    void preSend_DeveUsarJwtSessao() {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.create(
                        StompCommand.CONNECT);

        Map<String, Object> session =
                new HashMap<>();

        session.put(
                "jwt-token",
                "token");

        accessor.setSessionAttributes(session);

        Claims claims =
                Jwts.claims()
                        .subject("nuno@test.com")
                        .build();

        when(wsJwtService.parseClaims("token"))
                .thenReturn(claims);

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
    void preSend_DeveRetornarNullQuandoJwtInvalido() {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.create(
                        StompCommand.CONNECT);

        accessor.addNativeHeader(
                "Authorization",
                "Bearer token");

        when(wsJwtService.parseClaims("token"))
                .thenThrow(new RuntimeException());

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
    void preSend_DeveRetornarNullSemAuth() {

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
}