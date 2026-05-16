package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
            item.setId(1L);
            item.setCategoria("HIGIENE");
            item.setNome("Champô");
            item.setQuantidade(10);
            item.setQuantidadeMinima(5);
            item.setUnidade("un");

            when(itemArmazemRepository.findAllByOrderByCategoriaAscNomeAsc()).thenReturn(List.of(item));

            List<ItemArmazemDTO> result = armazemService.listarTodos();

            assertFalse(result.isEmpty());
            assertEquals("Champô", result.get(0).getNome());
            assertEquals("HIGIENE", result.get(0).getCategoria());
            assertEquals("OK", result.get(0).getEstado());
        }

        @Test
        @DisplayName("Deve listar itens por categoria")
        void listarPorCategoria_DeveFiltrarCorretamente() {
            ItemArmazem item = new ItemArmazem();
            item.setCategoria("VESTUARIO");
            item.setNome("T-shirt");
            item.setQuantidade(2);
            item.setQuantidadeMinima(5);

            when(itemArmazemRepository.findByCategoria("VESTUARIO")).thenReturn(List.of(item));

            List<ItemArmazemDTO> result = armazemService.listarPorCategoria("VESTUARIO");

            assertEquals(1, result.size());
            assertEquals("T-shirt", result.get(0).getNome());
            assertEquals("BAIXO", result.get(0).getEstado());
        }

        @Test
        @DisplayName("Deve criar novo item com sucesso")
        void criarItem_DeveGuardarESalvar() {
            ItemArmazemDTO dto = new ItemArmazemDTO();
            dto.setCategoria("HIGIENE");
            dto.setNome("Novo Item");
            dto.setQuantidade(10);
            dto.setQuantidadeMinima(5);
            dto.setUnidade("un");
            dto.setMarca("Dove");
            dto.setTamanho("M");
            dto.setVolume(0.5);
            dto.setDescricao("Desc");

            when(itemArmazemRepository.findByCategoriaAndNome("HIGIENE", "Novo Item")).thenReturn(Optional.empty());
            when(itemArmazemRepository.save(any(ItemArmazem.class))).thenAnswer(i -> i.getArgument(0));

            ItemArmazemDTO result = armazemService.criarItem(dto);

            assertNotNull(result);
            assertEquals("HIGIENE", result.getCategoria());
            assertEquals("Novo Item", result.getNome());
            assertEquals(10, result.getQuantidade());
            assertEquals(5, result.getQuantidadeMinima());
            assertEquals("Dove", result.getMarca());
            assertEquals("M", result.getTamanho());
            assertEquals(0.5, result.getVolume());
            assertEquals("Desc", result.getDescricao());
            verify(itemArmazemRepository).save(any(ItemArmazem.class));
        }

        @Test
        @DisplayName("Criar item deve lançar exceção se categoria for nula ou vazia")
        void criarItem_CategoriaVazia_DeveLancarException() {
            ItemArmazemDTO dto = new ItemArmazemDTO();
            dto.setNome("Item");

            assertThrows(IllegalArgumentException.class, () -> armazemService.criarItem(dto));

            dto.setCategoria("   ");
            assertThrows(IllegalArgumentException.class, () -> armazemService.criarItem(dto));
        }

        @Test
        @DisplayName("Criar item deve lançar exceção se nome for nulo ou vazio")
        void criarItem_NomeVazio_DeveLancarException() {
            ItemArmazemDTO dto = new ItemArmazemDTO();
            dto.setCategoria("HIGIENE");

            assertThrows(IllegalArgumentException.class, () -> armazemService.criarItem(dto));

            dto.setNome("   ");
            assertThrows(IllegalArgumentException.class, () -> armazemService.criarItem(dto));
        }

        @Test
        @DisplayName("Criar item deve lançar exceção se item com mesma categoria/nome já existe")
        void criarItem_ItemDuplicado_DeveLancarException() {
            ItemArmazemDTO dto = new ItemArmazemDTO();
            dto.setCategoria("HIGIENE");
            dto.setNome("Duplicado");

            when(itemArmazemRepository.findByCategoriaAndNome("HIGIENE", "Duplicado"))
                    .thenReturn(Optional.of(new ItemArmazem()));

            assertThrows(IllegalArgumentException.class, () -> armazemService.criarItem(dto));
        }

        @Test
        @DisplayName("Deve atualizar item com sucesso")
        void atualizarItem_ComSucesso() {
            ItemArmazem item = new ItemArmazem();
            item.setId(1L);
            item.setCategoria("HIGIENE");
            item.setNome("Antigo");
            item.setQuantidade(5);
            item.setQuantidadeMinima(2);

            ItemArmazemDTO dto = new ItemArmazemDTO();
            dto.setCategoria("VESTUARIO");
            dto.setNome("Novo Nome");
            dto.setQuantidade(10);
            dto.setQuantidadeMinima(4);
            dto.setUnidade("un");
            dto.setMarca("Florinhas");
            dto.setTamanho("L");
            dto.setVolume(1.0);
            dto.setDescricao("Nova desc");

            when(itemArmazemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(itemArmazemRepository.save(any(ItemArmazem.class))).thenAnswer(i -> i.getArgument(0));

            ItemArmazemDTO result = armazemService.atualizarItem(1L, dto);

            assertNotNull(result);
            assertEquals("VESTUARIO", item.getCategoria());
            assertEquals("Novo Nome", item.getNome());
            assertEquals(10, item.getQuantidade());
            assertEquals(4, item.getQuantidadeMinima());
            assertEquals("un", item.getUnidade());
            assertEquals("Florinhas", item.getMarca());
            assertEquals("L", item.getTamanho());
            assertEquals(1.0, item.getVolume());
            assertEquals("Nova desc", item.getDescricao());
        }

        @Test
        @DisplayName("Atualizar item deve lançar exceção se item não encontrado")
        void atualizarItem_ItemNaoEncontrado_DeveLancarException() {
            when(itemArmazemRepository.findById(1L)).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class, () -> armazemService.atualizarItem(1L, new ItemArmazemDTO()));
        }

        @Test
        @DisplayName("Atualizar item deve lançar exceção se categoria for vazia")
        void atualizarItem_CategoriaVazia_DeveLancarException() {
            ItemArmazem item = new ItemArmazem();
            when(itemArmazemRepository.findById(1L)).thenReturn(Optional.of(item));

            ItemArmazemDTO dto = new ItemArmazemDTO();
            dto.setCategoria("   ");

            assertThrows(IllegalArgumentException.class, () -> armazemService.atualizarItem(1L, dto));
        }

        @Test
        @DisplayName("Atualizar item deve lançar exceção se nome for vazio")
        void atualizarItem_NomeVazio_DeveLancarException() {
            ItemArmazem item = new ItemArmazem();
            when(itemArmazemRepository.findById(1L)).thenReturn(Optional.of(item));

            ItemArmazemDTO dto = new ItemArmazemDTO();
            dto.setNome("   ");

            assertThrows(IllegalArgumentException.class, () -> armazemService.atualizarItem(1L, dto));
        }

        @Test
        @DisplayName("Deve eliminar item com sucesso")
        void eliminarItem_ComSucesso() {
            when(itemArmazemRepository.existsById(1L)).thenReturn(true);
            when(roupaRepository.existsByItemId(1L)).thenReturn(false);

            armazemService.eliminarItem(1L);

            verify(itemArmazemRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Eliminar item deve lançar exceção se não existe")
        void eliminarItem_NaoExiste_DeveLancarException() {
            when(itemArmazemRepository.existsById(1L)).thenReturn(false);
            assertThrows(IllegalArgumentException.class, () -> armazemService.eliminarItem(1L));
        }

        @Test
        @DisplayName("Eliminar item deve lançar exceção se já referenciado por marcações")
        void eliminarItem_Referenciado_DeveLancarException() {
            when(itemArmazemRepository.existsById(1L)).thenReturn(true);
            when(roupaRepository.existsByItemId(1L)).thenReturn(true);

            assertThrows(IllegalStateException.class, () -> armazemService.eliminarItem(1L));
        }
    }

    @Nested
    @DisplayName("Testes de Desconto e Restauro de Stock")
    class StockDiscountTests {

        @Test
        @DisplayName("Descontar itens deve retornar vazio se não houver detalhes balneários")
        void descontarItens_SemDetalhesBalnearios_DeveRetornarVazio() {
            Marcacao m = new Marcacao();
            List<String> avisos = armazemService.descontarItens(m);
            assertTrue(avisos.isEmpty());
        }

        @Test
        @DisplayName("Deve descontar itens do stock ao marcar presença por ID de item")
        void descontarItens_DeveReduzirQuantidade() {
            ItemArmazem item = new ItemArmazem();
            item.setId(1L);
            item.setQuantidade(10);
            item.setNome("Champô");

            Roupa roupa = new Roupa();
            roupa.setItem(item);
            roupa.setQuantidade(2);

            MarcacaoBalneario bal = new MarcacaoBalneario();
            bal.setRoupas(List.of(roupa));
            Marcacao m = new Marcacao();
            m.setMarcacaoBalneario(bal);

            when(itemArmazemRepository.findById(1L)).thenReturn(Optional.of(item));

            List<String> avisos = armazemService.descontarItens(m);

            assertTrue(avisos.isEmpty());
            assertEquals(8, item.getQuantidade());
            verify(itemArmazemRepository).save(item);
        }

        @Test
        @DisplayName("Descontar itens com stock insuficiente deve zerar e gerar avisos")
        void descontarItens_StockInsuficiente_DeveZerarEGerarAviso() {
            ItemArmazem item = new ItemArmazem();
            item.setId(1L);
            item.setQuantidade(1);
            item.setNome("Toalha");

            Roupa roupa = new Roupa();
            roupa.setItem(item);
            roupa.setQuantidade(3);

            MarcacaoBalneario bal = new MarcacaoBalneario();
            bal.setRoupas(List.of(roupa));
            Marcacao m = new Marcacao();
            m.setMarcacaoBalneario(bal);

            when(itemArmazemRepository.findById(1L)).thenReturn(Optional.of(item));

            List<String> avisos = armazemService.descontarItens(m);

            assertFalse(avisos.isEmpty());
            assertEquals(0, item.getQuantidade());
            assertTrue(avisos.get(0).contains("Stock insuficiente"));
        }

        @Test
        @DisplayName("Descontar itens sem ID de item (Modo Legado) deve buscar por categoria/nome")
        void descontarItens_SemIdLegado_DeveMapearEBuscar() {
            ItemArmazem item = new ItemArmazem();
            item.setId(10L);
            item.setCategoria("HIGIENE");
            item.setNome("Champô");
            item.setQuantidade(10);

            Roupa roupa = new Roupa();
            roupa.setCategoria("Champô");
            roupa.setQuantidade(1);

            MarcacaoBalneario bal = new MarcacaoBalneario();
            bal.setRoupas(List.of(roupa));
            Marcacao m = new Marcacao();
            m.setMarcacaoBalneario(bal);

            when(itemArmazemRepository.findByCategoriaAndNome("HIGIENE", "Champô")).thenReturn(Optional.of(item));

            armazemService.descontarItens(m);

            assertEquals(9, item.getQuantidade());
        }

        @Test
        @DisplayName("Descontar sapatos sem ID de item deve buscar em CALCADO com nome do tamanho")
        void descontarItens_SapatoSemId_DeveMapearParaCalcado() {
            ItemArmazem item = new ItemArmazem();
            item.setId(20L);
            item.setCategoria("CALCADO");
            item.setNome("42");
            item.setQuantidade(5);

            Roupa roupa = new Roupa();
            roupa.setCategoria("Sapatos/Sapatilhas");
            roupa.setTamanho("42");
            roupa.setQuantidade(2);

            MarcacaoBalneario bal = new MarcacaoBalneario();
            bal.setRoupas(List.of(roupa));
            Marcacao m = new Marcacao();
            m.setMarcacaoBalneario(bal);

            when(itemArmazemRepository.findByCategoriaAndNome("CALCADO", "42")).thenReturn(Optional.of(item));

            armazemService.descontarItens(m);

            assertEquals(3, item.getQuantidade());
        }

        @Test
        @DisplayName("Descontar item legível sem categoria mapeada deve buscar em todas as categorias geridas")
        void descontarItens_ItemSemCategoriaMapeada_DeveBuscarEmOutrasCategorias() {
            ItemArmazem item = new ItemArmazem();
            item.setId(30L);
            item.setCategoria("HIGIENE");
            item.setNome("Sabonete/Creme");
            item.setQuantidade(5);

            Roupa roupa = new Roupa();
            roupa.setCategoria("Sabonete/Creme");
            roupa.setQuantidade(1);

            MarcacaoBalneario bal = new MarcacaoBalneario();
            bal.setRoupas(List.of(roupa));
            Marcacao m = new Marcacao();
            m.setMarcacaoBalneario(bal);

            // Mapeamentos de FORM_TO_CATEGORIA não contêm "Sabonete/Creme" logo cai no
            // fallback do loop de categorias
            when(itemArmazemRepository.findByCategoriaAndNome("HIGIENE", "Sabonete/Creme"))
                    .thenReturn(Optional.of(item));

            armazemService.descontarItens(m);

            assertEquals(4, item.getQuantidade());
        }

        @Test
        @DisplayName("Restaurar itens deve retornar imediatamente se não houver detalhes balneários")
        void restaurarItens_SemDetalhesBalnearios_DeveFazerNada() {
            Marcacao m = new Marcacao();
            armazemService.restaurarItens(m);
            verifyNoInteractions(itemArmazemRepository);
        }

        @Test
        @DisplayName("Deve restaurar itens ao stock com sucesso")
        void restaurarItens_ComIdDoItem_DeveAdicionarAoStock() {
            ItemArmazem item = new ItemArmazem();
            item.setId(1L);
            item.setQuantidade(5);
            item.setNome("Champô");

            Roupa roupa = new Roupa();
            roupa.setItem(item);
            roupa.setQuantidade(3);

            MarcacaoBalneario bal = new MarcacaoBalneario();
            bal.setRoupas(List.of(roupa));
            Marcacao m = new Marcacao();
            m.setMarcacaoBalneario(bal);

            when(itemArmazemRepository.findById(1L)).thenReturn(Optional.of(item));

            armazemService.restaurarItens(m);

            assertEquals(8, item.getQuantidade());
            verify(itemArmazemRepository).save(item);
        }

        @Test
        @DisplayName("Restaurar sapatos sem ID de item (Legado) deve repor em CALCADO")
        void restaurarItens_SapatoSemId_DeveReporNoCalcado() {
            ItemArmazem item = new ItemArmazem();
            item.setId(20L);
            item.setCategoria("CALCADO");
            item.setNome("40");
            item.setQuantidade(2);

            Roupa roupa = new Roupa();
            roupa.setCategoria("Sapatos/Sapatilhas");
            roupa.setTamanho("40");
            roupa.setQuantidade(1);

            MarcacaoBalneario bal = new MarcacaoBalneario();
            bal.setRoupas(List.of(roupa));
            Marcacao m = new Marcacao();
            m.setMarcacaoBalneario(bal);

            when(itemArmazemRepository.findByCategoriaAndNome("CALCADO", "40")).thenReturn(Optional.of(item));

            armazemService.restaurarItens(m);

            assertEquals(3, item.getQuantidade());
        }
    }

    @Nested
    @DisplayName("Testes de Verificação de Stock (Formulários)")
    class StockCheckTests {

        @Test
        @DisplayName("Deve verificar stock para itens de formulário mapeados")
        void verificarStock_ItensMapeados_DeveRetornarEstado() {
            ItemArmazem item = new ItemArmazem();
            item.setQuantidade(10);
            item.setQuantidadeMinima(5);

            when(itemArmazemRepository.findByCategoriaAndNome("HIGIENE", "Champô")).thenReturn(Optional.of(item));

            Map<String, Map<String, Object>> result = armazemService
                    .verificarStock(List.of("Champô", "ItemInexistente"));

            assertTrue(result.containsKey("Champô"));
            assertTrue((Boolean) result.get("Champô").get("tracked"));
            assertEquals("OK", result.get("Champô").get("estado"));

            assertTrue(result.containsKey("ItemInexistente"));
            assertFalse((Boolean) result.get("ItemInexistente").get("tracked"));
        }

        @Test
        @DisplayName("Deve verificar stock de calçado por tamanho")
        void verificarStockCalcado_DeveRetornarStatus() {
            ItemArmazem item = new ItemArmazem();
            item.setQuantidade(1);
            item.setQuantidadeMinima(3);

            when(itemArmazemRepository.findByCategoriaAndNome("CALCADO", "41")).thenReturn(Optional.of(item));

            Map<String, Map<String, Object>> result = armazemService.verificarStockCalcado(List.of("41", "35"));

            assertTrue(result.containsKey("41"));
            assertEquals("BAIXO", result.get("41").get("estado"));

            assertTrue(result.containsKey("35"));
            assertFalse((Boolean) result.get("35").get("tracked"));
        }
    }

    @Nested
    @DisplayName("Testes de Estatísticas e Eventos")
    class StatisticsAndEventsTests {

        @Test
        @DisplayName("Deve calcular estatísticas de consumo por período")
        void obterEstatisticas_DeveAgregarConsumos() {
            Marcacao m1 = new Marcacao();
            m1.setData(LocalDateTime.now());
            m1.setEstado(EventoEstado.CONCLUIDO);

            MarcacaoBalneario bal1 = new MarcacaoBalneario();
            ItemArmazem item = new ItemArmazem();
            item.setCategoria("HIGIENE");
            item.setNome("Champô");

            Roupa roupa = new Roupa();
            roupa.setItem(item);
            roupa.setQuantidade(3);
            bal1.setRoupas(List.of(roupa));
            m1.setMarcacaoBalneario(bal1);

            // Marcacao no estado EM_PROGRESSO
            Marcacao m2 = new Marcacao();
            m2.setData(LocalDateTime.now());
            m2.setEstado(EventoEstado.EM_PROGRESSO);

            MarcacaoBalneario bal2 = new MarcacaoBalneario();
            Roupa roupaLegado = new Roupa();
            roupaLegado.setCategoria("Sapatos/Sapatilhas");
            roupaLegado.setTamanho("42");
            roupaLegado.setQuantidade(1);
            bal2.setRoupas(List.of(roupaLegado));
            m2.setMarcacaoBalneario(bal2);

            when(marcacaoRepository.findMarcacoesBetweenDates(any(), any(), eq("BALNEARIO")))
                    .thenReturn(List.of(m1, m2));

            ConsumoEstatisticaDTO stats = armazemService.obterEstatisticas("SEMANA");

            assertNotNull(stats);
            assertEquals("SEMANA", stats.getPeriodo());
            assertEquals(4, stats.getTotalGeral());
            assertEquals(3, stats.getTotaisPorCategoria().get("HIGIENE"));
            assertEquals(1, stats.getTotaisPorCategoria().get("CALCADO"));
        }

        @Test
        @DisplayName("Deve inicializar dados padrão se base de dados vazia")
        void inicializarDadosPadrao_SeVazia_DeveInserirItens() {
            when(itemArmazemRepository.count()).thenReturn(0L);

            armazemService.inicializarDadosPadrao();

            verify(itemArmazemRepository, atLeastOnce()).save(any(ItemArmazem.class));
        }

        @Test
        @DisplayName("Não deve inicializar dados padrão se base de dados já contém dados")
        void inicializarDadosPadrao_SeJaContemDados_DeveIgnorar() {
            when(itemArmazemRepository.count()).thenReturn(10L);

            armazemService.inicializarDadosPadrao();

            verify(itemArmazemRepository, never()).save(any(ItemArmazem.class));
        }
    }
}