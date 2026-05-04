package pt.florinhas.marcacoes.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import pt.florinhas.marcacoes.service.AuthorizationService;

import pt.florinhas.marcacoes.dto.CreateUserRequestDTO;
import pt.florinhas.marcacoes.dto.RecoverAccountDTO;
import pt.florinhas.marcacoes.dto.TermsStatusDTO;
import pt.florinhas.marcacoes.exception.NotFoundException;
import pt.florinhas.marcacoes.service.AuditLogService;
import pt.florinhas.marcacoes.service.TermsService;
import pt.florinhas.marcacoes.service.UtilizadorService;

import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.common_data.dto.UtilizadorInfoDTO;
import pt.florinhas.common_data.dto.UtilizadorResponseDTO;

import jakarta.validation.Valid;

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

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private TermsService termsService;

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
        authorizationService.checkPermission(id, "visualizar este perfil");
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
    @PreAuthorize("hasRole('SECRETARIA') or hasRole('BALNEARIO')")
    public ResponseEntity<UtilizadorResponseDTO> buscarPorNif(@PathVariable String nif) {
        Utilizador utilizador = utilizadorService.buscarPorNif(nif)
                .orElseThrow(() -> new NotFoundException(
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
    @PreAuthorize("hasAnyRole('SECRETARIA', 'FUNCIONARIO')")
    public ResponseEntity<List<UtilizadorResponseDTO>> listarTodosFuncionarios() {
        return ResponseEntity.ok(utilizadorService.listarTodosFuncionarios());
    }

    /**
     * Lista todos os utentes (ativos e inativos).
     */
    @GetMapping("/utentes")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<List<UtilizadorResponseDTO>> listarTodosUtentes() {
        return ResponseEntity.ok(utilizadorService.listarTodosUtentes());
    }

    /**
     * Lista os funcionários pendentes de aprovação.
     */
    @GetMapping("/funcionarios/pendentes")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<List<UtilizadorResponseDTO>> listarFuncionariosPendentes() {
        return ResponseEntity.ok(utilizadorService.listarFuncionariosPendentes());
    }

    /**
     * Aprova um funcionário pendente.
     */
    @PutMapping("/{id}/aprovar")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<Void> aprovarFuncionario(@PathVariable Long id) {
        utilizadorService.aprovarFuncionario(id);
        return ResponseEntity.ok().build();
    }

    /*
     * =========================================================
     * ENDPOINTS DE GESTÃO PELA SECRETARIA (Novos)
     * =========================================================
     */

    /**
     * Pesquisa utilizador por NIF para recuperação de conta.
     * Devolve DTO com dados seguros para confirmação de identidade.
     */
    @GetMapping("/recovery/search/{nif}")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<UtilizadorResponseDTO> pesquisarPorNifParaRecuperacao(@PathVariable String nif) {
        // Reutiliza buscarPorNif do serviço, que valida formato e existência
        // Se serviço lançar exceção, deve ser tratado globalmente ou aqui
        Utilizador utilizador = utilizadorService.buscarPorNif(nif)
                .orElseThrow(
                        () -> new NotFoundException("Utilizador não encontrado"));
        return ResponseEntity.ok(UtilizadorResponseDTO.fromUtilizador(utilizador));
    }

    /**
     * Cria conta (Utente/Funcionario) pela Secretaria.
     */
    @PostMapping("/create-by-secretary")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<UtilizadorResponseDTO> criarPelaSecretaria(
            @Valid @RequestBody CreateUserRequestDTO request) {
        Utilizador criado = utilizadorService.criarUtilizadorPelaSecretaria(request);
        return ResponseEntity.ok(UtilizadorResponseDTO.fromUtilizador(criado));
    }

    /**
     * Recupera conta (Reset password + Atualização de dados) pela Secretaria.
     */
    @PostMapping("/recover")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<Void> recuperarConta(
            @Valid @RequestBody RecoverAccountDTO request) {
        utilizadorService.recuperarConta(request);
        return ResponseEntity.ok().build();
    }

    /**
     * Solicita eliminação de conta (RGPD Art.º 17 - Direito ao Esquecimento).
     * Marca flag na BD e notifica secretaria para processar anonimização.
     */
    @PostMapping("/me/delete-request")
    public ResponseEntity<Void> solicitarEliminacaoConta() {
        utilizadorService.solicitarEliminacaoConta();
        return ResponseEntity.ok().build();
    }

    /**
     * Anonimiza dados de um utilizador (RGPD Art.º 17).
     * Substitui dados pessoais por valores genéricos mantendo registos históricos.
     * Apenas secretaria pode executar.
     */
    @PostMapping("/{id}/anonimizar")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<Void> anonimizarUtilizador(@PathVariable Long id) {
        utilizadorService.anonimizarUtilizador(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Anonimiza e desativa um utilizador (RGPD Art.º 17).
     * O registo é mantido para preservar integridade referencial do histórico.
     * Apenas secretaria pode executar.
     */
    @DeleteMapping("/{id}/anonimizar-eliminar")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<Void> anonimizarEEliminarUtilizador(@PathVariable Long id) {
        utilizadorService.anonimizarEEliminarUtilizador(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Exporta todos os dados pessoais do utilizador (RGPD Art.º 20 - Direito de Portabilidade).
     * Retorna JSON com dados de utilizador, documentos, marcações e requisições.
     */
    @GetMapping("/me/export")
    public ResponseEntity<Map<String, Object>> exportarDados() {
        Map<String, Object> dados = utilizadorService.exportarDadosUtilizador();
        return ResponseEntity.ok(dados);
    }

    // =========================================================
    // TERMOS DE USO — VERSIONAMENTO (RGPD)
    // =========================================================

    /**
     * Verifica se o utilizador autenticado precisa de re-aceitar os termos.
     */
    @GetMapping("/me/terms-status")
    public ResponseEntity<TermsStatusDTO> verificarTermos() {
        Utilizador user = utilizadorService.getUtilizadorAutenticado();
        return ResponseEntity.ok(termsService.getStatus(user));
    }

    /**
     * Regista a aceitação dos termos pelo utilizador autenticado.
     */
    @PostMapping("/me/accept-terms")
    public ResponseEntity<Void> aceitarTermos(@RequestParam int version) {
        Utilizador user = utilizadorService.getUtilizadorAutenticado();
        termsService.acceptTerms(user, version);
        return ResponseEntity.ok().build();
    }

    /**
     * Atualiza a versão dos termos e notifica todos os utilizadores por email.
     * Apenas secretaria.
     */
    @PostMapping("/admin/terms-version")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<Void> atualizarVersaoTermos(
            @RequestParam int newVersion,
            @RequestParam(required = false) String changeDescription) {
        termsService.updateTermsVersion(newVersion, changeDescription);
        return ResponseEntity.ok().build();
    }

    /**
     * Obtém o conteúdo dos termos para um idioma específico (Público/Autenticado).
     */
    @GetMapping("/terms-content")
    public ResponseEntity<Map<String, String>> obterConteudoTermosPublico(@RequestParam String lang) {
        String content = termsService.getTermsContent(lang);
        return ResponseEntity.ok(Map.of("content", content));
    }

    /**
     * Obtém o conteúdo dos termos para um idioma específico (Secretaria).
     */
    @GetMapping("/admin/terms-content")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<Map<String, String>> obterConteudoTermos(@RequestParam String lang) {
        String content = termsService.getTermsContent(lang);
        return ResponseEntity.ok(Map.of("content", content));
    }

    /**
     * Atualiza o conteúdo dos termos para um idioma específico.
     */
    @PutMapping("/admin/terms-content")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<Void> atualizarConteudoTermos(
            @RequestParam String lang,
            @RequestBody Map<String, String> body) {
        String content = body.get("content");
        termsService.updateTermsContent(lang, content);
        return ResponseEntity.ok().build();
    }
}
