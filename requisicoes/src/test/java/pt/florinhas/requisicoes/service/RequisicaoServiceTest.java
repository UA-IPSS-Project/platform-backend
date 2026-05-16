package pt.florinhas.requisicoes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pt.florinhas.common_data.domain.Funcionario;
import pt.florinhas.common_data.repository.FuncionarioRepository;
import pt.florinhas.requisicoes.domain.Material;
import pt.florinhas.requisicoes.domain.Requisicao;
import pt.florinhas.requisicoes.domain.RequisicaoEstado;
import pt.florinhas.requisicoes.domain.RequisicaoManutencao;
import pt.florinhas.requisicoes.domain.RequisicaoMaterial;
import pt.florinhas.requisicoes.domain.RequisicaoPrioridade;
import pt.florinhas.requisicoes.domain.RequisicaoTipo;
import pt.florinhas.requisicoes.domain.RequisicaoTransporte;
import pt.florinhas.requisicoes.domain.Transporte;
import pt.florinhas.requisicoes.dto.CriarRequisicaoManutencaoRequest;
import pt.florinhas.requisicoes.dto.CriarRequisicaoMaterialRequest;
import pt.florinhas.requisicoes.dto.CriarRequisicaoTransporteRequest;
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
    private NotificacaoService notificacaoService;

    private RequisicaoService service;

    @BeforeEach
    void setUp() {
        requisicaoRepository = mock(RequisicaoRepository.class);
        requisicaoMaterialRepository = mock(RequisicaoMaterialRepository.class);
        requisicaoTransporteRepository = mock(RequisicaoTransporteRepository.class);
        requisicaoManutencaoRepository = mock(RequisicaoManutencaoRepository.class);
        funcionarioRepository = mock(FuncionarioRepository.class);
        materialRepository = mock(MaterialRepository.class);
        transporteRepository = mock(TransporteRepository.class);
        notificacaoService = mock(NotificacaoService.class);

        service = new RequisicaoService(
                requisicaoRepository,
                requisicaoMaterialRepository,
                requisicaoTransporteRepository,
                requisicaoManutencaoRepository,
                funcionarioRepository,
                materialRepository,
                transporteRepository,
                mock(TipoManutencaoRepository.class),
                mock(ManutencaoItemRepository.class),
                mock(RequisicaoManutencaoItemRepository.class),
                notificacaoService);
    }

    @Test
    void listarTodas_DeveRetornarLista() {
        Requisicao requisicao = new RequisicaoManutencao();
        when(requisicaoRepository.findAll()).thenReturn(List.of(requisicao));

        List<Requisicao> resultado = service.listarTodas();

        assertEquals(1, resultado.size());
        assertSame(requisicao, resultado.getFirst());
        verify(requisicaoRepository).findAll();
    }

    @Test
    void obterPorId_DeveRetornarRequisicao() {
        Requisicao req = new RequisicaoManutencao();
        when(requisicaoRepository.findById(1L)).thenReturn(Optional.of(req));

        Requisicao result = service.obterPorId(1L);

        assertNotNull(result);
        assertSame(req, result);
    }

    @Test
    void obterPorId_QuandoNaoExiste_DeveLancarExcecao() {
        when(requisicaoRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.obterPorId(1L));
    }

    @Test
    void listarPorEstado_DeveChamarRepositorio() {
        service.listarPorEstado(RequisicaoEstado.ABERTO);
        verify(requisicaoRepository).findByEstado(RequisicaoEstado.ABERTO);
    }

    @Test
    void procurar_DeveChamarRepositorioComFiltros() {
        Requisicao requisicao = new RequisicaoManutencao();
        requisicao.setEstado(RequisicaoEstado.ABERTO);
        requisicao.setTipo(RequisicaoTipo.MATERIAL);
        requisicao.setPrioridade(RequisicaoPrioridade.ALTA);
        Funcionario criadoPor = new Funcionario();
        criadoPor.setNome("Maria Silva");
        requisicao.setCriadoPor(criadoPor);

        when(requisicaoRepository.findWithFilters(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(requisicao));

        List<Requisicao> resultado = service.procurar(
                RequisicaoEstado.ABERTO, RequisicaoTipo.MATERIAL, RequisicaoPrioridade.ALTA, "Maria", null, null);

        assertEquals(1, resultado.size());
        assertSame(requisicao, resultado.getFirst());
    }

    @Test
    void criarMaterial_quandoDadosValidos_deveCriarComTipoEAssociacoes() {
        Funcionario criadoPor = funcionarioComId(1L);
        Material material = new Material();
        material.setId(3L);

        when(funcionarioRepository.findById(1L)).thenReturn(Optional.of(criadoPor));
        when(materialRepository.findById(3L)).thenReturn(Optional.of(material));
        when(requisicaoMaterialRepository.save(any(RequisicaoMaterial.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CriarRequisicaoMaterialRequest request = new CriarRequisicaoMaterialRequest(
                "Pedido de material",
                RequisicaoPrioridade.MEDIA,
                2L,
                List.of(new CriarRequisicaoMaterialRequest.ItemMaterialRequest(3L, 5)),
                null);

        RequisicaoMaterial resultado = service.criarMaterial(request, 1L);

        assertEquals("Pedido de material", resultado.getDescricao());
        assertEquals(RequisicaoPrioridade.MEDIA, resultado.getPrioridade());
        assertSame(criadoPor, resultado.getCriadoPor());
        assertEquals(1, resultado.getItens().size());
    }

    @Test
    void criarMaterial_quandoMaterialNaoExiste_deveLancarExcecao() {
        Funcionario criadoPor = funcionarioComId(1L);
        when(funcionarioRepository.findById(1L)).thenReturn(Optional.of(criadoPor));
        when(materialRepository.findById(30L)).thenReturn(Optional.empty());

        CriarRequisicaoMaterialRequest request = new CriarRequisicaoMaterialRequest(
                "Pedido",
                RequisicaoPrioridade.ALTA,
                null,
                List.of(new CriarRequisicaoMaterialRequest.ItemMaterialRequest(30L, 1)),
                null);

        assertThrows(ResourceNotFoundException.class, () -> service.criarMaterial(request, 1L));
    }

    @Test
    void criarTransporte_quandoDadosValidos_deveCriarEAssociarVeiculos() {
        Funcionario criadoPor = funcionarioComId(1L);
        Transporte transporte = new Transporte();
        transporte.setId(5L);

        when(funcionarioRepository.findById(1L)).thenReturn(Optional.of(criadoPor));
        when(transporteRepository.findById(5L)).thenReturn(Optional.of(transporte));
        when(requisicaoTransporteRepository.save(any(RequisicaoTransporte.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CriarRequisicaoTransporteRequest request = new CriarRequisicaoTransporteRequest(
                "Pedido de transporte",
                RequisicaoPrioridade.ALTA,
                null,
                "Lisboa",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2),
                3,
                "Manuel",
                List.of(5L),
                null);

        RequisicaoTransporte resultado = service.criarTransporte(request, 1L);

        assertEquals("Pedido de transporte", resultado.getDescricao());
        assertEquals("Lisboa", resultado.getDestino());
        assertSame(criadoPor, resultado.getCriadoPor());
        assertEquals(1, resultado.getTransportes().size());
    }

    @Test
    void criarManutencao_quandoDadosValidos_deveCriar() {
        Funcionario criadoPor = funcionarioComId(1L);
        when(funcionarioRepository.findById(1L)).thenReturn(Optional.of(criadoPor));
        when(requisicaoManutencaoRepository.save(any(RequisicaoManutencao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CriarRequisicaoManutencaoRequest request = new CriarRequisicaoManutencaoRequest(
                "Manutenção de AC",
                RequisicaoPrioridade.MEDIA,
                null,
                List.of(),
                null);

        RequisicaoManutencao resultado = service.criarManutencao(request, 1L);

        assertEquals("Manutenção de AC", resultado.getDescricao());
        assertSame(criadoPor, resultado.getCriadoPor());
    }

    @Test
    void atualizarEstado_quandoTransicaoValida_deveAlterar() {
        Requisicao req = new RequisicaoManutencao();
        req.setEstado(RequisicaoEstado.ABERTO);
        req.setTipo(RequisicaoTipo.MANUTENCAO);
        req.setId(10L);
        Funcionario criador = new Funcionario();
        criador.setId(99L);
        req.setCriadoPor(criador);

        when(requisicaoRepository.findById(10L)).thenReturn(Optional.of(req));
        when(requisicaoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(funcionarioRepository.findById(1L)).thenReturn(Optional.of(new Funcionario()));

        service.atualizarEstado(10L, RequisicaoEstado.EM_PROGRESSO, 1L);

        assertEquals(RequisicaoEstado.EM_PROGRESSO, req.getEstado());
        verify(notificacaoService).notificarMudancaEstado(any(), eq(req));
    }

    @Test
    void atualizarEstado_quandoTransicaoInvalida_deveLancarExcecao() {
        Requisicao req = new RequisicaoManutencao();
        req.setEstado(RequisicaoEstado.FECHADO);
        req.setId(20L);

        when(requisicaoRepository.findById(20L)).thenReturn(Optional.of(req));

        assertThrows(IllegalArgumentException.class,
                () -> service.atualizarEstado(20L, RequisicaoEstado.EM_PROGRESSO, 1L));
    }

    private Funcionario funcionarioComId(Long id) {
        Funcionario f = new Funcionario();
        f.setId(id);
        f.setNome("Funcionario " + id);
        return f;
    }
}