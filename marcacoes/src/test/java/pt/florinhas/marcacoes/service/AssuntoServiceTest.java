package pt.florinhas.marcacoes.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.florinhas.marcacoes.domain.Assunto;
import pt.florinhas.marcacoes.exception.NotFoundException;
import pt.florinhas.marcacoes.repository.AssuntoRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssuntoServiceTest {

    @Mock
    private AssuntoRepository assuntoRepository;

    @InjectMocks
    private AssuntoService assuntoService;

    private Assunto assunto;

    @BeforeEach
    void setup() {
        assunto = new Assunto();
        assunto.setId(1L);
        assunto.setNome("teste");
        assunto.setAtivo(true);
    }

    // =========================
    // listarAtivos
    // =========================

    @Test
    void shouldReturnActiveAssuntos() {
        when(assuntoRepository.findByAtivoTrue()).thenReturn(List.of(assunto));

        List<Assunto> result = assuntoService.listarAtivos();

        assertEquals(1, result.size());
        verify(assuntoRepository).findByAtivoTrue();
    }

    // =========================
    // listarTodos
    // =========================

    @Test
    void shouldReturnAllAssuntos() {
        when(assuntoRepository.findAll()).thenReturn(List.of(assunto));

        List<Assunto> result = assuntoService.listarTodos();

        assertEquals(1, result.size());
        verify(assuntoRepository).findAll();
    }

    // =========================
    // criar
    // =========================

    @Test
    void shouldCreateAssuntoSuccessfully() {
        when(assuntoRepository.findByNome("teste")).thenReturn(Optional.empty());
        when(assuntoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Assunto result = assuntoService.criar("  TESTE ");

        assertEquals("teste", result.getNome());
        assertTrue(result.isAtivo());
        verify(assuntoRepository).save(any());
    }

    @Test
    void shouldThrowWhenAssuntoAlreadyExists() {
        when(assuntoRepository.findByNome("teste")).thenReturn(Optional.of(assunto));

        assertThrows(IllegalArgumentException.class,
                () -> assuntoService.criar("teste"));
    }

    // =========================
    // atualizar
    // =========================

    @Test
    void shouldThrowWhenUpdatingNonExisting() {
        when(assuntoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> assuntoService.atualizar(1L, "novo"));
    }

    @Test
    void shouldUpdateNameSuccessfully() {
        when(assuntoRepository.findById(1L)).thenReturn(Optional.of(assunto));
        when(assuntoRepository.findByNome("novo")).thenReturn(Optional.empty());
        when(assuntoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Assunto result = assuntoService.atualizar(1L, " NOVO ");

        assertEquals("novo", result.getNome());
    }

    @Test
    void shouldAllowSameNameForSameId() {
        when(assuntoRepository.findById(1L)).thenReturn(Optional.of(assunto));
        when(assuntoRepository.findByNome("teste")).thenReturn(Optional.of(assunto));
        when(assuntoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Assunto result = assuntoService.atualizar(1L, "teste");

        assertEquals("teste", result.getNome());
    }

    @Test
    void shouldThrowWhenNameExistsForAnotherId() {
        Assunto other = new Assunto();
        other.setId(2L);

        when(assuntoRepository.findById(1L)).thenReturn(Optional.of(assunto));
        when(assuntoRepository.findByNome("novo")).thenReturn(Optional.of(other));

        assertThrows(IllegalArgumentException.class,
                () -> assuntoService.atualizar(1L, "novo"));
    }

    // =========================
    // apagar
    // =========================

    @Test
    void shouldThrowWhenDeletingNonExisting() {
        when(assuntoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> assuntoService.apagar(1L));
    }

    @Test
    void shouldSoftDeleteAssunto() {
        when(assuntoRepository.findById(1L)).thenReturn(Optional.of(assunto));

        assuntoService.apagar(1L);

        assertFalse(assunto.isAtivo());
        verify(assuntoRepository).save(assunto);
    }

    // =========================
    // setAtivo
    // =========================

    @Test
    void shouldThrowWhenSetAtivoNonExisting() {
        when(assuntoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> assuntoService.setAtivo(1L, true));
    }

    @Test
    void shouldSetAtivoSuccessfully() {
        when(assuntoRepository.findById(1L)).thenReturn(Optional.of(assunto));
        when(assuntoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Assunto result = assuntoService.setAtivo(1L, false);

        assertFalse(result.isAtivo());
    }
}