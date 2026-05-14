package pt.florinhas.requisicoes.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pt.florinhas.requisicoes.repository.TransporteRepository;

class TransporteSeedTest {

    private TransporteRepository transporteRepository;

    private TransporteSeed transporteSeed;

    @BeforeEach
    void setUp() {

        transporteRepository =
                mock(TransporteRepository.class);

        transporteSeed =
                new TransporteSeed(transporteRepository);
    }
    @Test
    void run_NaoDeveLancarExcecao() {

        when(transporteRepository.findAll())
                .thenReturn(new ArrayList<>());

        when(transporteRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() ->
                transporteSeed.run());

        verify(transporteRepository,
                atLeastOnce())
                .save(any());
    }
}