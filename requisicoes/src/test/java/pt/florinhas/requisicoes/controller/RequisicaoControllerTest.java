package pt.florinhas.requisicoes.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.requisicoes.domain.Material;
import pt.florinhas.requisicoes.domain.Requisicao;
import pt.florinhas.requisicoes.domain.RequisicaoEstado;
import pt.florinhas.requisicoes.domain.RequisicaoManutencao;
import pt.florinhas.requisicoes.domain.RequisicaoMaterial;
import pt.florinhas.requisicoes.domain.RequisicaoPrioridade;
import pt.florinhas.requisicoes.domain.RequisicaoTipo;
import pt.florinhas.requisicoes.domain.RequisicaoTransporte;
import pt.florinhas.requisicoes.domain.Transporte;
import pt.florinhas.requisicoes.domain.TransporteCategoria;
import pt.florinhas.requisicoes.domain.TipoManutencao;
import pt.florinhas.requisicoes.dto.CriarRequisicaoManutencaoRequest;
import pt.florinhas.requisicoes.dto.CriarRequisicaoMaterialRequest;
import pt.florinhas.requisicoes.dto.CriarRequisicaoTransporteRequest;
import pt.florinhas.requisicoes.service.AuditService;
import pt.florinhas.requisicoes.service.RequisicaoService;

class RequisicaoControllerTest {

    private RequisicaoService requisicaoService;
    private AuditService auditService;
    private RequisicaoController controller;

    @BeforeEach
    void setUp() {
        requisicaoService = mock(RequisicaoService.class);
        auditService = mock(AuditService.class);
        controller = new RequisicaoController(requisicaoService, auditService);
    }

