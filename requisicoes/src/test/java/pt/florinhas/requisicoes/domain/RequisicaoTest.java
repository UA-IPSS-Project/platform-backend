package pt.florinhas.requisicoes.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import pt.florinhas.common_data.domain.Funcionario;

class RequisicaoTest {

    private static class TestRequisicao
            extends Requisicao {
    }

    @Test
    void isPeriodica_DeveRetornarTrue() {

        TestRequisicao requisicao =
                new TestRequisicao();

        requisicao.setPeriodicaFrequencia(
                PeriodicidadeFrequencia.MENSAL);

        assertEquals(
                true,
                requisicao.isPeriodica());
    }

    @Test
    void isPeriodica_DeveRetornarFalse() {

        TestRequisicao requisicao =
                new TestRequisicao();

        assertEquals(
                false,
                requisicao.isPeriodica());
    }

    @Test
    void onCreate_DeveDefinirDefaults() {

        TestRequisicao requisicao =
                new TestRequisicao();

        requisicao.onCreate();

        assertEquals(
                RequisicaoEstado.ABERTO,
                requisicao.getEstado());
    }

    @Test
    void settersAndGetters_DeveFuncionar() {

        TestRequisicao requisicao =
                new TestRequisicao();

        Funcionario funcionario =
                new Funcionario();

        funcionario.setId(1L);

        requisicao.setId(1L);
        requisicao.setDescricao("Descricao");
        requisicao.setEstado(RequisicaoEstado.EM_PROGRESSO);
        requisicao.setPrioridade(RequisicaoPrioridade.ALTA);
        requisicao.setTipo(RequisicaoTipo.MATERIAL);
        requisicao.setCriadoPor(funcionario);
        requisicao.setGeridoPor(funcionario);
        requisicao.setPeriodicaFrequencia(
                PeriodicidadeFrequencia.SEMANAL);
        requisicao.setPeriodicaDataInicio(
                LocalDate.now());
        requisicao.setPeriodicaDataFim(
                LocalDate.now().plusDays(10));

        assertEquals(
                1L,
                requisicao.getId());

        assertEquals(
                "Descricao",
                requisicao.getDescricao());

        assertEquals(
                RequisicaoEstado.EM_PROGRESSO,
                requisicao.getEstado());

        assertEquals(
                RequisicaoPrioridade.ALTA,
                requisicao.getPrioridade());

        assertEquals(
                RequisicaoTipo.MATERIAL,
                requisicao.getTipo());

        assertEquals(
                funcionario,
                requisicao.getCriadoPor());

        assertEquals(
                funcionario,
                requisicao.getGeridoPor());

        assertEquals(
                PeriodicidadeFrequencia.SEMANAL,
                requisicao.getPeriodicaFrequencia());
    }
}