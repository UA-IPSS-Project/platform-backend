package pt.florinhas.marcacoes.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

class GatewayHeaderAuthenticationFilterTest {

    private CustomUserDetailsService userDetailsService;

    private GatewayHeaderAuthenticationFilter filter;

    @BeforeEach
    void setUp() {

        userDetailsService = mock(CustomUserDetailsService.class);

        filter = new GatewayHeaderAuthenticationFilter(userDetailsService, "secret");

        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_DeveIgnorarSemHeader() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(200, response.getStatus());
    }

    @Test
    void doFilterInternal_DeveBloquearSecretInvalido() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();

        request.addHeader("X-Authenticated-User", "teste");
        request.addHeader("X-Gateway-Secret", "wrong");

        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(401, response.getStatus());
    }

    @Test
    void doFilterInternal_DeveAutenticar() throws Exception {

        User user = new User("teste", "123", java.util.List.of());

        when(userDetailsService.loadUserByUsername("teste"))
                .thenReturn(user);

        MockHttpServletRequest request = new MockHttpServletRequest();

        request.addHeader("X-Authenticated-User", "teste");
        request.addHeader("X-Gateway-Secret", "secret");
        request.addHeader("X-Authenticated-Roles", "ROLE_SECRETARIA");

        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals( "teste", SecurityContextHolder.getContext().getAuthentication().getName());
    }
}