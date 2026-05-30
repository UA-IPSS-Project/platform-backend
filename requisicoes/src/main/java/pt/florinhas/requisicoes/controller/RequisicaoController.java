package pt.florinhas.requisicoes.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import pt.florinhas.requisicoes.domain.Material;
import pt.florinhas.requisicoes.domain.Requisicao;
import pt.florinhas.requisicoes.domain.RequisicaoEstado;
import pt.florinhas.requisicoes.domain.RequisicaoPrioridade;
import pt.florinhas.requisicoes.domain.RequisicaoTipo;
import pt.florinhas.requisicoes.domain.TipoManutencao;
import pt.florinhas.requisicoes.domain.Transporte;
import pt.florinhas.requisicoes.dto.AtualizarEstadoRequisicaoRequest;
import pt.florinhas.requisicoes.dto.CriarMaterialRequest;
import pt.florinhas.requisicoes.dto.CriarRequisicaoManutencaoRequest;
import pt.florinhas.requisicoes.dto.CriarRequisicaoMaterialRequest;
import pt.florinhas.requisicoes.dto.CriarRequisicaoTransporteRequest;
import pt.florinhas.requisicoes.dto.CriarTipoManutencaoRequest;
import pt.florinhas.requisicoes.dto.RequisicaoPeriodicaConfigRequest;
import pt.florinhas.requisicoes.dto.CriarTransporteRequest;
import pt.florinhas.requisicoes.dto.AtualizarCategoriaTransporteRequest;
import pt.florinhas.requisicoes.dto.MoverCategoriaTransporteRequest;
import pt.florinhas.requisicoes.service.AuditService;
import pt.florinhas.requisicoes.service.RequisicaoService;

import pt.florinhas.common_data.domain.Utilizador;

@RestController
@RequestMapping("/api/requisicoes")
public class RequisicaoController {

    private final RequisicaoService requisicaoService;
    private final AuditService auditService;

