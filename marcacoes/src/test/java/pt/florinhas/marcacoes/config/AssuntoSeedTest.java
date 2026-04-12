package pt.florinhas.marcacoes.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
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
    void run_DeveCriarTodosOsAssuntosQuandoNaoExistem() {
        when(assuntoRepository.findByNome("Pagar mensalidade")).thenReturn(Optional.empty());
        when(assuntoRepository.findByNome("Entregar documentos")).thenReturn(Optional.empty());
        when(assuntoRepository.findByNome("Reunião presencial")).thenReturn(Optional.empty());
        when(assuntoRepository.findByNome("Outro")).thenReturn(Optional.empty());

        assuntoSeed.run();

        verify(assuntoRepository, times(4)).save(any(Assunto.class));
    }

    @Test
    void run_NaoDeveCriarAssuntoQuandoJaExiste() {
        when(assuntoRepository.findByNome("Pagar mensalidade"))
                .thenReturn(Optional.of(new Assunto("Pagar mensalidade")));
        when(assuntoRepository.findByNome("Entregar documentos"))
                .thenReturn(Optional.of(new Assunto("Entregar documentos")));
        when(assuntoRepository.findByNome("Reunião presencial"))
                .thenReturn(Optional.of(new Assunto("Reunião presencial")));
        when(assuntoRepository.findByNome("Outro"))
                .thenReturn(Optional.of(new Assunto("Outro")));

        assuntoSeed.run();

        verify(assuntoRepository, never()).save(any(Assunto.class));
    }

    @Test
    void run_DeveCriarApenasOsAssuntosEmFalta() {
        when(assuntoRepository.findByNome("Pagar mensalidade"))
                .thenReturn(Optional.of(new Assunto("Pagar mensalidade")));
        when(assuntoRepository.findByNome("Entregar documentos"))
                .thenReturn(Optional.empty());
        when(assuntoRepository.findByNome("Reunião presencial"))
                .thenReturn(Optional.of(new Assunto("Reunião presencial")));
        when(assuntoRepository.findByNome("Outro"))
                .thenReturn(Optional.empty());

        assuntoSeed.run();

        verify(assuntoRepository, times(2)).save(any(Assunto.class));
    }

    @Test
    void run_DeveIgnorarDataIntegrityViolationException() {
        when(assuntoRepository.findByNome("Pagar mensalidade")).thenReturn(Optional.empty());
        when(assuntoRepository.findByNome("Entregar documentos")).thenReturn(Optional.empty());
        when(assuntoRepository.findByNome("Reunião presencial")).thenReturn(Optional.empty());
        when(assuntoRepository.findByNome("Outro")).thenReturn(Optional.empty());

        when(assuntoRepository.save(ArgumentMatchers.argThat(
                a -> a != null && "Pagar mensalidade".equals(a.getNome()))))
                .thenThrow(new DataIntegrityViolationException("duplicado"));

        assertDoesNotThrow(() -> assuntoSeed.run());

        verify(assuntoRepository, times(4)).findByNome(any(String.class));
        verify(assuntoRepository, times(4)).save(any(Assunto.class));
    }
}