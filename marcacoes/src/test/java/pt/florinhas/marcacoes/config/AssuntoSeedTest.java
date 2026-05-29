package pt.florinhas.marcacoes.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import pt.florinhas.marcacoes.repository.AssuntoRepository;

class AssuntoSeedTest {

    private AssuntoRepository repository;
    private AssuntoSeed seed;

    @BeforeEach
    void setUp() {

        repository =
                mock(AssuntoRepository.class);

        seed =
                new AssuntoSeed(repository);
    }

    @Test
    void run_DeveCriarAssuntos() throws Exception {

        when(repository.findByNome("Pagar mensalidade"))
                .thenReturn(Optional.empty());

        when(repository.findByNome("Entregar documentos"))
                .thenReturn(Optional.empty());

        when(repository.findByNome("Reunião presencial"))
                .thenReturn(Optional.empty());

        when(repository.findByNome("Outro"))
                .thenReturn(Optional.empty());

        seed.run();

        verify(repository, org.mockito.Mockito.times(4))
        .save(org.mockito.ArgumentMatchers.any());

        verify(repository)
                .findByNome("Outro");
    }

    @Test
    void run_DeveIgnorarDuplicados() {

        when(repository.findByNome(org.mockito.ArgumentMatchers.any()))
                .thenReturn(Optional.empty());

        when(repository.save(org.mockito.ArgumentMatchers.any()))
                .thenThrow(
                        new DataIntegrityViolationException("dup"));

        assertDoesNotThrow(
                () -> seed.run());
    }
}