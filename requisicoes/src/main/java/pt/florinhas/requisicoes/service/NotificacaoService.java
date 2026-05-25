package pt.florinhas.requisicoes.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;
import pt.florinhas.requisicoes.domain.Requisicao;

import java.util.Map;
import java.util.HashMap;

@Service
@Slf4j
public class NotificacaoService {

    @Value("${notificacoes.url:http://notificacoes:8083}")
    private String notificacoesUrl;

    @Value("${gateway.shared-secret:}")
    private String gatewaySecret;

    private final RestTemplate restTemplate = new RestTemplate();

    @Async
    public void notificarNovaRequisicao(Long destinatarioId, Requisicao requisicao) {
        String titulo = "Nova Requisição de " + requisicao.getTipo();
        String mensagem = "O funcionário " + requisicao.getCriadoPor().getNome() + 
                          " criou uma nova requisição de " + requisicao.getTipo().toString().toLowerCase() + 
                          " com prioridade " + requisicao.getPrioridade() + ".";

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("requisicaoId", requisicao.getId().toString());
        metadata.put("tipo", requisicao.getTipo().toString());
        metadata.put("prioridade", requisicao.getPrioridade().toString());

        enviarParaMicrosservico(destinatarioId, titulo, mensagem, metadata);
    }

    @Async
    public void notificarMudancaEstado(Long destinatarioId, Requisicao requisicao) {
        String titulo = "Atualização de Requisição";
        String mensagem = "A sua requisição de " + requisicao.getTipo().toString().toLowerCase() + 
                          " foi atualizada para o estado: " + requisicao.getEstado().toString() + ".";

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("requisicaoId", requisicao.getId().toString());
        metadata.put("novoEstado", requisicao.getEstado().toString());

        enviarParaMicrosservico(destinatarioId, titulo, mensagem, metadata);
    }

    private void enviarParaMicrosservico(Long utilizadorId, String titulo, String mensagem, Map<String, Object> metadata) {
        String url = notificacoesUrl + "/api/internal/notificacoes/criar";
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("utilizadorId", utilizadorId);
            requestBody.put("titulo", titulo);
            requestBody.put("mensagem", mensagem);
            requestBody.put("tipo", "REQUISICAO");
            requestBody.put("metadata", metadata);

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("X-Gateway-Secret", gatewaySecret);
            org.springframework.http.HttpEntity<Map<String, Object>> entity = new org.springframework.http.HttpEntity<>(requestBody, headers);

            restTemplate.postForObject(url, entity, Void.class);
            log.info("Notificação de requisição enviada para o utilizador {}: {}", utilizadorId, titulo);
        } catch (Exception e) {
            log.error("Erro ao enviar notificação de requisição para {}: {}", url, e.getMessage());
        }
    }
}
