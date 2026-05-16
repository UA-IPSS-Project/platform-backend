package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TermsStatusDTOTest {

    @Test
    void deveCriarDTO() {

        TermsStatusDTO dto =
                new TermsStatusDTO(
                        3,
                        1,
                        true
                );

        assertEquals(
                3,
                dto.getCurrentVersion()
        );

        assertEquals(
                1,
                dto.getUserVersion()
        );

        assertTrue(
                dto.isNeedsAcceptance()
        );
    }

    @Test
    void deveUsarSetters() {

        TermsStatusDTO dto =
                new TermsStatusDTO();

        dto.setCurrentVersion(5);
        dto.setUserVersion(4);
        dto.setNeedsAcceptance(false);

        assertEquals(
                5,
                dto.getCurrentVersion()
        );

        assertEquals(
                4,
                dto.getUserVersion()
        );

        assertFalse(
                dto.isNeedsAcceptance()
        );
    }
}