package pt.florinhas.requisicoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import pt.florinhas.requisicoes.domain.TransporteCategoria;

class CriarTransporteRequestTest {

    @Test
    void constructorAndAccessors_DeveFuncionar() {

        LocalDate data =
                LocalDate.now();

        CriarTransporteRequest request =
                new CriarTransporteRequest(
                        "T1",
                        "Carrinha",
                        TransporteCategoria.LIGEIRO_DE_PASSAGEIROS,
                        "AA-00-BB",
                        "Ford",
                        "Transit",
                        9,
                        data);

        assertEquals(
                "T1",
                request.codigo());

        assertEquals(
                "Carrinha",
                request.tipo());

        assertEquals(
                TransporteCategoria.LIGEIRO_DE_PASSAGEIROS,
                request.categoria());

        assertEquals(
                "AA-00-BB",
                request.matricula());

        assertEquals(
                "Ford",
                request.marca());

        assertEquals(
                "Transit",
                request.modelo());

        assertEquals(
                9,
                request.lotacao());

        assertEquals(
                data,
                request.dataMatricula());
    }
}