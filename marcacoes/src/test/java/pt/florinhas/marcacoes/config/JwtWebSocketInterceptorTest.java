package pt.florinhas.marcacoes.config;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

import io.jsonwebtoken.Claims;
import pt.florinhas.marcacoes.security.JwtService;

@ExtendWith(MockitoExtension.class)
class JwtWebSocketInterceptorTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private MessageChannel messageChannel;

    private JwtWebSocketInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new JwtWebSocketInterceptor(jwtService);
    }

    @Test
    void preSend_DeveChamarParseClaimsQuandoTokenValidoComBearer() {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("nuno");
        when(jwtService.parseClaims("token-valido")).thenReturn(claims);

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("Authorization", "Bearer token-valido");
        accessor.setLeaveMutable(true);

        Message<byte[]> message =
                MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> result = interceptor.preSend(message, messageChannel);

        verify(jwtService).parseClaims("token-valido");
        assertSame(message, result);
    }

    @Test
    void preSend_DeveChamarParseClaimsQuandoTokenValidoSemBearer() {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("nuno");
        when(jwtService.parseClaims("token-direto")).thenReturn(claims);

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("Authorization", "token-direto");
        accessor.setLeaveMutable(true);

        Message<byte[]> message =
                MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> result = interceptor.preSend(message, messageChannel);

        verify(jwtService).parseClaims("token-direto");
        assertSame(message, result);
    }

    @Test
    void preSend_NaoDeveChamarParseClaimsQuandoNaoExisteAuthorization() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setLeaveMutable(true);

        Message<byte[]> message =
                MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> result = interceptor.preSend(message, messageChannel);

        verify(jwtService, never()).parseClaims(org.mockito.ArgumentMatchers.anyString());
        assertSame(message, result);
    }

    @Test
    void preSend_NaoDeveChamarParseClaimsQuandoUserJaExiste() {
        Claims existingClaims = mock(Claims.class);

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setUser(new WebSocketUserPrincipal("ja-autenticado", existingClaims));
        accessor.addNativeHeader("Authorization", "Bearer token-valido");
        accessor.setLeaveMutable(true);

        Message<byte[]> message =
                MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> result = interceptor.preSend(message, messageChannel);

        verify(jwtService, never()).parseClaims(org.mockito.ArgumentMatchers.anyString());
        assertSame(message, result);
    }

    @Test
    void preSend_DeveIgnorarExcecaoQuandoTokenInvalido() {
        when(jwtService.parseClaims("token-invalido"))
                .thenThrow(new RuntimeException("jwt inválido"));

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("Authorization", "Bearer token-invalido");
        accessor.setLeaveMutable(true);

        Message<byte[]> message =
                MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertDoesNotThrow(() -> interceptor.preSend(message, messageChannel));

        verify(jwtService).parseClaims("token-invalido");
    }

    @Test
    void preSend_DeveUsarApenasPrimeiroHeaderAuthorization() {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("nuno");
        when(jwtService.parseClaims("primeiro-token")).thenReturn(claims);

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("Authorization", "Bearer primeiro-token");
        accessor.addNativeHeader("Authorization", "Bearer segundo-token");
        accessor.setLeaveMutable(true);

        Message<byte[]> message =
                MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> result = interceptor.preSend(message, messageChannel);

        verify(jwtService).parseClaims("primeiro-token");
        assertSame(message, result);
    }
}