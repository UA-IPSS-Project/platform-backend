package pt.florinhas.requisicoes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.requisicoes.dto.InternalAuditRequest;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private HttpServletRequest request;

    @Captor
    private ArgumentCaptor<HttpEntity<InternalAuditRequest>> captor;

    private AuditService service;

    @BeforeEach
    void setUp() {
        service = new AuditService(
                restTemplate,
                "http://localhost",
                "secret-secret-key",
                request
        );
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Log sem autenticação ativa deve registrar como Sistema")
    void log_SemAutenticacao_DeveUsarSistema() {
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(restTemplate.postForEntity(anyString(), captor.capture(), eq(Void.class)))
                .thenReturn(ResponseEntity.ok().build());

        assertDoesNotThrow(() -> service.log("CREATE", "ITEM", 1L, "Item criado"));

        InternalAuditRequest payload = captor.getValue().getBody();
        assertNotNull(payload);
        assertNull(payload.userId());
        assertEquals("Sistema", payload.userName());
        assertEquals("CREATE", payload.action());
        assertEquals("ITEM", payload.entityType());
        assertEquals(1L, payload.entityId());
        assertEquals("Item criado", payload.details());
        assertEquals("127.0.0.1", payload.ipAddress());
    }

    @Test
    @DisplayName("Log com autenticação de Utilizador deve extrair ID e Nome do Utilizador")
    void log_ComAutenticacaoUtilizador_DeveUsarDadosUtilizador() {
        Utilizador user = new Utilizador();
        user.setId(99L);
        user.setNome("Maria Silva");

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("maria@test.com");
        when(auth.getPrincipal()).thenReturn(user);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(restTemplate.postForEntity(anyString(), captor.capture(), eq(Void.class)))
                .thenReturn(ResponseEntity.ok().build());

        assertDoesNotThrow(() -> service.log("DELETE", "VEICULO", 2L, "Apagado"));

        InternalAuditRequest payload = captor.getValue().getBody();
        assertNotNull(payload);
        assertEquals(99L, payload.userId());
        assertEquals("Maria Silva", payload.userName());
        assertEquals("127.0.0.1", payload.ipAddress());
    }

    @Test
    @DisplayName("Log com autenticação que não é Utilizador deve usar getName()")
    void log_ComAutenticacaoNaoUtilizador_DeveUsarNomePrincipal() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("admin-user-keycloak");
        when(auth.getPrincipal()).thenReturn("some-string-principal");
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(restTemplate.postForEntity(anyString(), captor.capture(), eq(Void.class)))
                .thenReturn(ResponseEntity.ok().build());

        assertDoesNotThrow(() -> service.log("UPDATE", "PERFIL", 3L, "Atualizado"));

        InternalAuditRequest payload = captor.getValue().getBody();
        assertNotNull(payload);
        assertNull(payload.userId());
        assertEquals("admin-user-keycloak", payload.userName());
    }

    @Test
    @DisplayName("Log com utilizador anónimo deve usar o nome padrão Sistema")
    void log_ComAutenticacaoAnonima_DeveUsarSistema() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("anonymousUser");
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(restTemplate.postForEntity(anyString(), captor.capture(), eq(Void.class)))
                .thenReturn(ResponseEntity.ok().build());

        assertDoesNotThrow(() -> service.log("READ", "VEICULO", 4L, "Lido"));

        InternalAuditRequest payload = captor.getValue().getBody();
        assertNotNull(payload);
        assertNull(payload.userId());
        assertEquals("Sistema", payload.userName());
    }

    @Test
    @DisplayName("Quando restTemplate falha, a exceção não deve ser propagada")
    void log_QuandoRestTemplateFalha_NaoDevePropagarErro() {
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(restTemplate.postForEntity(anyString(), any(), eq(Void.class)))
                .thenThrow(new RuntimeException("Conexão recusada"));

        assertDoesNotThrow(() -> service.log("CREATE", "ITEM", 1L, "Detalhes"));
    }

    @Test
    @DisplayName("getClientIp com X-Forwarded-For contendo múltiplos IPs deve retornar o primeiro IP limpo")
    void getClientIp_XForwardedForMultiplosIps_DeveRetornarPrimeiro() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("  192.168.1.100, 10.0.0.1 ");
        when(restTemplate.postForEntity(anyString(), captor.capture(), eq(Void.class)))
                .thenReturn(ResponseEntity.ok().build());

        assertDoesNotThrow(() -> service.log("CREATE", "ITEM", 1L, "Detalhes"));

        InternalAuditRequest payload = captor.getValue().getBody();
        assertNotNull(payload);
        assertEquals("192.168.1.100", payload.ipAddress());
    }

    @Test
    @DisplayName("getClientIp com X-Forwarded-For vazio e X-Real-IP preenchido deve usar X-Real-IP")
    void getClientIp_XRealIp_DeveRetornarRealIp() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn("192.168.1.200");
        when(restTemplate.postForEntity(anyString(), captor.capture(), eq(Void.class)))
                .thenReturn(ResponseEntity.ok().build());

        assertDoesNotThrow(() -> service.log("CREATE", "ITEM", 1L, "Detalhes"));

        InternalAuditRequest payload = captor.getValue().getBody();
        assertNotNull(payload);
        assertEquals("192.168.1.200", payload.ipAddress());
    }

    @Test
    @DisplayName("getClientIp sem cabeçalhos adicionais deve usar getRemoteAddr()")
    void getClientIp_RemoteAddr_DeveRetornarRemoteAddr() {
        when(request.getHeader(anyString())).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.250");
        when(restTemplate.postForEntity(anyString(), captor.capture(), eq(Void.class)))
                .thenReturn(ResponseEntity.ok().build());

        assertDoesNotThrow(() -> service.log("CREATE", "ITEM", 1L, "Detalhes"));

        InternalAuditRequest payload = captor.getValue().getBody();
        assertNotNull(payload);
        assertEquals("192.168.1.250", payload.ipAddress());
    }

    @Test
    @DisplayName("getClientIp com HttpServletRequest nulo deve retornar 'unknown'")
    void getClientIp_RequestNulo_DeveRetornarUnknown() {
        AuditService serviceSemRequest = new AuditService(
                restTemplate,
                "http://localhost",
                "secret-secret-key",
                null
        );

        when(restTemplate.postForEntity(anyString(), captor.capture(), eq(Void.class)))
                .thenReturn(ResponseEntity.ok().build());

        assertDoesNotThrow(() -> serviceSemRequest.log("CREATE", "ITEM", 1L, "Detalhes"));

        InternalAuditRequest payload = captor.getValue().getBody();
        assertNotNull(payload);
        assertEquals("unknown", payload.ipAddress());
    }
}