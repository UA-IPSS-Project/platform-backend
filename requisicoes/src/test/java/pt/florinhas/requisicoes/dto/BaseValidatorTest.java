package pt.florinhas.requisicoes.dto;

import org.junit.jupiter.api.BeforeEach;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

public abstract class BaseValidatorTest {

    protected Validator validator;

    @BeforeEach
    void setupValidator() {

        ValidatorFactory factory =
                Validation.buildDefaultValidatorFactory();

        validator = factory.getValidator();
    }
}