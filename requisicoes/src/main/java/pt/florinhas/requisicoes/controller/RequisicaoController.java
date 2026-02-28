package pt.florinhas.requisicoes.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import pt.florinhas.requisicoes.domain.Requisicao;
import pt.florinhas.requisicoes.domain.RequisicaoEstado;
import pt.florinhas.requisicoes.domain.RequisicaoPrioridade;
import pt.florinhas.requisicoes.domain.RequisicaoTipo;
import pt.florinhas.requisicoes.dto.CriarRequisicaoManutencaoRequest;
import pt.florinhas.requisicoes.dto.CriarRequisicaoMaterialRequest;
import pt.florinhas.requisicoes.dto.CriarRequisicaoTransporteRequest;
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
            @RequestParam(required = false) Long criadoPorId,
            @RequestParam(required = false) Long geridoPorId) {
        return requisicaoService.procurar(estado, tipo, prioridade, criadoPorId, geridoPorId);
    }

    @GetMapping("/{id}")
    public Requisicao obter(@PathVariable Long id) {
        return requisicaoService.obterPorId(id);
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
}
