package pt.florinhas.marcacoes.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.marcacoes.service.TermsService;

class TermsControllerTest {

    private TermsService service;
    private TermsController controller;

    @BeforeEach
    void setUp() {

        service =
                mock(TermsService.class);

        controller =
                new TermsController(service);
    }

    @Test
    void getVersion_DeveRetornarVersao() {

        when(service.getCurrentVersion())
                .thenReturn(3);

        ResponseEntity<Map<String, Integer>> result =
                controller.getVersion();

        assertEquals(
                3,
                result.getBody().get("version"));
    }

    @Test
    void getContent_DeveRetornarConteudo() {

        when(service.getTermsContent("pt"))
                .thenReturn("conteudo");

        ResponseEntity<Map<String, String>> result =
                controller.getContent("pt");

        assertEquals(
                "conteudo",
                result.getBody().get("content"));
    }

    @Test
    void needsAcceptance_DeveRetornarBoolean() {

        Utilizador user =
                new Utilizador();

        when(service.needsAcceptance(user))
                .thenReturn(true);

        ResponseEntity<Map<String, Boolean>> result =
                controller.needsAcceptance(user);

        assertEquals(
                true,
                result.getBody().get("needsAcceptance"));
    }

    @Test
    void accept_DeveExecutarService() {

        Utilizador user =
                new Utilizador();

        ResponseEntity<Void> result =
                controller.accept(user);

        assertEquals(200,
                result.getStatusCode().value());

        verify(service)
                .acceptTerms(user);
    }

    @Test
    void publish_DeveRetornarNovaVersao() {

        Utilizador dpo =
                new Utilizador();

        dpo.setId(1L);

        TermsController.PublishTermsRequest request =
                new TermsController.PublishTermsRequest(
                        "pt",
                        "en",
                        "mudanças");

        when(service.publishTerms(
                "pt",
                "en",
                "mudanças",
                1L))
                .thenReturn(5);

        ResponseEntity<Map<String, Integer>> result =
                controller.publish(
                        request,
                        dpo);

        assertEquals(
                5,
                result.getBody().get("version"));
    }
}