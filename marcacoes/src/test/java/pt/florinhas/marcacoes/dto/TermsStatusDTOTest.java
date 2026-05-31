package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TermsStatusDTOTest {

    @Test
    void termsStatusDTO_DeveGuardarValores() {

        TermsStatusDTO dto = new TermsStatusDTO();

        dto.setCurrentVersion(5);
        dto.setUserVersion(3);
        dto.setNeedsAcceptance(true);

        assertEquals(5, dto.getCurrentVersion());
        assertEquals(3, dto.getUserVersion());
        assertEquals(true, dto.isNeedsAcceptance());
    }
}