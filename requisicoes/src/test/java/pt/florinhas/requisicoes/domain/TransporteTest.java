package pt.florinhas.requisicoes.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class TransporteTest {

    @Test
    void settersAndGetters_DeveFuncionar() {

        Transporte transporte =
                new Transporte();

        LocalDate data =
                LocalDate.now();

        transporte.setId(1L);
        transporte.setCodigo("T1");
        transporte.setTipo("Carrinha");
        transporte.setCategoria(TransporteCategoria.LIGEIRO_DE_PASSAGEIROS);
        transporte.setMatricula("AA-00-BB");
        transporte.setMarca("Ford");
        transporte.setModelo("Transit");
        transporte.setLotacao(9);
        transporte.setDataMatricula(data);

        assertEquals(
                1L,
                transporte.getId());

        assertEquals(
                "T1",
                transporte.getCodigo());

        assertEquals(
                "Carrinha",
                transporte.getTipo());

        assertEquals(
                TransporteCategoria.LIGEIRO_DE_PASSAGEIROS,
                transporte.getCategoria());

        assertEquals(
                "AA-00-BB",
                transporte.getMatricula());

        assertEquals(
                "Ford",
                transporte.getMarca());

        assertEquals(
                "Transit",
                transporte.getModelo());

        assertEquals(
                9,
                transporte.getLotacao());

        assertEquals(
                data,
                transporte.getDataMatricula());
    }

    @Test
    void constructorVazio_DeveInicializarNull() {

        Transporte transporte =
                new Transporte();

        assertNull(transporte.getId());
        assertNull(transporte.getCodigo());
        assertNull(transporte.getTipo());
        assertNull(transporte.getCategoria());
        assertNull(transporte.getMatricula());
        assertNull(transporte.getMarca());
        assertNull(transporte.getModelo());
        assertNull(transporte.getLotacao());
        assertNull(transporte.getDataMatricula());
    }
}