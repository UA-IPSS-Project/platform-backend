package pt.florinhas.marcacoes.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MarcacaoSecretariaTest {

    @Test
    void deveCriarMarcacaoSecretaria() {

        MarcacaoSecretaria secretaria =
                new MarcacaoSecretaria();

        secretaria.setMarcacaoId(1L);
        secretaria.setAssunto("Consulta");
        secretaria.setDescricao("Descrição");
        secretaria.setTipoAtendimento(AtendimentoTipo.PRESENCIAL);

        assertEquals(1L, secretaria.getMarcacaoId());
        assertEquals("Consulta", secretaria.getAssunto());
        assertEquals("Descrição", secretaria.getDescricao());
        assertEquals(
                AtendimentoTipo.PRESENCIAL,
                secretaria.getTipoAtendimento()
        );
    }
}