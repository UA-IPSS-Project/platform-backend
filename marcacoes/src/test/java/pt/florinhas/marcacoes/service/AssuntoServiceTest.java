package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pt.florinhas.marcacoes.domain.Assunto;
import pt.florinhas.marcacoes.exception.NotFoundException;
import pt.florinhas.marcacoes.repository.AssuntoRepository;

class AssuntoServiceTest {

    private AssuntoRepository repository;

    private AssuntoService service;

    @BeforeEach
    void setUp() {

        repository = mock(AssuntoRepository.class);

        service = new AssuntoService(repository);
    }

    @Test
    void listarAtivos_DeveRetornarLista() {

        when(repository.findByAtivoTrue())
                .thenReturn(List.of(new Assunto()));

        List<Assunto> result = service.listarAtivos();

        assertEquals(1, result.size());
    }

    @Test
    void listarTodos_DeveRetornarLista() {

        when(repository.findAll())
                .thenReturn(List.of(new Assunto()));

        List<Assunto> result = service.listarTodos();

        assertEquals(1, result.size());
    }

    @Test
    void criar_DeveCriarAssunto() {

        Assunto assunto = new Assunto();
        assunto.setNome("consulta");

        when(repository.findByNome("consulta"))
                .thenReturn(Optional.empty());

        when(repository.save(any()))
                .thenReturn(assunto);

        Assunto result = service.criar(" Consulta ");

        assertEquals("consulta", result.getNome());
    }

    @Test
    void criar_DeveLancarErroQuandoDuplicado() {

        when(repository.findByNome("consulta"))
                .thenReturn(Optional.of(new Assunto()));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.criar("consulta"));

        verify(repository, never()).save(any());
    }

    @Test
    void atualizar_DeveAtualizarNome() {

        Assunto assunto = new Assunto();

        assunto.setId(1L);
        assunto.setNome("antigo");

        when(repository.findById(1L))
                .thenReturn(Optional.of(assunto));

        when(repository.findByNome("novo"))
                .thenReturn(Optional.empty());

        when(repository.save(any()))
                .thenReturn(assunto);

        Assunto result = service.atualizar(1L, " novo ");

        assertEquals("novo", result.getNome());
    }

    @Test
    void atualizar_DeveLancarErroQuandoNaoExiste() {

        when(repository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> service.atualizar(1L, "novo"));
    }

    @Test
    void apagar_DeveDesativarAssunto() {

        Assunto assunto = new Assunto();

        assunto.setAtivo(true);

        when(repository.findById(1L))
                .thenReturn(Optional.of(assunto));

        service.apagar(1L);

        assertEquals(false, assunto.isAtivo());

        verify(repository).save(assunto);
    }

    @Test
    void setAtivo_DeveAtualizarEstado() {

        Assunto assunto = new Assunto();

        when(repository.findById(1L))
                .thenReturn(Optional.of(assunto));

        when(repository.save(any()))
                .thenReturn(assunto);

        Assunto result = service.setAtivo(1L, true);

        assertEquals(true, result.isAtivo());
    }
}