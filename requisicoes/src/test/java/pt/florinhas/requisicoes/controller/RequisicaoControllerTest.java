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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

import pt.florinhas.common_data.domain.Utilizador;

@ExtendWith(MockitoExtension.class)
class RequisicaoControllerTest {

    @Mock
    private RequisicaoService requisicaoService;

    @InjectMocks
    private RequisicaoController requisicaoController;

    @Test
    void listar_semEstado_deveRetornarPaginaVazia() {
        Page<Requisicao> esperado = new PageImpl<>(List.of());
        when(requisicaoService.procurarPaginated(null, null, null, null, null, null, Pageable.unpaged()))
                .thenReturn(esperado);

        Page<Requisicao> resultado = requisicaoController.listar(null, Pageable.unpaged());

        assertSame(esperado, resultado);
        verify(requisicaoService).procurarPaginated(null, null, null, null, null, null, Pageable.unpaged());
    }

    @Test
    void listar_comEstado_deveRetornarPaginaFiltrada() {
        Page<Requisicao> esperado = new PageImpl<>(List.of(new RequisicaoManutencao()));
        when(requisicaoService.procurarPaginated(RequisicaoEstado.EM_PROGRESSO, null, null, null, null, null, Pageable.unpaged()))
                .thenReturn(esperado);

        Page<Requisicao> resultado = requisicaoController.listar(RequisicaoEstado.EM_PROGRESSO, Pageable.unpaged());

        assertSame(esperado, resultado);
        verify(requisicaoService).procurarPaginated(RequisicaoEstado.EM_PROGRESSO, null, null, null, null, null, Pageable.unpaged());
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
        Page<Requisicao> esperado = new PageImpl<>(List.of(new RequisicaoManutencao()));
        when(requisicaoService.procurarPaginated(
                RequisicaoEstado.ABERTO, RequisicaoTipo.MANUTENCAO, RequisicaoPrioridade.ALTA,
                "Maria", null, null, Pageable.unpaged()))
                .thenReturn(esperado);

        Page<Requisicao> resultado = requisicaoController.procurar(
                RequisicaoEstado.ABERTO, RequisicaoTipo.MANUTENCAO, RequisicaoPrioridade.ALTA,
                "Maria", null, null, Pageable.unpaged());

        assertSame(esperado, resultado);
    }

    @Test
    void criarMaterial_deveRetornarResponseOkComBody() {
        CriarRequisicaoMaterialRequest request = new CriarRequisicaoMaterialRequest(
                "material", RequisicaoPrioridade.MEDIA, null,
                List.of(new CriarRequisicaoMaterialRequest.ItemMaterialRequest(2L, 3)));
        Requisicao resposta = new RequisicaoMaterial();
        Utilizador utilizador = new Utilizador();
        utilizador.setId(1L);
        when(requisicaoService.criarMaterial(request, 1L)).thenReturn((RequisicaoMaterial) resposta);

        ResponseEntity<Requisicao> responseEntity = requisicaoController.criarMaterial(request, utilizador);

        assertEquals(200, responseEntity.getStatusCode().value());
        assertSame(resposta, responseEntity.getBody());
    }

    @Test
    void criarTransporte_deveRetornarResponseOkComBody() {
        CriarRequisicaoTransporteRequest request = new CriarRequisicaoTransporteRequest(
                "transporte", RequisicaoPrioridade.BAIXA, null, "Porto",
                LocalDateTime.of(2026, 3, 21, 9, 0), LocalDateTime.of(2026, 3, 21, 12, 0),
                4, "Motorista", List.of(2L), null);
        Requisicao resposta = new RequisicaoTransporte();
        Utilizador utilizador = new Utilizador();
        utilizador.setId(1L);
        when(requisicaoService.criarTransporte(request, 1L)).thenReturn((RequisicaoTransporte) resposta);

        ResponseEntity<Requisicao> responseEntity = requisicaoController.criarTransporte(request, utilizador);

        assertEquals(200, responseEntity.getStatusCode().value());
        assertSame(resposta, responseEntity.getBody());
    }

    @Test
    void criarManutencao_deveRetornarResponseOkComBody() {
        CriarRequisicaoManutencaoRequest request = new CriarRequisicaoManutencaoRequest(
                "manutencao", RequisicaoPrioridade.URGENTE, 2L, List.of());
        Requisicao resposta = new RequisicaoManutencao();
        Utilizador utilizador = new Utilizador();
        utilizador.setId(1L);
        when(requisicaoService.criarManutencao(request, 1L)).thenReturn((RequisicaoManutencao) resposta);

        ResponseEntity<Requisicao> responseEntity = requisicaoController.criarManutencao(request, utilizador);

        assertEquals(200, responseEntity.getStatusCode().value());
        assertSame(resposta, responseEntity.getBody());
    }
}
