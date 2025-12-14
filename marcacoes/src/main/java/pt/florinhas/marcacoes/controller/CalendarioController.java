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

        /**
         * Serviço que contém a lógica de negócio associada ao calendário,
         * incluindo criação, remoção e consulta de bloqueios.
         */
        private final CalendarioService calendarioService;

        /**
         * Repositório de utilizadores.
         * É usado aqui para validar e obter o funcionário responsável pelo bloqueio.
         */
        private final UtilizadorRepository utilizadorRepository;

        /**
         * Endpoint para criar um bloqueio de agenda.
         *
         * O bloqueio pode ser:
         * - parcial (intervalo horário)
         * - total (dia completo, dependendo dos valores enviados)
         *
         * Apenas funcionários/admins devem ter acesso a este endpoint.
         *
         * param request DTO com a data, intervalo horário, motivo e ID do funcionário
         * return mensagem de confirmação da operação
         */
        @PostMapping("/bloquear")
        public ResponseEntity<?> bloquearHorario(@RequestBody BloquearHorarioRequest request) {

                // Obtém o funcionário responsável pelo bloqueio
                Utilizador admin = utilizadorRepository.findById(request.getFuncionarioId())
                                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));

                // Delegação da lógica de criação do bloqueio para o serviço
                calendarioService.bloquearHorario(
                                request.getData(),
                                request.getHoraInicio(),
                                request.getHoraFim(),
                                request.getMotivo(),
                                admin);

                // Resposta simples para o frontend
                return ResponseEntity.ok(
                                Map.of("message", "Bloqueio registado com sucesso"));
        }

        /**
         * Endpoint para remover um bloqueio de agenda existente.
         *
         * Usado tipicamente quando um funcionário decide reabrir um horário
         * previamente bloqueado.
         *
         * param id identificador do bloqueio a remover
         * return mensagem de confirmação da remoção
         */
        @DeleteMapping("/{id}")
        public ResponseEntity<?> removerBloqueio(@PathVariable Long id) {
                // Remove o bloqueio através do serviço
                calendarioService.removerBloqueio(id);
                return ResponseEntity.ok(
                                Map.of("message", "Bloqueio removido"));
        }

        /**
         * Endpoint para listar todos os bloqueios de um determinado mês.
         *
         * Este endpoint é usado pelo frontend para desenhar visualmente
         * os períodos indisponíveis (ex.: caixas cinzentas no calendário).
         *
         * param ano ano pretendido
         * param mes mês pretendido (1-12)
         * return lista de bloqueios existentes no mês indicado
         */
        @GetMapping("/bloqueios")
        public ResponseEntity<List<BloqueioAgenda>> listarBloqueios(
                        @RequestParam(required = false) Integer ano,
                        @RequestParam(required = false) Integer mes) {

                if (ano == null || mes == null) {
                        return ResponseEntity.ok(calendarioService.getTodosBloqueios());
                }

                return ResponseEntity.ok(
                                calendarioService.getBloqueiosDoMes(ano, mes));
        }

        /**
         * Endpoint rápido para verificar se um slot específico está bloqueado.
         *
         * É tipicamente chamado quando o utilizador abre o modal de criação
         * de uma nova marcação, permitindo validar imediatamente a disponibilidade.
         *
         * param data data no formato ISO (YYYY-MM-DD)
         * param hora hora no formato HH:mm
         * return TRUE se o slot estiver bloqueado (indisponível), FALSE caso contrário
         */
        @GetMapping("/verificar-slot")
        public ResponseEntity<Boolean> verificarSlot(
                        @RequestParam String data,
                        @RequestParam String hora) {
                // Converte os parâmetros recebidos para tipos temporais
                boolean bloqueado = calendarioService.isSlotBloqueado(
                                LocalDate.parse(data),
                                java.time.LocalTime.parse(hora));
                // Retorna true se o horário estiver bloqueado
                return ResponseEntity.ok(bloqueado);
        }
}
