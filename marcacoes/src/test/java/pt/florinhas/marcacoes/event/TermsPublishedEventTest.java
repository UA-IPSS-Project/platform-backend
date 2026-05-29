package pt.florinhas.marcacoes.event;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TermsPublishedEventTest {

    @Test
    void termsPublishedEvent_DeveGuardarValores() {

        TermsPublishedEvent event = new TermsPublishedEvent( 2, "Mudanças");

        assertEquals( 2, event.newVersion());

        assertEquals( "Mudanças", event.changeDescription());
    }
}