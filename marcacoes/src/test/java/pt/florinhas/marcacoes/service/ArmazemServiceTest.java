package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.florinhas.marcacoes.domain.ItemArmazem;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.domain.MarcacaoBalneario;
import pt.florinhas.marcacoes.domain.Roupa;
import pt.florinhas.marcacoes.dto.ItemArmazemDTO;
import pt.florinhas.marcacoes.repository.ItemArmazemRepository;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;
import pt.florinhas.marcacoes.repository.RoupaRepository;

@ExtendWith(MockitoExtension.class)
class ArmazemServiceTest {

    @Mock
    private ItemArmazemRepository itemArmazemRepository;
    @Mock
    private MarcacaoRepository marcacaoRepository;
    @Mock
    private RoupaRepository roupaRepository;

    private ArmazemService armazemService;

    @BeforeEach
    void setUp() {
        armazemService = new ArmazemService(itemArmazemRepository, marcacaoRepository, roupaRepository);
    }

    @Nested
    @DisplayName("Testes de CRUD")
    class CRUDTests {
        @Test
        @DisplayName("Deve listar todos os itens ordenados")
        void listarTodos_DeveRetornarListaOrdenada() {
            ItemArmazem item = new ItemArmazem();
            item.setCategoria("HIGIENE");
            item.setNome("Champô");
            when(itemArmazemRepository.findAllByOrderByCategoriaAscNomeAsc()).thenReturn(List.of(item));

            List<ItemArmazemDTO> result = armazemService.listarTodos();

            assertFalse(result.isEmpty());
            assertEquals("Champô", result.get(0).getNome());
        }

        @Test
        @DisplayName("Deve criar novo item com sucesso")
        void criarItem_DeveGuardarESalvar() {
            ItemArmazemDTO dto = new ItemArmazemDTO();
            dto.setCategoria("HIGIENE");
            dto.setNome("Novo Item");

            when(itemArmazemRepository.findByCategoriaAndNome(anyString(), anyString())).thenReturn(Optional.empty());
            when(itemArmazemRepository.save(any(ItemArmazem.class))).thenAnswer(i -> i.getArgument(0));

            ItemArmazemDTO result = armazemService.criarItem(dto);

            assertNotNull(result);
            assertEquals("HIGIENE", result.getCategoria());
            verify(itemArmazemRepository).save(any(ItemArmazem.class));
        }
    }

    @Nested
    @DisplayName("Testes de Stock")
    class StockTests {
        @Test
        @DisplayName("Deve descontar itens do stock ao marcar presença")
        void descontarItens_DeveReduzirQuantidade() {
            ItemArmazem item = new ItemArmazem();
            item.setId(1L);
            item.setQuantidade(10);
            item.setNome("Item");

            Roupa roupa = new Roupa();
            roupa.setItem(item);
            roupa.setQuantidade(2);

            MarcacaoBalneario bal = new MarcacaoBalneario();
            bal.setRoupas(List.of(roupa));
            Marcacao m = new Marcacao();
            m.setMarcacaoBalneario(bal);

            when(itemArmazemRepository.findById(1L)).thenReturn(Optional.of(item));

            armazemService.descontarItens(m);

            assertEquals(8, item.getQuantidade());
            verify(itemArmazemRepository).save(item);
        }
    }
}