package pt.florinhas.marcacoes.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MarcacaoSecretariaTest {

    @Test
    void marcacaoSecretaria_DeveGuardarValores() {

        MarcacaoSecretaria marcacao = new MarcacaoSecretaria();

        marcacao.setMarcacaoId(1L);
        marcacao.setAssunto("Teste");
        marcacao.setDescricao("Descrição");
        marcacao.setTipoAtendimento(AtendimentoTipo.PRESENCIAL);

        assertEquals(1L, marcacao.getMarcacaoId());
        assertEquals("Teste", marcacao.getAssunto());
        assertEquals("Descrição", marcacao.getDescricao());
        assertEquals(
                AtendimentoTipo.PRESENCIAL,
                marcacao.getTipoAtendimento());
    }
}