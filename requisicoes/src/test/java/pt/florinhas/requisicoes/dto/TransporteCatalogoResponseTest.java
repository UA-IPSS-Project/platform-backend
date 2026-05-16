package pt.florinhas.requisicoes.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import pt.florinhas.requisicoes.domain.Transporte;
import pt.florinhas.requisicoes.domain.TransporteCategoria;

class TransporteCatalogoResponseTest {

    @Test
    void from_DeveConverterTransporteCompleto() {

        Transporte transporte =
                new Transporte();

        transporte.setId(1L);
        transporte.setCodigo("V1");
        transporte.setTipo("Carrinha");

        transporte.setCategoria(
                TransporteCategoria.ESCOLAR);

        transporte.setMatricula("AA-00-BB");
        transporte.setMarca("Ford");
        transporte.setModelo("Transit");
        transporte.setLotacao(9);

        transporte.setDataMatricula(
                LocalDate.of(2020, 1, 1));

        TransporteCatalogoResponse response =
                TransporteCatalogoResponse
                        .from(transporte);

        assertEquals(1L, response.id());

        assertEquals("V1", response.codigo());

        assertEquals(
                "Carrinha",
                response.tipo());

        assertEquals(
                "ESCOLAR",
                response.categoria());

        assertEquals(
                "AA-00-BB",
                response.matricula());

        assertEquals(
                "Ford",
                response.marca());

        assertEquals(
                "Transit",
                response.modelo());

        assertEquals(
                9,
                response.lotacao());

        assertEquals(
                "2020-01-01",
                response.dataMatricula());
    }

    @Test
    void from_DevePermitirCamposNull() {

        Transporte transporte =
                new Transporte();

        TransporteCatalogoResponse response =
                TransporteCatalogoResponse
                        .from(transporte);

        assertNull(response.id());

        assertNull(response.codigo());

        assertNull(response.tipo());

        assertNull(response.categoria());

        assertNull(response.matricula());

        assertNull(response.marca());

        assertNull(response.modelo());

        assertNull(response.lotacao());

        assertNull(response.dataMatricula());
    }
}