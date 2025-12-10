package pt.florinhas.marcacoes.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.florinhas.marcacoes.domain.BloqueioAgenda;
import pt.florinhas.marcacoes.domain.Utilizador;
import pt.florinhas.marcacoes.dto.BloquearHorarioRequest;
import pt.florinhas.marcacoes.repository.UtilizadorRepository;
import pt.florinhas.marcacoes.service.CalendarioService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/calendario")
@RequiredArgsConstructor
public class CalendarioController {

    private final CalendarioService calendarioService;
    private final UtilizadorRepository utilizadorRepository;

    // POST: Criar Bloqueio (Parcial ou Dia todo)
    @PostMapping("/bloquear")
    public ResponseEntity<?> bloquearHorario(@RequestBody BloquearHorarioRequest request) {
        Utilizador admin = utilizadorRepository.findById(request.getFuncionarioId())
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));

        calendarioService.bloquearHorario(
                request.getData(), 
                request.getHoraInicio(), 
                request.getHoraFim(), 
                request.getMotivo(), 
                admin
        );
        
        return ResponseEntity.ok(Map.of("message", "Bloqueio registado com sucesso"));
    }

    // DELETE: Remover Bloqueio
    @DeleteMapping("/{id}")
    public ResponseEntity<?> removerBloqueio(@PathVariable Long id) {
        calendarioService.removerBloqueio(id);
        return ResponseEntity.ok(Map.of("message", "Bloqueio removido"));
    }

    // GET: Listar bloqueios (Para o frontend desenhar as caixas cinzentas)
    @GetMapping("/bloqueios")
    public ResponseEntity<List<BloqueioAgenda>> listarBloqueios(
            @RequestParam int ano,
            @RequestParam int mes) {
        return ResponseEntity.ok(calendarioService.getBloqueiosDoMes(ano, mes));
    }
    
    // GET: Endpoint rápido para verificar se um slot específico está livre (usado ao abrir o modal de nova marcação)
    @GetMapping("/verificar-slot")
    public ResponseEntity<Boolean> verificarSlot(
            @RequestParam String data,
            @RequestParam String hora) { // Formato HH:mm
            
        boolean bloqueado = calendarioService.isSlotBloqueado(
            LocalDate.parse(data), 
            java.time.LocalTime.parse(hora)
        );
        
        // Retorna TRUE se estiver bloqueado (indisponível)
        return ResponseEntity.ok(bloqueado);
    }
}