package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import pt.florinhas.marcacoes.domain.Assunto;
import pt.florinhas.marcacoes.exception.NotFoundException;
import pt.florinhas.marcacoes.repository.AssuntoRepository;

class AssuntoServiceTest {

    private AssuntoRepository assuntoRepository;
    private AssuntoService assuntoService;

    @BeforeEach
    void setup() {

        assuntoRepository =
                Mockito.mock(AssuntoRepository.class);

        assuntoService =
                new AssuntoService(assuntoRepository);
    }

    @Test
    void listarAtivos_DeveRetornarLista() {

        when(assuntoRepository.findByAtivoTrue())
                .thenReturn(List.of(new Assunto()));

        List<Assunto> resultado =
                assuntoService.listarAtivos();

        assertEquals(1, resultado.size());
    }

    @Test
    void listarTodos_DeveRetornarLista() {

        when(assuntoRepository.findAll())
                .thenReturn(List.of(new Assunto()));

        List<Assunto> resultado =
                assuntoService.listarTodos();

        assertEquals(1, resultado.size());
    }

    @Test
    void criar_DeveCriarAssunto() {

        Assunto assunto = new Assunto();

        when(assuntoRepository.findByNome("consulta"))
                .thenReturn(Optional.empty());

        when(assuntoRepository.save(any()))
                .thenReturn(assunto);

        Assunto resultado =
                assuntoService.criar(" Consulta ");

        assertNotNull(resultado);

        verify(assuntoRepository)
                .save(any());
    }

    @Test
    void criar_DeveLancarErroQuandoDuplicado() {

        when(assuntoRepository.findByNome("consulta"))
                .thenReturn(Optional.of(new Assunto()));

        assertThrows(
                IllegalArgumentException.class,
                () -> assuntoService.criar("consulta")
        );
    }

    @Test
    void atualizar_DeveAtualizarNome() {

        Assunto assunto = new Assunto();
        assunto.setId(1L);

        when(assuntoRepository.findById(1L))
                .thenReturn(Optional.of(assunto));

        when(assuntoRepository.findByNome("novo"))
                .thenReturn(Optional.empty());

        when(assuntoRepository.save(any()))
                .thenReturn(assunto);

        Assunto resultado =
                assuntoService.atualizar(
                        1L,
                        "novo"
                );

        assertNotNull(resultado);
    }

    @Test
    void atualizar_DeveLancarErroQuandoNaoExiste() {

        when(assuntoRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> assuntoService.atualizar(1L, "novo")
        );
    }

    @Test
    void apagar_DeveDesativarAssunto() {

        Assunto assunto = new Assunto();

        when(assuntoRepository.findById(1L))
                .thenReturn(Optional.of(assunto));

        assuntoService.apagar(1L);

        assertFalse(assunto.isAtivo());

        verify(assuntoRepository)
                .save(assunto);
    }

    @Test
    void setAtivo_DeveAtualizarEstado() {

        Assunto assunto = new Assunto();

        when(assuntoRepository.findById(1L))
                .thenReturn(Optional.of(assunto));

        when(assuntoRepository.save(any()))
                .thenReturn(assunto);

        Assunto resultado =
                assuntoService.setAtivo(
                        1L,
                        true
                );

        assertTrue(resultado.isAtivo());
    }
}