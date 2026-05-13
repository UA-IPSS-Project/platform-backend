package pt.florinhas.common_data.validation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pt.florinhas.common_data.exception.BadRequestException;

class NifValidatorTest {

    private NifValidator validator;

    @BeforeEach
    void setUp() {
        validator = new NifValidator();
    }

    @Test
    void normalize_DeveRemoverCaracteres() {

        String result =
                NifValidator.normalize(
                        " 123 456 789 "
                );

        assertEquals(
                "123456789",
                result
        );
    }

    @Test
    void isFormatValid_DeveRetornarTrue() {

        assertTrue(
                validator.isFormatValid(
                        "123456789"
                )
        );
    }

    @Test
    void isFormatValid_DeveRetornarFalse() {

        assertFalse(
                validator.isFormatValid(
                        "123"
                )
        );
    }

    @Test
    void isValidOptional_Null_DeveRetornarTrue() {

        assertTrue(
                validator.isValidOptional(null)
        );
    }

    @Test
    void validateRequiredOrThrow_DeveLancarExcecao() {

        assertThrows(
                BadRequestException.class,
                () -> validator.validateRequiredOrThrow("")
        );
    }

    @Test
    void validateOptionalOrThrow_Null() {

        assertDoesNotThrow(
                () -> validator.validateOptionalOrThrow(null)
        );
    }
}