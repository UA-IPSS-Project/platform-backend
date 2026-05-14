package pt.florinhas.requisicoes.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.requisicoes.domain.Material;
import pt.florinhas.requisicoes.domain.Requisicao;
import pt.florinhas.requisicoes.domain.RequisicaoEstado;
import pt.florinhas.requisicoes.domain.RequisicaoMaterial;
import pt.florinhas.requisicoes.service.AuditService;
import pt.florinhas.requisicoes.service.RequisicaoService;

class RequisicaoControllerTest {

    private RequisicaoService requisicaoService;

    private AuditService auditService;

    private RequisicaoController controller;

    @BeforeEach
    void setUp() {

        requisicaoService =
                mock(RequisicaoService.class);

        auditService =
                mock(AuditService.class);

        controller =
                new RequisicaoController(
                        requisicaoService,
                        auditService);
    }

    @Test
    void listar_DeveRetornarPagina() {

        Page<Requisicao> page =
                new PageImpl<>(
                        List.of(mock(Requisicao.class)));

        when(requisicaoService.procurarPaginated(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()))
                .thenReturn(page);

        Page<Requisicao> result =
                controller.listar(
                        RequisicaoEstado.ABERTO,
                        PageRequest.of(0, 20));

        assertNotNull(result);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void obter_DeveRetornarRequisicao() {

        Requisicao req =
                mock(Requisicao.class);

        when(requisicaoService.obterPorId(1L))
                .thenReturn(req);

        Requisicao result =
                controller.obter(1L);

        assertNotNull(result);
    }

    @Test
    void listarMateriais_DeveRetornarLista() {

        when(requisicaoService.listarMateriais())
                .thenReturn(
                        List.of(mock(Material.class)));

        List<Material> result =
                controller.listarMateriais();

        assertEquals(1, result.size());
    }

    @Test
    void apagarMaterialCatalogo_DeveExecutar() {

        assertDoesNotThrow(() ->
                controller.apagarMaterialCatalogo(1L));

        verify(requisicaoService)
                .apagarMaterialCatalogo(1L);

        verify(auditService)
                .log(
                        eq("APAGAR_MATERIAL_CATALOGO"),
                        eq("MATERIAL"),
                        eq(1L),
                        anyString());
    }

    @Test
    void apagarTipoManutencao_DeveExecutar() {

        assertDoesNotThrow(() ->
                controller.apagarTipoManutencao(1L));

        verify(requisicaoService)
                .apagarTipoManutencao(1L);

        verify(auditService)
                .log(
                        eq("APAGAR_TIPO_MANUTENCAO"),
                        eq("TIPO_MANUTENCAO"),
                        eq(1L),
                        anyString());
    }

    @Test
    void apagarManutencaoItem_DeveExecutar() {

        assertDoesNotThrow(() ->
                controller.apagarManutencaoItem(1L));

        verify(requisicaoService)
                .apagarManutencaoItem(1L);

        verify(auditService)
                .log(
                        eq("APAGAR_ITEM_MANUTENCAO_CATALOGO"),
                        eq("MANUTENCAO_ITEM"),
                        eq(1L),
                        anyString());
    }

    @Test
    void procurar_DeveRetornarPagina() {

        Page<Requisicao> page =
                new PageImpl<>(
                        List.of(mock(Requisicao.class)));

        when(requisicaoService.procurarPaginated(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()))
                .thenReturn(page);

        Page<Requisicao> result =
                controller.procurar(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        PageRequest.of(0, 20));

        assertNotNull(result);

        assertEquals(
                1,
                result.getContent().size());
    }

        @Test
        void criarMaterial_DeveExecutar() {

        Utilizador utilizador =
                mock(Utilizador.class);

        RequisicaoMaterial requisicao =
                mock(RequisicaoMaterial.class);

        when(utilizador.getId())
                .thenReturn(1L);

        when(utilizador.getNome())
                .thenReturn("Ana");

        when(requisicao.getId())
                .thenReturn(10L);

        when(requisicaoService.criarMaterial(
                any(),
                eq(1L)))
                .thenReturn(requisicao);

        assertDoesNotThrow(() ->
                controller.criarMaterial(
                        null,
                        utilizador));

        verify(auditService)
                .log(
                        eq("CRIAR_REQUISICAO_MATERIAL"),
                        eq("REQUISICAO"),
                        eq(10L),
                        contains("Ana"));
        }
}