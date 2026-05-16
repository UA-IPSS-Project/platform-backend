package pt.florinhas.marcacoes.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MarcacaoBalnearioTest {

    @Test
    @DisplayName("deve adicionar roupa corretamente")
    void addRoupa_deveAdicionar() {
        MarcacaoBalneario balneario = new MarcacaoBalneario();
        Roupa roupa = new Roupa();

        balneario.addRoupa(roupa);

        assertEquals(1, balneario.getRoupas().size());
        assertEquals(balneario, roupa.getMarcacaoBalneario());
    }

    @Test
    @DisplayName("deve remover roupa corretamente")
    void removeRoupa_deveRemover() {
        MarcacaoBalneario balneario = new MarcacaoBalneario();
        Roupa roupa = new Roupa();

        balneario.addRoupa(roupa);
        balneario.removeRoupa(roupa);

        assertTrue(balneario.getRoupas().isEmpty());
        assertNull(roupa.getMarcacaoBalneario());
    }
}