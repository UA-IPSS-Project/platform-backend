package pt.florinhas.marcacoes.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pt.florinhas.marcacoes.domain.Utilizador;
import pt.florinhas.marcacoes.dto.UtilizadorInfoDTO;
import pt.florinhas.marcacoes.dto.UtilizadorResponseDTO;
import pt.florinhas.marcacoes.service.UtilizadorService;

/**
 * Controller responsável por operações de consulta e atualização de
 * Utilizadores.
 *
 * Endpoints principais:
 * - GET /api/utilizadores/{id}: obter utilizador por ID (formato DTO para o
 * frontend)
 * - GET /api/utilizadores/nif/{nif}: obter utilizador por NIF (objeto de
 * domínio)
 * - PUT /api/utilizadores/{id}: atualizar dados de perfil do utilizador
 * - GET /api/utilizadores/utentes/count: contar utentes ativos (métrica para
 * dashboards)
 *
 * Nota: a lógica de negócio reside em UtilizadorService; o controller apenas
 * valida,
 * delega e adapta respostas HTTP/DTO.
 */
@RestController
@RequestMapping("/api/utilizadores")

public class UtilizadorController {

    /**
     * Serviço de domínio responsável por ler/atualizar utilizadores.
     */
    @Autowired
    private UtilizadorService utilizadorService;

    /**
     * Obtém um utilizador por ID e devolve um DTO adequado ao consumo pelo
     * frontend.
     *
     * param id identificador único do utilizador
     * return 200 OK com UtilizadorResponseDTO; 404 se não for encontrado
     */
    // Buscar utilizador por ID
    @GetMapping("/{id}")
    public ResponseEntity<UtilizadorResponseDTO> obterUtilizadorPorId(@PathVariable Long id) {
        Utilizador utilizador = utilizadorService.obterUtilizadorPorId(id);
        UtilizadorResponseDTO response = UtilizadorResponseDTO.fromUtilizador(utilizador);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtém um utilizador por NIF.
     *
     * Atenção: este endpoint devolve o objeto de domínio completo (Utilizador).
     * Caso seja necessário ocultar campos sensíveis, considerar trocar para DTO.
     *
     * param nif NIF do utilizador a procurar
     * return 200 OK com Utilizador; 404 se não for encontrado
     */
    // Buscar utilizador por NIF
    @GetMapping("/nif/{nif}")
    public ResponseEntity<UtilizadorResponseDTO> buscarPorNif(@PathVariable String nif) {
        Utilizador utilizador = utilizadorService.buscarPorNif(nif)
                .orElseThrow(() -> new pt.florinhas.marcacoes.exception.NotFoundException(
                        "Utilizador não encontrado com NIF: " + nif));
        return ResponseEntity.ok(UtilizadorResponseDTO.fromUtilizador(utilizador));
    }

    /**
     * Atualiza os dados de perfil de um utilizador.
     *
     * O corpo (UtilizadorInfoDTO) deve conter apenas campos editáveis do perfil.
     * A lógica de validação e persistência é delegada ao serviço.
     *
     * param id identificador do utilizador a atualizar
     * param request DTO com os novos dados de perfil
     * return 200 OK com UtilizadorResponseDTO atualizado; 400 em erro de
     * validação/negócio
     */
    // Atualizar informações do utilizador
    @PutMapping("/{id}")
    public ResponseEntity<UtilizadorResponseDTO> atualizarUtilizador(
            @PathVariable Long id,
            @RequestBody UtilizadorInfoDTO request) {
        Utilizador utilizador = utilizadorService.atualizarUtilizador(id, request);
        UtilizadorResponseDTO response = UtilizadorResponseDTO.fromUtilizador(utilizador);
        return ResponseEntity.ok(response);
    }

    /**
     * Devolve o número de utentes ativos no sistema.
     * Útil para widgets/indicadores no dashboard administrativo.
     * return 200 OK com contagem de utentes ativos
     */
    // Obter número de utentes
    @GetMapping("/utentes/count")
    public ResponseEntity<Long> contarUtentes() {
        long count = utilizadorService.contarUtentesAtivos();
        return ResponseEntity.ok(count);
    }

    /**
     * Lista todos os funcionários (ativos e inativos).
     */
    @GetMapping("/funcionarios")
    public ResponseEntity<java.util.List<UtilizadorResponseDTO>> listarTodosFuncionarios() {
        return ResponseEntity.ok(utilizadorService.listarTodosFuncionarios());
    }

    /**
     * Lista os funcionários pendentes de aprovação.
     */
    @GetMapping("/funcionarios/pendentes")
    public ResponseEntity<java.util.List<UtilizadorResponseDTO>> listarFuncionariosPendentes() {
        return ResponseEntity.ok(utilizadorService.listarFuncionariosPendentes());
    }

    /**
     * Aprova um funcionário pendente.
     */
    @PutMapping("/{id}/aprovar")
    public ResponseEntity<Void> aprovarFuncionario(@PathVariable Long id) {
        utilizadorService.aprovarFuncionario(id);
        return ResponseEntity.ok().build();
    }
}
