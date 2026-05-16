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
    void obterPorId_DeveLancarException() {
        when(requisicaoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.obterPorId(1L));
    }

    @Test
    void listarPorEstado_DeveRetornarLista() {
        when(requisicaoRepository.findByEstado(RequisicaoEstado.ABERTO))
                .thenReturn(List.of(mock(Requisicao.class)));

        assertEquals(1, service.listarPorEstado(RequisicaoEstado.ABERTO).size());
    }

    @Test
    void procurar_deveDelegarFindWithFilters() {
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
                RequisicaoPrioridade.BAIXA,
                null,
                List.of(new CriarRequisicaoMaterialRequest.ItemMaterialRequest(30L, 1)), null);

        assertThrows(ResourceNotFoundException.class, () -> service.criarMaterial(request, 1L));
    }

    @Test
    void criarTransporte_quandoDadosValidos_deveCriarComTipoEAssociacoes() {
        Funcionario criadoPor = funcionarioComId(10L);
        Transporte transporte = new Transporte();
        transporte.setId(30L);

        when(funcionarioRepository.findById(10L)).thenReturn(Optional.of(criadoPor));
        when(transporteRepository.findById(30L)).thenReturn(Optional.of(transporte));
        when(requisicaoTransporteRepository.save(any(RequisicaoTransporte.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CriarRequisicaoTransporteRequest request = new CriarRequisicaoTransporteRequest(
                "Pedido de carrinha",
                RequisicaoPrioridade.ALTA,
                20L,
                "Centro de Dia",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(4),
                8,
                "Condutor 1",
                List.of(30L), null);

        RequisicaoTransporte resultado = service.criarTransporte(request, 10L);

        assertEquals(RequisicaoTipo.TRANSPORTE, resultado.getTipo());
        assertSame(criadoPor, resultado.getCriadoPor());
        assertSame(transporte, resultado.getTransporte());
        assertEquals(1, resultado.getTransportes().size());
    }

    @Test
    void criarTransporte_quandoTransporteNaoExiste_deveLancarExcecao() {
        Funcionario criadoPor = funcionarioComId(10L);
        when(funcionarioRepository.findById(10L)).thenReturn(Optional.of(criadoPor));
        when(transporteRepository.findById(90L)).thenReturn(Optional.empty());

        CriarRequisicaoTransporteRequest request = new CriarRequisicaoTransporteRequest(
                "Pedido",
                RequisicaoPrioridade.BAIXA,
                null,
                "Hospital",
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(2).plusHours(2),
                3,
                "Condutor Teste",
                List.of(90L), null);

        assertThrows(ResourceNotFoundException.class, () -> service.criarTransporte(request, 10L));
    }

    @Test
    void criarTransporte_quandoNenhumTransporteFornecido_deveLancarErro() {
        CriarRequisicaoTransporteRequest request = new CriarRequisicaoTransporteRequest(
                "Pedido inválido",
                RequisicaoPrioridade.MEDIA,
                null,
                "Centro",
                LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(3).plusHours(1),
                2,
                "Condutor Teste",
                null, null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.criarTransporte(request, 10L));

        assertEquals("É obrigatório indicar pelo menos um transporte.", exception.getMessage());
    }

    @Test
    void criarTransporte_quandoRegressoAntesDaSaida_deveLancarErro() {
        CriarRequisicaoTransporteRequest request = new CriarRequisicaoTransporteRequest(
                "Pedido inválido",
                RequisicaoPrioridade.MEDIA,
                null,
                "Centro",
                LocalDateTime.now().plusDays(3).plusHours(2),
                LocalDateTime.now().plusDays(3).plusHours(1),
                2,
                "Condutor Teste",
                List.of(30L), null);

        assertThrows(IllegalArgumentException.class, () -> service.criarTransporte(request, 10L));
    }

    @Test
    void criarManutencao_quandoDadosValidos_deveCriarComTipo() {
        Funcionario criadoPor = funcionarioComId(100L);
        when(funcionarioRepository.findById(100L)).thenReturn(Optional.of(criadoPor));
        when(requisicaoManutencaoRepository.save(any(RequisicaoManutencao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CriarRequisicaoManutencaoRequest request = new CriarRequisicaoManutencaoRequest(
                "Reparar janela",
                RequisicaoPrioridade.URGENTE,
                200L,
                List.of(), null);

        RequisicaoManutencao resultado = service.criarManutencao(request, 100L);

        assertNotNull(resultado);
        assertEquals(RequisicaoTipo.MANUTENCAO, resultado.getTipo());
        assertSame(criadoPor, resultado.getCriadoPor());
    }

    private Funcionario funcionarioComId(Long id) {
        Funcionario funcionario = new Funcionario();
        funcionario.setId(id);
        return funcionario;
    }
}