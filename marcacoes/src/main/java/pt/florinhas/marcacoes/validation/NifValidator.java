package pt.florinhas.marcacoes.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pt.florinhas.marcacoes.exception.BadRequestException;
import pt.florinhas.marcacoes.service.nif.NifValidationService;

/**
 * Validador modular de NIFs portugueses.
 *
 * Regras:
 * - Primeiro valida formato (9 dígitos numéricos)
 * - Valida prefixos permitidos (1,2,3,5,6,8,9)
 * - Valida dígito de controlo (módulo 11)
 * - Só depois consulta o serviço externo de validação
 */
@Component
@Slf4j
public class NifValidator {

    private static final String NIF_REGEX = "\\d{9}";
    private final NifValidationService nifValidationService;

    public NifValidator(NifValidationService nifValidationService) {
        this.nifValidationService = nifValidationService;
    }

    public boolean isFormatValid(String nif) {
        return nif != null && nif.matches(NIF_REGEX);
    }

    public boolean isValidRequired(String nif) {
        if (!isFormatValid(nif)) {
            log.debug("NIF validation failed at format check. nif={}", maskNif(nif));
            return false;
        }

        LocalFailureReason localFailure = getLocalFailureReason(nif);
        if (localFailure != LocalFailureReason.NONE) {
            log.debug("NIF validation failed at local rules. nif={}, reason={}", maskNif(nif), localFailure);
            return false;
        }

        boolean externalValid = nifValidationService.validate(nif);
        if (!externalValid) {
            log.debug("NIF validation failed at external service. nif={}", maskNif(nif));
        }
        return externalValid;
    }

    public boolean isValidOptional(String nif) {
        if (nif == null || nif.trim().isEmpty()) {
            return true;
        }
        return isValidRequired(nif);
    }

    public void validateRequiredOrThrow(String nif) {
        if (nif == null || nif.trim().isEmpty()) {
            log.debug("NIF validation failed: value is null/blank");
            throw new BadRequestException("NIF é obrigatório");
        }
        if (!isFormatValid(nif)) {
            log.debug("NIF validation failed at format check. nif={}", maskNif(nif));
            throw new BadRequestException("NIF deve conter exatamente 9 dígitos numéricos");
        }
        LocalFailureReason localFailure = getLocalFailureReason(nif);
        if (localFailure != LocalFailureReason.NONE) {
            log.debug("NIF validation failed at local rules. nif={}, reason={}", maskNif(nif), localFailure);
            throw new BadRequestException("NIF inválido");
        }
        if (!nifValidationService.validate(nif)) {
            log.debug("NIF validation failed at external service. nif={}", maskNif(nif));
            throw new BadRequestException("NIF inválido");
        }
    }

    public void validateOptionalOrThrow(String nif) {
        if (nif == null || nif.trim().isEmpty()) {
            return;
        }
        validateRequiredOrThrow(nif);
    }

    private LocalFailureReason getLocalFailureReason(String nif) {
        if (!hasValidPrefix(nif)) {
            return LocalFailureReason.INVALID_PREFIX;
        }
        if (allDigitsEqual(nif)) {
            return LocalFailureReason.ALL_DIGITS_EQUAL;
        }
        if (!hasValidChecksum(nif)) {
            return LocalFailureReason.INVALID_CHECKSUM;
        }
        return LocalFailureReason.NONE;
    }

    private boolean hasValidPrefix(String nif) {
        char prefix = nif.charAt(0);
        return prefix == '1' || prefix == '2' || prefix == '3' || prefix == '5'
                || prefix == '6' || prefix == '8' || prefix == '9';
    }

    private boolean allDigitsEqual(String nif) {
        char first = nif.charAt(0);
        for (int i = 1; i < nif.length(); i++) {
            if (nif.charAt(i) != first) {
                return false;
            }
        }
        return true;
    }

    private boolean hasValidChecksum(String nif) {
        int sum = 0;
        for (int i = 0; i < 8; i++) {
            int digit = Character.getNumericValue(nif.charAt(i));
            sum += digit * (9 - i);
        }

        int mod = sum % 11;
        int checkDigit = 11 - mod;
        if (checkDigit >= 10) {
            checkDigit = 0;
        }

        int lastDigit = Character.getNumericValue(nif.charAt(8));
        return checkDigit == lastDigit;
    }

    private String maskNif(String nif) {
        if (nif == null || nif.length() < 5) {
            return "<invalid>";
        }
        return nif.substring(0, 3) + "****" + nif.substring(nif.length() - 2);
    }

    private enum LocalFailureReason {
        NONE,
        INVALID_PREFIX,
        ALL_DIGITS_EQUAL,
        INVALID_CHECKSUM
    }
}
