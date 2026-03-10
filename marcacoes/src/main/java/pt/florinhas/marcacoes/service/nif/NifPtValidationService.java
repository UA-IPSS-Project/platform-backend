package pt.florinhas.marcacoes.service.nif;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;

@Service
@Slf4j
public class NifPtValidationService implements NifValidationService {

    @Value("${nif.pt.key}")
    private String apiKey;

    @Value("${nif.pt.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public boolean validate(String nif) {
        if (nif == null || !nif.matches("\\d{9}")) {
            log.debug("External NIF validation skipped due to invalid format. nif={}", nif);
            return false;
        }

        try {
            String url = String.format("%s/?json=1&q=%s&key=%s", apiUrl, nif, apiKey);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                boolean valid = isValidResponse(response, nif);
                log.debug("External NIF validation result. nif={}, valid={}, responseKeys={}",
                    nif,
                    valid,
                    response != null ? response.keySet() : "<null>");
                return valid;

        } catch (Exception e) {
            log.warn("Erro ao validar NIF na API externa para nif={}: {}", nif, e.getMessage());
            // Fail-closed: se não foi possível validar externamente, rejeita.
            return false;
        }
    }

    private boolean isValidResponse(Map<String, Object> response, String nif) {
        if (response == null) {
            log.debug("External NIF validation failed: null response. nif={}", nif);
            return false;
        }

        Object isNif = response.get("is_nif");
        Object nifValidation = response.get("nif_validation");
        Object valid = response.get("valid");

        if (isExplicitFalse(isNif) || isExplicitFalse(nifValidation) || isExplicitFalse(valid)) {
            log.debug("External NIF validation failed by explicit false flags. nif={}, is_nif={}, nif_validation={}, valid={}",
                    nif, isNif, nifValidation, valid);
            return false;
        }

        if (isExplicitTrue(isNif) && isExplicitTrue(nifValidation)) {
            log.debug("External NIF validation accepted by is_nif+nif_validation flags. nif={}", nif);
            return true;
        }

        if (isExplicitTrue(valid)) {
            log.debug("External NIF validation accepted by valid flag. nif={}", nif);
            return true;
        }

        Object recordsObj = response.get("records");
        if (!(recordsObj instanceof Map<?, ?> recordsMap)) {
            log.debug("External NIF validation failed: records missing/invalid. nif={}", nif);
            return false;
        }

        if (recordsMap.containsKey(nif)) {
            log.debug("External NIF validation accepted by records key match. nif={}", nif);
            return true;
        }

        for (Object value : recordsMap.values()) {
            if (value instanceof Map<?, ?> recordMapValue) {
                Object recordNif = recordMapValue.get("nif");
                if (nif.equals(String.valueOf(recordNif))) {
                    log.debug("External NIF validation accepted by records value match. nif={}", nif);
                    return true;
                }
            }
        }

        log.debug("External NIF validation failed: no positive signal found in response. nif={}", nif);
        return false;
    }

    private boolean isExplicitTrue(Object value) {
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof Number n) {
            return n.intValue() == 1;
        }
        if (value instanceof String s) {
            return "true".equalsIgnoreCase(s) || "1".equals(s);
        }
        return false;
    }

    private boolean isExplicitFalse(Object value) {
        if (value instanceof Boolean b) {
            return !b;
        }
        if (value instanceof Number n) {
            return n.intValue() == 0;
        }
        if (value instanceof String s) {
            return "false".equalsIgnoreCase(s) || "0".equals(s);
        }
        return false;
    }
}
