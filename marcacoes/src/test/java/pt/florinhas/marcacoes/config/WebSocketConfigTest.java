package pt.florinhas.marcacoes.config;

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
    private WebSocketConfig webSocketConfig;

    @BeforeEach
    void setUp() {
        jwtWebSocketInterceptor = mock(JwtWebSocketInterceptor.class);
        webSocketConfig = new WebSocketConfig(jwtWebSocketInterceptor);
    }

    @Test
    void configureMessageBroker_DeveConfigurarBrokerEPrefixos() {
        MessageBrokerRegistry registry = mock(MessageBrokerRegistry.class);

        webSocketConfig.configureMessageBroker(registry);

        verify(registry).enableSimpleBroker("/topic", "/queue");
        verify(registry).setApplicationDestinationPrefixes("/app");
        verify(registry).setUserDestinationPrefix("/user");
    }

    @Test
    void registerStompEndpoints_DeveRegistarEndpointWs() {
        StompEndpointRegistry registry = mock(StompEndpointRegistry.class);
        StompWebSocketEndpointRegistration endpointRegistration = mock(StompWebSocketEndpointRegistration.class);

        when(registry.addEndpoint("/ws")).thenReturn(endpointRegistration);
        when(endpointRegistration.setAllowedOriginPatterns("*")).thenReturn(endpointRegistration);

        webSocketConfig.registerStompEndpoints(registry);

        verify(registry).addEndpoint("/ws");
        verify(endpointRegistration).setAllowedOriginPatterns("*");
    }

    @Test
    void configureClientInboundChannel_DeveAdicionarInterceptor() {
        ChannelRegistration registration = mock(ChannelRegistration.class);

        webSocketConfig.configureClientInboundChannel(registration);

        verify(registration).interceptors(jwtWebSocketInterceptor);
    }
}