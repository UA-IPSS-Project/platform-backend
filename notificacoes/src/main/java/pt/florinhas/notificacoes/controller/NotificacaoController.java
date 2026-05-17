package pt.florinhas.notificacoes.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

import lombok.RequiredArgsConstructor;
import pt.florinhas.notificacoes.service.NotificacaoService;
import pt.florinhas.common_data.repository.UtilizadorRepository;

import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.common_data.dto.NotificacaoResponseDTO;

@RestController
@RequestMapping("/api/notificacoes")
@RequiredArgsConstructor
public class NotificacaoController {

    private final NotificacaoService notificacaoService;
    private final UtilizadorRepository utilizadorRepository;

    @GetMapping
    public ResponseEntity<List<NotificacaoResponseDTO>> listar(@AuthenticationPrincipal String email) {
        Utilizador user = utilizadorRepository.findByEmail(email).stream().findFirst().orElseThrow();
        return ResponseEntity.ok(notificacaoService.listarPorUtilizador(user.getId()));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> contarNaoLidas(@AuthenticationPrincipal String email) {
        Utilizador user = utilizadorRepository.findByEmail(email).stream().findFirst().orElseThrow();
        return ResponseEntity.ok(notificacaoService.contarNaoLidas(user.getId()));
    }

    @PutMapping("/{id}/ler")
    public ResponseEntity<Void> marcarComoLida(
            @PathVariable Long id,
            @AuthenticationPrincipal String email) {
        Utilizador user = utilizadorRepository.findByEmail(email).stream().findFirst().orElseThrow();
        notificacaoService.marcarComoLida(id, user.getId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/ler-todas")
    public ResponseEntity<Void> marcarTodasComoLidas(@AuthenticationPrincipal String email) {
        Utilizador user = utilizadorRepository.findByEmail(email).stream().findFirst().orElseThrow();
        notificacaoService.marcarTodasComoLidas(user.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarNotificacao(
            @PathVariable Long id,
            @AuthenticationPrincipal String email) {
        Utilizador user = utilizadorRepository.findByEmail(email).stream().findFirst().orElseThrow();
        notificacaoService.eliminarNotificacao(id, user.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> eliminarTodas(@AuthenticationPrincipal String email) {
        Utilizador user = utilizadorRepository.findByEmail(email).stream().findFirst().orElseThrow();
        notificacaoService.eliminarTodas(user.getId());
        return ResponseEntity.ok().build();
    }
}
