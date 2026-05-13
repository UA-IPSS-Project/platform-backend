package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
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

    @InjectMocks
    private ArmazemService armazemService;

    @Nested
    @DisplayName("listarTodos")
    class ListarTodosTests {

        @Test
        @DisplayName("deve listar todos os itens ordenados e converter para DTO")
        void listarTodos_deveRetornarDTOs() {
            ItemArmazem item1 = item(1L, "HIGIENE", "Champô", 10, 5);
            ItemArmazem item2 = item(2L, "VESTUARIO", "Meias", 2, 5);

            when(itemArmazemRepository.findAllByOrderByCategoriaAscNomeAsc())
                    .thenReturn(List.of(item1, item2));

            List<ItemArmazemDTO> resultado = armazemService.listarTodos();

            assertEquals(2, resultado.size());

            assertEquals(1L, resultado.get(0).getId());
            assertEquals("HIGIENE", resultado.get(0).getCategoria());
            assertEquals("Champô", resultado.get(0).getNome());
            assertEquals("OK", resultado.get(0).getEstado());

            assertEquals(2L, resultado.get(1).getId());
            assertEquals("VESTUARIO", resultado.get(1).getCategoria());
            assertEquals("Meias", resultado.get(1).getNome());
            assertEquals("BAIXO", resultado.get(1).getEstado());

            verify(itemArmazemRepository).findAllByOrderByCategoriaAscNomeAsc();
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não existem itens")
        void listarTodos_quandoVazio_deveRetornarListaVazia() {
            when(itemArmazemRepository.findAllByOrderByCategoriaAscNomeAsc())
                    .thenReturn(List.of());

            List<ItemArmazemDTO> resultado = armazemService.listarTodos();

            assertTrue(resultado.isEmpty());
            verify(itemArmazemRepository).findAllByOrderByCategoriaAscNomeAsc();
        }
    }

    @Nested
    @DisplayName("listarPorCategoria")
    class ListarPorCategoriaTests {

        @Test
        @DisplayName("deve listar itens por categoria")
        void listarPorCategoria_deveRetornarDTOs() {
            ItemArmazem item = item(1L, "HIGIENE", "Toalha", 20, 10);

            when(itemArmazemRepository.findByCategoria("HIGIENE"))
                    .thenReturn(List.of(item));

            List<ItemArmazemDTO> resultado = armazemService.listarPorCategoria("HIGIENE");

            assertEquals(1, resultado.size());
            assertEquals("HIGIENE", resultado.get(0).getCategoria());
            assertEquals("Toalha", resultado.get(0).getNome());
            assertEquals("OK", resultado.get(0).getEstado());

            verify(itemArmazemRepository).findByCategoria("HIGIENE");
        }

        @Test
        @DisplayName("deve retornar lista vazia quando categoria não tem itens")
        void listarPorCategoria_quandoSemItens_deveRetornarListaVazia() {
            when(itemArmazemRepository.findByCategoria("CALCADO"))
                    .thenReturn(List.of());

            List<ItemArmazemDTO> resultado = armazemService.listarPorCategoria("CALCADO");

            assertTrue(resultado.isEmpty());
            verify(itemArmazemRepository).findByCategoria("CALCADO");
        }
    }

    @Nested
    @DisplayName("criarItem")
    class CriarItemTests {

        @Test
        @DisplayName("deve criar item válido normalizando categoria e nome")
        void criarItem_deveCriarItemValido() {
            ItemArmazemDTO dto = dto(null, " higiene ", " Champô ", 10, 5);
            dto.setUnidade("un");
            dto.setMarca("Marca A");
            dto.setTamanho("M");
            dto.setVolume(0.5);
            dto.setDescricao("Descrição");

            when(itemArmazemRepository.findByCategoriaAndNome("HIGIENE", "Champô"))
                    .thenReturn(Optional.empty());

            when(itemArmazemRepository.save(any(ItemArmazem.class)))
                    .thenAnswer(invocation -> {
                        ItemArmazem saved = invocation.getArgument(0);
                        saved.setId(99L);
                        return saved;
                    });

            ItemArmazemDTO resultado = armazemService.criarItem(dto);

            assertEquals(99L, resultado.getId());
            assertEquals("HIGIENE", resultado.getCategoria());
            assertEquals("Champô", resultado.getNome());
            assertEquals(10, resultado.getQuantidade());
            assertEquals(5, resultado.getQuantidadeMinima());
            assertEquals("un", resultado.getUnidade());
            assertEquals("Marca A", resultado.getMarca());
            assertEquals("M", resultado.getTamanho());
            assertEquals(0.5, resultado.getVolume());
            assertEquals("Descrição", resultado.getDescricao());
            assertEquals("OK", resultado.getEstado());

            verify(itemArmazemRepository).save(any(ItemArmazem.class));
        }

        @Test
        @DisplayName("deve usar valores default quando quantidade, mínimo e unidade vêm nulos")
        void criarItem_comValoresNulos_deveAplicarDefaults() {
            ItemArmazemDTO dto = dto(null, "HIGIENE", "Toalha", null, null);
            dto.setUnidade(null);

            when(itemArmazemRepository.findByCategoriaAndNome("HIGIENE", "Toalha"))
                    .thenReturn(Optional.empty());

            when(itemArmazemRepository.save(any(ItemArmazem.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ItemArmazemDTO resultado = armazemService.criarItem(dto);

            assertEquals(0, resultado.getQuantidade());
            assertEquals(0, resultado.getQuantidadeMinima());
            assertEquals("un", resultado.getUnidade());
            assertEquals("OK", resultado.getEstado());
        }

        @Test
        @DisplayName("deve converter quantidade e mínimo negativos para zero")
        void criarItem_comValoresNegativos_deveConverterParaZero() {
            ItemArmazemDTO dto = dto(null, "HIGIENE", "Toalha", -10, -5);

            when(itemArmazemRepository.findByCategoriaAndNome("HIGIENE", "Toalha"))
                    .thenReturn(Optional.empty());

            when(itemArmazemRepository.save(any(ItemArmazem.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ItemArmazemDTO resultado = armazemService.criarItem(dto);

            assertEquals(0, resultado.getQuantidade());
            assertEquals(0, resultado.getQuantidadeMinima());
        }

        @Test
        @DisplayName("deve lançar exceção quando categoria é nula")
        void criarItem_categoriaNula_deveLancarExcecao() {
            ItemArmazemDTO dto = dto(null, null, "Champô", 10, 5);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> armazemService.criarItem(dto));

            assertEquals("A categoria é obrigatória.", ex.getMessage());
            verify(itemArmazemRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando categoria é vazia")
        void criarItem_categoriaVazia_deveLancarExcecao() {
            ItemArmazemDTO dto = dto(null, "   ", "Champô", 10, 5);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> armazemService.criarItem(dto));

            assertEquals("A categoria é obrigatória.", ex.getMessage());
            verify(itemArmazemRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando nome é nulo")
        void criarItem_nomeNulo_deveLancarExcecao() {
            ItemArmazemDTO dto = dto(null, "HIGIENE", null, 10, 5);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> armazemService.criarItem(dto));

            assertEquals("O nome do item é obrigatório.", ex.getMessage());
            verify(itemArmazemRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando nome é vazio")
        void criarItem_nomeVazio_deveLancarExcecao() {
            ItemArmazemDTO dto = dto(null, "HIGIENE", "   ", 10, 5);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> armazemService.criarItem(dto));

            assertEquals("O nome do item é obrigatório.", ex.getMessage());
            verify(itemArmazemRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando item já existe")
        void criarItem_itemDuplicado_deveLancarExcecao() {
            ItemArmazemDTO dto = dto(null, " higiene ", " Champô ", 10, 5);

            when(itemArmazemRepository.findByCategoriaAndNome("HIGIENE", "Champô"))
                    .thenReturn(Optional.of(item(1L, "HIGIENE", "Champô", 10, 5)));

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> armazemService.criarItem(dto));

            assertEquals("Já existe um item com esta categoria e nome.", ex.getMessage());
            verify(itemArmazemRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("atualizarItem")
    class AtualizarItemTests {

        @Test
        @DisplayName("deve atualizar todos os campos permitidos")
        void atualizarItem_deveAtualizarTodosCampos() {
            ItemArmazem existente = item(1L, "HIGIENE", "Champô", 10, 5);

            ItemArmazemDTO dto = dto(null, " vestuario ", " Meias ", 20, 8);
            dto.setUnidade("pares");
            dto.setMarca("Marca B");
            dto.setTamanho("39-42");
            dto.setVolume(1.5);
            dto.setDescricao("Nova descrição");

            when(itemArmazemRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(itemArmazemRepository.save(any(ItemArmazem.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ItemArmazemDTO resultado = armazemService.atualizarItem(1L, dto);

            assertEquals("VESTUARIO", resultado.getCategoria());
            assertEquals("Meias", resultado.getNome());
            assertEquals(20, resultado.getQuantidade());
            assertEquals(8, resultado.getQuantidadeMinima());
            assertEquals("pares", resultado.getUnidade());
            assertEquals("Marca B", resultado.getMarca());
            assertEquals("39-42", resultado.getTamanho());
            assertEquals(1.5, resultado.getVolume());
            assertEquals("Nova descrição", resultado.getDescricao());
            assertEquals("OK", resultado.getEstado());

            verify(itemArmazemRepository).save(existente);
        }

        @Test
        @DisplayName("deve atualizar parcialmente mantendo campos nulos inalterados")
        void atualizarItem_camposNulos_deveManterValoresOriginais() {
            ItemArmazem existente = item(1L, "HIGIENE", "Champô", 10, 5);
            existente.setUnidade("un");
            existente.setMarca("Marca Original");
            existente.setTamanho("M");
            existente.setVolume(0.4);
            existente.setDescricao("Descrição original");

            ItemArmazemDTO dto = new ItemArmazemDTO();
            dto.setQuantidade(3);

            when(itemArmazemRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(itemArmazemRepository.save(any(ItemArmazem.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ItemArmazemDTO resultado = armazemService.atualizarItem(1L, dto);

            assertEquals("HIGIENE", resultado.getCategoria());
            assertEquals("Champô", resultado.getNome());
            assertEquals(3, resultado.getQuantidade());
            assertEquals(5, resultado.getQuantidadeMinima());
            assertEquals("un", resultado.getUnidade());
            assertEquals("Marca Original", resultado.getMarca());
            assertEquals("M", resultado.getTamanho());
            assertEquals(0.4, resultado.getVolume());
            assertEquals("Descrição original", resultado.getDescricao());
            assertEquals("BAIXO", resultado.getEstado());
        }

        @Test
        @DisplayName("deve converter quantidade e mínimo negativos para zero")
        void atualizarItem_valoresNegativos_deveConverterParaZero() {
            ItemArmazem existente = item(1L, "HIGIENE", "Champô", 10, 5);

            ItemArmazemDTO dto = new ItemArmazemDTO();
            dto.setQuantidade(-20);
            dto.setQuantidadeMinima(-3);

            when(itemArmazemRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(itemArmazemRepository.save(any(ItemArmazem.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ItemArmazemDTO resultado = armazemService.atualizarItem(1L, dto);

            assertEquals(0, resultado.getQuantidade());
            assertEquals(0, resultado.getQuantidadeMinima());
        }

        @Test
        @DisplayName("deve lançar exceção quando item não existe")
        void atualizarItem_itemInexistente_deveLancarExcecao() {
            when(itemArmazemRepository.findById(99L)).thenReturn(Optional.empty());

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> armazemService.atualizarItem(99L, new ItemArmazemDTO()));

            assertEquals("Item do armazém não encontrado com ID: 99", ex.getMessage());
            verify(itemArmazemRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando categoria é vazia")
        void atualizarItem_categoriaVazia_deveLancarExcecao() {
            ItemArmazem existente = item(1L, "HIGIENE", "Champô", 10, 5);
            ItemArmazemDTO dto = new ItemArmazemDTO();
            dto.setCategoria("   ");

            when(itemArmazemRepository.findById(1L)).thenReturn(Optional.of(existente));

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> armazemService.atualizarItem(1L, dto));

            assertEquals("A categoria não pode ser vazia.", ex.getMessage());
            verify(itemArmazemRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando nome é vazio")
        void atualizarItem_nomeVazio_deveLancarExcecao() {
            ItemArmazem existente = item(1L, "HIGIENE", "Champô", 10, 5);
            ItemArmazemDTO dto = new ItemArmazemDTO();
            dto.setNome("   ");

            when(itemArmazemRepository.findById(1L)).thenReturn(Optional.of(existente));

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> armazemService.atualizarItem(1L, dto));

            assertEquals("O nome do item não pode ser vazio.", ex.getMessage());
            verify(itemArmazemRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("eliminarItem")
    class EliminarItemTests {

        @Test
        @DisplayName("deve eliminar item quando existe e não foi usado")
        void eliminarItem_deveEliminarComSucesso() {
            when(itemArmazemRepository.existsById(1L)).thenReturn(true);
            when(roupaRepository.existsByItemId(1L)).thenReturn(false);

            assertDoesNotThrow(() -> armazemService.eliminarItem(1L));

            verify(itemArmazemRepository).deleteById(1L);
        }

        @Test
        @DisplayName("deve lançar exceção quando item não existe")
        void eliminarItem_itemInexistente_deveLancarExcecao() {
            when(itemArmazemRepository.existsById(1L)).thenReturn(false);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> armazemService.eliminarItem(1L));

            assertEquals("Item do armazém não encontrado com ID: 1", ex.getMessage());
            verify(roupaRepository, never()).existsByItemId(anyLong());
            verify(itemArmazemRepository, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("deve lançar exceção quando item já foi usado em marcações")
        void eliminarItem_itemUsado_deveLancarExcecao() {
            when(itemArmazemRepository.existsById(1L)).thenReturn(true);
            when(roupaRepository.existsByItemId(1L)).thenReturn(true);

            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> armazemService.eliminarItem(1L));

            assertEquals(
                    "Não é possível eliminar o item porque ele já foi utilizado em marcações. Considere alterar apenas os níveis de stock.",
                    ex.getMessage());

            verify(itemArmazemRepository, never()).deleteById(anyLong());
        }
    }

    @Nested
    @DisplayName("descontarItens")
    class DescontarItensTests {

        @Test
        @DisplayName("deve retornar lista vazia quando marcação não tem detalhes de balneário")
        void descontarItens_semDetalhes_deveRetornarVazio() {
            Marcacao marcacao = new Marcacao();

            List<String> resultado = armazemService.descontarItens(marcacao);

            assertTrue(resultado.isEmpty());
            verifyNoInteractions(itemArmazemRepository);
        }

        @Test
        @DisplayName("deve retornar lista vazia quando roupas são nulas")
        void descontarItens_roupasNulas_deveRetornarVazio() {
            MarcacaoBalneario balneario = new MarcacaoBalneario();
            balneario.setRoupas(null);

            Marcacao marcacao = new Marcacao();
            marcacao.setMarcacaoBalneario(balneario);

            List<String> resultado = armazemService.descontarItens(marcacao);

            assertTrue(resultado.isEmpty());
            verifyNoInteractions(itemArmazemRepository);
        }

        @Test
        @DisplayName("deve descontar stock usando item por ID")
        void descontarItens_comItemDireto_deveDescontarStock() {
            ItemArmazem item = item(10L, "HIGIENE", "Champô", 10, 5);
            Roupa roupa = roupaComItem(item, 3);
            Marcacao marcacao = marcacaoComRoupas(List.of(roupa));

            when(itemArmazemRepository.findById(10L)).thenReturn(Optional.of(item));

            List<String> avisos = armazemService.descontarItens(marcacao);

            assertTrue(avisos.isEmpty());
            assertEquals(7, item.getQuantidade());
            verify(itemArmazemRepository).save(item);
        }

        @Test
        @DisplayName("deve colocar stock a zero e devolver aviso quando stock é insuficiente")
        void descontarItens_stockInsuficiente_deveGerarAviso() {
            ItemArmazem item = item(10L, "HIGIENE", "Champô", 2, 5);
            Roupa roupa = roupaComItem(item, 5);
            Marcacao marcacao = marcacaoComRoupas(List.of(roupa));

            when(itemArmazemRepository.findById(10L)).thenReturn(Optional.of(item));

            List<String> avisos = armazemService.descontarItens(marcacao);

            assertEquals(1, avisos.size());
            assertEquals("Stock insuficiente para 'Champô' (disponível: 2, necessário: 5)", avisos.get(0));
            assertEquals(0, item.getQuantidade());
            verify(itemArmazemRepository).save(item);
        }

        @Test
        @DisplayName("deve usar fallback por categoria/nome quando item direto não existe")
        void descontarItens_itemDiretoNaoEncontrado_deveUsarFallback() {
            ItemArmazem itemRef = item(10L, "HIGIENE", "Champô", 10, 5);
            ItemArmazem itemReal = item(20L, "HIGIENE", "Champô", 10, 5);

            Roupa roupa = roupaComItem(itemRef, 2);
            roupa.setCategoria("Shampoo");

            Marcacao marcacao = marcacaoComRoupas(List.of(roupa));

            when(itemArmazemRepository.findById(10L)).thenReturn(Optional.empty());
            when(itemArmazemRepository.findByCategoriaAndNome("HIGIENE", "Champô"))
                    .thenReturn(Optional.of(itemReal));

            List<String> avisos = armazemService.descontarItens(marcacao);

            assertTrue(avisos.isEmpty());
            assertEquals(8, itemReal.getQuantidade());
            verify(itemArmazemRepository).save(itemReal);
        }

        @Test
        @DisplayName("deve ignorar roupa com categoria nula")
        void descontarItens_categoriaNula_deveIgnorar() {
            Roupa roupa = new Roupa();
            roupa.setCategoria(null);
            roupa.setQuantidade(1);

            Marcacao marcacao = marcacaoComRoupas(List.of(roupa));

            List<String> avisos = armazemService.descontarItens(marcacao);

            assertTrue(avisos.isEmpty());
            verify(itemArmazemRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve ignorar roupa com categoria vazia")
        void descontarItens_categoriaVazia_deveIgnorar() {
            Roupa roupa = new Roupa();
            roupa.setCategoria("   ");
            roupa.setQuantidade(1);

            Marcacao marcacao = marcacaoComRoupas(List.of(roupa));

            List<String> avisos = armazemService.descontarItens(marcacao);

            assertTrue(avisos.isEmpty());
            verify(itemArmazemRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve mapear Sapatos/Sapatilhas para CALCADO pelo tamanho")
        void descontarItens_calcado_deveUsarTamanhoComoNome() {
            ItemArmazem item = item(1L, "CALCADO", "42", 10, 3);

            Roupa roupa = roupaLegacy("Sapatos/Sapatilhas", "42", 2);
            Marcacao marcacao = marcacaoComRoupas(List.of(roupa));

            when(itemArmazemRepository.findByCategoriaAndNome("CALCADO", "42"))
                    .thenReturn(Optional.of(item));

            List<String> avisos = armazemService.descontarItens(marcacao);

            assertTrue(avisos.isEmpty());
            assertEquals(8, item.getQuantidade());
            verify(itemArmazemRepository).save(item);
        }

        @Test
        @DisplayName("deve procurar em categorias geridas quando categoria não tem mapeamento")
        void descontarItens_semCategoriaMapeada_deveProcurarManagedCats() {
            ItemArmazem item = item(1L, "VESTUARIO", "Produto Estranho", 10, 5);

            Roupa roupa = roupaLegacy("Produto Estranho", null, 2);
            Marcacao marcacao = marcacaoComRoupas(List.of(roupa));

            when(itemArmazemRepository.findByCategoriaAndNome(anyString(), eq("Produto Estranho")))
                    .thenAnswer(invocation -> {
                        String categoria = invocation.getArgument(0);
                        if ("VESTUARIO".equals(categoria)) {
                            return Optional.of(item);
                        }
                        return Optional.empty();
                    });

            List<String> avisos = armazemService.descontarItens(marcacao);

            assertTrue(avisos.isEmpty());
            assertEquals(8, item.getQuantidade());
            verify(itemArmazemRepository).save(item);
        }

        @Test
        @DisplayName("deve não guardar quando item não é encontrado")
        void descontarItens_itemNaoEncontrado_deveNaoGuardar() {
            Roupa roupa = roupaLegacy("Gel de Banho", null, 2);
            Marcacao marcacao = marcacaoComRoupas(List.of(roupa));

            when(itemArmazemRepository.findByCategoriaAndNome("HIGIENE", "Gel de Banho"))
                    .thenReturn(Optional.empty());

            List<String> avisos = armazemService.descontarItens(marcacao);

            assertTrue(avisos.isEmpty());
            verify(itemArmazemRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve processar múltiplas roupas e devolver apenas avisos de stock insuficiente")
        void descontarItens_multiplasRoupas_deveProcessarTodas() {
            ItemArmazem champo = item(1L, "HIGIENE", "Champô", 10, 5);
            ItemArmazem toalha = item(2L, "HIGIENE", "Toalha", 1, 5);

            Roupa roupa1 = roupaComItem(champo, 2);
            Roupa roupa2 = roupaComItem(toalha, 3);

            Marcacao marcacao = marcacaoComRoupas(List.of(roupa1, roupa2));

            when(itemArmazemRepository.findById(1L)).thenReturn(Optional.of(champo));
            when(itemArmazemRepository.findById(2L)).thenReturn(Optional.of(toalha));

            List<String> avisos = armazemService.descontarItens(marcacao);

            assertEquals(1, avisos.size());
            assertEquals(8, champo.getQuantidade());
            assertEquals(0, toalha.getQuantidade());
            verify(itemArmazemRepository).save(champo);
            verify(itemArmazemRepository).save(toalha);
        }
    }

    @Nested
    @DisplayName("restaurarItens")
    class RestaurarItensTests {

        @Test
        @DisplayName("deve terminar sem erro quando marcação não tem detalhes")
        void restaurarItens_semDetalhes_deveRetornar() {
            Marcacao marcacao = new Marcacao();

            assertDoesNotThrow(() -> armazemService.restaurarItens(marcacao));

            verifyNoInteractions(itemArmazemRepository);
        }

        @Test
        @DisplayName("deve terminar sem erro quando roupas são nulas")
        void restaurarItens_roupasNulas_deveRetornar() {
            MarcacaoBalneario balneario = new MarcacaoBalneario();
            balneario.setRoupas(null);

            Marcacao marcacao = new Marcacao();
            marcacao.setMarcacaoBalneario(balneario);

            assertDoesNotThrow(() -> armazemService.restaurarItens(marcacao));

            verifyNoInteractions(itemArmazemRepository);
        }

        @Test
        @DisplayName("deve restaurar stock usando item por ID")
        void restaurarItens_comItemDireto_deveRestaurar() {
            ItemArmazem item = item(1L, "HIGIENE", "Champô", 5, 3);
            Roupa roupa = roupaComItem(item, 4);
            Marcacao marcacao = marcacaoComRoupas(List.of(roupa));

            when(itemArmazemRepository.findById(1L)).thenReturn(Optional.of(item));

            armazemService.restaurarItens(marcacao);

            assertEquals(9, item.getQuantidade());
            verify(itemArmazemRepository).save(item);
        }

        @Test
        @DisplayName("deve usar fallback quando item por ID não existe")
        void restaurarItens_itemDiretoNaoExiste_deveUsarFallback() {
            ItemArmazem itemRef = item(1L, "HIGIENE", "Champô", 5, 3);
            ItemArmazem itemReal = item(2L, "HIGIENE", "Champô", 5, 3);

            Roupa roupa = roupaComItem(itemRef, 4);
            roupa.setCategoria("Shampoo");

            Marcacao marcacao = marcacaoComRoupas(List.of(roupa));

            when(itemArmazemRepository.findById(1L)).thenReturn(Optional.empty());
            when(itemArmazemRepository.findByCategoriaAndNome("HIGIENE", "Champô"))
                    .thenReturn(Optional.of(itemReal));

            armazemService.restaurarItens(marcacao);

            assertEquals(9, itemReal.getQuantidade());
            verify(itemArmazemRepository).save(itemReal);
        }

        @Test
        @DisplayName("deve ignorar roupa com categoria nula")
        void restaurarItens_categoriaNula_deveIgnorar() {
            Roupa roupa = new Roupa();
            roupa.setCategoria(null);
            roupa.setQuantidade(2);

            Marcacao marcacao = marcacaoComRoupas(List.of(roupa));

            armazemService.restaurarItens(marcacao);

            verify(itemArmazemRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve ignorar roupa com categoria vazia")
        void restaurarItens_categoriaVazia_deveIgnorar() {
            Roupa roupa = new Roupa();
            roupa.setCategoria("   ");
            roupa.setQuantidade(2);

            Marcacao marcacao = marcacaoComRoupas(List.of(roupa));

            armazemService.restaurarItens(marcacao);

            verify(itemArmazemRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve restaurar calçado usando tamanho")
        void restaurarItens_calcado_deveUsarTamanho() {
            ItemArmazem item = item(1L, "CALCADO", "41", 5, 3);

            Roupa roupa = roupaLegacy("Sapatos/Sapatilhas", "41", 2);
            Marcacao marcacao = marcacaoComRoupas(List.of(roupa));

            when(itemArmazemRepository.findByCategoriaAndNome("CALCADO", "41"))
                    .thenReturn(Optional.of(item));

            armazemService.restaurarItens(marcacao);

            assertEquals(7, item.getQuantidade());
            verify(itemArmazemRepository).save(item);
        }

        @Test
        @DisplayName("deve procurar em categorias geridas quando não existe categoria mapeada")
        void restaurarItens_semCategoriaMapeada_deveProcurarManagedCats() {
            ItemArmazem item = item(1L, "DETERGENTES", "Produto X", 5, 3);

            Roupa roupa = roupaLegacy("Produto X", null, 2);
            Marcacao marcacao = marcacaoComRoupas(List.of(roupa));

            when(itemArmazemRepository.findByCategoriaAndNome(anyString(), eq("Produto X")))
                    .thenAnswer(invocation -> {
                        String categoria = invocation.getArgument(0);
                        if ("DETERGENTES".equals(categoria)) {
                            return Optional.of(item);
                        }
                        return Optional.empty();
                    });

            armazemService.restaurarItens(marcacao);

            assertEquals(7, item.getQuantidade());
            verify(itemArmazemRepository).save(item);
        }

        @Test
        @DisplayName("deve não guardar quando item não existe")
        void restaurarItens_itemNaoEncontrado_deveNaoGuardar() {
            Roupa roupa = roupaLegacy("Gel de Banho", null, 2);
            Marcacao marcacao = marcacaoComRoupas(List.of(roupa));

            when(itemArmazemRepository.findByCategoriaAndNome("HIGIENE", "Gel de Banho"))
                    .thenReturn(Optional.empty());

            armazemService.restaurarItens(marcacao);

            verify(itemArmazemRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("verificarStock")
    class VerificarStockTests {

        @Test
        @DisplayName("deve indicar item tracked com estado OK")
        void verificarStock_itemOK_deveRetornarOK() {
            ItemArmazem item = item(1L, "HIGIENE", "Champô", 10, 5);

            when(itemArmazemRepository.findByCategoriaAndNome("HIGIENE", "Champô"))
                    .thenReturn(Optional.of(item));

            Map<String, Map<String, Object>> resultado = armazemService.verificarStock(List.of("Shampoo"));

            assertTrue((Boolean) resultado.get("Shampoo").get("tracked"));
            assertEquals(10, resultado.get("Shampoo").get("quantidade"));
            assertEquals(5, resultado.get("Shampoo").get("quantidadeMinima"));
            assertEquals("OK", resultado.get("Shampoo").get("estado"));
            assertEquals(false, resultado.get("Shampoo").get("esgotado"));
        }

        @Test
        @DisplayName("deve indicar item tracked com estado BAIXO")
        void verificarStock_itemBaixo_deveRetornarBaixo() {
            ItemArmazem item = item(1L, "HIGIENE", "Toalha", 3, 5);

            when(itemArmazemRepository.findByCategoriaAndNome("HIGIENE", "Toalha"))
                    .thenReturn(Optional.of(item));

            Map<String, Map<String, Object>> resultado = armazemService.verificarStock(List.of("Toalha"));

            assertTrue((Boolean) resultado.get("Toalha").get("tracked"));
            assertEquals("BAIXO", resultado.get("Toalha").get("estado"));
            assertEquals(false, resultado.get("Toalha").get("esgotado"));
        }

        @Test
        @DisplayName("deve indicar esgotado quando quantidade é zero")
        void verificarStock_itemEsgotado_deveRetornarEsgotadoTrue() {
            ItemArmazem item = item(1L, "HIGIENE", "Toalha", 0, 5);

            when(itemArmazemRepository.findByCategoriaAndNome("HIGIENE", "Toalha"))
                    .thenReturn(Optional.of(item));

            Map<String, Map<String, Object>> resultado = armazemService.verificarStock(List.of("Toalha"));

            assertEquals("BAIXO", resultado.get("Toalha").get("estado"));
            assertEquals(true, resultado.get("Toalha").get("esgotado"));
        }

        @Test
        @DisplayName("deve indicar tracked false quando item não tem mapeamento")
        void verificarStock_semMapeamento_deveRetornarTrackedFalse() {
            Map<String, Map<String, Object>> resultado = armazemService.verificarStock(List.of("Produto X"));

            assertEquals(Map.of("tracked", false), resultado.get("Produto X"));
            verifyNoInteractions(itemArmazemRepository);
        }

        @Test
        @DisplayName("deve indicar tracked false quando item mapeado não existe no armazém")
        void verificarStock_itemNaoExiste_deveRetornarTrackedFalse() {
            when(itemArmazemRepository.findByCategoriaAndNome("HIGIENE", "Gel de Banho"))
                    .thenReturn(Optional.empty());

            Map<String, Map<String, Object>> resultado = armazemService.verificarStock(List.of("Gel de Banho"));

            assertEquals(Map.of("tracked", false), resultado.get("Gel de Banho"));
        }

        @Test
        @DisplayName("deve manter ordem dos itens recebidos")
        void verificarStock_deveManterOrdem() {
            when(itemArmazemRepository.findByCategoriaAndNome(anyString(), anyString()))
                    .thenReturn(Optional.empty());

            Map<String, Map<String, Object>> resultado = armazemService.verificarStock(
                    List.of("Shampoo", "Toalha", "Produto X"));

            assertEquals(List.of("Shampoo", "Toalha", "Produto X"), List.copyOf(resultado.keySet()));
        }
    }

    @Nested
    @DisplayName("verificarStockCalcado")
    class VerificarStockCalcadoTests {

        @Test
        @DisplayName("deve indicar calçado tracked com estado OK")
        void verificarStockCalcado_itemOK_deveRetornarOK() {
            ItemArmazem item = item(1L, "CALCADO", "42", 10, 3);

            when(itemArmazemRepository.findByCategoriaAndNome("CALCADO", "42"))
                    .thenReturn(Optional.of(item));

            Map<String, Map<String, Object>> resultado = armazemService.verificarStockCalcado(List.of("42"));

            assertTrue((Boolean) resultado.get("42").get("tracked"));
            assertEquals(10, resultado.get("42").get("quantidade"));
            assertEquals(3, resultado.get("42").get("quantidadeMinima"));
            assertEquals("OK", resultado.get("42").get("estado"));
            assertEquals(false, resultado.get("42").get("esgotado"));
        }

        @Test
        @DisplayName("deve indicar calçado tracked com estado BAIXO e esgotado")
        void verificarStockCalcado_itemBaixoEsgotado_deveRetornarBaixo() {
            ItemArmazem item = item(1L, "CALCADO", "43", 0, 3);

            when(itemArmazemRepository.findByCategoriaAndNome("CALCADO", "43"))
                    .thenReturn(Optional.of(item));

            Map<String, Map<String, Object>> resultado = armazemService.verificarStockCalcado(List.of("43"));

            assertTrue((Boolean) resultado.get("43").get("tracked"));
            assertEquals("BAIXO", resultado.get("43").get("estado"));
            assertEquals(true, resultado.get("43").get("esgotado"));
        }

        @Test
        @DisplayName("deve indicar tracked false quando tamanho não existe")
        void verificarStockCalcado_itemNaoExiste_deveRetornarTrackedFalse() {
            when(itemArmazemRepository.findByCategoriaAndNome("CALCADO", "44"))
                    .thenReturn(Optional.empty());

            Map<String, Map<String, Object>> resultado = armazemService.verificarStockCalcado(List.of("44"));

            assertEquals(Map.of("tracked", false), resultado.get("44"));
        }

        @Test
        @DisplayName("deve manter ordem dos tamanhos")
        void verificarStockCalcado_deveManterOrdem() {
            when(itemArmazemRepository.findByCategoriaAndNome(eq("CALCADO"), anyString()))
                    .thenReturn(Optional.empty());

            Map<String, Map<String, Object>> resultado = armazemService.verificarStockCalcado(
                    List.of("40", "41", "42"));

            assertEquals(List.of("40", "41", "42"), List.copyOf(resultado.keySet()));
        }
    }

    @Nested
    @DisplayName("obterEstatisticas")
    class ObterEstatisticasTests {

        @Test
        @DisplayName("deve obter estatísticas para período DIA")
        void obterEstatisticas_dia_deveAgregarConsumos() {
            ItemArmazem item = item(1L, "HIGIENE", "Champô", 10, 5);
            Roupa roupa = roupaComItem(item, 2);

            Marcacao marcacao = marcacaoEstatistica(EventoEstado.EM_PROGRESSO, List.of(roupa));

            when(marcacaoRepository.findMarcacoesBetweenDates(any(), any(), eq("BALNEARIO")))
                    .thenReturn(List.of(marcacao));

            ConsumoEstatisticaDTO resultado = armazemService.obterEstatisticas("DIA");

            assertEquals("DIA", resultado.getPeriodo());
            assertEquals(1, resultado.getItens().size());
            assertEquals("HIGIENE", resultado.getItens().get(0).getCategoria());
            assertEquals("Champô", resultado.getItens().get(0).getNome());
            assertEquals(2, resultado.getItens().get(0).getQuantidade());
            assertEquals(2, resultado.getTotaisPorCategoria().get("HIGIENE"));
            assertEquals(2, resultado.getTotalGeral());

            verify(marcacaoRepository).findMarcacoesBetweenDates(any(), any(), eq("BALNEARIO"));
        }

        @Test
        @DisplayName("deve obter estatísticas para período SEMANA")
        void obterEstatisticas_semana_deveExecutar() {
            when(marcacaoRepository.findMarcacoesBetweenDates(any(), any(), eq("BALNEARIO")))
                    .thenReturn(List.of());

            ConsumoEstatisticaDTO resultado = armazemService.obterEstatisticas("SEMANA");

            assertEquals("SEMANA", resultado.getPeriodo());
            assertTrue(resultado.getItens().isEmpty());
            assertTrue(resultado.getTotaisPorCategoria().isEmpty());
            assertEquals(0, resultado.getTotalGeral());
        }

        @Test
        @DisplayName("deve obter estatísticas para período MES")
        void obterEstatisticas_mes_deveExecutar() {
            when(marcacaoRepository.findMarcacoesBetweenDates(any(), any(), eq("BALNEARIO")))
                    .thenReturn(List.of());

            ConsumoEstatisticaDTO resultado = armazemService.obterEstatisticas("MES");

            assertEquals("MES", resultado.getPeriodo());
            assertTrue(resultado.getItens().isEmpty());
            assertEquals(0, resultado.getTotalGeral());
        }

        @Test
        @DisplayName("deve usar branch default para período desconhecido")
        void obterEstatisticas_periodoDesconhecido_deveUsarDefault() {
            when(marcacaoRepository.findMarcacoesBetweenDates(any(), any(), eq("BALNEARIO")))
                    .thenReturn(List.of());

            ConsumoEstatisticaDTO resultado = armazemService.obterEstatisticas("ANO");

            assertEquals("ANO", resultado.getPeriodo());
            assertTrue(resultado.getItens().isEmpty());
        }

        @Test
        @DisplayName("deve aceitar período em minúsculas")
        void obterEstatisticas_periodoMinusculo_deveFuncionar() {
            when(marcacaoRepository.findMarcacoesBetweenDates(any(), any(), eq("BALNEARIO")))
                    .thenReturn(List.of());

            ConsumoEstatisticaDTO resultado = armazemService.obterEstatisticas("dia");

            assertEquals("dia", resultado.getPeriodo());
            assertTrue(resultado.getItens().isEmpty());
        }

        @Test
        @DisplayName("deve ignorar marcações com estado fora de EM_PROGRESSO e CONCLUIDO")
        void obterEstatisticas_estadoIgnorado_deveIgnorarMarcacao() {
            Roupa roupa = roupaLegacy("Shampoo", null, 2);
            Marcacao marcacao = marcacaoEstatistica(EventoEstado.AGENDADO, List.of(roupa));

            when(marcacaoRepository.findMarcacoesBetweenDates(any(), any(), eq("BALNEARIO")))
                    .thenReturn(List.of(marcacao));

            ConsumoEstatisticaDTO resultado = armazemService.obterEstatisticas("MES");

            assertTrue(resultado.getItens().isEmpty());
            assertTrue(resultado.getTotaisPorCategoria().isEmpty());
            assertEquals(0, resultado.getTotalGeral());
        }

        @Test
        @DisplayName("deve ignorar marcações sem balneário")
        void obterEstatisticas_semBalneario_deveIgnorar() {
            Marcacao marcacao = new Marcacao();
            marcacao.setEstado(EventoEstado.EM_PROGRESSO);
            marcacao.setData(LocalDateTime.now());
            marcacao.setMarcacaoBalneario(null);

            when(marcacaoRepository.findMarcacoesBetweenDates(any(), any(), eq("BALNEARIO")))
                    .thenReturn(List.of(marcacao));

            ConsumoEstatisticaDTO resultado = armazemService.obterEstatisticas("MES");

            assertTrue(resultado.getItens().isEmpty());
            assertEquals(0, resultado.getTotalGeral());
        }

        @Test
        @DisplayName("deve ignorar marcações com lista de roupas nula")
        void obterEstatisticas_roupasNulas_deveIgnorar() {
            MarcacaoBalneario balneario = new MarcacaoBalneario();
            balneario.setRoupas(null);

            Marcacao marcacao = new Marcacao();
            marcacao.setEstado(EventoEstado.CONCLUIDO);
            marcacao.setData(LocalDateTime.now());
            marcacao.setMarcacaoBalneario(balneario);

            when(marcacaoRepository.findMarcacoesBetweenDates(any(), any(), eq("BALNEARIO")))
                    .thenReturn(List.of(marcacao));

            ConsumoEstatisticaDTO resultado = armazemService.obterEstatisticas("MES");

            assertTrue(resultado.getItens().isEmpty());
            assertEquals(0, resultado.getTotalGeral());
        }

        @Test
        @DisplayName("deve agregar modo legado com categoria mapeada")
        void obterEstatisticas_modoLegadoMapeado_deveAgregar() {
            Roupa roupa = roupaLegacy("Shampoo", null, 3);
            Marcacao marcacao = marcacaoEstatistica(EventoEstado.CONCLUIDO, List.of(roupa));

            when(marcacaoRepository.findMarcacoesBetweenDates(any(), any(), eq("BALNEARIO")))
                    .thenReturn(List.of(marcacao));

            ConsumoEstatisticaDTO resultado = armazemService.obterEstatisticas("MES");

            assertEquals(1, resultado.getItens().size());
            assertEquals("HIGIENE", resultado.getItens().get(0).getCategoria());
            assertEquals("Champô", resultado.getItens().get(0).getNome());
            assertEquals(3, resultado.getTotalGeral());
        }

        @Test
        @DisplayName("deve agregar modo legado de calçado com tamanho")
        void obterEstatisticas_calcadoComTamanho_deveAgregar() {
            Roupa roupa = roupaLegacy("Sapatos/Sapatilhas", "39", 1);
            Marcacao marcacao = marcacaoEstatistica(EventoEstado.CONCLUIDO, List.of(roupa));

            when(marcacaoRepository.findMarcacoesBetweenDates(any(), any(), eq("BALNEARIO")))
                    .thenReturn(List.of(marcacao));

            ConsumoEstatisticaDTO resultado = armazemService.obterEstatisticas("MES");

            assertEquals("CALCADO", resultado.getItens().get(0).getCategoria());
            assertEquals("39", resultado.getItens().get(0).getNome());
            assertEquals(1, resultado.getTotaisPorCategoria().get("CALCADO"));
        }

        @Test
        @DisplayName("deve agregar modo legado de calçado sem tamanho usando categoria como nome")
        void obterEstatisticas_calcadoSemTamanho_deveUsarCategoriaComoNome() {
            Roupa roupa = roupaLegacy("Sapatos/Sapatilhas", null, 1);
            Marcacao marcacao = marcacaoEstatistica(EventoEstado.CONCLUIDO, List.of(roupa));

            when(marcacaoRepository.findMarcacoesBetweenDates(any(), any(), eq("BALNEARIO")))
                    .thenReturn(List.of(marcacao));

            ConsumoEstatisticaDTO resultado = armazemService.obterEstatisticas("MES");

            assertEquals("CALCADO", resultado.getItens().get(0).getCategoria());
            assertEquals("Sapatos/Sapatilhas", resultado.getItens().get(0).getNome());
        }

        @Test
        @DisplayName("deve agregar modo legado sem mapeamento como OUTRO")
        void obterEstatisticas_semMapeamento_deveUsarOutro() {
            Roupa roupa = roupaLegacy("Produto X", null, 4);
            Marcacao marcacao = marcacaoEstatistica(EventoEstado.CONCLUIDO, List.of(roupa));

            when(marcacaoRepository.findMarcacoesBetweenDates(any(), any(), eq("BALNEARIO")))
                    .thenReturn(List.of(marcacao));

            ConsumoEstatisticaDTO resultado = armazemService.obterEstatisticas("MES");

            assertEquals("OUTRO", resultado.getItens().get(0).getCategoria());
            assertEquals("Produto X", resultado.getItens().get(0).getNome());
            assertEquals(4, resultado.getTotaisPorCategoria().get("OUTRO"));
        }

        @Test
        @DisplayName("deve somar totais por categoria e total geral")
        void obterEstatisticas_multiplosItens_deveSomarTotais() {
            Roupa roupa1 = roupaLegacy("Shampoo", null, 2);
            Roupa roupa2 = roupaLegacy("Gel de Banho", null, 3);
            Roupa roupa3 = roupaLegacy("Meias", null, 4);

            Marcacao marcacao = marcacaoEstatistica(
                    EventoEstado.CONCLUIDO,
                    List.of(roupa1, roupa2, roupa3));

            when(marcacaoRepository.findMarcacoesBetweenDates(any(), any(), eq("BALNEARIO")))
                    .thenReturn(List.of(marcacao));

            ConsumoEstatisticaDTO resultado = armazemService.obterEstatisticas("MES");

            assertEquals(3, resultado.getItens().size());
            assertEquals(5, resultado.getTotaisPorCategoria().get("HIGIENE"));
            assertEquals(4, resultado.getTotaisPorCategoria().get("VESTUARIO"));
            assertEquals(9, resultado.getTotalGeral());
        }

        @Test
        @DisplayName("deve lançar NullPointerException quando período é nulo")
        void obterEstatisticas_periodoNulo_deveLancarNullPointerException() {
            assertThrows(NullPointerException.class, () -> armazemService.obterEstatisticas(null));
            verifyNoInteractions(marcacaoRepository);
        }
    }

    @Nested
    @DisplayName("inicializarDadosPadrao")
    class InicializarDadosPadraoTests {

        @Test
        @DisplayName("deve ignorar inicialização quando armazém já tem dados")
        void inicializarDadosPadrao_quandoJaTemDados_deveIgnorar() {
            when(itemArmazemRepository.count()).thenReturn(1L);

            armazemService.inicializarDadosPadrao();

            verify(itemArmazemRepository).count();
            verify(itemArmazemRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve criar dados padrão quando armazém está vazio")
        void inicializarDadosPadrao_quandoVazio_deveCriarDados() {
            when(itemArmazemRepository.count()).thenReturn(0L);
            when(itemArmazemRepository.findByCategoriaAndNome(anyString(), anyString()))
                    .thenReturn(Optional.empty());
            when(itemArmazemRepository.save(any(ItemArmazem.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            armazemService.inicializarDadosPadrao();

            verify(itemArmazemRepository).count();
            verify(itemArmazemRepository, times(27)).save(any(ItemArmazem.class));
            verify(itemArmazemRepository).findByCategoriaAndNome("DETERGENTES", "Amaciador");
            verify(itemArmazemRepository).findByCategoriaAndNome("HIGIENE", "Champô");
            verify(itemArmazemRepository).findByCategoriaAndNome("VESTUARIO", "Meias");
            verify(itemArmazemRepository).findByCategoriaAndNome("CALCADO", "35");
            verify(itemArmazemRepository).findByCategoriaAndNome("CALCADO", "46");
        }

        @Test
        @DisplayName("deve não criar item padrão quando esse item já existe")
        void inicializarDadosPadrao_quandoItensJaExistem_deveNaoGuardar() {
            when(itemArmazemRepository.count()).thenReturn(0L);
            when(itemArmazemRepository.findByCategoriaAndNome(anyString(), anyString()))
                    .thenReturn(Optional.of(item(1L, "HIGIENE", "Existente", 1, 1)));

            armazemService.inicializarDadosPadrao();

            verify(itemArmazemRepository).count();
            verify(itemArmazemRepository, never()).save(any());
            verify(itemArmazemRepository, atLeast(27)).findByCategoriaAndNome(anyString(), anyString());
        }

        @Test
        @DisplayName("deve guardar item padrão com categoria normalizada")
        void inicializarDadosPadrao_deveGuardarComCamposEsperados() {
            when(itemArmazemRepository.count()).thenReturn(0L);
            when(itemArmazemRepository.findByCategoriaAndNome(anyString(), anyString()))
                    .thenReturn(Optional.empty());
            when(itemArmazemRepository.save(any(ItemArmazem.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ArgumentCaptor<ItemArmazem> captor = ArgumentCaptor.forClass(ItemArmazem.class);

            armazemService.inicializarDadosPadrao();

            verify(itemArmazemRepository, times(27)).save(captor.capture());

            List<ItemArmazem> guardados = captor.getAllValues();

            assertTrue(guardados.stream().anyMatch(i ->
                    "DETERGENTES".equals(i.getCategoria())
                            && "Amaciador".equals(i.getNome())
                            && i.getQuantidade() == 20
                            && i.getQuantidadeMinima() == 5));

            assertTrue(guardados.stream().anyMatch(i ->
                    "CALCADO".equals(i.getCategoria())
                            && "46".equals(i.getNome())
                            && "pares".equals(i.getUnidade())
                            && "46".equals(i.getTamanho())));
        }
    }

    @Nested
    @DisplayName("DTO conversion")
    class ConversaoTests {

        @Test
        @DisplayName("deve converter para estado OK quando quantidade é maior ou igual ao mínimo")
        void toDTO_quantidadeMaiorOuIgualMinimo_deveSerOK() {
            ItemArmazem item = item(1L, "HIGIENE", "Champô", 5, 5);

            when(itemArmazemRepository.findAllByOrderByCategoriaAscNomeAsc())
                    .thenReturn(List.of(item));

            ItemArmazemDTO dto = armazemService.listarTodos().get(0);

            assertEquals("OK", dto.getEstado());
        }

        @Test
        @DisplayName("deve converter para estado BAIXO quando quantidade é menor que mínimo")
        void toDTO_quantidadeMenorMinimo_deveSerBaixo() {
            ItemArmazem item = item(1L, "HIGIENE", "Champô", 4, 5);

            when(itemArmazemRepository.findAllByOrderByCategoriaAscNomeAsc())
                    .thenReturn(List.of(item));

            ItemArmazemDTO dto = armazemService.listarTodos().get(0);

            assertEquals("BAIXO", dto.getEstado());
        }
    }

    @Nested
    @DisplayName("sanity")
    class SanityTests {

        @Test
        @DisplayName("deve criar DTO de consumo com inner class correta")
        void consumoItemDTO_deveSerInstanciavel() {
            ConsumoEstatisticaDTO.ConsumoItemDTO dto =
                    new ConsumoEstatisticaDTO.ConsumoItemDTO("HIGIENE", "Champô", 2, "2026-01-01");

            assertInstanceOf(ConsumoEstatisticaDTO.ConsumoItemDTO.class, dto);
            assertEquals("HIGIENE", dto.getCategoria());
            assertEquals("Champô", dto.getNome());
            assertEquals(2, dto.getQuantidade());
            assertEquals("2026-01-01", dto.getData());
        }

        @Test
        @DisplayName("helpers devem criar objetos válidos para os testes")
        void helpers_devemCriarObjetosValidos() {
            ItemArmazem item = item(1L, "HIGIENE", "Champô", 10, 5);
            Roupa roupa = roupaComItem(item, 1);
            Marcacao marcacao = marcacaoComRoupas(List.of(roupa));

            assertNotNull(item);
            assertNotNull(roupa);
            assertNotNull(marcacao);
            assertNotNull(marcacao.getMarcacaoBalneario());
            assertFalse(marcacao.getMarcacaoBalneario().getRoupas().isEmpty());
        }
    }

    private static ItemArmazem item(
            Long id,
            String categoria,
            String nome,
            Integer quantidade,
            Integer quantidadeMinima) {

        ItemArmazem item = new ItemArmazem();
        item.setId(id);
        item.setCategoria(categoria);
        item.setNome(nome);
        item.setQuantidade(quantidade);
        item.setQuantidadeMinima(quantidadeMinima);
        item.setUnidade("un");
        item.setMarca("Marca");
        item.setTamanho("M");
        item.setVolume(1.0);
        item.setDescricao("Descrição");
        return item;
    }

    private static ItemArmazemDTO dto(
            Long id,
            String categoria,
            String nome,
            Integer quantidade,
            Integer quantidadeMinima) {

        ItemArmazemDTO dto = new ItemArmazemDTO();
        dto.setId(id);
        dto.setCategoria(categoria);
        dto.setNome(nome);
        dto.setQuantidade(quantidade);
        dto.setQuantidadeMinima(quantidadeMinima);
        return dto;
    }

    private static Roupa roupaComItem(ItemArmazem item, Integer quantidade) {
        Roupa roupa = new Roupa();
        roupa.setItem(item);
        roupa.setCategoria(item.getNome());
        roupa.setQuantidade(quantidade);
        return roupa;
    }

    private static Roupa roupaLegacy(String categoria, String tamanho, Integer quantidade) {
        Roupa roupa = new Roupa();
        roupa.setCategoria(categoria);
        roupa.setTamanho(tamanho);
        roupa.setQuantidade(quantidade);
        return roupa;
    }

    private static Marcacao marcacaoComRoupas(List<Roupa> roupas) {
        MarcacaoBalneario balneario = new MarcacaoBalneario();
        balneario.setRoupas(roupas);

        Marcacao marcacao = new Marcacao();
        marcacao.setMarcacaoBalneario(balneario);

        return marcacao;
    }

    private static Marcacao marcacaoEstatistica(EventoEstado estado, List<Roupa> roupas) {
        MarcacaoBalneario balneario = new MarcacaoBalneario();
        balneario.setRoupas(roupas);

        Marcacao marcacao = new Marcacao();
        marcacao.setEstado(estado);
        marcacao.setData(LocalDateTime.now());
        marcacao.setMarcacaoBalneario(balneario);

        return marcacao;
    }
}