package pt.florinhas.requisicoes.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pt.florinhas.requisicoes.repository.ManutencaoItemRepository;

class ManutencaoItemSeedTest {

    private ManutencaoItemRepository manutencaoItemRepository;

    private ManutencaoItemSeed manutencaoItemSeed;

    @BeforeEach
    void setUp() {

        manutencaoItemRepository =
                mock(ManutencaoItemRepository.class);

        manutencaoItemSeed =
                new ManutencaoItemSeed(
                        manutencaoItemRepository);
    }

    @Test
    void run_NaoDeveLancarExcecao() {

        when(manutencaoItemRepository.findAll())
                .thenReturn(new ArrayList<>());

        when(manutencaoItemRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() ->
                manutencaoItemSeed.run());

        verify(manutencaoItemRepository,
                atLeastOnce())
                .save(any());
    }
}