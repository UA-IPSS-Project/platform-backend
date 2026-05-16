package pt.florinhas.requisicoes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import pt.florinhas.common_data.domain.Funcionario;
import pt.florinhas.common_data.repository.FuncionarioRepository;
import pt.florinhas.requisicoes.domain.*;
import pt.florinhas.requisicoes.dto.*;
import pt.florinhas.requisicoes.exception.ResourceNotFoundException;
import pt.florinhas.requisicoes.repository.*;

@ExtendWith(MockitoExtension.class)
class RequisicaoServiceTest {

    @Mock
    private RequisicaoRepository requisicaoRepository;
    @Mock
    private RequisicaoMaterialRepository requisicaoMaterialRepository;
    @Mock
    private RequisicaoTransporteRepository requisicaoTransporteRepository;
    @Mock
    private RequisicaoManutencaoRepository requisicaoManutencaoRepository;
    @Mock
    private FuncionarioRepository funcionarioRepository;
    @Mock
    private MaterialRepository materialRepository;
    @Mock
    private TransporteRepository transporteRepository;
    @Mock
    private TipoManutencaoRepository tipoManutencaoRepository;
    @Mock
    private ManutencaoItemRepository manutencaoItemRepository;
    @Mock
    private RequisicaoManutencaoItemRepository requisicaoManutencaoItemRepository;
    @Mock
    private NotificacaoService notificacaoService;

    private RequisicaoService service;

    @BeforeEach
    void setUp() {
        service = new RequisicaoService(
                requisicaoRepository,
                requisicaoMaterialRepository,
                requisicaoTransporteRepository,
                requisicaoManutencaoRepository,
                funcionarioRepository,
                materialRepository,
                transporteRepository,
                tipoManutencaoRepository,
                manutencaoItemRepository,
                requisicaoManutencaoItemRepository,
                notificacaoService);
    }

    @Nested
    @DisplayName("Testes de Consulta e Pesquisa")
    class SearchTests {

        @Test
        @DisplayName("Listar todas as requisições")
        void listarTodas_DeveRetornarTodas() {
            Requisicao req = new RequisicaoMaterial();
            when(requisicaoRepository.findAll()).thenReturn(List.of(req));

            List<Requisicao> result = service.listarTodas();
            assertEquals(1, result.size());
            assertSame(req, result.get(0));
        }

        @Test
        @DisplayName("Obter requisição por ID")
        void obterPorId_DeveRetornarSeExiste() {
            Requisicao req = new RequisicaoMaterial();
            when(requisicaoRepository.findById(1L)).thenReturn(Optional.of(req));

            Requisicao result = service.obterPorId(1L);
            assertSame(req, result);
        }

        @Test
        @DisplayName("Obter requisição por ID lança exceção se não existe")
        void obterPorId_NaoExiste_DeveLancarExcecao() {
            when(requisicaoRepository.findById(1L)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> service.obterPorId(1L));
        }

