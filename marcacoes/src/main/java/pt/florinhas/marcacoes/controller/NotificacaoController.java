package pt.florinhas.marcacoes.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import pt.florinhas.marcacoes.domain.Utilizador;
import pt.florinhas.marcacoes.dto.NotificacaoResponseDTO;
import pt.florinhas.marcacoes.service.NotificacaoService;
import pt.florinhas.marcacoes.service.UtilizadorService;

@RestController
@RequestMapping("/api/notificacoes")
@RequiredArgsConstructor
public class NotificacaoController {

    private final NotificacaoService notificacaoService;
    private final UtilizadorService utilizadorService;

    @GetMapping
    public ResponseEntity<List<NotificacaoResponseDTO>> listar(@AuthenticationPrincipal UserDetails userDetails) {
        Utilizador user = utilizadorService.buscarPorEmail(userDetails.getUsername());
        return ResponseEntity.ok(notificacaoService.listarPorUtilizador(user.getId()));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> contarNaoLidas(@AuthenticationPrincipal UserDetails userDetails) {
        Utilizador user = utilizadorService.buscarPorEmail(userDetails.getUsername());
        return ResponseEntity.ok(notificacaoService.contarNaoLidas(user.getId()));
    }

    @PutMapping("/{id}/ler")
    public ResponseEntity<Void> marcarComoLida(@PathVariable Long id) {
        notificacaoService.marcarComoLida(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/ler-todas")
    public ResponseEntity<Void> marcarTodasComoLidas(@AuthenticationPrincipal UserDetails userDetails) {
        Utilizador user = utilizadorService.buscarPorEmail(userDetails.getUsername());
        notificacaoService.marcarTodasComoLidas(user.getId());
        return ResponseEntity.ok().build();
    }
}
