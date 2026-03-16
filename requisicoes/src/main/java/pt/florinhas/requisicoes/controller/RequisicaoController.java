package pt.florinhas.requisicoes.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

import jakarta.validation.Valid;
import pt.florinhas.requisicoes.domain.Material;
import pt.florinhas.requisicoes.domain.Requisicao;
import pt.florinhas.requisicoes.domain.RequisicaoEstado;
import pt.florinhas.requisicoes.domain.RequisicaoPrioridade;
import pt.florinhas.requisicoes.domain.RequisicaoTipo;
import pt.florinhas.requisicoes.domain.TipoManutencao;
import pt.florinhas.requisicoes.domain.Transporte;
import pt.florinhas.requisicoes.domain.Utilizador;
import pt.florinhas.requisicoes.dto.AtualizarEstadoRequisicaoRequest;
import pt.florinhas.requisicoes.dto.CriarMaterialRequest;
import pt.florinhas.requisicoes.dto.CriarRequisicaoManutencaoRequest;
import pt.florinhas.requisicoes.dto.CriarRequisicaoMaterialRequest;
import pt.florinhas.requisicoes.dto.CriarRequisicaoTransporteRequest;
import pt.florinhas.requisicoes.dto.CriarTipoManutencaoRequest;
import pt.florinhas.requisicoes.dto.CriarTransporteRequest;
import pt.florinhas.requisicoes.service.RequisicaoService;

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
            @RequestParam(required = false) String geridoPorNome) {
        return requisicaoService.procurar(estado, tipo, prioridade, criadoPorNome, geridoPorNome);
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Material> criarMaterialCatalogo(@Valid @RequestBody CriarMaterialRequest request) {
        return ResponseEntity.ok(requisicaoService.criarMaterialCatalogo(request));
    }

    @PutMapping("/materiais/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Material> atualizarMaterialCatalogo(
            @PathVariable Long id,
            @Valid @RequestBody CriarMaterialRequest request) {
        return ResponseEntity.ok(requisicaoService.atualizarMaterialCatalogo(id, request));
    }

    @DeleteMapping("/materiais/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> apagarMaterialCatalogo(@PathVariable Long id) {
        requisicaoService.apagarMaterialCatalogo(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/transportes")
    public List<Transporte> listarTransportes() {
        return requisicaoService.listarTransportes();
    }

    @PostMapping("/transportes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Transporte> criarTransporteCatalogo(@Valid @RequestBody CriarTransporteRequest request) {
        return ResponseEntity.ok(requisicaoService.criarTransporteCatalogo(request));
    }

    @PutMapping("/transportes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Transporte> atualizarTransporteCatalogo(
            @PathVariable Long id,
            @Valid @RequestBody CriarTransporteRequest request) {
        return ResponseEntity.ok(requisicaoService.atualizarTransporteCatalogo(id, request));
    }

    @DeleteMapping("/transportes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> apagarTransporteCatalogo(@PathVariable Long id) {
        requisicaoService.apagarTransporteCatalogo(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tipos-manutencao")
    public List<TipoManutencao> listarTiposManutencao() {
        return requisicaoService.listarTiposManutencao();
    }

    @PostMapping("/tipos-manutencao")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TipoManutencao> criarTipoManutencao(@Valid @RequestBody CriarTipoManutencaoRequest request) {
        return ResponseEntity.ok(requisicaoService.criarTipoManutencao(request));
    }

    @PutMapping("/tipos-manutencao/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TipoManutencao> atualizarTipoManutencao(
            @PathVariable Long id,
            @Valid @RequestBody CriarTipoManutencaoRequest request) {
        return ResponseEntity.ok(requisicaoService.atualizarTipoManutencao(id, request));
    }

    @DeleteMapping("/tipos-manutencao/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> apagarTipoManutencao(@PathVariable Long id) {
        requisicaoService.apagarTipoManutencao(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/material")
    public ResponseEntity<Requisicao> criarMaterial(@Valid @RequestBody CriarRequisicaoMaterialRequest request) {
        return ResponseEntity.ok(requisicaoService.criarMaterial(request));
    }

    @PostMapping("/transporte")
    public ResponseEntity<Requisicao> criarTransporte(@Valid @RequestBody CriarRequisicaoTransporteRequest request) {
        return ResponseEntity.ok(requisicaoService.criarTransporte(request));
    }

    @PostMapping("/manutencao")
    public ResponseEntity<Requisicao> criarManutencao(@Valid @RequestBody CriarRequisicaoManutencaoRequest request) {
        return ResponseEntity.ok(requisicaoService.criarManutencao(request));
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
