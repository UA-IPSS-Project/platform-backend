package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pt.florinhas.marcacoes.domain.EventoEstado;
import pt.florinhas.marcacoes.domain.ItemArmazem;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.domain.MarcacaoBalneario;
import pt.florinhas.marcacoes.domain.Roupa;
import pt.florinhas.marcacoes.dto.ConsumoEstatisticaDTO;
import pt.florinhas.marcacoes.dto.ItemArmazemDTO;
import pt.florinhas.marcacoes.repository.ItemArmazemRepository;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;
import pt.florinhas.marcacoes.repository.RoupaRepository;

class ArmazemServiceTest {

    private ItemArmazemRepository itemArmazemRepository;
    private MarcacaoRepository marcacaoRepository;
    private RoupaRepository roupaRepository;

    private ArmazemService service;

    @BeforeEach
    void setUp() {

        itemArmazemRepository = mock(ItemArmazemRepository.class);
        marcacaoRepository = mock(MarcacaoRepository.class);
        roupaRepository = mock(RoupaRepository.class);

        service = new ArmazemService(
                itemArmazemRepository,
                marcacaoRepository,
                roupaRepository);
    }

    @Test
    void listarTodos_DeveRetornarLista() {

        ItemArmazem item = new ItemArmazem();
        item.setCategoria("HIGIENE");
        item.setNome("Champô");

        when(itemArmazemRepository.findAllByOrderByCategoriaAscNomeAsc())
                .thenReturn(List.of(item));

        List<ItemArmazemDTO> result = service.listarTodos();

        assertEquals(1, result.size());
        assertEquals("Champô", result.get(0).getNome());
    }

    @Test
    void criarItem_DeveCriarItem() {

        ItemArmazemDTO dto = new ItemArmazemDTO();

        dto.setCategoria(" higiene ");
        dto.setNome(" Champô ");
        dto.setQuantidade(5);

        ItemArmazem saved = new ItemArmazem();

        saved.setCategoria("HIGIENE");
        saved.setNome("Champô");

        when(itemArmazemRepository.findByCategoriaAndNome("HIGIENE", "Champô"))
                .thenReturn(Optional.empty());

        when(itemArmazemRepository.save(any()))
                .thenReturn(saved);

        ItemArmazemDTO result = service.criarItem(dto);

        assertEquals("HIGIENE", result.getCategoria());
        assertEquals("Champô", result.getNome());
    }

    @Test
    void criarItem_DeveLancarErroCategoriaNull() {

        ItemArmazemDTO dto = new ItemArmazemDTO();

        dto.setNome("Champô");

        assertThrows(
                IllegalArgumentException.class,
                () -> service.criarItem(dto));
    }

    @Test
    void criarItem_DeveLancarErroDuplicado() {

        ItemArmazemDTO dto = new ItemArmazemDTO();

        dto.setCategoria("HIGIENE");
        dto.setNome("Champô");

        when(itemArmazemRepository.findByCategoriaAndNome("HIGIENE", "Champô"))
                .thenReturn(Optional.of(new ItemArmazem()));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.criarItem(dto));

