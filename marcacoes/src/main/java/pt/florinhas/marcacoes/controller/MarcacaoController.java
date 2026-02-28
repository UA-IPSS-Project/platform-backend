package pt.florinhas.marcacoes.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pt.florinhas.marcacoes.domain.EventoEstado;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.dto.AtualizarEstadoRequest;
import pt.florinhas.marcacoes.dto.CriarMarcacaoRequest;
import pt.florinhas.marcacoes.dto.CriarMarcacaoBalnearioRequest;
import pt.florinhas.marcacoes.dto.MarcacaoResponseDTO;
import pt.florinhas.marcacoes.dto.NotificarDocumentosRequest;
import pt.florinhas.marcacoes.service.AuthService;
import pt.florinhas.marcacoes.service.MarcacaoService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import lombok.RequiredArgsConstructor;

/**
 * Controller responsável pela gestão de marcações.
 *
 * Este controller expõe endpoints REST para:
 * - criação de marcações (presenciais, remotas e temporárias)
 * - consulta de agendas e históricos
 * - atualização do estado das marcações
 * - notificação de documentos inválidos
 * - consulta de marcações por utente ou funcionário
 */
@RestController
@RequestMapping("/api/marcacoes")
@RequiredArgsConstructor
public class MarcacaoController {

    /**
     * Serviço que contém toda a lógica de negócio relacionada com marcações.
     * O controller limita-se a validar pedidos e delegar a lógica no serviço.
     */
    private final MarcacaoService marcacaoService;

    /**
     * Serviço de autenticação para verificação de permissões.
     */
    private final AuthService authService;

    // =====================================================================
    // MÉTODOS AUXILIARES DE PERMISSÕES (DRY)
    // =====================================================================

    /**
     * Verifica se o utilizador atual tem permissão para aceder aos dados de um
     * utente.
     * Administradores podem aceder a qualquer utente.
     * Utentes normais só podem aceder aos seus próprios dados.
     *
     * @param targetUtenteId ID do utente alvo (pode ser null)
     * @return o ID do utente a usar na query (forçado para o atual se não for
     *         admin)
     * @throws AccessDeniedException se não tiver permissão
     */
    private Long verificarPermissaoUtente(Long targetUtenteId) {
        Long currentUserId = authService.getCurrentUserId();
        boolean isAdmin = authService.isAdmin();

        if (!isAdmin) {
            if (targetUtenteId != null && !targetUtenteId.equals(currentUserId)) {
                throw new AccessDeniedException("Não tem permissão para consultar dados de outro utente.");
            }
            return currentUserId; // Força o ID do utilizador atual
        }
        return targetUtenteId; // Admin pode usar qualquer ID
    }

    /**
     * Verifica se o utilizador atual (não-admin) tem permissão de aceder a um
     * recurso
     * baseado no ID do proprietário.
     *
     * @param ownerId      ID do proprietário do recurso
     * @param resourceType tipo de recurso para a mensagem de erro
     * @throws AccessDeniedException se não tiver permissão
     */
    private void verificarPermissaoProprietario(Long ownerId, String resourceType) {
        Long currentUserId = authService.getCurrentUserId();
        boolean isAdmin = authService.isAdmin();

        if (!isAdmin && (currentUserId == null || !currentUserId.equals(ownerId))) {
            throw new AccessDeniedException(
                    String.format("Não tem permissão para %s.", resourceType));
        }
    }

    /**
     * Endpoint para contar o número de marcações do dia atual.
     *
     * Usado tipicamente para dashboards ou indicadores rápidos no frontend (ex.:
     * número de atendimentos hoje).
     *
     * return número total de marcações no dia atual
     */
    @GetMapping("/count/hoje")
    public ResponseEntity<Long> contarMarcacoesHoje() {
        long count = marcacaoService.contarMarcacoesDiarias(LocalDateTime.now());
        return ResponseEntity.ok(count);
    }

