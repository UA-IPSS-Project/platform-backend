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

@RestController
@RequestMapping("/api/marcacoes")
@CrossOrigin(origins = "*")
public class MarcacaoController {

    @Autowired
    private MarcacaoService marcacaoService;

    // Contar marcações do dia
    @GetMapping("/count/hoje")
    public ResponseEntity<Long> contarMarcacoesHoje() {
        long count = marcacaoService.contarMarcacoesDiarias(LocalDateTime.now());
        return ResponseEntity.ok(count);
    }

    // Criar marcação presencial (Secretaria)
    @PostMapping("/presencial")
    public ResponseEntity<?> criarMarcacaoPresencial(@RequestBody CriarMarcacaoRequest request) {
        try {
            Marcacao marcacao = marcacaoService.criarMarcacaoPresencial(request);
            
            return ResponseEntity.ok().body(java.util.Map.of(
                "id", marcacao.getId(),
                "data", marcacao.getData().toString(),
                "estado", marcacao.getEstado().toString(),
                "message", "Marcação criada com sucesso"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                "error", e.getMessage()
            ));
        }
    }

    // Criar marcação remota (Utente)
    @PostMapping("/remota")
    public ResponseEntity<?> criarMarcacaoRemota(@RequestBody CriarMarcacaoRequest request) {
        try {
            Marcacao marcacao = marcacaoService.criarMarcacaoRemota(request);
            
            return ResponseEntity.ok().body(java.util.Map.of(
                "id", marcacao.getId(),
                "data", marcacao.getData().toString(),
                "estado", marcacao.getEstado().toString(),
                "message", "Marcação criada com sucesso"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                "error", e.getMessage()
            ));
        }
    }

    // RF1.2.3 - Consultar agenda (todas as marcações)
    @GetMapping("/agenda")
    public ResponseEntity<List<MarcacaoResponseDTO>> consultarAgenda(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim) {
        
        try {
            List<MarcacaoResponseDTO> response = marcacaoService.consultarAgendaDTO(dataInicio, dataFim);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Procurar agenda com filtros
    @GetMapping("/agenda/procurar")
    public ResponseEntity<List<MarcacaoResponseDTO>> procurarAgenda(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            @RequestParam(required = false) Long criadoPorId,
            @RequestParam(required = false) Long utenteId,
            @RequestParam(required = false) EventoEstado estado) {
        
        try {
            List<MarcacaoResponseDTO> response = marcacaoService.procurarAgendaDTO(dataInicio, dataFim, criadoPorId, utenteId, estado);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Atualizar estado da marcação
    @PutMapping("/{id}/estado")
    public ResponseEntity<MarcacaoResponseDTO> atualizarEstadoMarcacao(@PathVariable Long id, @RequestBody AtualizarEstadoRequest request) {
        try {
            MarcacaoResponseDTO response = marcacaoService.atualizarEstadoMarcacaoDTO(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Consultar marcações passadas (histórico)
    @GetMapping("/passadas")
    public ResponseEntity<List<MarcacaoResponseDTO>> consultarMarcacoesPassadas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            @RequestParam(required = false) Long utenteId,
            @RequestParam(required = false) EventoEstado estado) {
        
        try {
            List<MarcacaoResponseDTO> response = marcacaoService.consultarMarcacoesPassadasDTO(dataInicio, dataFim, utenteId, estado);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Notificar documentos inválidos
    @PutMapping("/{id}/notificar-documentos-invalidos")
    public ResponseEntity<MarcacaoResponseDTO> notificarDocumentosInvalidos(@PathVariable Long id, @RequestBody NotificarDocumentosRequest request) {
        try {
            MarcacaoResponseDTO response = marcacaoService.notificarDocumentosInvalidosDTO(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Consultar marcações de um utente
    @GetMapping("/utente/{utenteId}")
    public ResponseEntity<List<MarcacaoResponseDTO>> consultarMarcacoesUtente(@PathVariable Long utenteId) {
        try {
            List<MarcacaoResponseDTO> response = marcacaoService.consultarMarcacoesUtenteDTO(utenteId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Consultar marcações bloqueadas (de outros utentes)
    @GetMapping("/utente/{utenteId}/bloqueadas")
    public ResponseEntity<List<Map<String, Object>>> consultarMarcacoesBloqueadas(@PathVariable Long utenteId) {
        try {
            List<Map<String, Object>> marcacoesBloqueadas = marcacaoService.consultarMarcacoesBloqueadas(utenteId);
            return ResponseEntity.ok(marcacoesBloqueadas);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Consultar marcações de um funcionário
    @GetMapping("/funcionario/{funcionarioId}")
    public ResponseEntity<List<MarcacaoResponseDTO>> consultarMarcacoesFuncionario(@PathVariable Long funcionarioId) {
        try {
            List<MarcacaoResponseDTO> response = marcacaoService.consultarMarcacoesFuncionarioDTO(funcionarioId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Obter detalhes de uma marcação específica
    @GetMapping("/{id}")
    public ResponseEntity<MarcacaoResponseDTO> obterMarcacao(@PathVariable Long id) {
        try {
            MarcacaoResponseDTO response = marcacaoService.obterMarcacaoDTO(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Listar todas as marcações
    @GetMapping
    public ResponseEntity<List<MarcacaoResponseDTO>> listarTodasMarcacoes() {
        try {
            List<MarcacaoResponseDTO> response = marcacaoService.listarTodasMarcacoesDTO();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }


    @PostMapping("/reservar-slot")
    public ResponseEntity<?> reservarSlotTemporariamente(@RequestBody CriarMarcacaoRequest request) {
        try {
            // O service vai verificar se JÁ existe alguém nesse horário
            // Se estiver livre, cria uma marcação com estado EM_PREENCHIMENTO
            Long tempId = marcacaoService.criarReservaTemporaria(request);
            return ResponseEntity.ok(Map.of("tempId", tempId, "message", "Slot bloqueado por 10 minutos"));
        } catch (Exception e) {
            // Se já estiver ocupado (por reserva real ou temporária de outro), devolve 409 Conflict
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @DeleteMapping("/libertar-slot/{id}")
    public ResponseEntity<?> libertarSlot(@PathVariable Long id) {
        marcacaoService.apagarReservaTemporaria(id);
        return ResponseEntity.ok().build();
    }

}