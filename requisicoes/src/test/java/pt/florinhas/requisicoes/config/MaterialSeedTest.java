package pt.florinhas.requisicoes.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pt.florinhas.requisicoes.repository.MaterialRepository;

class MaterialSeedTest {

    private MaterialRepository repository;

    private MaterialSeed seed;

    @BeforeEach
    void setUp() {

        repository =
                mock(MaterialRepository.class);

        seed =
                new MaterialSeed(repository);

        when(repository.findAll())
                .thenReturn(List.of());
    }

    @Test
    void run_NaoDeveFalhar() {

        assertDoesNotThrow(() ->
                seed.run());
    }
}