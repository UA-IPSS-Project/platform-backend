package pt.florinhas.requisicoes.controller;

import java.util.List;

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
import pt.florinhas.requisicoes.dto.CriarTransporteRequest;
import pt.florinhas.requisicoes.service.RequisicaoService;

import pt.florinhas.common_data.domain.Utilizador;

@RestController
@RequestMapping("/api/requisicoes")
public class RequisicaoController {

    private final RequisicaoService requisicaoService;

    public RequisicaoController(RequisicaoService requisicaoService) {
        this.requisicaoService = requisicaoService;
    }

    @GetMapping
    public List<Requisicao> listar(@RequestParam(required = false) RequisicaoEstado estado) {
        if (estado == null) {
            return requisicaoService.listarTodas();
        }
        return requisicaoService.listarPorEstado(estado);
    }

    @GetMapping("/procurar")
    public List<Requisicao> procurar(
            @RequestParam(required = false) RequisicaoEstado estado,
            @RequestParam(required = false) RequisicaoTipo tipo,
            @RequestParam(required = false) RequisicaoPrioridade prioridade,
            @RequestParam(required = false) String criadoPorNome,
            @RequestParam(required = false) String dataInicio,
            @RequestParam(required = false) String dataFim) {
        return requisicaoService.procurar(estado, tipo, prioridade, criadoPorNome, dataInicio, dataFim);
    }

    @GetMapping("/{id}")
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
        return ResponseEntity.ok(requisicaoService.criarMaterialCatalogo(request));
    }

    @PutMapping("/materiais/{id}")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<Material> atualizarMaterialCatalogo(
            @PathVariable Long id,
            @Valid @RequestBody CriarMaterialRequest request) {
        return ResponseEntity.ok(requisicaoService.atualizarMaterialCatalogo(id, request));
    }

    @DeleteMapping("/materiais/{id}")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<Void> apagarMaterialCatalogo(@PathVariable Long id) {
        requisicaoService.apagarMaterialCatalogo(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/transportes")
    public List<Transporte> listarTransportes() {
        return requisicaoService.listarTransportes();
    }

    @PostMapping("/transportes")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<Transporte> criarTransporteCatalogo(@Valid @RequestBody CriarTransporteRequest request) {
        return ResponseEntity.ok(requisicaoService.criarTransporteCatalogo(request));
    }

    @PutMapping("/transportes/{id}")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<Transporte> atualizarTransporteCatalogo(
            @PathVariable Long id,
            @Valid @RequestBody CriarTransporteRequest request) {
        return ResponseEntity.ok(requisicaoService.atualizarTransporteCatalogo(id, request));
    }

    @DeleteMapping("/transportes/{id}")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<Void> apagarTransporteCatalogo(@PathVariable Long id) {
        requisicaoService.apagarTransporteCatalogo(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tipos-manutencao")
    public List<TipoManutencao> listarTiposManutencao() {
        return requisicaoService.listarTiposManutencao();
    }

    @PostMapping("/tipos-manutencao")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<TipoManutencao> criarTipoManutencao(@Valid @RequestBody CriarTipoManutencaoRequest request) {
        return ResponseEntity.ok(requisicaoService.criarTipoManutencao(request));
    }

    @PutMapping("/tipos-manutencao/{id}")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<TipoManutencao> atualizarTipoManutencao(
            @PathVariable Long id,
            @Valid @RequestBody CriarTipoManutencaoRequest request) {
        return ResponseEntity.ok(requisicaoService.atualizarTipoManutencao(id, request));
    }

    @DeleteMapping("/tipos-manutencao/{id}")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<Void> apagarTipoManutencao(@PathVariable Long id) {
        requisicaoService.apagarTipoManutencao(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/material")
    public ResponseEntity<Requisicao> criarMaterial(
            @Valid @RequestBody CriarRequisicaoMaterialRequest request,
            @AuthenticationPrincipal Utilizador utilizador) {
        return ResponseEntity.ok(requisicaoService.criarMaterial(request, utilizador.getId()));
    }

    @PostMapping("/transporte")
    public ResponseEntity<Requisicao> criarTransporte(
            @Valid @RequestBody CriarRequisicaoTransporteRequest request,
            @AuthenticationPrincipal Utilizador utilizador) {
        return ResponseEntity.ok(requisicaoService.criarTransporte(request, utilizador.getId()));
    }

    @PostMapping("/manutencao")
    public ResponseEntity<Requisicao> criarManutencao(
            @Valid @RequestBody CriarRequisicaoManutencaoRequest request,
            @AuthenticationPrincipal Utilizador utilizador) {
        return ResponseEntity.ok(requisicaoService.criarManutencao(request, utilizador.getId()));
    }

    @GetMapping("/manutencao-items")
    public List<pt.florinhas.requisicoes.domain.ManutencaoItem> listarManutencaoItems() {
        return requisicaoService.listarManutencaoItems();
    }

    @PostMapping("/manutencao-items")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<pt.florinhas.requisicoes.domain.ManutencaoItem> criarManutencaoItem(
            @Valid @RequestBody pt.florinhas.requisicoes.dto.CriarManutencaoItemRequest request) {
        return ResponseEntity.ok(requisicaoService.criarManutencaoItem(request));
    }

    @PutMapping("/manutencao-items/{id}")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<pt.florinhas.requisicoes.domain.ManutencaoItem> atualizarManutencaoItem(
            @PathVariable Long id,
            @Valid @RequestBody pt.florinhas.requisicoes.dto.CriarManutencaoItemRequest request) {
        return ResponseEntity.ok(requisicaoService.atualizarManutencaoItem(id, request));
    }

    @DeleteMapping("/manutencao-items/{id}")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<Void> apagarManutencaoItem(@PathVariable Long id) {
        requisicaoService.apagarManutencaoItem(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<Requisicao> atualizarEstado(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarEstadoRequisicaoRequest request,
            @AuthenticationPrincipal Utilizador utilizador) {
        return ResponseEntity.ok(requisicaoService.atualizarEstado(id, request.estado(), utilizador.getId()));
    }
}
