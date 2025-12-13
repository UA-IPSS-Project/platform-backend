package pt.florinhas.marcacoes.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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
import pt.florinhas.marcacoes.dto.MarcacaoResponseDTO;
import pt.florinhas.marcacoes.dto.NotificarDocumentosRequest;
import pt.florinhas.marcacoes.service.MarcacaoService;

/**
 * Controller responsável pela gestão de marcações.
 *
 * Este controller expõe endpoints REST para:
 *  - criação de marcações (presenciais, remotas e temporárias)
 *  - consulta de agendas e históricos
 *  - atualização do estado das marcações
 *  - notificação de documentos inválidos
 *  - consulta de marcações por utente ou funcionário
 */
@RestController
@RequestMapping("/api/marcacoes")
@CrossOrigin(origins = "*")
public class MarcacaoController {

    /**
     * Serviço que contém toda a lógica de negócio relacionada com marcações.
     * O controller limita-se a validar pedidos e delegar a lógica no serviço.
     */
    @Autowired
    private MarcacaoService marcacaoService;

    /**
     * Endpoint para contar o número de marcações do dia atual.
     *
     * Usado tipicamente para dashboards ou indicadores rápidos no frontend (ex.: número de atendimentos hoje).
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
    public ResponseEntity<?> criarMarcacaoPresencial(
            @RequestBody CriarMarcacaoRequest request) {

        try {
            Marcacao marcacao = marcacaoService.criarMarcacaoPresencial(request);

            return ResponseEntity.ok().body(Map.of(
                    "id", marcacao.getId(),
                    "data", marcacao.getData().toString(),
                    "estado", marcacao.getEstado().toString(),
                    "message", "Marcação criada com sucesso"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
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
    public ResponseEntity<?> criarMarcacaoRemota(
            @RequestBody CriarMarcacaoRequest request) {

        try {
            Marcacao marcacao = marcacaoService.criarMarcacaoRemota(request);

            return ResponseEntity.ok().body(Map.of(
                    "id", marcacao.getId(),
                    "data", marcacao.getData().toString(),
                    "estado", marcacao.getEstado().toString(),
                    "message", "Marcação criada com sucesso"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
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
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime dataInicio,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime dataFim) {

        try {
            List<MarcacaoResponseDTO> response =
                    marcacaoService.consultarAgenda(dataInicio, dataFim);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Consulta da agenda com filtros avançados.
     *
     * Permite filtrar marcações por:
     *  - intervalo temporal
     *  - criador da marcação
     *  - utente
     *  - estado da marcação
     *
     * return lista de marcações que cumprem os critérios
     */
    @GetMapping("/agenda/procurar")
    public ResponseEntity<List<MarcacaoResponseDTO>> procurarAgenda(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime dataInicio,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime dataFim,

            @RequestParam(required = false) Long criadoPorId,
            @RequestParam(required = false) Long utenteId,
            @RequestParam(required = false) EventoEstado estado) {

        try {
            List<MarcacaoResponseDTO> response =
                    marcacaoService.procurarAgenda(
                            dataInicio, dataFim, criadoPorId, utenteId, estado
                    );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Atualização do estado de uma marcação.
     *
     * Exemplo de estados:
     *  - CONFIRMADA
     *  - CONCLUIDA
     *  - CANCELADA
     *
     * param id identificador da marcação
     * param request DTO com o novo estado
     * return marcação atualizada
     */
    @PutMapping("/{id}/estado")
    public ResponseEntity<MarcacaoResponseDTO> atualizarEstadoMarcacao(
            @PathVariable Long id,
            @RequestBody AtualizarEstadoRequest request) {

        try {
            MarcacaoResponseDTO response =
                    marcacaoService.atualizarEstadoMarcacao(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Consulta do histórico de marcações (marcações passadas).
     *
     * Pode ser filtrado por:
     *  - intervalo temporal
     *  - utente
     *  - estado
     */
    @GetMapping("/passadas")
    public ResponseEntity<List<MarcacaoResponseDTO>> consultarMarcacoesPassadas(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime dataInicio,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime dataFim,

            @RequestParam(required = false) Long utenteId,
            @RequestParam(required = false) EventoEstado estado) {

        try {
            List<MarcacaoResponseDTO> response =
                    marcacaoService.consultarMarcacoesPassadas(
                            dataInicio, dataFim, utenteId, estado
                    );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
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

        try {
            MarcacaoResponseDTO response =
                    marcacaoService.notificarDocumentosInvalidos(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
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

        try {
            List<MarcacaoResponseDTO> response =
                    marcacaoService.consultarMarcacoesUtente(utenteId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
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

        try {
            List<Map<String, Object>> marcacoesBloqueadas =
                    marcacaoService.consultarMarcacoesBloqueadas(utenteId);
            return ResponseEntity.ok(marcacoesBloqueadas);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
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

        try {
            List<MarcacaoResponseDTO> response =
                    marcacaoService.consultarMarcacoesFuncionario(funcionarioId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
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

        try {
            MarcacaoResponseDTO response =
                    marcacaoService.obterMarcacaoDTO(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Listagem de todas as marcações do sistema.
     *
     * Endpoint de uso administrativo.
     *
     * return lista completa de marcações
     */
    @GetMapping
    public ResponseEntity<List<MarcacaoResponseDTO>> listarTodasMarcacoes() {

        try {
            List<MarcacaoResponseDTO> response =
                    marcacaoService.listarTodasMarcacoesDTO();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
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
    public ResponseEntity<?> reservarSlotTemporariamente(
            @RequestBody CriarMarcacaoRequest request) {

        try {
            Long tempId = marcacaoService.criarReservaTemporaria(request);
            return ResponseEntity.ok(
                    Map.of("tempId", tempId, "message", "Slot bloqueado por 10 minutos")
            );
        } catch (Exception e) {
            // Caso o slot já esteja ocupado
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(e.getMessage());
        }
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
}
