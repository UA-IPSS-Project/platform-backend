package pt.florinhas.requisicoes.config;

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

    private WebSocketConfig config;

    private JwtWebSocketInterceptor jwtInterceptor;
    private GatewayHandshakeInterceptor gatewayInterceptor;

    @BeforeEach
    void setUp() {

        jwtInterceptor =
                mock(JwtWebSocketInterceptor.class);

        gatewayInterceptor =
                mock(GatewayHandshakeInterceptor.class);

        config =
                new WebSocketConfig(
                        jwtInterceptor,
                        gatewayInterceptor);
    }

    @Test
    void configureMessageBroker_NaoDeveFalhar() {

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
                .interceptors(jwtInterceptor);
    }

    @Test
    void registerStompEndpoints_DeveAdicionarEndpoint() {

        StompEndpointRegistry registry =
                mock(StompEndpointRegistry.class);

        StompWebSocketEndpointRegistration endpoint =
                mock(StompWebSocketEndpointRegistration.class);

        when(registry.addEndpoint("/ws"))
                .thenReturn(endpoint);

        when(endpoint.addInterceptors(gatewayInterceptor))
                .thenReturn(endpoint);

        config.registerStompEndpoints(registry);

        verify(endpoint)
                .addInterceptors(gatewayInterceptor);
    }
}