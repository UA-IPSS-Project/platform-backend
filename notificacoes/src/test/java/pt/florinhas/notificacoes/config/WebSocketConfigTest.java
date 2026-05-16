package pt.florinhas.notificacoes.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

class WebSocketConfigTest {

    private JwtWebSocketInterceptor jwtInterceptor;

    private GatewayHandshakeInterceptor handshakeInterceptor;

    private WebSocketConfig config;

    @BeforeEach
    void setUp() {

        jwtInterceptor =
                org.mockito.Mockito.mock(
                        JwtWebSocketInterceptor.class);

        handshakeInterceptor =
                org.mockito.Mockito.mock(
                        GatewayHandshakeInterceptor.class);

        config =
                new WebSocketConfig(
                        jwtInterceptor,
                        handshakeInterceptor);
    }

    @Test
    void configureMessageBroker_DeveConfigurarBroker() {

        MessageBrokerRegistry registry =
                org.mockito.Mockito.mock(
                        MessageBrokerRegistry.class);

        config.configureMessageBroker(
                registry);

        assertNotNull(registry);
    }

    @Test
    void configureClientInboundChannel_DeveAdicionarInterceptor() {

        ChannelRegistration registration =
                org.mockito.Mockito.mock(
                        ChannelRegistration.class);

        config.configureClientInboundChannel(
                registration);

        verify(registration)
                .interceptors(jwtInterceptor);
    }

    @Test
    void registerStompEndpoints_DeveExecutar() {

        StompEndpointRegistry registry =
                org.mockito.Mockito.mock(
                        StompEndpointRegistry.class);

        assertNotNull(registry);
    }
}