    @Test
    void listar_DeveRetornarPagina() {
        Page<Requisicao> page = new PageImpl<>(List.of(mock(Requisicao.class)));

        when(requisicaoService.procurarPaginated(
                any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(page);

        Page<Requisicao> result = controller.listar(RequisicaoEstado.ABERTO, PageRequest.of(0, 20));

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void listar_semEstado_deveUsarProcurarPaginated() {
        Page<Requisicao> esperado = new PageImpl<>(List.of(new RequisicaoManutencao()));
        Pageable pageable = Pageable.unpaged();
        when(requisicaoService.procurarPaginated(null, null, null, null, null, null, pageable)).thenReturn(esperado);

        Page<Requisicao> resultado = controller.listar(null, pageable);

        assertSame(esperado, resultado);
        verify(requisicaoService).procurarPaginated(null, null, null, null, null, null, pageable);
    }

    @Test
    void obter_DeveRetornarRequisicao() {
        Requisicao req = mock(Requisicao.class);
        when(requisicaoService.obterPorId(1L)).thenReturn(req);

        Requisicao result = controller.obter(1L);

        assertNotNull(result);
        assertSame(req, result);
    }

    @Test
    void listarMateriais_DeveRetornarLista() {
        when(requisicaoService.listarMateriais())
                .thenReturn(List.of(mock(Material.class)));

        List<Material> result = controller.listarMateriais();

        assertEquals(1, result.size());
    }

    @Test
    void apagarMaterialCatalogo_DeveExecutar() {
        assertDoesNotThrow(() -> controller.apagarMaterialCatalogo(1L));

        verify(requisicaoService).apagarMaterialCatalogo(1L);
        verify(auditService).log(
                eq("APAGAR_MATERIAL_CATALOGO"),
                eq("MATERIAL"),
                eq(1L),
                anyString());
    }

    @Test
    void apagarTipoManutencao_DeveExecutar() {
        assertDoesNotThrow(() -> controller.apagarTipoManutencao(1L));

        verify(requisicaoService).apagarTipoManutencao(1L);
        verify(auditService).log(
                eq("APAGAR_TIPO_MANUTENCAO"),
                eq("TIPO_MANUTENCAO"),
                eq(1L),
                anyString());
    }

    @Test
    void apagarManutencaoItem_DeveExecutar() {
        assertDoesNotThrow(() -> controller.apagarManutencaoItem(1L));

        verify(requisicaoService).apagarManutencaoItem(1L);
        verify(auditService).log(
                eq("APAGAR_ITEM_MANUTENCAO_CATALOGO"),
                eq("MANUTENCAO_ITEM"),
                eq(1L),
                anyString());
    }

    @Test
    void procurar_DeveRetornarPagina() {
        Page<Requisicao> page = new PageImpl<>(List.of(mock(Requisicao.class)));

        when(requisicaoService.procurarPaginated(
                any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(page);

        Page<Requisicao> result = controller.procurar(
                null, null, null, null, null, null, PageRequest.of(0, 20));

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void procurar_deveDelegarNoServiceComTodosOsParametros() {
        Page<Requisicao> esperado = new PageImpl<>(List.of(new RequisicaoManutencao()));
        Pageable pageable = Pageable.unpaged();
        when(requisicaoService.procurarPaginated(
                RequisicaoEstado.ABERTO,
                RequisicaoTipo.MANUTENCAO,
                RequisicaoPrioridade.ALTA,
                "Maria",
                null,
                null,
                pageable)).thenReturn(esperado);

        Page<Requisicao> resultado = controller.procurar(
                RequisicaoEstado.ABERTO,
                RequisicaoTipo.MANUTENCAO,
                RequisicaoPrioridade.ALTA,
                "Maria",
                null,
                null,
                pageable);

        assertSame(esperado, resultado);
    }

    @Test
    void criarMaterial_DeveExecutar() {
        Utilizador utilizador = mock(Utilizador.class);
        RequisicaoMaterial requisicao = mock(RequisicaoMaterial.class);

        when(utilizador.getId()).thenReturn(1L);
        when(utilizador.getNome()).thenReturn("Ana");
        when(requisicao.getId()).thenReturn(10L);

        when(requisicaoService.criarMaterial(any(), eq(1L))).thenReturn(requisicao);

        assertDoesNotThrow(() -> controller.criarMaterial(null, utilizador));

        verify(auditService).log(
                eq("CRIAR_REQUISICAO_MATERIAL"),
                eq("REQUISICAO"),
                eq(10L),
                contains("Ana"));
    }

    @Test
    void criarMaterial_deveRetornarResponseOkComBody() {
        CriarRequisicaoMaterialRequest request = new CriarRequisicaoMaterialRequest(
                "material",
                RequisicaoPrioridade.MEDIA,
                null,
                List.of(new CriarRequisicaoMaterialRequest.ItemMaterialRequest(2L, 3)), null);
        Requisicao resposta = new RequisicaoMaterial();
        Utilizador utilizador = new Utilizador();
        utilizador.setId(1L);
        when(requisicaoService.criarMaterial(request, 1L)).thenReturn((RequisicaoMaterial) resposta);

        ResponseEntity<Requisicao> responseEntity = controller.criarMaterial(request, utilizador);

        assertEquals(200, responseEntity.getStatusCode().value());
        assertSame(resposta, responseEntity.getBody());
    }

    @Test
    void criarTransporte_deveRetornarResponseOkComBody() {
        CriarRequisicaoTransporteRequest request = new CriarRequisicaoTransporteRequest(
                "transporte",
                RequisicaoPrioridade.BAIXA,
                null,
                "Porto",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(3),
                4,
                "Motorista",
                List.of(2L), null);
        Requisicao resposta = new RequisicaoTransporte();
        Utilizador utilizador = new Utilizador();
        utilizador.setId(1L);
        when(requisicaoService.criarTransporte(request, 1L)).thenReturn((RequisicaoTransporte) resposta);

        ResponseEntity<Requisicao> responseEntity = controller.criarTransporte(request, utilizador);

        assertEquals(200, responseEntity.getStatusCode().value());
        assertSame(resposta, responseEntity.getBody());
    }

    @Test
    void criarManutencao_deveRetornarResponseOkComBody() {
        CriarRequisicaoManutencaoRequest request = new CriarRequisicaoManutencaoRequest(
                "manutencao",
                RequisicaoPrioridade.URGENTE,
                2L,
                List.of(), null);
        Requisicao resposta = new RequisicaoManutencao();
        Utilizador utilizador = new Utilizador();
        utilizador.setId(1L);
        when(requisicaoService.criarManutencao(request, 1L)).thenReturn((RequisicaoManutencao) resposta);

        ResponseEntity<Requisicao> responseEntity = controller.criarManutencao(request, utilizador);

        assertEquals(200, responseEntity.getStatusCode().value());
        assertSame(resposta, responseEntity.getBody());
    }

    @Test
    void criarMaterialCatalogo_deveRetornarMaterial() {
        pt.florinhas.requisicoes.dto.CriarMaterialRequest request = new pt.florinhas.requisicoes.dto.CriarMaterialRequest("detergente", "LIGEIRO", "Atributo", "Valor");
        Material mat = new Material();
        mat.setId(1L);
        mat.setNome("detergente");
        when(requisicaoService.criarMaterialCatalogo(request)).thenReturn(mat);

        ResponseEntity<Material> response = controller.criarMaterialCatalogo(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(mat, response.getBody());
        verify(auditService).log(eq("CRIAR_MATERIAL_CATALOGO"), eq("MATERIAL"), eq(1L), anyString());
    }

    @Test
    void atualizarMaterialCatalogo_deveRetornarMaterial() {
        pt.florinhas.requisicoes.dto.CriarMaterialRequest request = new pt.florinhas.requisicoes.dto.CriarMaterialRequest("detergente", "LIGEIRO", "Atributo", "Valor");
        Material mat = new Material();
        mat.setId(1L);
        mat.setNome("detergente");
        when(requisicaoService.atualizarMaterialCatalogo(1L, request)).thenReturn(mat);

        ResponseEntity<Material> response = controller.atualizarMaterialCatalogo(1L, request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(mat, response.getBody());
        verify(auditService).log(eq("ATUALIZAR_MATERIAL_CATALOGO"), eq("MATERIAL"), eq(1L), anyString());
    }

    @Test
    void listarTransportes_deveRetornarLista() {
        Transporte transporte = new Transporte();
        when(requisicaoService.listarTransportes()).thenReturn(List.of(transporte));

        List<Transporte> result = controller.listarTransportes();

        assertEquals(1, result.size());
        assertSame(transporte, result.get(0));
    }

    @Test
    void criarTransporteCatalogo_deveRetornarTransporte() {
        pt.florinhas.requisicoes.dto.CriarTransporteRequest request = new pt.florinhas.requisicoes.dto.CriarTransporteRequest("TX-01", "Ligeiro", TransporteCategoria.LIGEIRO_DE_PASSAGEIROS, "12-AA-34", "Ford", "Transit", 9, java.time.LocalDate.now());
        Transporte transporte = new Transporte();
        transporte.setId(1L);
        transporte.setMarca("Ford");
        transporte.setModelo("Transit");
        when(requisicaoService.criarTransporteCatalogo(request)).thenReturn(transporte);

        ResponseEntity<Transporte> response = controller.criarTransporteCatalogo(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(transporte, response.getBody());
        verify(auditService).log(eq("CRIAR_TRANSPORTE_CATALOGO"), eq("TRANSPORTE"), eq(1L), anyString());
    }

    @Test
    void atualizarTransporteCatalogo_deveRetornarTransporte() {
        pt.florinhas.requisicoes.dto.CriarTransporteRequest request = new pt.florinhas.requisicoes.dto.CriarTransporteRequest("TX-01", "Ligeiro", TransporteCategoria.LIGEIRO_DE_PASSAGEIROS, "12-AA-34", "Ford", "Transit", 9, java.time.LocalDate.now());
        Transporte transporte = new Transporte();
        transporte.setId(1L);
        transporte.setMarca("Ford");
        transporte.setModelo("Transit");
        when(requisicaoService.atualizarTransporteCatalogo(1L, request)).thenReturn(transporte);

        ResponseEntity<Transporte> response = controller.atualizarTransporteCatalogo(1L, request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(transporte, response.getBody());
        verify(auditService).log(eq("ATUALIZAR_TRANSPORTE_CATALOGO"), eq("TRANSPORTE"), eq(1L), anyString());
    }

    @Test
    void atualizarCategoriaTransporte_deveRetornarTransporte() {
        pt.florinhas.requisicoes.dto.AtualizarCategoriaTransporteRequest request = new pt.florinhas.requisicoes.dto.AtualizarCategoriaTransporteRequest(TransporteCategoria.ABATIDO_VENDIDO_DESCONTINUADO);
        Transporte transporte = new Transporte();
        transporte.setId(1L);
        when(requisicaoService.atualizarCategoriaTransporte(1L, TransporteCategoria.ABATIDO_VENDIDO_DESCONTINUADO)).thenReturn(transporte);

        ResponseEntity<Transporte> response = controller.atualizarCategoriaTransporte(1L, request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(transporte, response.getBody());
    }

    @Test
    void moverCategoria_deveMoverCategoria() {
        pt.florinhas.requisicoes.dto.MoverCategoriaTransporteRequest request = new pt.florinhas.requisicoes.dto.MoverCategoriaTransporteRequest(TransporteCategoria.LIGEIRO_DE_PASSAGEIROS, TransporteCategoria.ABATIDO_VENDIDO_DESCONTINUADO);

        ResponseEntity<Void> response = controller.moverCategoria(request);

        assertNotNull(response);
        assertEquals(204, response.getStatusCode().value());
        verify(requisicaoService).moverVeiculosPorCategoria(TransporteCategoria.LIGEIRO_DE_PASSAGEIROS, TransporteCategoria.ABATIDO_VENDIDO_DESCONTINUADO);
        verify(auditService).log(eq("MOVER_CATEGORIA_TRANSPORTE"), eq("TRANSPORTE"), eq(0L), anyString());
    }

    @Test
    void listarTiposManutencao_deveRetornarLista() {
        TipoManutencao tipo = new TipoManutencao();
        when(requisicaoService.listarTiposManutencao()).thenReturn(List.of(tipo));

        List<TipoManutencao> result = controller.listarTiposManutencao();

        assertEquals(1, result.size());
    }

    @Test
    void criarTipoManutencao_deveRetornarTipo() {
        pt.florinhas.requisicoes.dto.CriarTipoManutencaoRequest request = new pt.florinhas.requisicoes.dto.CriarTipoManutencaoRequest("Elétrica", "Descrição");
        TipoManutencao tipo = new TipoManutencao();
        tipo.setId(1L);
        tipo.setNome("Elétrica");
        when(requisicaoService.criarTipoManutencao(request)).thenReturn(tipo);

        ResponseEntity<TipoManutencao> response = controller.criarTipoManutencao(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(tipo, response.getBody());
        verify(auditService).log(eq("CRIAR_TIPO_MANUTENCAO"), eq("TIPO_MANUTENCAO"), eq(1L), anyString());
    }

    @Test
    void atualizarTipoManutencao_deveRetornarTipo() {
        pt.florinhas.requisicoes.dto.CriarTipoManutencaoRequest request = new pt.florinhas.requisicoes.dto.CriarTipoManutencaoRequest("Elétrica", "Descrição");
        TipoManutencao tipo = new TipoManutencao();
        tipo.setId(1L);
        tipo.setNome("Elétrica");
        when(requisicaoService.atualizarTipoManutencao(1L, request)).thenReturn(tipo);

        ResponseEntity<TipoManutencao> response = controller.atualizarTipoManutencao(1L, request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(tipo, response.getBody());
        verify(auditService).log(eq("ATUALIZAR_TIPO_MANUTENCAO"), eq("TIPO_MANUTENCAO"), eq(1L), anyString());
    }

    @Test
    void listarManutencaoItems_deveRetornarLista() {
        pt.florinhas.requisicoes.domain.ManutencaoItem item = new pt.florinhas.requisicoes.domain.ManutencaoItem();
        when(requisicaoService.listarManutencaoItems()).thenReturn(List.of(item));

        List<pt.florinhas.requisicoes.domain.ManutencaoItem> result = controller.listarManutencaoItems();

        assertEquals(1, result.size());
    }

    @Test
    void criarManutencaoItem_deveRetornarItem() {
        pt.florinhas.requisicoes.dto.CriarManutencaoItemRequest request = new pt.florinhas.requisicoes.dto.CriarManutencaoItemRequest("Categoria", "Espaco", "Verificacao");
        pt.florinhas.requisicoes.domain.ManutencaoItem item = new pt.florinhas.requisicoes.domain.ManutencaoItem();
        item.setId(1L);
        item.setEspaco("Espaco");
        item.setItemVerificacao("Verificacao");
        when(requisicaoService.criarManutencaoItem(request)).thenReturn(item);

        ResponseEntity<pt.florinhas.requisicoes.domain.ManutencaoItem> response = controller.criarManutencaoItem(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(item, response.getBody());
        verify(auditService).log(eq("CRIAR_ITEM_MANUTENCAO_CATALOGO"), eq("MANUTENCAO_ITEM"), eq(1L), anyString());
    }

    @Test
    void atualizarManutencaoItem_deveRetornarItem() {
        pt.florinhas.requisicoes.dto.CriarManutencaoItemRequest request = new pt.florinhas.requisicoes.dto.CriarManutencaoItemRequest("Categoria", "Espaco", "Verificacao");
        pt.florinhas.requisicoes.domain.ManutencaoItem item = new pt.florinhas.requisicoes.domain.ManutencaoItem();
        item.setId(1L);
        item.setEspaco("Espaco");
        item.setItemVerificacao("Verificacao");
        when(requisicaoService.atualizarManutencaoItem(1L, request)).thenReturn(item);

        ResponseEntity<pt.florinhas.requisicoes.domain.ManutencaoItem> response = controller.atualizarManutencaoItem(1L, request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(item, response.getBody());
        verify(auditService).log(eq("ATUALIZAR_ITEM_MANUTENCAO_CATALOGO"), eq("MANUTENCAO_ITEM"), eq(1L), anyString());
    }

    @Test
    void atualizarEstado_deveAtualizarEstado() {
        pt.florinhas.requisicoes.dto.AtualizarEstadoRequisicaoRequest request = new pt.florinhas.requisicoes.dto.AtualizarEstadoRequisicaoRequest(RequisicaoEstado.FECHADO);
        Requisicao req = new RequisicaoManutencao();
        req.setId(1L);
        Utilizador utilizador = new Utilizador();
        utilizador.setId(2L);
        utilizador.setNome("Secretaria A");
        when(requisicaoService.atualizarEstado(1L, RequisicaoEstado.FECHADO, 2L)).thenReturn(req);

        ResponseEntity<Requisicao> response = controller.atualizarEstado(1L, request, utilizador);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(req, response.getBody());
        verify(auditService).log(eq("ATUALIZAR_ESTADO_REQUISICAO"), eq("REQUISICAO"), eq(1L), anyString());
    }
}