    /**
     * Criação de uma marcação presencial.
     *
     * Este endpoint é normalmente usado pela secretaria,
     * quando o utente está fisicamente presente.
     *
     * param request DTO com os dados da marcação
     * return dados básicos da marcação criada ou erro
     */
    @PostMapping("/presencial")
    public ResponseEntity<Map<String, String>> criarMarcacaoPresencial(
            @Valid @RequestBody CriarMarcacaoRequest request) {

        Marcacao marcacao = marcacaoService.criarMarcacaoPresencial(request);

        return ResponseEntity.ok().body(Map.of(
                "id", marcacao.getId().toString(),
                "data", marcacao.getData().toString(),
                "estado", marcacao.getEstado().toString(),
                "message", "Marcação criada com sucesso"));
    }

    /**
     * Criação de uma marcação remota.
     *
     * Este endpoint é usado por utentes que efetuam o pedido
     * remotamente através da aplicação.
     *
     * param request DTO com os dados da marcação
     * return dados básicos da marcação criada ou erro
     */
    @PostMapping("/remota")
    public ResponseEntity<Map<String, String>> criarMarcacaoRemota(
            @Valid @RequestBody CriarMarcacaoRequest request) {

        Marcacao marcacao = marcacaoService.criarMarcacaoRemota(request);

        return ResponseEntity.ok().body(Map.of(
                "id", marcacao.getId().toString(),
                "data", marcacao.getData().toString(),
                "estado", marcacao.getEstado().toString(),
                "message", "Marcação criada com sucesso"));
    }

    /**
     * Criação de uma marcação específica para o Balneário.
     *
     * param request DTO com os dados do utente anónimo e necessidades de higiene.
     * return dados básicos da marcação criada
     */
    @PostMapping("/balneario")
    public ResponseEntity<Map<String, String>> criarMarcacaoBalneario(
            @Valid @RequestBody CriarMarcacaoBalnearioRequest request) {

        Marcacao marcacao = marcacaoService.criarMarcacaoBalneario(request);

        return ResponseEntity.ok().body(Map.of(
                "id", marcacao.getId().toString(),
                "data", marcacao.getData().toString(),
                "estado", marcacao.getEstado().toString(),
                "message", "Marcação de balneário registada com sucesso"));
    }

    /**
     * Atualiza os detalhes de serviços de uma marcação de balneário.
     */
    @PutMapping("/balneario/{id}/detalhes")
    public ResponseEntity<MarcacaoResponseDTO> atualizarDetalhesBalneario(
            @PathVariable Long id,
            @RequestBody CriarMarcacaoBalnearioRequest request) {

        MarcacaoResponseDTO updated = marcacaoService.atualizarDetalhesBalneario(
                id,
                request.getProdutosHigiene(),
                request.getLavagemRoupa(),
                request.getRoupas());

        return ResponseEntity.ok(updated);
    }

    /**
     * RF1.2.3 – Consulta da agenda geral.
     *
     * Permite consultar todas as marcações num intervalo temporal,
     * ou a totalidade das marcações caso os filtros não sejam fornecidos.
     *
     * param dataInicio data/hora inicial (opcional)
     * param dataFim data/hora final (opcional)
     * return lista de marcações no intervalo indicado
     */
    @GetMapping("/agenda")
    public ResponseEntity<List<MarcacaoResponseDTO>> consultarAgenda(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            @RequestParam(required = false) String tipo) {

        if (!authService.isAdmin()) {
            throw new AccessDeniedException("Acesso restrito a administradores/secretaria.");
        }

        List<MarcacaoResponseDTO> response = marcacaoService.consultarAgenda(dataInicio, dataFim, tipo);
        return ResponseEntity.ok(response);
    }

    /**
     * Consulta da agenda com filtros avançados.
     *
     * Permite filtrar marcações por:
     * - intervalo temporal
     * - criador da marcação
     * - utente
     * - estado da marcação
     *
     * return lista de marcações que cumprem os critérios
     */
    @GetMapping("/agenda/procurar")
    public ResponseEntity<List<MarcacaoResponseDTO>> procurarAgenda(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,

            @RequestParam(required = false) Long criadoPorId,
            @RequestParam(required = false) Long utenteId,
            @RequestParam(required = false) EventoEstado estado) {

        Long utenteIdFiltrado = verificarPermissaoUtente(utenteId);

        List<MarcacaoResponseDTO> response = marcacaoService.procurarAgenda(
                dataInicio, dataFim, criadoPorId, utenteIdFiltrado, estado);
        return ResponseEntity.ok(response);
    }

