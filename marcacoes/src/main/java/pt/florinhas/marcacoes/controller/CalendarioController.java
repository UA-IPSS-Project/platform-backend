package pt.florinhas.marcacoes.controller;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pt.florinhas.marcacoes.domain.BloqueioAgenda;
import pt.florinhas.marcacoes.domain.Utilizador;
import pt.florinhas.marcacoes.dto.AtualizarConfiguracaoSlotRequest;
import pt.florinhas.marcacoes.dto.BloquearHorarioRequest;
import pt.florinhas.marcacoes.dto.ConfiguracaoSlotDTO;
import pt.florinhas.marcacoes.repository.UtilizadorRepository;
import pt.florinhas.marcacoes.service.CalendarioService;
import pt.florinhas.marcacoes.exception.NotFoundException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller responsável pela gestão do calendário e dos bloqueios de agenda.
 *
 * Permite:
 * - bloquear horários específicos ou dias completos
 * - remover bloqueios existentes
 * - listar bloqueios por mês (para visualização no frontend)
 * - verificar rapidamente se um slot está disponível
 */
@RestController
@RequestMapping("/api/calendario")
@RequiredArgsConstructor
public class CalendarioController {

        private final CalendarioService calendarioService;
        private final UtilizadorRepository utilizadorRepository;

        /**
         * Endpoint para criar um bloqueio de agenda.
         * Acessível a funcionários de SECRETARIA e BALNEARIO.
         */
        @PostMapping("/bloquear")
        @PreAuthorize("hasAnyRole('SECRETARIA', 'BALNEARIO')")
        public ResponseEntity<?> bloquearHorario(@RequestBody BloquearHorarioRequest request) {

                Utilizador funcionario = utilizadorRepository.findById(request.getFuncionarioId())
                                .orElseThrow(() -> new NotFoundException("Funcionário não encontrado"));

                String tipo = request.getTipo() != null ? request.getTipo() : "SECRETARIA";

                calendarioService.bloquearHorario(
                                request.getData(),
                                request.getHoraInicio(),
                                request.getHoraFim(),
                                request.getMotivo(),
                                funcionario,
                                tipo);

                return ResponseEntity.ok(
                                Map.of("message", "Bloqueio registado com sucesso"));
        }

        /**
         * Endpoint para remover um bloqueio de agenda existente.
         * Acessível a funcionários de SECRETARIA e BALNEARIO.
         */
        @DeleteMapping("/{id}")
        @PreAuthorize("hasAnyRole('SECRETARIA', 'BALNEARIO')")
        public ResponseEntity<?> removerBloqueio(@PathVariable Long id) {
                calendarioService.removerBloqueio(id);
                return ResponseEntity.ok(
                                Map.of("message", "Bloqueio removido"));
        }

        /**
         * Endpoint para listar bloqueios, opcionalmente filtrados por mês e tipo.
         */
        @GetMapping("/bloqueios")
        public ResponseEntity<List<BloqueioAgenda>> listarBloqueios(
                        @RequestParam(required = false) Integer ano,
                        @RequestParam(required = false) Integer mes,
                        @RequestParam(required = false) String tipo) {

                if (ano != null && mes != null) {
                        return ResponseEntity.ok(
                                        calendarioService.getBloqueiosDoMes(ano, mes, tipo));
                }

                return ResponseEntity.ok(calendarioService.getTodosBloqueios(tipo));
        }

        /**
         * Endpoint para listar feriados de um ano.
         */
        @GetMapping("/feriados")
        public ResponseEntity<List<String>> listarFeriados(@RequestParam Integer ano) {
                if (ano == null) {
                        return ResponseEntity.ok(List.of());
                }

                List<String> dates = calendarioService.getFeriadosDoAno(ano).stream()
                                .map(LocalDate::toString)
                                .collect(Collectors.toList());
                return ResponseEntity.ok(dates);
        }

        /**
         * Endpoint para verificar se um slot está bloqueado.
         */
        @GetMapping("/verificar-slot")
        public ResponseEntity<Boolean> verificarSlot(
                        @RequestParam String data,
                        @RequestParam String hora,
                        @RequestParam(required = false) String tipo) {
                boolean bloqueado = calendarioService.isSlotBloqueado(
                                LocalDate.parse(data),
                                LocalTime.parse(hora),
                                tipo);
                return ResponseEntity.ok(bloqueado);
        }

        @GetMapping("/configuracao-slots")
        public ResponseEntity<List<ConfiguracaoSlotDTO>> listarConfiguracaoSlots() {
                return ResponseEntity.ok(calendarioService.listarConfiguracoesSlot());
        }

        @PutMapping("/configuracao-slots/{tipo}")
        @PreAuthorize("hasRole('SECRETARIA') or (hasRole('BALNEARIO') and #tipo == 'BALNEARIO')")
        public ResponseEntity<ConfiguracaoSlotDTO> atualizarConfiguracaoSlot(
                        @PathVariable String tipo,
                        @Valid @RequestBody AtualizarConfiguracaoSlotRequest request) {
                ConfiguracaoSlotDTO dto = calendarioService.atualizarCapacidadePorSlot(tipo,
                                request.getCapacidadePorSlot());
                return ResponseEntity.ok(dto);
        }
}
