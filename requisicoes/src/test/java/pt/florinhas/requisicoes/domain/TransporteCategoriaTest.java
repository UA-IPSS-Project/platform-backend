package pt.florinhas.requisicoes.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TransporteCategoriaTest {

    @Test
    void enum_DeveConterValoresEsperados() {

        assertEquals(
                TransporteCategoria.ESCOLAR,
                TransporteCategoria.valueOf(
                        "ESCOLAR"));

        assertEquals(
                TransporteCategoria.AMBULANCIA,
                TransporteCategoria.valueOf(
                        "AMBULANCIA"));

        assertEquals(
                TransporteCategoria.OUTRO,
                TransporteCategoria.valueOf(
                        "OUTRO"));
    }
}