package pt.florinhas.marcacoes.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pt.florinhas.marcacoes.domain.EventoEstado;
import pt.florinhas.marcacoes.domain.Funcionario;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.domain.Utente;
import pt.florinhas.marcacoes.domain.Utilizador;
import pt.florinhas.marcacoes.repository.UtilizadorRepository;
import pt.florinhas.marcacoes.dto.AtualizarEstadoRequest;
import pt.florinhas.marcacoes.dto.CancelarMarcacaoRequest;
import pt.florinhas.marcacoes.dto.CriarMarcacaoRequest;
import pt.florinhas.marcacoes.dto.NotificarDocumentosRequest;
import pt.florinhas.marcacoes.repository.FuncionarioRepository;
import pt.florinhas.marcacoes.repository.UtenteRepository;
import pt.florinhas.marcacoes.service.MarcacaoService;

@RestController
@RequestMapping("/api/marcacoes")
@CrossOrigin(origins = "*")
public class MarcacaoController {

    @Autowired
    private MarcacaoService marcacaoService;
    
    @Autowired
    private UtenteRepository utenteRepository;
    
    @Autowired
    private FuncionarioRepository funcionarioRepository;
    
    @Autowired
    private UtilizadorRepository utilizadorRepository;

    // RF1.2.1 - Criar marcação presencial (Secretaria)
    @PostMapping("/presencial")
    public ResponseEntity<Marcacao> criarMarcacaoPresencial(@RequestBody CriarMarcacaoRequest request) {
        try {
            Utente utente = utenteRepository.findById(request.getUtenteId())
                    .orElseThrow(() -> new RuntimeException("Utente não encontrado"));
            Funcionario criadoPor = funcionarioRepository.findById(request.getCriadoPorId())
                    .orElseThrow(() -> new RuntimeException("Funcionário (criado por) não encontrado"));

            Marcacao marcacao = marcacaoService.criarMarcacaoPresencial(
                    request.getData(), request.getAssunto(),
                    utente, criadoPor);

            return ResponseEntity.ok(marcacao);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // RF1.2.10 - Criar marcação remota (Utente)
    @PostMapping("/remota")
    public ResponseEntity<Marcacao> criarMarcacaoRemota(@RequestBody CriarMarcacaoRequest request) {
        try {
            Utente utente = utenteRepository.findById(request.getUtenteId())
                    .orElseThrow(() -> new RuntimeException("Utente não encontrado"));

            Marcacao marcacao = marcacaoService.criarMarcacaoRemota(
                    request.getData(), request.getAssunto(),
                    utente);

            return ResponseEntity.ok(marcacao);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // RF1.2.2 - Cancelar marcação
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<Marcacao> cancelarMarcacao(@PathVariable Long id, @RequestBody CancelarMarcacaoRequest request) {
        try {
            Utilizador canceladoPor = utilizadorRepository.findById(request.getFuncionarioId())
                    .orElseThrow(() -> new RuntimeException("Utilizador não encontrado"));

            marcacaoService.cancelarMarcacao(id, request.getMotivo(), canceladoPor);
            
            Marcacao marcacaoCancelada = marcacaoService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Marcação não encontrada"));
                    
            return ResponseEntity.ok(marcacaoCancelada);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // RF1.2.3 - Consultar agenda (todas as marcações)
    @GetMapping("/agenda")
    public ResponseEntity<List<Marcacao>> consultarAgenda(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim) {
        
        try {
            List<Marcacao> marcacoes = marcacaoService.consultarAgenda(dataInicio, dataFim);
            return ResponseEntity.ok(marcacoes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Procurar agenda com filtros
    @GetMapping("/agenda/procurar")
    public ResponseEntity<List<Marcacao>> procurarAgenda(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            @RequestParam(required = false) Long criadoPorId,
            @RequestParam(required = false) Long utenteId,
            @RequestParam(required = false) EventoEstado estado) {
        
        try {
            List<Marcacao> marcacoes = marcacaoService.procurarAgenda(dataInicio, dataFim, criadoPorId, utenteId, estado);
            return ResponseEntity.ok(marcacoes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // RF1.2.4 - Atualizar estado da marcação
    @PutMapping("/{id}/estado")
    public ResponseEntity<Marcacao> atualizarEstadoMarcacao(@PathVariable Long id, @RequestBody AtualizarEstadoRequest request) {
        try {
            Utilizador atualizadoPor = utilizadorRepository.findById(request.getFuncionarioId())
                    .orElseThrow(() -> new RuntimeException("Utilizador não encontrado"));

            Marcacao marcacao = marcacaoService.atualizarEstadoMarcacao(id, request.getNovoEstado(), atualizadoPor);
            return ResponseEntity.ok(marcacao);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // RF1.2.14 - Consultar marcações passadas
    @GetMapping("/passadas")
    public ResponseEntity<List<Marcacao>> consultarMarcacoesPassadas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            @RequestParam(required = false) Long utenteId,
            @RequestParam(required = false) EventoEstado estado) {
        
        try {
            List<Marcacao> marcacoes = marcacaoService.consultarMarcacoesPassadas(dataInicio, dataFim, utenteId, estado);
            return ResponseEntity.ok(marcacoes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // RF1.2.11 - Notificar documentos inválidos
    @PutMapping("/{id}/notificar-documentos-invalidos")
    public ResponseEntity<Marcacao> notificarDocumentosInvalidos(@PathVariable Long id, @RequestBody NotificarDocumentosRequest request) {
        try {
            Funcionario notificadoPor = funcionarioRepository.findById(request.getFuncionarioId())
                    .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));

            marcacaoService.notificarDocumentosInvalidos(id, request.getObservacoes(), notificadoPor);
            
            Marcacao marcacao = marcacaoService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Marcação não encontrada"));
                    
            return ResponseEntity.ok(marcacao);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Consultar marcações de um utente
    @GetMapping("/utente/{utenteId}")
    public ResponseEntity<List<Marcacao>> consultarMarcacoesUtente(@PathVariable Long utenteId) {
        try {
            Utente utente = utenteRepository.findById(utenteId)
                    .orElseThrow(() -> new RuntimeException("Utente não encontrado"));
            
            List<Marcacao> marcacoes = marcacaoService.consultarMarcacoesUtente(utente);
            return ResponseEntity.ok(marcacoes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Consultar marcações de um funcionário
    @GetMapping("/funcionario/{funcionarioId}")
    public ResponseEntity<List<Marcacao>> consultarMarcacoesFuncionario(@PathVariable Long funcionarioId) {
        try {
            Funcionario funcionario = funcionarioRepository.findById(funcionarioId)
                    .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));
            
            List<Marcacao> marcacoes = marcacaoService.consultarMarcacoesFuncionario(funcionario);
            return ResponseEntity.ok(marcacoes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Obter detalhes de uma marcação específica
    @GetMapping("/{id}")
    public ResponseEntity<Marcacao> obterMarcacao(@PathVariable Long id) {
        try {
            Marcacao marcacao = marcacaoService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Marcação não encontrada"));
            return ResponseEntity.ok(marcacao);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Listar todas as marcações
    @GetMapping
    public ResponseEntity<List<Marcacao>> listarTodasMarcacoes() {
        try {
            List<Marcacao> marcacoes = marcacaoService.findAll();
            return ResponseEntity.ok(marcacoes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}