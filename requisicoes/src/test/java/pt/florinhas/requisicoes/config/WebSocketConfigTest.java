package pt.florinhas.requisicoes.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

        config = new WebSocketConfig(
                jwtWebSocketInterceptor,
                gatewayHandshakeInterceptor);
    }
    @Test
    void configureMessageBroker_NaoDeveLancarExcecao() {

        MessageBrokerRegistry registry =
                mock(MessageBrokerRegistry.class);

        when(registry.enableSimpleBroker(
                "/topic",
                "/queue"))
                .thenReturn(null);

        assertDoesNotThrow(() ->
                config.configureMessageBroker(registry));
    }

    @Test
    void configureClientInboundChannel_DeveAdicionarInterceptor() {

        ChannelRegistration registration =
                mock(ChannelRegistration.class);

        when(registration.interceptors(any()))
                .thenReturn(registration);

        assertDoesNotThrow(() ->
                config.configureClientInboundChannel(registration));

        verify(registration)
                .interceptors(jwtWebSocketInterceptor);
    }

   @Test
    void registerStompEndpoints_DeveAdicionarEndpoint() {

        StompEndpointRegistry registry =
                mock(StompEndpointRegistry.class);

        StompWebSocketEndpointRegistration endpointRegistration =
                mock(StompWebSocketEndpointRegistration.class);

        when(registry.addEndpoint("/ws"))
                .thenReturn(endpointRegistration);

        when(endpointRegistration.addInterceptors(any()))
                .thenReturn(endpointRegistration);

        assertDoesNotThrow(() ->
                config.registerStompEndpoints(registry));

        verify(registry)
                .addEndpoint("/ws");

        verify(endpointRegistration)
                .addInterceptors(
                        gatewayHandshakeInterceptor);
    }
}