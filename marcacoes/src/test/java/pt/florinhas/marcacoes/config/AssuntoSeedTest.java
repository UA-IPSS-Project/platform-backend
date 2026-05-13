package pt.florinhas.marcacoes.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.dao.DataIntegrityViolationException;

import pt.florinhas.marcacoes.domain.Assunto;
import pt.florinhas.marcacoes.repository.AssuntoRepository;

class AssuntoSeedTest {

    private AssuntoRepository assuntoRepository;

    private AssuntoSeed assuntoSeed;

    @BeforeEach
    void setUp() {

        assuntoRepository = mock(AssuntoRepository.class);

        assuntoSeed = new AssuntoSeed(assuntoRepository);
    }

    @Test
    @DisplayName("Deve criar assuntos base quando não existem")
    void run_DeveCriarAssuntosBase() throws Exception {

        when(assuntoRepository.findByNome(anyString()))
                .thenReturn(Optional.empty());

        assuntoSeed.run();

        verify(assuntoRepository, times(4))
                .save(any(Assunto.class));
    }

    @Test
    @DisplayName("Não deve criar assuntos quando já existem")
    void run_NaoDeveCriarAssuntosExistentes() throws Exception {

        when(assuntoRepository.findByNome(anyString()))
                .thenReturn(Optional.of(new Assunto()));

        assuntoSeed.run();

        verify(assuntoRepository, never())
                .save(any());
    }

    @Test
    @DisplayName("Deve ignorar DataIntegrityViolationException")
    void run_DeveIgnorarDataIntegrityViolationException() {

        when(assuntoRepository.findByNome(anyString()))
                .thenReturn(Optional.empty());

        doThrow(DataIntegrityViolationException.class)
                .when(assuntoRepository)
                .save(any(Assunto.class));

        assertDoesNotThrow(() -> assuntoSeed.run());
    }
}