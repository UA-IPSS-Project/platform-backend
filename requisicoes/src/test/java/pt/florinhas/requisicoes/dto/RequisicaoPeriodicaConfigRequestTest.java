package pt.florinhas.requisicoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import pt.florinhas.requisicoes.domain.PeriodicidadeFrequencia;

class RequisicaoPeriodicaConfigRequestTest {

    @Test
    void constructorAndAccessors_DeveFuncionar() {

        LocalDate inicio =
                LocalDate.now();

        LocalDate fim =
                inicio.plusDays(10);

        RequisicaoPeriodicaConfigRequest request =
                new RequisicaoPeriodicaConfigRequest(
                        PeriodicidadeFrequencia.SEMANAL,
                        inicio,
                        fim);

        assertEquals(
                PeriodicidadeFrequencia.SEMANAL,
                request.frequencia());

        assertEquals(
                inicio,
                request.dataInicio());

        assertEquals(
                fim,
                request.dataFim());
    }

    @Test
    void isIntervaloDatasValido_DeveRetornarTrue() {

        RequisicaoPeriodicaConfigRequest request =
                new RequisicaoPeriodicaConfigRequest(
                        PeriodicidadeFrequencia.MENSAL,
                        LocalDate.now(),
                        LocalDate.now().plusDays(1));

        assertEquals(
                true,
                request.isIntervaloDatasValido());
    }

    @Test
    void isIntervaloDatasValido_DeveRetornarFalse() {

        RequisicaoPeriodicaConfigRequest request =
                new RequisicaoPeriodicaConfigRequest(
                        PeriodicidadeFrequencia.MENSAL,
                        LocalDate.now(),
                        LocalDate.now().minusDays(1));

        assertEquals(
                false,
                request.isIntervaloDatasValido());
    }
}