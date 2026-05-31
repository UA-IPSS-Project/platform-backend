package pt.florinhas.requisicoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import pt.florinhas.requisicoes.domain.Transporte;
import pt.florinhas.requisicoes.domain.TransporteCategoria;

class TransporteCatalogoResponseTest {

    @Test
    void from_DeveMapearTudo() {

        Transporte transporte =
                new Transporte();

        transporte.setId(1L);
        transporte.setCodigo("T1");
        transporte.setTipo("Carrinha");
        transporte.setCategoria(
                TransporteCategoria.LIGEIRO_DE_PASSAGEIROS);
        transporte.setMatricula("AA-00-BB");
        transporte.setMarca("Ford");
        transporte.setModelo("Transit");
        transporte.setLotacao(9);
        transporte.setDataMatricula(
                LocalDate.of(2024, 1, 1));

        TransporteCatalogoResponse response =
                TransporteCatalogoResponse.from(
                        transporte);

        assertEquals(
                1L,
                response.id());

        assertEquals(
                "T1",
                response.codigo());

        assertEquals(
                "Carrinha",
                response.tipo());

        assertEquals(
                "LIGEIRO_DE_PASSAGEIROS",
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
                "2024-01-01",
                response.dataMatricula());
    }

    @Test
    void from_DeveAceitarCamposNull() {

        Transporte responseSource =
                new Transporte();

        TransporteCatalogoResponse response =
                TransporteCatalogoResponse.from(
                        responseSource);

        assertNull(response.categoria());
        assertNull(response.dataMatricula());
    }
}