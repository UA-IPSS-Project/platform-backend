package pt.florinhas.requisicoes.controller;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import pt.florinhas.requisicoes.domain.Requisicao;
import pt.florinhas.requisicoes.domain.RequisicaoEstado;
import pt.florinhas.requisicoes.domain.RequisicaoManutencao;
import pt.florinhas.requisicoes.domain.RequisicaoMaterial;
import pt.florinhas.requisicoes.domain.RequisicaoPrioridade;
import pt.florinhas.requisicoes.domain.RequisicaoTipo;
import pt.florinhas.requisicoes.domain.RequisicaoTransporte;
import pt.florinhas.requisicoes.dto.CriarRequisicaoManutencaoRequest;
import pt.florinhas.requisicoes.dto.CriarRequisicaoMaterialRequest;
import pt.florinhas.requisicoes.dto.CriarRequisicaoTransporteRequest;
import pt.florinhas.requisicoes.service.RequisicaoService;

@ExtendWith(MockitoExtension.class)
class RequisicaoControllerTest {

    @Mock
    private RequisicaoService requisicaoService;

    @InjectMocks
    private RequisicaoController requisicaoController;

    @Test
    void listar_semEstado_deveUsarListarTodas() {
        List<Requisicao> esperado = List.of(new RequisicaoManutencao());
        when(requisicaoService.listarTodas()).thenReturn(esperado);

        List<Requisicao> resultado = requisicaoController.listar(null);

        assertSame(esperado, resultado);
        verify(requisicaoService).listarTodas();
    }

    @Test
    void listar_comEstado_deveUsarListarPorEstado() {
        List<Requisicao> esperado = List.of(new RequisicaoManutencao());
        when(requisicaoService.listarPorEstado(RequisicaoEstado.EM_PROGRESSO)).thenReturn(esperado);

        List<Requisicao> resultado = requisicaoController.listar(RequisicaoEstado.EM_PROGRESSO);

        assertSame(esperado, resultado);
        verify(requisicaoService).listarPorEstado(RequisicaoEstado.EM_PROGRESSO);
    }

    @Test
    void obter_deveDelegarNoService() {
        Requisicao esperado = new RequisicaoManutencao();
        when(requisicaoService.obterPorId(5L)).thenReturn(esperado);

        Requisicao resultado = requisicaoController.obter(5L);

        assertSame(esperado, resultado);
        verify(requisicaoService).obterPorId(5L);
    }

    @Test
    void procurar_deveDelegarNoServiceComTodosOsParametros() {
        List<Requisicao> esperado = List.of(new RequisicaoManutencao());
        when(requisicaoService.procurar(
                RequisicaoEstado.ABERTO,
                RequisicaoTipo.MANUTENCAO,
                RequisicaoPrioridade.ALTA,
                "Maria",
                null,
                null)).thenReturn(esperado);

        List<Requisicao> resultado = requisicaoController.procurar(
                RequisicaoEstado.ABERTO,
                RequisicaoTipo.MANUTENCAO,
                RequisicaoPrioridade.ALTA,
                "Maria",
                null,
                null);

        assertSame(esperado, resultado);
        verify(requisicaoService).procurar(
                RequisicaoEstado.ABERTO,
                RequisicaoTipo.MANUTENCAO,
                RequisicaoPrioridade.ALTA,
                "Maria",
                null,
                null);
    }

    @Test
    void criarMaterial_deveRetornarResponseOkComBody() {
        CriarRequisicaoMaterialRequest request = new CriarRequisicaoMaterialRequest(
                "material",
                RequisicaoPrioridade.MEDIA,
                1L,
                null,
            List.of(new CriarRequisicaoMaterialRequest.ItemMaterialRequest(2L, 3)));
        Requisicao resposta = new RequisicaoMaterial();
        when(requisicaoService.criarMaterial(request)).thenReturn((RequisicaoMaterial) resposta);

        ResponseEntity<Requisicao> responseEntity = requisicaoController.criarMaterial(request);

        assertEquals(200, responseEntity.getStatusCode().value());
        assertSame(resposta, responseEntity.getBody());
    }

    @Test
    void criarTransporte_deveRetornarResponseOkComBody() {
        CriarRequisicaoTransporteRequest request = new CriarRequisicaoTransporteRequest(
                "transporte",
                RequisicaoPrioridade.BAIXA,
                1L,
                null,
            "Porto",
            LocalDateTime.of(2026, 3, 21, 9, 0),
            LocalDateTime.of(2026, 3, 21, 12, 0),
            4,
            "Motorista",
            List.of(2L),
            null);
        Requisicao resposta = new RequisicaoTransporte();
        when(requisicaoService.criarTransporte(request)).thenReturn((RequisicaoTransporte) resposta);

        ResponseEntity<Requisicao> responseEntity = requisicaoController.criarTransporte(request);

        assertEquals(200, responseEntity.getStatusCode().value());
        assertSame(resposta, responseEntity.getBody());
    }

    @Test
    void criarManutencao_deveRetornarResponseOkComBody() {
        CriarRequisicaoManutencaoRequest request = new CriarRequisicaoManutencaoRequest(
                "manutencao",
                RequisicaoPrioridade.URGENTE,
                1L,
                2L,
            "porta",
            null);
        Requisicao resposta = new RequisicaoManutencao();
        when(requisicaoService.criarManutencao(request)).thenReturn((RequisicaoManutencao) resposta);

        ResponseEntity<Requisicao> responseEntity = requisicaoController.criarManutencao(request);

        assertEquals(200, responseEntity.getStatusCode().value());
        assertSame(resposta, responseEntity.getBody());
    }
}