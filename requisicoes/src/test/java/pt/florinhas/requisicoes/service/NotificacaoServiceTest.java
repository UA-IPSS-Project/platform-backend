package pt.florinhas.requisicoes.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import pt.florinhas.common_data.domain.Funcionario;
import pt.florinhas.requisicoes.domain.RequisicaoEstado;
import pt.florinhas.requisicoes.domain.RequisicaoMaterial;
import pt.florinhas.requisicoes.domain.RequisicaoPrioridade;
import pt.florinhas.requisicoes.domain.RequisicaoTipo;

class NotificacaoServiceTest {

    private NotificacaoService service;

    @BeforeEach
    void setUp() throws Exception {

        service =
                new NotificacaoService();

        setField(
                "notificacoesUrl",
                "http://localhost");

        setField(
                "gatewaySecret",
                "secret");
    }

    @Test
    void notificarNovaRequisicao_NaoDeveFalhar() {

        RequisicaoMaterial requisicao =
                criarRequisicao();

        assertDoesNotThrow(() ->
                service.notificarNovaRequisicao(
                        1L,
                        requisicao));
    }

    @Test
    void notificarMudancaEstado_NaoDeveFalhar() {

        RequisicaoMaterial requisicao =
                criarRequisicao();

        requisicao.setEstado(
                RequisicaoEstado.EM_PROGRESSO);

        assertDoesNotThrow(() ->
                service.notificarMudancaEstado(
                        1L,
                        requisicao));
    }

    private RequisicaoMaterial criarRequisicao() {

        Funcionario funcionario =
                new Funcionario();

        funcionario.setId(1L);
        funcionario.setNome("Nuno");

        RequisicaoMaterial requisicao =
                new RequisicaoMaterial();

        requisicao.setId(1L);
        requisicao.setCriadoPor(funcionario);
        requisicao.setPrioridade(
                RequisicaoPrioridade.ALTA);
        requisicao.setTipo(
                RequisicaoTipo.MATERIAL);

        return requisicao;
    }

    private void setField(
            String field,
            Object value)
            throws Exception {

        Field f =
                NotificacaoService.class
                        .getDeclaredField(field);

        f.setAccessible(true);

        f.set(service, value);
    }
}