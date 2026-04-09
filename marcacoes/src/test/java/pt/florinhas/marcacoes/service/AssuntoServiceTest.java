package pt.florinhas.marcacoes.service;

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

    @Test
    void testCriarAssuntoComSucesso() {
        String nome = "  Novo Assunto  ";
        String nomeNormalizado = "novo assunto";
        Assunto assunto = new Assunto();
        assunto.setNome(nomeNormalizado);
        assunto.setAtivo(true);

        when(assuntoRepository.findByNome(nomeNormalizado)).thenReturn(Optional.empty());
        when(assuntoRepository.save(any(Assunto.class))).thenAnswer(invocation -> {
            Assunto a = invocation.getArgument(0);
            a.setId(1L);
            return a;
        });

        Assunto resultado = assuntoService.criar(nome);

        assertNotNull(resultado);
        assertEquals(nomeNormalizado, resultado.getNome());
        assertTrue(resultado.isAtivo());
        verify(assuntoRepository).findByNome(nomeNormalizado);
        verify(assuntoRepository).save(any(Assunto.class));
    }

    @Test
    void testCriarAssuntoDuplicado() {
        String nome = "Assunto Existente";
        String nomeNormalizado = "assunto existente";
        when(assuntoRepository.findByNome(nomeNormalizado)).thenReturn(Optional.of(new Assunto()));

        assertThrows(IllegalArgumentException.class, () -> assuntoService.criar(nome));

        verify(assuntoRepository).findByNome(nomeNormalizado);
        verify(assuntoRepository, never()).save(any(Assunto.class));
    }

    @Test
    void testAtualizarAssuntoComSucesso() {
        Long id = 1L;
        String novoNome = "  Nome Atualizado  ";
        String nomeNormalizado = "nome atualizado";
        Assunto existente = new Assunto();
        existente.setId(id);
        existente.setNome("nome antigo");

        when(assuntoRepository.findById(id)).thenReturn(Optional.of(existente));
        when(assuntoRepository.findByNome(nomeNormalizado)).thenReturn(Optional.empty());
        when(assuntoRepository.save(any(Assunto.class))).thenReturn(existente);

        Assunto resultado = assuntoService.atualizar(id, novoNome);

        assertNotNull(resultado);
        assertEquals(nomeNormalizado, resultado.getNome());
        verify(assuntoRepository).findById(id);
        verify(assuntoRepository).findByNome(nomeNormalizado);
        verify(assuntoRepository).save(existente);
    }

    @Test
    void testAtualizarAssuntoNaoEncontrado() {
        Long id = 1L;
        String novoNome = "Novo Nome";

        when(assuntoRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> assuntoService.atualizar(id, novoNome));

        verify(assuntoRepository).findById(id);
        verify(assuntoRepository, never()).save(any(Assunto.class));
    }

    @Test
    void testAtualizarAssuntoParaNomeDuplicado() {
        Long id = 1L;
        String novoNome = "Nome Duplicado";
        String nomeNormalizado = "nome duplicado";

        Assunto existente = new Assunto();
        existente.setId(id);
        existente.setNome("nome antigo");

        Assunto outro = new Assunto();
        outro.setId(2L);
        outro.setNome(nomeNormalizado);

        when(assuntoRepository.findById(id)).thenReturn(Optional.of(existente));
        when(assuntoRepository.findByNome(nomeNormalizado)).thenReturn(Optional.of(outro));

        assertThrows(IllegalArgumentException.class, () -> assuntoService.atualizar(id, novoNome));

        verify(assuntoRepository).findById(id);
        verify(assuntoRepository).findByNome(nomeNormalizado);
        verify(assuntoRepository, never()).save(any(Assunto.class));
    }

    @Test
    void testListarAtivos() {
        Assunto a1 = new Assunto();
        a1.setAtivo(true);
        Assunto a2 = new Assunto();
        a2.setAtivo(true);
        when(assuntoRepository.findByAtivoTrue()).thenReturn(List.of(a1, a2));

        List<Assunto> resultado = assuntoService.listarAtivos();

        assertEquals(2, resultado.size());
        verify(assuntoRepository).findByAtivoTrue();
    }

    @Test
    void testListarTodos() {
        Assunto a1 = new Assunto();
        Assunto a2 = new Assunto();
        when(assuntoRepository.findAll()).thenReturn(List.of(a1, a2));

        List<Assunto> resultado = assuntoService.listarTodos();

        assertEquals(2, resultado.size());
        verify(assuntoRepository).findAll();
    }
}
