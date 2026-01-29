package pt.florinhas.marcacoes.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotação de validação customizada para NIF português.
 * Valida formato (9 dígitos) e algoritmo de checksum.
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NifValidator.class)
public @interface ValidNif {
    String message() default "NIF inválido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
