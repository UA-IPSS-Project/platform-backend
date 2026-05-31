package pt.florinhas.marcacoes.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class MarcacaoBalnearioTest {

    @Test
    void addRoupa_DeveAssociarRoupa() {

        MarcacaoBalneario balneario = new MarcacaoBalneario();
        Roupa roupa = new Roupa();

        balneario.addRoupa(roupa);

        assertEquals(1, balneario.getRoupas().size());
        assertEquals(balneario, roupa.getMarcacaoBalneario());
    }

    @Test
    void removeRoupa_DeveRemoverAssociacao() {

        MarcacaoBalneario balneario = new MarcacaoBalneario();
        Roupa roupa = new Roupa();

        balneario.addRoupa(roupa);
        balneario.removeRoupa(roupa);

        assertEquals(0, balneario.getRoupas().size());
        assertNull(roupa.getMarcacaoBalneario());
    }
}