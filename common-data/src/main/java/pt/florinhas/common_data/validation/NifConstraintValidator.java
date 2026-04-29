package pt.florinhas.common_data.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class NifConstraintValidator implements ConstraintValidator<Nif, String> {

    @Autowired
    private NifValidator nifValidator;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true; // Let @NotBlank or @NotNull handle empty values if required
        }
        
        // Fallback if not injected by Spring
        if (nifValidator == null) {
            nifValidator = new NifValidator();
        }
        
        return nifValidator.isValidOptional(value);
    }
}
