package pt.florinhas.api_gateway.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import pt.florinhas.api_gateway.security.JwtAuthenticationFilter;

class SecurityConfigTest {

    private final JwtAuthenticationFilter jwtAuthenticationFilter =
            org.mockito.Mockito.mock(JwtAuthenticationFilter.class);

    private final UserDetailsService userDetailsService =
            org.mockito.Mockito.mock(UserDetailsService.class);

    private final SecurityConfig config =
            new SecurityConfig(
                    jwtAuthenticationFilter,
                    userDetailsService);

    @Test
    void passwordEncoder_DeveCriarBCryptPasswordEncoder() {
        PasswordEncoder encoder = config.passwordEncoder();

        assertInstanceOf(
                BCryptPasswordEncoder.class,
                encoder);
    }

    @Test
    void authenticationProvider_DeveCriarDaoAuthenticationProvider() {
        AuthenticationProvider provider =
                config.authenticationProvider();

        assertInstanceOf(
                DaoAuthenticationProvider.class,
                provider);
    }

    @Test
    void authenticationManager_DeveCriarProviderManager() {
        AuthenticationProvider provider =
                config.authenticationProvider();

        AuthenticationManager manager =
                config.authenticationManager(provider);

        assertInstanceOf(
                ProviderManager.class,
                manager);
    }
}