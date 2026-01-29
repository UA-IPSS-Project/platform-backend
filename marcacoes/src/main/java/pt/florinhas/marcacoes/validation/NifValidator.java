package pt.florinhas.marcacoes.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pt.florinhas.marcacoes.service.nif.NifValidationService;

/**
 * Validador customizado para NIFs portugueses.
 * Verifica:
 * - Formato (9 dígitos)
 * - Algoritmo de checksum português
 */
@Component
public class NifValidator implements ConstraintValidator<ValidNif, String> {

    @Autowired(required = false) // Optional dependency - degrades gracefully
    private NifValidationService nifValidationService;

    @Override
    public boolean isValid(String nif, ConstraintValidatorContext context) {
        if (nif == null || nif.trim().isEmpty()) {
            return false;
        }

        // Check format (9 digits)
        if (!nif.matches("\\d{9}")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("NIF deve conter exatamente 9 dígitos")
                    .addConstraintViolation();
            return false;
        }

        // Check checksum algorithm if service is available
        if (nifValidationService != null && !nifValidationService.validate(nif)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("NIF inválido (falha na validação do algoritmo)")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