        verify(itemArmazemRepository, never()).save(any());
    }

    @Test
    void atualizarItem_DeveAtualizarNome() {

        ItemArmazem existing = new ItemArmazem();
        existing.setNome("Antigo");

        ItemArmazemDTO dto = new ItemArmazemDTO();
        dto.setNome("Novo");

        when(itemArmazemRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        when(itemArmazemRepository.save(any()))
                .thenReturn(existing);

        ItemArmazemDTO result = service.atualizarItem(1L, dto);

        assertEquals("Novo", result.getNome());
    }

    @Test
    void eliminarItem_DeveEliminar() {

        when(itemArmazemRepository.existsById(1L))
                .thenReturn(true);

        when(roupaRepository.existsByItemId(1L))
                .thenReturn(false);

        service.eliminarItem(1L);

        verify(itemArmazemRepository).deleteById(1L);
    }

    @Test
    void eliminarItem_DeveLancarErroQuandoEmUso() {

        when(itemArmazemRepository.existsById(1L))
                .thenReturn(true);

        when(roupaRepository.existsByItemId(1L))
                .thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> service.eliminarItem(1L));
    }

    @Test
    void descontarItens_DeveDescontarStock() {

        ItemArmazem item = new ItemArmazem();
        item.setId(1L);
        item.setQuantidade(10);

        Roupa roupa = new Roupa();
        roupa.setQuantidade(2);
        roupa.setItem(item);

        MarcacaoBalneario balneario = new MarcacaoBalneario();
        balneario.setRoupas(List.of(roupa));

        Marcacao marcacao = new Marcacao();
        marcacao.setMarcacaoBalneario(balneario);

        when(itemArmazemRepository.findById(1L))
                .thenReturn(Optional.of(item));

        List<String> result = service.descontarItens(marcacao);

        assertEquals(0, result.size());
        assertEquals(8, item.getQuantidade());

        verify(itemArmazemRepository).save(item);
    }

    @Test
    void descontarItens_DeveRetornarAvisoStockInsuficiente() {

        ItemArmazem item = new ItemArmazem();
        item.setId(1L);
        item.setQuantidade(1);

        Roupa roupa = new Roupa();
        roupa.setQuantidade(5);
        roupa.setItem(item);

        MarcacaoBalneario balneario = new MarcacaoBalneario();
        balneario.setRoupas(List.of(roupa));

        Marcacao marcacao = new Marcacao();
        marcacao.setMarcacaoBalneario(balneario);

        when(itemArmazemRepository.findById(1L))
                .thenReturn(Optional.of(item));

        List<String> result = service.descontarItens(marcacao);

        assertFalse(result.isEmpty());
        assertEquals(0, item.getQuantidade());
    }

    @Test
    void restaurarItens_DeveRestaurarStock() {

        ItemArmazem item = new ItemArmazem();
        item.setId(1L);
        item.setQuantidade(5);

        Roupa roupa = new Roupa();
        roupa.setQuantidade(2);
        roupa.setItem(item);

        MarcacaoBalneario balneario = new MarcacaoBalneario();
        balneario.setRoupas(List.of(roupa));

        Marcacao marcacao = new Marcacao();
        marcacao.setMarcacaoBalneario(balneario);

        when(itemArmazemRepository.findById(1L))
                .thenReturn(Optional.of(item));

        service.restaurarItens(marcacao);

        assertEquals(7, item.getQuantidade());

        verify(itemArmazemRepository).save(item);
    }

    @Test
    void verificarStock_DeveRetornarEstado() {

        ItemArmazem item = new ItemArmazem();

        item.setQuantidade(10);
        item.setQuantidadeMinima(5);

        when(itemArmazemRepository.findByCategoriaAndNome("HIGIENE", "Champô"))
                .thenReturn(Optional.of(item));

        Map<String, Map<String, Object>> result =
                service.verificarStock(List.of("Champô"));

        assertEquals(true, result.get("Champô").get("tracked"));
        assertEquals("OK", result.get("Champô").get("estado"));
    }

    @Test
    void obterEstatisticas_DeveRetornarDados() {

        ItemArmazem item = new ItemArmazem();

        item.setCategoria("HIGIENE");
        item.setNome("Champô");

        Roupa roupa = new Roupa();
        roupa.setQuantidade(2);
        roupa.setItem(item);

        MarcacaoBalneario balneario = new MarcacaoBalneario();
        balneario.setRoupas(List.of(roupa));

        Marcacao marcacao = new Marcacao();

        marcacao.setEstado(EventoEstado.CONCLUIDO);
        marcacao.setData(LocalDateTime.now());
        marcacao.setMarcacaoBalneario(balneario);

        when(marcacaoRepository.findMarcacoesBetweenDates(any(), any(), any()))
                .thenReturn(List.of(marcacao));

        ConsumoEstatisticaDTO result =
                service.obterEstatisticas("MES");

        assertEquals(2, result.getTotalGeral());
        assertEquals(1, result.getItens().size());
    }

    @Test
    void inicializarDadosPadrao_DeveIgnorarQuandoJaExistemDados() {

        when(itemArmazemRepository.count())
                .thenReturn(1L);

        service.inicializarDadosPadrao();

        verify(itemArmazemRepository, never()).save(any());
    }
}