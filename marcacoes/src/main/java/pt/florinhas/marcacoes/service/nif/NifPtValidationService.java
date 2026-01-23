package pt.florinhas.marcacoes.service.nif;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class NifPtValidationService implements NifValidationService {

    @Value("${nif.pt.key}")
    private String apiKey;

    @Value("${nif.pt.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public boolean validate(String nif) {
        // Validação básica de estrutura antes de chamar a API
        if (nif == null || !nif.matches("\\d{9}")) {
            return false;
        }

        try {
            // Construir URL: https://www.nif.pt/?json=1&q={nif}&key={key}
            String url = String.format("%s/?json=1&q=%s&key=%s", apiUrl, nif, apiKey);

            // Fazer a chamada HTTP
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            // Analisar resposta
            // Estrutura esperada:
            // {
            // "result": "success",
            // "records": {
            // "509442013": { "nif": 509442013, ... }
            // },
            // "nif_validation": true,
            // "is_nif": true
            // }

            if (response != null && response.containsKey("records")) {
                Object recordsObj = response.get("records");
                if (recordsObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> recordsMap = (Map<String, Object>) recordsObj;
                    // Se o mapa de records contiver a chave do NIF, é válido
                    if (recordsMap.containsKey(nif)) {
                        return true;
                    }
                }
            }

            // Alternativa: verificar is_nif e nif_validation
            if (response != null &&
                    Boolean.TRUE.equals(response.get("is_nif")) &&
                    Boolean.TRUE.equals(response.get("nif_validation"))) {
                return true;
            }
            return false;

        } catch (Exception e) {
            // Em caso de erro de rede ou timeout (API indisponível),
            // fazemos fallback para "fail-open" (aceitar se estrutura válida) ou
            // "fail-closed"
            // Por agora, para não bloquear o utilizador, vamos assumir válido se o formato
            // estiver correto
            // e logar o erro.
            System.err.println("Erro ao validar NIF na API externa: " + e.getMessage());
            return true;
        }
    }
}
