package pt.florinhas.requisicoes.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pt.florinhas.requisicoes.repository.MaterialRepository;

class MaterialSeedTest {

    private MaterialRepository materialRepository;

    private MaterialSeed materialSeed;

    @BeforeEach
    void setUp() {

        materialRepository =
                mock(MaterialRepository.class);

        materialSeed =
                new MaterialSeed(materialRepository);
    }
    @Test
    void run_NaoDeveLancarExcecao() {

        when(materialRepository.findAll())
                .thenReturn(new ArrayList<>());

        assertDoesNotThrow(() ->
                materialSeed.run());

        verify(materialRepository,
                atLeastOnce())
                .save(any());
    }
}