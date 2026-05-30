package pt.florinhas.requisicoes.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import pt.florinhas.common_data.domain.Funcionario;
import pt.florinhas.common_data.domain.FuncionarioTipo;
import pt.florinhas.common_data.repository.FuncionarioRepository;
import pt.florinhas.requisicoes.domain.Material;
import pt.florinhas.requisicoes.domain.RequisicaoEstado;
import pt.florinhas.requisicoes.domain.RequisicaoMaterial;
import pt.florinhas.requisicoes.domain.RequisicaoPrioridade;
import pt.florinhas.requisicoes.domain.RequisicaoTipo;
import pt.florinhas.requisicoes.domain.Transporte;
import pt.florinhas.requisicoes.domain.TransporteCategoria;
import pt.florinhas.requisicoes.dto.CriarMaterialRequest;
import pt.florinhas.requisicoes.dto.CriarRequisicaoMaterialRequest;
import pt.florinhas.requisicoes.dto.CriarTransporteRequest;
import pt.florinhas.requisicoes.dto.RequisicaoPeriodicaConfigRequest;
import pt.florinhas.requisicoes.exception.ResourceNotFoundException;
import pt.florinhas.requisicoes.repository.ManutencaoItemRepository;
import pt.florinhas.requisicoes.repository.MaterialRepository;
import pt.florinhas.requisicoes.repository.RequisicaoManutencaoItemRepository;
import pt.florinhas.requisicoes.repository.RequisicaoManutencaoRepository;
import pt.florinhas.requisicoes.repository.RequisicaoMaterialRepository;
import pt.florinhas.requisicoes.repository.RequisicaoRepository;
import pt.florinhas.requisicoes.repository.RequisicaoTransporteRepository;
import pt.florinhas.requisicoes.repository.TipoManutencaoRepository;
import pt.florinhas.requisicoes.repository.TransporteRepository;

class RequisicaoServiceTest {

    private RequisicaoRepository requisicaoRepository;
    private RequisicaoMaterialRepository requisicaoMaterialRepository;
    private RequisicaoTransporteRepository requisicaoTransporteRepository;
    private RequisicaoManutencaoRepository requisicaoManutencaoRepository;
    private FuncionarioRepository funcionarioRepository;
    private MaterialRepository materialRepository;
    private TransporteRepository transporteRepository;
    private TipoManutencaoRepository tipoManutencaoRepository;
    private ManutencaoItemRepository manutencaoItemRepository;
    private RequisicaoManutencaoItemRepository requisicaoManutencaoItemRepository;
    private NotificacaoService notificacaoService;

    private RequisicaoService service;

