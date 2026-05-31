package pt.florinhas.notificacoes.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;

class WebSocketConfigTest {

    private JwtWebSocketInterceptor jwtWebSocketInterceptor;
    private GatewayHandshakeInterceptor gatewayHandshakeInterceptor;

    private WebSocketConfig config;

    @BeforeEach
    void setUp() {

        jwtWebSocketInterceptor =
                mock(JwtWebSocketInterceptor.class);

        gatewayHandshakeInterceptor =
                mock(GatewayHandshakeInterceptor.class);

        config =
                new WebSocketConfig(
                        jwtWebSocketInterceptor,
                        gatewayHandshakeInterceptor);
    }

    @Test
    void configureMessageBroker_DeveConfigurar() {

        MessageBrokerRegistry registry =
                mock(MessageBrokerRegistry.class);

        assertDoesNotThrow(() ->
                config.configureMessageBroker(
                        registry));
    }

    @Test
    void configureClientInboundChannel_DeveAdicionarInterceptor() {

        ChannelRegistration registration =
                mock(ChannelRegistration.class);

        config.configureClientInboundChannel(
                registration);

        verify(registration)
                .interceptors(
                        jwtWebSocketInterceptor);
    }

    @Test
    void registerStompEndpoints_DeveAdicionarEndpoint() {

        StompEndpointRegistry registry =
                mock(StompEndpointRegistry.class);

        StompWebSocketEndpointRegistration registration =
                mock(StompWebSocketEndpointRegistration.class);

        when(registry.addEndpoint("/ws"))
                .thenReturn(registration);

        when(registration.setAllowedOriginPatterns("*"))
                .thenReturn(registration);

        config.registerStompEndpoints(registry);

        verify(registration)
                .addInterceptors(
                        gatewayHandshakeInterceptor);
    }
}