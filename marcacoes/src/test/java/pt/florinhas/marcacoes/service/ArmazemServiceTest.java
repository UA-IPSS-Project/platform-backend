package pt.florinhas.marcacoes.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.florinhas.marcacoes.domain.ItemArmazem;
import pt.florinhas.marcacoes.dto.ItemArmazemDTO;
import pt.florinhas.marcacoes.repository.ItemArmazemRepository;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;
import pt.florinhas.marcacoes.repository.RoupaRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArmazemServiceTest {

    @Mock private ItemArmazemRepository itemRepo;
    @Mock private MarcacaoRepository marcacaoRepo;
    @Mock private RoupaRepository roupaRepo;

    @InjectMocks
    private ArmazemService service;

    private ItemArmazem item;

    @BeforeEach
    void setup() {
        item = new ItemArmazem();
        item.setId(1L);
        item.setCategoria("HIGIENE");
        item.setNome("Champô");
        item.setQuantidade(10);
        item.setQuantidadeMinima(5);
        item.setUnidade("un");
    }

    @Test
    void shouldListAll() {
        when(itemRepo.findAllByOrderByCategoriaAscNomeAsc()).thenReturn(List.of(item));

        var result = service.listarTodos();

        assertEquals(1, result.size());
        assertEquals("Champô", result.get(0).getNome());
    }

    @Test
    void shouldCreateItem() {
        ItemArmazemDTO dto = new ItemArmazemDTO();
        dto.setCategoria(" higiene ");
        dto.setNome(" Champô ");

        when(itemRepo.findByCategoriaAndNome("HIGIENE", "Champô")).thenReturn(Optional.empty());
        when(itemRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = service.criarItem(dto);

        assertEquals("HIGIENE", result.getCategoria());
        assertEquals("Champô", result.getNome());
    }

    @Test
    void shouldThrowWhenDuplicateItem() {
        ItemArmazemDTO dto = new ItemArmazemDTO();
        dto.setCategoria("HIGIENE");
        dto.setNome("Champô");

        when(itemRepo.findByCategoriaAndNome("HIGIENE", "Champô")).thenReturn(Optional.of(item));

        assertThrows(IllegalArgumentException.class,
                () -> service.criarItem(dto));
    }

    @Test
    void shouldUpdateItem() {
        ItemArmazemDTO dto = new ItemArmazemDTO();
        dto.setNome("Novo Nome");

        when(itemRepo.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = service.atualizarItem(1L, dto);

        assertEquals("Novo Nome", result.getNome());
    }

    @Test
    void shouldThrowWhenUpdateNotFound() {
        when(itemRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.atualizarItem(1L, new ItemArmazemDTO()));
    }

    @Test
    void shouldDeleteItem() {
        when(itemRepo.existsById(1L)).thenReturn(true);
        when(roupaRepo.existsByItemId(1L)).thenReturn(false);

        service.eliminarItem(1L);

        verify(itemRepo).deleteById(1L);
    }

    @Test
    void shouldThrowWhenDeleteNotFound() {
        when(itemRepo.existsById(1L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> service.eliminarItem(1L));
    }

    @Test
    void shouldThrowWhenDeleteHasReferences() {
        when(itemRepo.existsById(1L)).thenReturn(true);
        when(roupaRepo.existsByItemId(1L)).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> service.eliminarItem(1L));
    }
}