    @BeforeEach
    void setUp() {

        requisicaoRepository =
                mock(RequisicaoRepository.class);

        requisicaoMaterialRepository =
                mock(RequisicaoMaterialRepository.class);

        requisicaoTransporteRepository =
                mock(RequisicaoTransporteRepository.class);

        requisicaoManutencaoRepository =
                mock(RequisicaoManutencaoRepository.class);

        funcionarioRepository =
                mock(FuncionarioRepository.class);

        materialRepository =
                mock(MaterialRepository.class);

        transporteRepository =
                mock(TransporteRepository.class);

        tipoManutencaoRepository =
                mock(TipoManutencaoRepository.class);

        manutencaoItemRepository =
                mock(ManutencaoItemRepository.class);

        requisicaoManutencaoItemRepository =
                mock(RequisicaoManutencaoItemRepository.class);

        notificacaoService =
                mock(NotificacaoService.class);

        service =
                new RequisicaoService(
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

    @Test
    void listarTodas_DeveRetornarLista() {

        when(requisicaoRepository.findAll())
                .thenReturn(List.of(
                        new RequisicaoMaterial()));

        assertEquals(
                1,
                service.listarTodas().size());
    }

    @Test
    void obterPorId_DeveRetornar() {

        RequisicaoMaterial requisicao =
                new RequisicaoMaterial();

        when(requisicaoRepository.findById(1L))
                .thenReturn(Optional.of(requisicao));

        assertEquals(
                requisicao,
                service.obterPorId(1L));
    }

    @Test
    void obterPorId_DeveFalhar() {

        when(requisicaoRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.obterPorId(1L));
    }

    @Test
    void listarPorEstado_DeveRetornar() {

        when(requisicaoRepository.findByEstado(
                RequisicaoEstado.ABERTO))
                .thenReturn(List.of(
                        new RequisicaoMaterial()));

        assertEquals(
                1,
                service.listarPorEstado(
                        RequisicaoEstado.ABERTO)
                        .size());
    }

    @Test
    void procurar_DeveExecutar() {

        when(requisicaoRepository.findWithFilters(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()))
                .thenReturn(List.of(
                        new RequisicaoMaterial()));

        assertEquals(
                1,
                service.procurar(
                        null,
                        null,
                        null,
                        "nuno",
                        null,
                        null)
                        .size());
    }

    @Test
    void procurarPaginated_DeveExecutar() {

        when(requisicaoRepository.findIdsPaginated(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()))
                .thenReturn(
                        new PageImpl<>(
                                List.of(1L)));

        RequisicaoMaterial req =
                new RequisicaoMaterial();

        req.setId(1L);

        when(requisicaoRepository.findByIdsWithCriadoPor(
                List.of(1L)))
                .thenReturn(List.of(req));

        assertEquals(
                1,
                service.procurarPaginated(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        PageRequest.of(0, 10))
                        .getContent()
                        .size());
    }

    @Test
    void criarMaterial_DeveCriar() {

        Funcionario funcionario =
                criarFuncionario();

        Material material =
                new Material();

        material.setId(1L);

        when(funcionarioRepository.findById(1L))
                .thenReturn(Optional.of(funcionario));

        when(materialRepository.findAllById(any()))
                .thenReturn(List.of(material));

        when(requisicaoMaterialRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        when(funcionarioRepository.findByTipo(
                FuncionarioTipo.SECRETARIA))
                .thenReturn(List.of());

        CriarRequisicaoMaterialRequest request =
                new CriarRequisicaoMaterialRequest(
                        "Descricao",
                        RequisicaoPrioridade.ALTA,
                        null,
                        List.of(
                                new CriarRequisicaoMaterialRequest.ItemMaterialRequest(
                                        1L,
                                        5)),
                        null);

        RequisicaoMaterial result =
                service.criarMaterial(
                        request,
                        1L);

        assertEquals(
                RequisicaoTipo.MATERIAL,
                result.getTipo());
    }

    @Test
    void atualizarEstado_DeveAtualizar() {

        Funcionario funcionario =
                criarFuncionario();

        RequisicaoMaterial requisicao =
                new RequisicaoMaterial();

        requisicao.setEstado(
                RequisicaoEstado.ABERTO);

        Funcionario criador =
                new Funcionario();

        criador.setId(5L);

        requisicao.setCriadoPor(criador);

        when(requisicaoRepository.findById(1L))
                .thenReturn(Optional.of(requisicao));

        when(funcionarioRepository.findById(1L))
                .thenReturn(Optional.of(funcionario));

        when(requisicaoRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        service.atualizarEstado(
                1L,
                RequisicaoEstado.EM_PROGRESSO,
                1L);

        assertEquals(
                RequisicaoEstado.EM_PROGRESSO,
                requisicao.getEstado());

        verify(notificacaoService)
                .notificarMudancaEstado(
                        5L,
                        requisicao);
    }

    @Test
    void atualizarEstado_NaoDeveNotificarMesmoAutor() {

        Funcionario funcionario =
                criarFuncionario();

        RequisicaoMaterial requisicao =
                new RequisicaoMaterial();

        requisicao.setEstado(
                RequisicaoEstado.ABERTO);

        requisicao.setCriadoPor(funcionario);

        when(requisicaoRepository.findById(1L))
                .thenReturn(Optional.of(requisicao));

        when(funcionarioRepository.findById(1L))
                .thenReturn(Optional.of(funcionario));

        when(requisicaoRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        service.atualizarEstado(
                1L,
                RequisicaoEstado.EM_PROGRESSO,
                1L);

        verify(notificacaoService, never())
                .notificarMudancaEstado(
                        any(),
                        any());
    }

    @Test
    void criarMaterialCatalogo_DeveCriar() {

        when(materialRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        Material result =
                service.criarMaterialCatalogo(
                        new CriarMaterialRequest(
                                "Caneta",
                                "ESC",
                                "Cor",
                                "Azul"));

        assertEquals(
                "Caneta",
                result.getNome());
    }

    @Test
    void apagarMaterialCatalogo_DeveApagar() {

        when(materialRepository.existsById(1L))
                .thenReturn(true);

        when(requisicaoMaterialRepository
                .existsByItensMaterialId(1L))
                .thenReturn(false);

        service.apagarMaterialCatalogo(1L);

        verify(materialRepository)
                .deleteById(1L);
    }

    @Test
    void apagarMaterialCatalogo_DeveFalharQuandoAssociado() {

        when(requisicaoMaterialRepository
                .existsByItensMaterialId(1L))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.apagarMaterialCatalogo(
                        1L));
    }

    @Test
    void criarTransporteCatalogo_DeveCriar() {

        when(transporteRepository.findByMatricula(
                "AA-00-BB"))
                .thenReturn(Optional.empty());

        when(transporteRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        Transporte result =
                service.criarTransporteCatalogo(
                        new CriarTransporteRequest(
                                "T1",
                                "Carrinha",
                                TransporteCategoria.LIGEIRO_DE_PASSAGEIROS,
                                "AA-00-BB",
                                "Ford",
                                "Transit",
                                9,
                                LocalDate.now()));

        assertEquals(
                "T1",
                result.getCodigo());
    }

    @Test
    void atualizarPeriodicidade_DeveAtualizar() {

        RequisicaoMaterial req =
                new RequisicaoMaterial();

        when(requisicaoRepository.findById(1L))
                .thenReturn(Optional.of(req));

        when(requisicaoRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        RequisicaoPeriodicaConfigRequest config =
                new RequisicaoPeriodicaConfigRequest(
                        pt.florinhas.requisicoes.domain.PeriodicidadeFrequencia.SEMANAL,
                        LocalDate.now(),
                        LocalDate.now().plusDays(1));

        service.atualizarPeriodicidade(
                1L,
                config);

        assertEquals(
                pt.florinhas.requisicoes.domain.PeriodicidadeFrequencia.SEMANAL,
                req.getPeriodicaFrequencia());
    }

    @Test
    void cancelarPeriodicidade_DeveLimpar() {

        RequisicaoMaterial req =
                new RequisicaoMaterial();

        req.setPeriodicaFrequencia(
                pt.florinhas.requisicoes.domain.PeriodicidadeFrequencia.MENSAL);

        when(requisicaoRepository.findById(1L))
                .thenReturn(Optional.of(req));

        when(requisicaoRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        service.cancelarPeriodicidade(1L);

        assertEquals(
                null,
                req.getPeriodicaFrequencia());
    }

    @Test
        void notificarSecretarias_NaoDeveFalhar() {

        Funcionario funcionario =
                criarFuncionario();

        Material material =
                new Material();

        material.setId(1L);

        when(funcionarioRepository.findById(1L))
                .thenReturn(Optional.of(funcionario));

        when(materialRepository.findAllById(any()))
                .thenReturn(List.of(material));

        when(requisicaoMaterialRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        when(funcionarioRepository.findByTipo(
                FuncionarioTipo.SECRETARIA))
                .thenThrow(new RuntimeException());

        assertDoesNotThrow(() ->
                service.criarMaterial(
                        new CriarRequisicaoMaterialRequest(
                                "",
                                RequisicaoPrioridade.ALTA,
                                null,
                                List.of(
                                        new CriarRequisicaoMaterialRequest.ItemMaterialRequest(
                                                1L,
                                                1)),
                                null),
                        1L));
        }

    private Funcionario criarFuncionario() {

        Funcionario funcionario =
                new Funcionario();

        funcionario.setId(1L);
        funcionario.setNome("Nuno");

        return funcionario;
    }
}