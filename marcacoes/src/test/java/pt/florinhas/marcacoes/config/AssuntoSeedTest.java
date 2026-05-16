package pt.florinhas.marcacoes.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import pt.florinhas.marcacoes.domain.Assunto;
import pt.florinhas.marcacoes.repository.AssuntoRepository;

@ExtendWith(MockitoExtension.class)
class AssuntoSeedTest {

    @Mock
    private AssuntoRepository assuntoRepository;

    private AssuntoSeed assuntoSeed;

    @BeforeEach
    void setUp() {
        assuntoSeed = new AssuntoSeed(assuntoRepository);
    }

    @Test
    @DisplayName("Deve criar todos os assuntos quando não existem")
    void run_DeveCriarTodosOsAssuntosQuandoNaoExistem() {
        when(assuntoRepository.findByNome(anyString())).thenReturn(Optional.empty());

        assuntoSeed.run();

        verify(assuntoRepository, times(4)).save(any(Assunto.class));
    }

    @Test
    @DisplayName("Não deve criar assuntos quando já existem")
    void run_NaoDeveCriarAssuntoQuandoJaExiste() {
        when(assuntoRepository.findByNome(anyString()))
                .thenReturn(Optional.of(new Assunto("Existente")));

        assuntoSeed.run();

        verify(assuntoRepository, never()).save(any(Assunto.class));
    }

    @Test
    @DisplayName("Deve ignorar DataIntegrityViolationException")
    void run_DeveIgnorarDataIntegrityViolationException() {
        when(assuntoRepository.findByNome(anyString())).thenReturn(Optional.empty());
        doThrow(new DataIntegrityViolationException("duplicado"))
                .when(assuntoRepository).save(any(Assunto.class));

        assertDoesNotThrow(() -> assuntoSeed.run());

        verify(assuntoRepository, times(4)).save(any(Assunto.class));
    }
}