    public RequisicaoController(RequisicaoService requisicaoService, AuditService auditService) {
        this.requisicaoService = requisicaoService;
        this.auditService = auditService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SECRETARIA', 'BALNEARIO', 'FUNCIONARIO', 'ESCOLA', 'INTERNO')")
    public Page<Requisicao> listar(
            @RequestParam(required = false) RequisicaoEstado estado,
            @PageableDefault(size = 20, sort = "criadoEm") Pageable pageable) {
        return requisicaoService.procurarPaginated(estado, null, null, null, null, null, pageable);
    }

    @GetMapping("/procurar")
    @PreAuthorize("hasAnyRole('SECRETARIA', 'BALNEARIO', 'FUNCIONARIO', 'ESCOLA', 'INTERNO')")
    public Page<Requisicao> procurar(
            @RequestParam(required = false) RequisicaoEstado estado,
            @RequestParam(required = false) RequisicaoTipo tipo,
            @RequestParam(required = false) RequisicaoPrioridade prioridade,
            @RequestParam(required = false) String criadoPorNome,
            @RequestParam(required = false) String dataInicio,
            @RequestParam(required = false) String dataFim,
            @PageableDefault(size = 20, sort = "criadoEm") Pageable pageable) {
        return requisicaoService.procurarPaginated(estado, tipo, prioridade, criadoPorNome, dataInicio, dataFim, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SECRETARIA', 'BALNEARIO', 'FUNCIONARIO', 'ESCOLA', 'INTERNO')")
    public Requisicao obter(@PathVariable Long id) {
        return requisicaoService.obterPorId(id);
    }

    @GetMapping("/materiais")
    public List<Material> listarMateriais() {
        return requisicaoService.listarMateriais();
    }

    @PostMapping("/materiais")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<Material> criarMaterialCatalogo(@Valid @RequestBody CriarMaterialRequest request) {
        Material material = requisicaoService.criarMaterialCatalogo(request);
        auditService.log("CRIAR_MATERIAL_CATALOGO", "MATERIAL", material.getId(), 
            "Criado material no catálogo: " + material.getNome());
        return ResponseEntity.ok(material);
    }

    @PutMapping("/materiais/{id}")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<Material> atualizarMaterialCatalogo(
            @PathVariable Long id,
            @Valid @RequestBody CriarMaterialRequest request) {
        Material material = requisicaoService.atualizarMaterialCatalogo(id, request);
        auditService.log("ATUALIZAR_MATERIAL_CATALOGO", "MATERIAL", id, 
            "Atualizado material no catálogo: " + material.getNome());
        return ResponseEntity.ok(material);
    }

    @DeleteMapping("/materiais/{id}")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<Void> apagarMaterialCatalogo(@PathVariable Long id) {
        requisicaoService.apagarMaterialCatalogo(id);
        auditService.log("APAGAR_MATERIAL_CATALOGO", "MATERIAL", id, "Material apagado do catálogo");
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/transportes")
    public List<Transporte> listarTransportes() {
        return requisicaoService.listarTransportes();
    }

    @PostMapping("/transportes")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<Transporte> criarTransporteCatalogo(@Valid @RequestBody CriarTransporteRequest request) {
        Transporte transporte = requisicaoService.criarTransporteCatalogo(request);
        auditService.log("CRIAR_TRANSPORTE_CATALOGO", "TRANSPORTE", transporte.getId(), 
            "Criado transporte no catálogo: " + transporte.getMarca() + " " + transporte.getModelo());
        return ResponseEntity.ok(transporte);
    }

    @PutMapping("/transportes/{id}")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<Transporte> atualizarTransporteCatalogo(
            @PathVariable Long id,
            @Valid @RequestBody CriarTransporteRequest request) {
        Transporte transporte = requisicaoService.atualizarTransporteCatalogo(id, request);
        auditService.log("ATUALIZAR_TRANSPORTE_CATALOGO", "TRANSPORTE", id, 
            "Atualizado transporte no catálogo: " + transporte.getMarca() + " " + transporte.getModelo());
        return ResponseEntity.ok(transporte);
    }

    @PatchMapping("/transportes/{id}/categoria")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<Transporte> atualizarCategoriaTransporte(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarCategoriaTransporteRequest request) {
        return ResponseEntity.ok(requisicaoService.atualizarCategoriaTransporte(id, request.categoria()));
    }

    @PatchMapping("/transportes/mover-categoria")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<Void> moverCategoria(
            @Valid @RequestBody MoverCategoriaTransporteRequest request) {
        requisicaoService.moverVeiculosPorCategoria(request.origem(), request.destino());
        auditService.log("MOVER_CATEGORIA_TRANSPORTE", "TRANSPORTE", 0L, 
            "Movidos veículos da categoria " + request.origem() + " para " + request.destino());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tipos-manutencao")
    public List<TipoManutencao> listarTiposManutencao() {
        return requisicaoService.listarTiposManutencao();
    }

    @PostMapping("/tipos-manutencao")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<TipoManutencao> criarTipoManutencao(@Valid @RequestBody CriarTipoManutencaoRequest request) {
        TipoManutencao tipo = requisicaoService.criarTipoManutencao(request);
        auditService.log("CRIAR_TIPO_MANUTENCAO", "TIPO_MANUTENCAO", tipo.getId(), 
            "Criado tipo de manutenção: " + tipo.getNome());
        return ResponseEntity.ok(tipo);
    }

    @PutMapping("/tipos-manutencao/{id}")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<TipoManutencao> atualizarTipoManutencao(
            @PathVariable Long id,
            @Valid @RequestBody CriarTipoManutencaoRequest request) {
        TipoManutencao tipo = requisicaoService.atualizarTipoManutencao(id, request);
        auditService.log("ATUALIZAR_TIPO_MANUTENCAO", "TIPO_MANUTENCAO", id, 
            "Atualizado tipo de manutenção: " + tipo.getNome());
        return ResponseEntity.ok(tipo);
    }

    @DeleteMapping("/tipos-manutencao/{id}")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<Void> apagarTipoManutencao(@PathVariable Long id) {
        requisicaoService.apagarTipoManutencao(id);
        auditService.log("APAGAR_TIPO_MANUTENCAO", "TIPO_MANUTENCAO", id, "Tipo de manutenção apagado");
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/material")
    @PreAuthorize("hasAnyRole('SECRETARIA', 'BALNEARIO', 'FUNCIONARIO', 'ESCOLA', 'INTERNO')")
    public ResponseEntity<Requisicao> criarMaterial(
            @Valid @RequestBody CriarRequisicaoMaterialRequest request,
            @AuthenticationPrincipal Utilizador utilizador) {
        Requisicao req = requisicaoService.criarMaterial(request, utilizador.getId());
        auditService.log("CRIAR_REQUISICAO_MATERIAL", "REQUISICAO", req.getId(), 
            "Criada requisição de material por " + utilizador.getNome());
        return ResponseEntity.ok(req);
    }

    @PostMapping("/transporte")
    @PreAuthorize("hasAnyRole('SECRETARIA', 'BALNEARIO', 'FUNCIONARIO', 'ESCOLA', 'INTERNO')")
    public ResponseEntity<Requisicao> criarTransporte(
            @Valid @RequestBody CriarRequisicaoTransporteRequest request,
            @AuthenticationPrincipal Utilizador utilizador) {
        Requisicao req = requisicaoService.criarTransporte(request, utilizador.getId());
        auditService.log("CRIAR_REQUISICAO_TRANSPORTE", "REQUISICAO", req.getId(), 
            "Criada requisição de transporte por " + utilizador.getNome());
        return ResponseEntity.ok(req);
    }

    @PostMapping("/manutencao")
    @PreAuthorize("hasAnyRole('SECRETARIA', 'BALNEARIO', 'FUNCIONARIO', 'ESCOLA', 'INTERNO')")
    public ResponseEntity<Requisicao> criarManutencao(
            @Valid @RequestBody CriarRequisicaoManutencaoRequest request,
            @AuthenticationPrincipal Utilizador utilizador) {
        Requisicao req = requisicaoService.criarManutencao(request, utilizador.getId());
        auditService.log("CRIAR_REQUISICAO_MANUTENCAO", "REQUISICAO", req.getId(), 
            "Criada requisição de manutenção por " + utilizador.getNome());
        return ResponseEntity.ok(req);
    }

    @GetMapping("/manutencao-items")
    public List<pt.florinhas.requisicoes.domain.ManutencaoItem> listarManutencaoItems() {
        return requisicaoService.listarManutencaoItems();
    }

    @PostMapping("/manutencao-items")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<pt.florinhas.requisicoes.domain.ManutencaoItem> criarManutencaoItem(
            @Valid @RequestBody pt.florinhas.requisicoes.dto.CriarManutencaoItemRequest request) {
        pt.florinhas.requisicoes.domain.ManutencaoItem item = requisicaoService.criarManutencaoItem(request);
        auditService.log("CRIAR_ITEM_MANUTENCAO_CATALOGO", "MANUTENCAO_ITEM", item.getId(), 
            "Criado item de manutenção no catálogo: " + item.getEspaco() + " - " + item.getItemVerificacao());
        return ResponseEntity.ok(item);
    }

    @PutMapping("/manutencao-items/{id}")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<pt.florinhas.requisicoes.domain.ManutencaoItem> atualizarManutencaoItem(
            @PathVariable Long id,
            @Valid @RequestBody pt.florinhas.requisicoes.dto.CriarManutencaoItemRequest request) {
        pt.florinhas.requisicoes.domain.ManutencaoItem item = requisicaoService.atualizarManutencaoItem(id, request);
        auditService.log("ATUALIZAR_ITEM_MANUTENCAO_CATALOGO", "MANUTENCAO_ITEM", id, 
            "Atualizado item de manutenção no catálogo: " + item.getEspaco() + " - " + item.getItemVerificacao());
        return ResponseEntity.ok(item);
    }

    @DeleteMapping("/manutencao-items/{id}")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<Void> apagarManutencaoItem(@PathVariable Long id) {
        requisicaoService.apagarManutencaoItem(id);
        auditService.log("APAGAR_ITEM_MANUTENCAO_CATALOGO", "MANUTENCAO_ITEM", id, 
            "Item de manutenção apagado do catálogo");
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<Requisicao> atualizarEstado(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarEstadoRequisicaoRequest request,
            @AuthenticationPrincipal Utilizador utilizador) {
        Requisicao req = requisicaoService.atualizarEstado(id, request.estado(), utilizador.getId());
        auditService.log("ATUALIZAR_ESTADO_REQUISICAO", "REQUISICAO", id, 
            "Estado da requisição atualizado para " + request.estado() + " por " + utilizador.getNome());
        return ResponseEntity.ok(req);
    }

    @PutMapping("/{id}/periodicidade")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<Requisicao> atualizarPeriodicidade(
            @PathVariable Long id,
            @Valid @RequestBody RequisicaoPeriodicaConfigRequest config) {
        Requisicao req = requisicaoService.atualizarPeriodicidade(id, config);
        return ResponseEntity.ok(req);
    }

    @DeleteMapping("/{id}/periodicidade")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<Requisicao> cancelarPeriodicidade(@PathVariable Long id) {
        Requisicao req = requisicaoService.cancelarPeriodicidade(id);
        return ResponseEntity.ok(req);
    }
}
