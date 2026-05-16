package pt.florinhas.requisicoes.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class TransporteTest {

        @Test
        void gettersAndSetters_DeveFuncionar() {

                Transporte transporte = new Transporte();

                LocalDate data = LocalDate.of(2020, 1, 1);

                transporte.setId(1L);
                transporte.setCodigo("V1");
                transporte.setTipo("Carrinha");
                transporte.setCategoria(
                                TransporteCategoria.ESCOLAR);

                transporte.setMatricula("AA-00-BB");
                transporte.setMarca("Ford");
                transporte.setModelo("Transit");
                transporte.setLotacao(9);
                transporte.setDataMatricula(data);

                assertEquals(1L, transporte.getId());
                assertEquals("V1", transporte.getCodigo());
                assertEquals("Carrinha", transporte.getTipo());

                assertEquals(
                                TransporteCategoria.ESCOLAR,
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
}