        @Test
        @DisplayName("Listar requisições por estado")
        void listarPorEstado_DeveFiltrar() {
            Requisicao req = new RequisicaoMaterial();
            when(requisicaoRepository.findByEstado(RequisicaoEstado.ABERTO)).thenReturn(List.of(req));

            List<Requisicao> result = service.listarPorEstado(RequisicaoEstado.ABERTO);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Procurar com filtros flexíveis")
        void procurar_ComFiltros_DeveNormalizarEFiltrar() {
            Requisicao req = new RequisicaoMaterial();
            when(requisicaoRepository.findWithFilters(any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(req));

            List<Requisicao> result = service.procurar(
                    RequisicaoEstado.ABERTO, RequisicaoTipo.MATERIAL, RequisicaoPrioridade.ALTA,
                    "  Maria  ", "2026-05-16", "2026-05-17");

            assertFalse(result.isEmpty());
            verify(requisicaoRepository).findWithFilters(
                    eq(RequisicaoEstado.ABERTO),
                    eq(RequisicaoTipo.MATERIAL),
                    eq(RequisicaoPrioridade.ALTA),
                    eq("%maria%"),
                    any(LocalDateTime.class),
                    any(LocalDateTime.class));
        }

        @Test
        @DisplayName("Procurar paginado")
        void procurarPaginated_DeveChamarRepositorio() {
            Page<Requisicao> page = new PageImpl<>(List.of(new RequisicaoMaterial()));
            Pageable pageable = PageRequest.of(0, 10);
            when(requisicaoRepository.findWithFiltersPaginated(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(page);

            Page<Requisicao> result = service.procurarPaginated(
                    RequisicaoEstado.FECHADO, RequisicaoTipo.TRANSPORTE, RequisicaoPrioridade.BAIXA,
                    "Maria", "2026-05-16", "2026-05-17", pageable);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
        }
    }

    @Nested
    @DisplayName("Testes de Criação de Requisições")
    class CreationTests {

        private Funcionario criadoPor;

        @BeforeEach
        void setupFuncionario() {
            criadoPor = new Funcionario();
            criadoPor.setId(10L);
            criadoPor.setNome("Criador");
        }

        @Test
        @DisplayName("Criar requisição de Material com sucesso")
        void criarMaterial_ComSucesso() {
            Material mat = new Material();
            mat.setId(100L);

            when(funcionarioRepository.findById(10L)).thenReturn(Optional.of(criadoPor));
            when(materialRepository.findById(100L)).thenReturn(Optional.of(mat));
            when(requisicaoMaterialRepository.save(any(RequisicaoMaterial.class)))
                    .thenAnswer(i -> i.getArgument(0));

            CriarRequisicaoMaterialRequest request = new CriarRequisicaoMaterialRequest(
                    "Pedido de canetas",
                    RequisicaoPrioridade.MEDIA,
                    null,
                    List.of(new CriarRequisicaoMaterialRequest.ItemMaterialRequest(100L, 5)),
                    new RequisicaoPeriodicaConfigRequest(PeriodicidadeFrequencia.SEMANAL, LocalDate.now(),
                            LocalDate.now().plusWeeks(4)));

            RequisicaoMaterial result = service.criarMaterial(request, 10L);

            assertNotNull(result);
            assertEquals("Pedido de canetas", result.getDescricao());
            assertEquals(RequisicaoPrioridade.MEDIA, result.getPrioridade());
            assertEquals(1, result.getItens().size());
            assertEquals(5, result.getItens().get(0).getQuantidade());
            assertEquals(PeriodicidadeFrequencia.SEMANAL, result.getPeriodicaFrequencia());
        }

        @Test
        @DisplayName("Criar requisição de Material lança erro se material não existe")
        void criarMaterial_MaterialInexistente_DeveLancarExcecao() {
            when(funcionarioRepository.findById(10L)).thenReturn(Optional.of(criadoPor));
            when(materialRepository.findById(100L)).thenReturn(Optional.empty());

            CriarRequisicaoMaterialRequest request = new CriarRequisicaoMaterialRequest(
                    "Pedido", RequisicaoPrioridade.MEDIA, null,
                    List.of(new CriarRequisicaoMaterialRequest.ItemMaterialRequest(100L, 5)), null);

            assertThrows(ResourceNotFoundException.class, () -> service.criarMaterial(request, 10L));
        }

        @Test
        @DisplayName("Criar requisição de Transporte lança erro se sem transportes selecionados")
        void criarTransporte_SemTransportes_DeveLancarExcecao() {
            CriarRequisicaoTransporteRequest request = new CriarRequisicaoTransporteRequest(
                    "Viagem", RequisicaoPrioridade.MEDIA, null, "Porto",
                    LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2),
                    3, "Pedro", null, null);

            assertThrows(IllegalArgumentException.class, () -> service.criarTransporte(request, 10L));
        }

        @Test
        @DisplayName("Criar requisição de Transporte com sucesso")
        void criarTransporte_ComSucesso() {
            Transporte veiculo = new Transporte();
            veiculo.setId(200L);

            when(funcionarioRepository.findById(10L)).thenReturn(Optional.of(criadoPor));
            when(transporteRepository.findById(200L)).thenReturn(Optional.of(veiculo));
            when(requisicaoTransporteRepository.save(any(RequisicaoTransporte.class)))
                    .thenAnswer(i -> i.getArgument(0));

            CriarRequisicaoTransporteRequest request = new CriarRequisicaoTransporteRequest(
                    "Viagem de serviço  ",
                    RequisicaoPrioridade.ALTA,
                    null,
                    "Porto",
                    LocalDateTime.now().plusDays(1),
                    LocalDateTime.now().plusDays(1).plusHours(2),
                    5,
                    "  Pedro ",
                    List.of(200L),
                    null);

            RequisicaoTransporte result = service.criarTransporte(request, 10L);

            assertNotNull(result);
            assertEquals("Viagem de serviço", result.getDescricao());
            assertEquals("Pedro", result.getCondutor());
            assertEquals("Porto", result.getDestino());
            assertEquals(1, result.getTransportes().size());
        }

        @Test
        @DisplayName("Criar transporte lança erro com datas inválidas")
        void criarTransporte_DatasInvalidas_DeveLancarExcecao() {
            // Saída no passado
            CriarRequisicaoTransporteRequest reqSaidaPassado = new CriarRequisicaoTransporteRequest(
                    "Viagem", RequisicaoPrioridade.MEDIA, null, "Dest",
                    LocalDateTime.now().minusDays(1), LocalDateTime.now().plusHours(2),
                    2, "Pedro", List.of(1L), null);
            assertThrows(IllegalArgumentException.class, () -> service.criarTransporte(reqSaidaPassado, 10L));

            // Regresso antes da saída
            CriarRequisicaoTransporteRequest reqRegressoAntes = new CriarRequisicaoTransporteRequest(
                    "Viagem", RequisicaoPrioridade.MEDIA, null, "Dest",
                    LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).minusHours(1),
                    2, "Pedro", List.of(1L), null);
            assertThrows(IllegalArgumentException.class, () -> service.criarTransporte(reqRegressoAntes, 10L));
        }

        @Test
        @DisplayName("Criar requisição de Manutenção com sucesso")
        void criarManutencao_ComSucesso() {
            ManutencaoItem item = new ManutencaoItem();
            item.setId(300L);
            Transporte veiculo = new Transporte();
            veiculo.setId(400L);

            when(funcionarioRepository.findById(10L)).thenReturn(Optional.of(criadoPor));
            when(manutencaoItemRepository.findById(300L)).thenReturn(Optional.of(item));
            when(transporteRepository.findById(400L)).thenReturn(Optional.of(veiculo));
            when(requisicaoManutencaoRepository.save(any(RequisicaoManutencao.class)))
                    .thenAnswer(i -> i.getArgument(0));

            ManutencaoItemRequest itemReq = new ManutencaoItemRequest(300L, 400L, "Fuga de óleo");

            CriarRequisicaoManutencaoRequest request = new CriarRequisicaoManutencaoRequest(
                    "Reparar viatura", RequisicaoPrioridade.ALTA, null, List.of(itemReq), null);

            RequisicaoManutencao result = service.criarManutencao(request, 10L);

            assertNotNull(result);
            assertEquals("Reparar viatura", result.getDescricao());
            assertEquals(1, result.getItens().size());
            assertEquals("Fuga de óleo", result.getItens().get(0).getObservacoes());
            assertEquals(veiculo, result.getItens().get(0).getTransporte());
        }

        @Test
        @DisplayName("Criar manutenção lança erro se transporte do item não encontrado")
        void criarManutencao_TransporteInexistente_DeveLancarExcecao() {
            ManutencaoItem item = new ManutencaoItem();
            item.setId(300L);

            when(funcionarioRepository.findById(10L)).thenReturn(Optional.of(criadoPor));
            when(manutencaoItemRepository.findById(300L)).thenReturn(Optional.of(item));
            when(transporteRepository.findById(400L)).thenReturn(Optional.empty());

            ManutencaoItemRequest itemReq = new ManutencaoItemRequest(300L, 400L, "Fuga");

            CriarRequisicaoManutencaoRequest request = new CriarRequisicaoManutencaoRequest(
                    "Reparar viatura", RequisicaoPrioridade.ALTA, null, List.of(itemReq), null);

            assertThrows(ResourceNotFoundException.class, () -> service.criarManutencao(request, 10L));
        }

        @Test
        @DisplayName("Validar configuração periódica com data de fim antes de início deve lançar exceção")
        void aplicarConfiguracaoPeriodica_DataFimAntesInicio_DeveLancarExcecao() {
            when(funcionarioRepository.findById(10L)).thenReturn(Optional.of(criadoPor));
            CriarRequisicaoMaterialRequest request = new CriarRequisicaoMaterialRequest(
                    "Pedido", RequisicaoPrioridade.MEDIA, null, List.of(),
                    new RequisicaoPeriodicaConfigRequest(PeriodicidadeFrequencia.MENSAL, LocalDate.now().plusDays(5),
                            LocalDate.now()));

            assertThrows(IllegalArgumentException.class, () -> service.criarMaterial(request, 10L));
        }
    }

    @Nested
    @DisplayName("Testes de Atualização de Estado")
    class StatusUpdateTests {

        @Test
        @DisplayName("Atualizar estado transição idêntica deve retornar imediatamente")
        void atualizarEstado_TransicaoIdentica_DeveFazerNada() {
            Requisicao req = new RequisicaoMaterial();
            req.setEstado(RequisicaoEstado.ABERTO);
            Funcionario alterador = new Funcionario();
            alterador.setId(10L);

            when(requisicaoRepository.findById(1L)).thenReturn(Optional.of(req));
            when(funcionarioRepository.findById(10L)).thenReturn(Optional.of(alterador));
            when(requisicaoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            Requisicao result = service.atualizarEstado(1L, RequisicaoEstado.ABERTO, 10L);
            assertSame(req, result);
        }

        @Test
        @DisplayName("Atualizar estado de requisição já finalizada (FECHADO/RECUSADO) deve lançar exceção")
        void atualizarEstado_RequisicaoFinalizada_DeveLancarExcecao() {
            Requisicao req = new RequisicaoMaterial();
            req.setEstado(RequisicaoEstado.FECHADO);
            when(requisicaoRepository.findById(1L)).thenReturn(Optional.of(req));

            assertThrows(IllegalArgumentException.class,
                    () -> service.atualizarEstado(1L, RequisicaoEstado.EM_PROGRESSO, 10L));
        }

        @Test
        @DisplayName("Atualizar estado com transição inválida deve lançar exceção")
        void atualizarEstado_TransicaoInvalida_DeveLancarExcecao() {
            Requisicao req = new RequisicaoMaterial();
            req.setEstado(RequisicaoEstado.FECHADO);
            when(requisicaoRepository.findById(1L)).thenReturn(Optional.of(req));

            assertThrows(IllegalArgumentException.class,
                    () -> service.atualizarEstado(1L, RequisicaoEstado.EM_PROGRESSO, 10L));
        }

        @Test
        @DisplayName("Atualizar estado deve notificar criador se diferente de quem alterou")
        void atualizarEstado_DeveNotificarCriador() {
            Requisicao req = new RequisicaoMaterial();
            req.setEstado(RequisicaoEstado.ABERTO);
            Funcionario criador = new Funcionario();
            criador.setId(55L);
            req.setCriadoPor(criador);

            Funcionario alterador = new Funcionario();
            alterador.setId(66L);

            when(requisicaoRepository.findById(1L)).thenReturn(Optional.of(req));
            when(funcionarioRepository.findById(66L)).thenReturn(Optional.of(alterador));
            when(requisicaoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            service.atualizarEstado(1L, RequisicaoEstado.EM_PROGRESSO, 66L);

            verify(notificacaoService).notificarMudancaEstado(55L, req);
        }

        @Test
        @DisplayName("Atualizar estado não deve notificar se alterador for o próprio criador")
        void atualizarEstado_NaoDeveNotificarSeCriadorAlterar() {
            Requisicao req = new RequisicaoMaterial();
            req.setEstado(RequisicaoEstado.ABERTO);
            Funcionario criador = new Funcionario();
            criador.setId(55L);
            req.setCriadoPor(criador);

            when(requisicaoRepository.findById(1L)).thenReturn(Optional.of(req));
            when(funcionarioRepository.findById(55L)).thenReturn(Optional.of(criador));
            when(requisicaoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            service.atualizarEstado(1L, RequisicaoEstado.FECHADO, 55L);

            verifyNoInteractions(notificacaoService);
        }
    }

    @Nested
    @DisplayName("Testes de Catálogos (Materiais, Transportes, Tipos Manutenção, Itens)")
    class CatalogTests {

        @Test
        @DisplayName("Catálogo de Material - listar, criar, atualizar, eliminar")
        void materialCatalogFlow() {
            Material mat = new Material();
            when(materialRepository.findAllByOrderByCategoriaAscNomeAscAtributoAscValorAtributoAsc())
                    .thenReturn(List.of(mat));

            List<Material> listResult = service.listarMateriais();
            assertFalse(listResult.isEmpty());

            // Criar
            CriarMaterialRequest createReq = new CriarMaterialRequest(
                    "  Caneta Azul  ", "ESCRITORIO", "  Cor ", "  Azul ");
            when(materialRepository.save(any(Material.class))).thenAnswer(i -> i.getArgument(0));
            Material created = service.criarMaterialCatalogo(createReq);
            assertEquals("Caneta Azul", created.getNome());
            assertEquals("ESCRITORIO", created.getCategoria());
            assertEquals("Cor", created.getAtributo());
            assertEquals("Azul", created.getValorAtributo());

            // Atualizar
            when(materialRepository.findById(1L)).thenReturn(Optional.of(mat));
            Material updated = service.atualizarMaterialCatalogo(1L, createReq);
            assertNotNull(updated);

            // Apagar material associado lança erro
            when(requisicaoMaterialRepository.existsByItensMaterialId(1L)).thenReturn(true);
            assertThrows(IllegalArgumentException.class, () -> service.apagarMaterialCatalogo(1L));

            // Apagar com sucesso
            when(requisicaoMaterialRepository.existsByItensMaterialId(2L)).thenReturn(false);
            when(materialRepository.existsById(2L)).thenReturn(true);
            service.apagarMaterialCatalogo(2L);
            verify(materialRepository).deleteById(2L);
        }

        @Test
        @DisplayName("Catálogo de Transporte - listar, criar, atualizar, mudar categoria")
        void transporteCatalogFlow() {
            Transporte v = new Transporte();
            when(transporteRepository.findAll(any(org.springframework.data.domain.Sort.class)))
                    .thenReturn(List.of(v));

            List<Transporte> listResult = service.listarTransportes();
            assertFalse(listResult.isEmpty());

            // Criar
            CriarTransporteRequest createReq = new CriarTransporteRequest(
                    "  TX-01 ", "Ligeiro", TransporteCategoria.LIGEIRO_DE_PASSAGEIROS, "  00-AA-00 ", "Renault", "Clio",
                    5, LocalDate.now());
            when(transporteRepository.findByMatricula("00-AA-00")).thenReturn(Optional.empty());
            when(transporteRepository.save(any(Transporte.class))).thenAnswer(i -> {
                Transporte t = i.getArgument(0);
                t.setId(1L);
                return t;
            });
            Transporte created = service.criarTransporteCatalogo(createReq);
            assertEquals("TX-01", created.getCodigo());
            assertEquals("00-AA-00", created.getMatricula());

            // Criar com matrícula repetida lança exceção
            when(transporteRepository.findByMatricula("00-AA-00")).thenReturn(Optional.of(v));
            assertThrows(IllegalArgumentException.class, () -> service.criarTransporteCatalogo(createReq));

            // Atualizar
            when(transporteRepository.findById(1L)).thenReturn(Optional.of(created));
            when(transporteRepository.findByMatricula("00-AA-00")).thenReturn(Optional.of(created));
            Transporte updated = service.atualizarTransporteCatalogo(1L, createReq);
            assertNotNull(updated);

            // Mudar categoria para ABATIDO com requisições ativas lança erro
            when(transporteRepository.findById(1L)).thenReturn(Optional.of(created));
            when(requisicaoTransporteRepository.existsByTransporteId(1L)).thenReturn(true);
            assertThrows(IllegalStateException.class,
                    () -> service.atualizarCategoriaTransporte(1L, TransporteCategoria.ABATIDO_VENDIDO_DESCONTINUADO));
        }

        @Test
        @DisplayName("Catálogo de Transporte - Mover veículos por categoria")
        void moverVeiculosPorCategoria_DeveAlterarEmLote() {
            Transporte t1 = new Transporte();
            t1.setCategoria(TransporteCategoria.LIGEIRO_DE_PASSAGEIROS);
            Transporte t2 = new Transporte();
            t2.setCategoria(TransporteCategoria.LIGEIRO_DE_PASSAGEIROS);

            when(transporteRepository.findByCategoria(TransporteCategoria.LIGEIRO_DE_PASSAGEIROS))
                    .thenReturn(List.of(t1, t2));

            service.moverVeiculosPorCategoria(TransporteCategoria.LIGEIRO_DE_PASSAGEIROS,
                    TransporteCategoria.ABATIDO_VENDIDO_DESCONTINUADO);

            assertEquals(TransporteCategoria.ABATIDO_VENDIDO_DESCONTINUADO, t1.getCategoria());
            assertEquals(TransporteCategoria.ABATIDO_VENDIDO_DESCONTINUADO, t2.getCategoria());
            verify(transporteRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("Mover veículos com categorias de origem e destino iguais deve lançar exceção")
        void moverVeiculosPorCategoria_Iguais_DeveLancarExcecao() {
            assertThrows(IllegalArgumentException.class,
                    () -> service.moverVeiculosPorCategoria(TransporteCategoria.LIGEIRO_DE_PASSAGEIROS,
                            TransporteCategoria.LIGEIRO_DE_PASSAGEIROS));
        }

        @Test
        @DisplayName("Catálogo de Tipos Manutenção - listar, criar, atualizar, eliminar")
        void tipoManutencaoCatalogFlow() {
            TipoManutencao tipo = new TipoManutencao();
            tipo.setId(1L);
            tipo.setNome("Preventiva");

            when(tipoManutencaoRepository.findAllByOrderByNomeAsc()).thenReturn(List.of(tipo));

            List<TipoManutencao> listResult = service.listarTiposManutencao();
            assertEquals(1, listResult.size());

            // Criar
            CriarTipoManutencaoRequest createReq = new CriarTipoManutencaoRequest("  Corretiva ", "Desc");
            when(tipoManutencaoRepository.findByNomeIgnoreCase("Corretiva")).thenReturn(Optional.empty());
            when(tipoManutencaoRepository.save(any(TipoManutencao.class))).thenAnswer(i -> i.getArgument(0));

            TipoManutencao created = service.criarTipoManutencao(createReq);
            assertEquals("Corretiva", created.getNome());

            // Criar nome duplicado
            when(tipoManutencaoRepository.findByNomeIgnoreCase("Corretiva")).thenReturn(Optional.of(tipo));
            assertThrows(IllegalArgumentException.class, () -> service.criarTipoManutencao(createReq));

            // Atualizar
            when(tipoManutencaoRepository.findById(1L)).thenReturn(Optional.of(tipo));
            when(tipoManutencaoRepository.findByNomeIgnoreCase("Corretiva")).thenReturn(Optional.empty());
            TipoManutencao updated = service.atualizarTipoManutencao(1L, createReq);
            assertNotNull(updated);

            // Eliminar
            service.apagarTipoManutencao(1L);
            verify(tipoManutencaoRepository).delete(tipo);
        }

        @Test
        @DisplayName("Catálogo de Itens Manutenção - listar, criar, atualizar, eliminar")
        void manutencaoItemCatalogFlow() {
            ManutencaoItem item = new ManutencaoItem();
            item.setId(1L);

            when(manutencaoItemRepository.findAllByOrderByCategoriaAscEspacoAsc()).thenReturn(List.of(item));
            when(manutencaoItemRepository.findByCategoria("Predial")).thenReturn(List.of(item));

            assertFalse(service.listarManutencaoItems().isEmpty());
            assertFalse(service.listarManutencaoItemsPorCategoria("Predial").isEmpty());

            // Criar
            CriarManutencaoItemRequest createReq = new CriarManutencaoItemRequest("  Jardim ", " Exterior", " Rega ");
            when(manutencaoItemRepository.save(any(ManutencaoItem.class))).thenAnswer(i -> i.getArgument(0));
            ManutencaoItem created = service.criarManutencaoItem(createReq);
            assertEquals("Jardim", created.getCategoria());
            assertEquals("Exterior", created.getEspaco());
            assertEquals("Rega", created.getItemVerificacao());

            // Atualizar
            when(manutencaoItemRepository.findById(1L)).thenReturn(Optional.of(item));
            ManutencaoItem updated = service.atualizarManutencaoItem(1L, createReq);
            assertNotNull(updated);

            // Apagar com requisições associadas lança erro
            when(manutencaoItemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(requisicaoManutencaoItemRepository.existsByManutencaoItemId(1L)).thenReturn(true);
            assertThrows(IllegalArgumentException.class, () -> service.apagarManutencaoItem(1L));

            // Apagar com sucesso
            when(requisicaoManutencaoItemRepository.existsByManutencaoItemId(1L)).thenReturn(false);
            service.apagarManutencaoItem(1L);
            verify(manutencaoItemRepository).delete(item);
        }
    }
}