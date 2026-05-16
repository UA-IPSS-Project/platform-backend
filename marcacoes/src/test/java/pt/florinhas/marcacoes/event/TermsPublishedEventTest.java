package pt.florinhas.marcacoes.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TermsPublishedEventTest {

    @Test
    @DisplayName("Deve criar TermsPublishedEvent")
    void deveCriarTermsPublishedEvent() {

        TermsPublishedEvent event =
                new TermsPublishedEvent(
                        2,
                        "Alteração termos"
                );

        assertNotNull(event);

        assertEquals(
                2,
                event.newVersion()
        );

        assertEquals(
                "Alteração termos",
                event.changeDescription()
        );
    }
}