    /**
     * Atualização do estado de uma marcação.
     *
     * Exemplo de estados:
     * - CONFIRMADA
     * - CONCLUIDA
     * - CANCELADA
     *
     * param id identificador da marcação
     * param request DTO com o novo estado
     * return marcação atualizada
     */
    @PutMapping("/{id}/estado")
    public ResponseEntity<MarcacaoResponseDTO> atualizarEstadoMarcacao(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarEstadoRequest request) {

        // Validar permissões
        Long currentUserId = authService.getCurrentUserId();
        boolean isAdmin = authService.isAdmin();

        if (!isAdmin) {
            // Se não é admin, verificar se a marcação pertence ao utilizador
            MarcacaoResponseDTO existing = marcacaoService.obterMarcacaoDTO(id);
            if (existing != null && existing.getMarcacaoSecretaria() != null
                    && existing.getMarcacaoSecretaria().getUtente() != null) {

                Long ownerId = existing.getMarcacaoSecretaria().getUtente().getId();
                if (!ownerId.equals(currentUserId)) {
                    throw new AccessDeniedException("Não tem permissão para alterar esta marcação.");
                }
            } else if (existing != null) {
                // Se não tem dados de utente (estranho), por segurança bloqueamos se não for
                // admin
                throw new AccessDeniedException("Não tem permissão para alterar esta marcação.");
            }
            // Se existing == null, deixamos o serviço lidar com o 404 normal
        }

        MarcacaoResponseDTO response = marcacaoService.atualizarEstadoMarcacao(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Consulta do histórico de marcações (marcações passadas).
     *
     * Pode ser filtrado por:
     * - intervalo temporal
     * - utente
     * - estado
     */
    @GetMapping("/passadas")
    public ResponseEntity<List<MarcacaoResponseDTO>> consultarMarcacoesPassadas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,

            @RequestParam(required = false) Long utenteId,
            @RequestParam(required = false) EventoEstado estado) {

        Long utenteIdFiltrado = verificarPermissaoUtente(utenteId);

        List<MarcacaoResponseDTO> response = marcacaoService.consultarMarcacoesPassadas(
                dataInicio, dataFim, utenteIdFiltrado, estado);
        return ResponseEntity.ok(response);
    }

    /**
     * Notificação de documentos inválidos associados a uma marcação.
     *
     * Este endpoint permite informar o utente de que existem
     * documentos incorretos ou em falta.
     *
     * param id identificador da marcação
     * param request DTO com a mensagem/notificação
     * return marcação atualizada
     */
    @PutMapping("/{id}/notificar-documentos-invalidos")
    public ResponseEntity<MarcacaoResponseDTO> notificarDocumentosInvalidos(
            @PathVariable Long id,
            @RequestBody NotificarDocumentosRequest request) {

        MarcacaoResponseDTO response = marcacaoService.notificarDocumentosInvalidos(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Consulta de todas as marcações associadas a um utente.
     *
     * param utenteId identificador do utente
     * return lista de marcações do utente
     */
    @GetMapping("/utente/{utenteId}")
    public ResponseEntity<List<MarcacaoResponseDTO>> consultarMarcacoesUtente(
            @PathVariable Long utenteId) {

        verificarPermissaoProprietario(utenteId, "consultar dados de outro utente");

        List<MarcacaoResponseDTO> response = marcacaoService.consultarMarcacoesUtente(utenteId);
        return ResponseEntity.ok(response);
    }

    /**
     * Consulta de marcações bloqueadas por outros utentes.
     *
     * Usado para impedir conflitos de horários no frontend,
     * escondendo slots ocupados.
     *
     * param utenteId identificador do utente atual
     * return lista simplificada de marcações bloqueadas
     */
    @GetMapping("/utente/{utenteId}/bloqueadas")
    public ResponseEntity<List<Map<String, Object>>> consultarMarcacoesBloqueadas(
            @PathVariable Long utenteId) {

        verificarPermissaoProprietario(utenteId, "consultar dados de outro utente");

        List<Map<String, Object>> marcacoesBloqueadas = marcacaoService.consultarMarcacoesBloqueadas(utenteId);
        return ResponseEntity.ok(marcacoesBloqueadas);
    }

    /**
     * Consulta de todas as marcações associadas a um funcionário.
     *
     * param funcionarioId identificador do funcionário
     * return lista de marcações atribuídas ao funcionário
     */
    @GetMapping("/funcionario/{funcionarioId}")
    public ResponseEntity<List<MarcacaoResponseDTO>> consultarMarcacoesFuncionario(
            @PathVariable Long funcionarioId) {

        verificarPermissaoProprietario(funcionarioId, "consultar marcações deste funcionário");

        List<MarcacaoResponseDTO> response = marcacaoService.consultarMarcacoesFuncionario(funcionarioId);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtenção dos detalhes de uma marcação específica.
     *
     * param id identificador da marcação
     * return detalhes completos da marcação
     */
    @GetMapping("/{id}")
    public ResponseEntity<MarcacaoResponseDTO> obterMarcacao(
            @PathVariable Long id) {

        Long currentUserId = authService.getCurrentUserId();
        boolean isAdmin = authService.isAdmin();

        // Obter marcação primeiro para verificar ownership
        MarcacaoResponseDTO response = marcacaoService.obterMarcacaoDTO(id);

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        // Se não é admin, verificar se a marcação pertence ao utilizador
        if (!isAdmin) {
            Long ownerId = null;
            if (response.getMarcacaoSecretaria() != null
                    && response.getMarcacaoSecretaria().getUtente() != null) {
                ownerId = response.getMarcacaoSecretaria().getUtente().getId();
            }

            boolean isOwner = ownerId != null && ownerId.equals(currentUserId);

            if (!isOwner) {
                throw new AccessDeniedException("Não tem permissão para visualizar esta marcação.");
            }
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Listagem de todas as marcações do sistema.
     *
     * Endpoint de uso administrativo.
     *
     * return lista completa de marcações
     */
    @GetMapping
    public ResponseEntity<Page<MarcacaoResponseDTO>> listarTodasMarcacoes(
            @PageableDefault(size = 20, sort = "data") Pageable pageable) {

        if (!authService.isAdmin()) {
            throw new AccessDeniedException("Acesso restrito a administradores.");
        }
        Page<MarcacaoResponseDTO> response = marcacaoService.listarTodasMarcacoesPaginated(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Reserva temporária de um slot de agenda.
     *
     * Usada para evitar condições de corrida quando vários utentes
     * tentam marcar o mesmo horário ao mesmo tempo.
     *
     * A reserva fica com estado EM_PREENCHIMENTO e expira após um período definido
     *
     * param request dados da marcação
     * return identificador temporário da reserva
     */
    @PostMapping("/reservar-slot")
    public ResponseEntity<Map<String, Object>> reservarSlotTemporariamente(
            @RequestBody CriarMarcacaoRequest request) {

        Long tempId = marcacaoService.criarReservaTemporaria(request);
        return ResponseEntity.ok(
                Map.of("tempId", tempId, "message", "Slot bloqueado por 10 minutos"));
    }

    /**
     * Libertação de um slot reservado temporariamente.
     *
     * Usado quando o utilizador cancela o processo de marcação
     * ou quando o tempo de reserva expira.
     *
     * param id identificador da reserva temporária
     * return resposta vazia de sucesso
     */
    @DeleteMapping("/libertar-slot/{id}")
    public ResponseEntity<?> libertarSlot(@PathVariable Long id) {

        marcacaoService.apagarReservaTemporaria(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Reagendamento de uma marcação (alterar data/hora).
     */
    @PutMapping("/{id}/reagendar")
    public ResponseEntity<MarcacaoResponseDTO> reagendarMarcacao(
            @PathVariable Long id,
            @RequestBody pt.florinhas.marcacoes.dto.ReagendarMarcacaoRequest request) {

        MarcacaoResponseDTO response = marcacaoService.reagendarMarcacao(id, request);
        return ResponseEntity.ok(response);
    }
}
