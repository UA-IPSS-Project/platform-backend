package pt.florinhas.marcacoes.controller;

import java.time.LocalDate;
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

import pt.florinhas.marcacoes.domain.Funcionario;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.domain.MarcacaoBalneario;
import pt.florinhas.marcacoes.domain.Utente;
import pt.florinhas.marcacoes.domain.Valencia;
import pt.florinhas.marcacoes.dto.AtualizarConsumosRequest;
import pt.florinhas.marcacoes.dto.AtualizarEstadoRequest;
import pt.florinhas.marcacoes.dto.CancelarMarcacaoRequest;
import pt.florinhas.marcacoes.dto.ConfirmarPresencaRequest;
import pt.florinhas.marcacoes.dto.CriarBalnearioRequest;
import pt.florinhas.marcacoes.dto.CriarMarcacaoRequest;
import pt.florinhas.marcacoes.dto.NotificarDocumentosRequest;
import pt.florinhas.marcacoes.repository.FuncionarioRepository;
import pt.florinhas.marcacoes.repository.UtenteRepository;
import pt.florinhas.marcacoes.repository.ValenciaRepository;
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
    private ValenciaRepository valenciaRepository;

    // RF1.2.1 - Criar marcação presencial (Secretaria)
    @PostMapping("/presencial")
    public ResponseEntity<Marcacao> criarMarcacaoPresencial(@RequestBody CriarMarcacaoRequest request) {
        try {
            Utente utente = utenteRepository.findById(request.getUtenteId())
                    .orElseThrow(() -> new RuntimeException("Utente não encontrado"));
            Funcionario funcionario = funcionarioRepository.findById(request.getFuncionarioId())
                    .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));
            Valencia valencia = valenciaRepository.findById(request.getValenciaId())
                    .orElseThrow(() -> new RuntimeException("Valência não encontrada"));
            Funcionario criadoPor = funcionarioRepository.findById(request.getCriadoPorId())
                    .orElseThrow(() -> new RuntimeException("Funcionário (criado por) não encontrado"));

            Marcacao marcacao = marcacaoService.criarMarcacaoPresencial(
                    request.getData(), request.getHora(), request.getTipoAtendimento(),
                    utente, funcionario, valencia, criadoPor);

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
            Funcionario funcionario = funcionarioRepository.findById(request.getFuncionarioId())
                    .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));
            Valencia valencia = valenciaRepository.findById(request.getValenciaId())
                    .orElseThrow(() -> new RuntimeException("Valência não encontrada"));

            Marcacao marcacao = marcacaoService.criarMarcacaoRemota(
                    request.getData(), request.getHora(), request.getTipoAtendimento(),
                    utente, funcionario, valencia);

            return ResponseEntity.ok(marcacao);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // RF1.2.2 - Cancelar marcação
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<Marcacao> cancelarMarcacao(@PathVariable Long id, @RequestBody CancelarMarcacaoRequest request) {
        try {
            Funcionario canceladoPor = funcionarioRepository.findById(request.getFuncionarioId())
                    .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));

            marcacaoService.cancelarMarcacao(id, request.getMotivo(), canceladoPor);
            
            Marcacao marcacaoCancelada = marcacaoService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Marcação não encontrada"));
                    
            return ResponseEntity.ok(marcacaoCancelada);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // RF1.2.3 - Consultar agenda com filtros
    @GetMapping("/agenda")
    public ResponseEntity<List<Marcacao>> consultarAgenda(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) Long funcionarioId,
            @RequestParam(required = false) Long valenciaId,
            @RequestParam(required = false) String estado) {
        
        try {
            List<Marcacao> marcacoes = marcacaoService.consultarAgenda(dataInicio, dataFim, funcionarioId, valenciaId, estado);
            return ResponseEntity.ok(marcacoes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // RF1.2.4 - Atualizar estado da marcação
    @PutMapping("/{id}/estado")
    public ResponseEntity<Marcacao> atualizarEstadoMarcacao(@PathVariable Long id, @RequestBody AtualizarEstadoRequest request) {
        try {
            Funcionario atualizadoPor = funcionarioRepository.findById(request.getFuncionarioId())
                    .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));

            Marcacao marcacao = marcacaoService.atualizarEstadoMarcacao(id, request.getNovoEstado(), atualizadoPor);
            return ResponseEntity.ok(marcacao);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // RF1.2.14 - Consultar marcações passadas
    @GetMapping("/passadas")
    public ResponseEntity<List<Marcacao>> consultarMarcacoesPassadas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) Long utenteId,
            @RequestParam(required = false) String estado) {
        
        try {
            List<Marcacao> marcacoes = marcacaoService.consultarMarcacoesPassadas(dataInicio, dataFim, utenteId, estado);
            return ResponseEntity.ok(marcacoes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // RF2.1 e RF2.2 - Criar marcação de balneário
    @PostMapping("/balneario")
    public ResponseEntity<MarcacaoBalneario> criarMarcacaoBalneario(@RequestBody CriarBalnearioRequest request) {
        try {
            Utente utente = utenteRepository.findById(request.getUtenteId())
                    .orElseThrow(() -> new RuntimeException("Utente não encontrado"));
            Funcionario funcionario = funcionarioRepository.findById(request.getFuncionarioId())
                    .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));

            MarcacaoBalneario marcacao;
            
            if ("TECNICO".equals(request.getTipoCriador())) {
                marcacao = marcacaoService.criarMarcacaoBalnearioTecnico(
                        request.getData(), request.getHora(), utente, funcionario,
                        request.getProdutosHigiene(), request.getLavagemRoupa(), request.getRoupaDescricao());
            } else {
                marcacao = marcacaoService.criarMarcacaoBalnearioResponsavel(
                        request.getData(), request.getHora(), utente, funcionario,
                        request.getProdutosHigiene(), request.getLavagemRoupa(), request.getRoupaDescricao());
            }

            return ResponseEntity.ok(marcacao);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // RF2.3 - Confirmar presença no balneário
    @PutMapping("/balneario/{id}/confirmar-presenca")
    public ResponseEntity<MarcacaoBalneario> confirmarPresencaBalneario(@PathVariable Long id, @RequestBody ConfirmarPresencaRequest request) {
        try {
            Funcionario confirmadoPor = funcionarioRepository.findById(request.getFuncionarioId())
                    .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));

            marcacaoService.confirmarPresencaBalneario(id, request.getPresencaConfirmada(), confirmadoPor);
            
            MarcacaoBalneario marcacao = (MarcacaoBalneario) marcacaoService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Marcação não encontrada"));
                    
            return ResponseEntity.ok(marcacao);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // RF2.4 - Atualizar consumos do balneário
    @PutMapping("/balneario/{id}/consumos")
    public ResponseEntity<MarcacaoBalneario> atualizarConsumosBalneario(@PathVariable Long id, @RequestBody AtualizarConsumosRequest request) {
        try {
            Funcionario atualizadoPor = funcionarioRepository.findById(request.getFuncionarioId())
                    .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));

            MarcacaoBalneario marcacao = marcacaoService.atualizarConsumosBalneario(
                    id, request.getQuantidadeProdutos(), request.getQuantidadeRoupa(), 
                    request.getObservacoesConsumo(), atualizadoPor);

            return ResponseEntity.ok(marcacao);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // RF2.5 - Consultar marcações do balneário
    @GetMapping("/balneario")
    public ResponseEntity<List<MarcacaoBalneario>> consultarMarcacoesBalneario(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(required = false) String estado) {
        
        try {
            List<MarcacaoBalneario> marcacoes = marcacaoService.consultarMarcacoesBalneario(data, estado);
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