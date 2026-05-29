package pt.florinhas.marcacoes.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class RelatorioPeriodicoTest {

    @Test
    void relatorioPeriodico_DeveGuardarValores() {

        RelatorioPeriodico relatorio = new RelatorioPeriodico();

        relatorio.setId(1L);
        relatorio.setDestinatarios("a@a.com");
        relatorio.setFrequencia("SEMANAL");
        relatorio.setDataInicio(LocalDate.now());
        relatorio.setSeccoes("secretaria");
        relatorio.setActivo(true);

        assertEquals(1L, relatorio.getId());
        assertEquals("a@a.com", relatorio.getDestinatarios());
        assertEquals("SEMANAL", relatorio.getFrequencia());
        assertEquals("secretaria", relatorio.getSeccoes());
        assertTrue(relatorio.isActivo());
    }
}