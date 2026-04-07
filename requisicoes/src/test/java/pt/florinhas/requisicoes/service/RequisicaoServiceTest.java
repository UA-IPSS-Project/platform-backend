package pt.florinhas.requisicoes.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import pt.florinhas.requisicoes.domain.Funcionario;
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
import pt.florinhas.requisicoes.repository.FuncionarioRepository;
import pt.florinhas.requisicoes.repository.MaterialRepository;
import pt.florinhas.requisicoes.repository.RequisicaoManutencaoRepository;
import pt.florinhas.requisicoes.repository.RequisicaoMaterialRepository;
import pt.florinhas.requisicoes.repository.RequisicaoRepository;
import pt.florinhas.requisicoes.repository.RequisicaoTransporteRepository;
import pt.florinhas.requisicoes.repository.TransporteRepository;

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

    @InjectMocks
    private RequisicaoService requisicaoService;

    @Test
    void listarTodas_deveRetornarListaDoRepositorio() {
        Requisicao requisicao = new RequisicaoManutencao();
        when(requisicaoRepository.findAll()).thenReturn(List.of(requisicao));

        List<Requisicao> resultado = requisicaoService.listarTodas();

        assertEquals(1, resultado.size());
        assertSame(requisicao, resultado.getFirst());
        verify(requisicaoRepository).findAll();
    }

    @Test
    void obterPorId_quandoExiste_deveRetornarRequisicao() {
        Requisicao requisicao = new RequisicaoManutencao();
        when(requisicaoRepository.findById(10L)).thenReturn(Optional.of(requisicao));

        Requisicao resultado = requisicaoService.obterPorId(10L);

        assertSame(requisicao, resultado);
        verify(requisicaoRepository).findById(10L);
    }

    @Test
    void obterPorId_quandoNaoExiste_deveLancarExcecao() {
        when(requisicaoRepository.findById(77L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> requisicaoService.obterPorId(77L));

        assertEquals("Requisição não encontrada: 77", exception.getMessage());
    }

    @Test
    void listarPorEstado_deveDelegarNoRepositorio() {
        when(requisicaoRepository.findByEstado(RequisicaoEstado.EM_PROGRESSO)).thenReturn(List.of());

        requisicaoService.listarPorEstado(RequisicaoEstado.EM_PROGRESSO);

        verify(requisicaoRepository).findByEstado(RequisicaoEstado.EM_PROGRESSO);
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
        Funcionario geridoPor = new Funcionario();
        geridoPor.setNome("João Costa");
        requisicao.setGeridoPor(geridoPor);

        when(requisicaoRepository.findWithFilters(
                any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(requisicao));

        List<Requisicao> resultado = requisicaoService.procurar(
                RequisicaoEstado.ABERTO,
                RequisicaoTipo.MATERIAL,
                RequisicaoPrioridade.ALTA,
                "Maria",
                null,
                null);

        assertEquals(1, resultado.size());
        assertSame(requisicao, resultado.getFirst());

        verify(requisicaoRepository).findWithFilters(
                any(), any(), any(), any(), any(), any());
    }

    @Test
    void criarMaterial_quandoDadosValidos_deveCriarComTipoEAssociacoes() {
        Funcionario criadoPor = funcionarioComId(1L);
        Material material = new Material();
        material.setId(3L);
        Material material2 = new Material();
        material2.setId(4L);

        when(funcionarioRepository.findById(1L)).thenReturn(Optional.of(criadoPor));
        when(materialRepository.findById(3L)).thenReturn(Optional.of(material));
        when(materialRepository.findById(4L)).thenReturn(Optional.of(material2));
        when(requisicaoMaterialRepository.save(any(RequisicaoMaterial.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CriarRequisicaoMaterialRequest request = new CriarRequisicaoMaterialRequest(
                "Pedido de material",
                RequisicaoPrioridade.MEDIA,
                1L,
                2L,
                List.of(
                        new CriarRequisicaoMaterialRequest.ItemMaterialRequest(3L, 5),
                        new CriarRequisicaoMaterialRequest.ItemMaterialRequest(4L, 2)));

        RequisicaoMaterial resultado = requisicaoService.criarMaterial(request, request.criadoPorId());

        assertEquals("Pedido de material", resultado.getDescricao());
        assertEquals(RequisicaoPrioridade.MEDIA, resultado.getPrioridade());
        assertEquals(RequisicaoTipo.MATERIAL, resultado.getTipo());
        assertSame(criadoPor, resultado.getCriadoPor());
        assertNull(resultado.getGeridoPor());
        assertEquals(2, resultado.getItens().size());
        assertSame(material, resultado.getItens().getFirst().getMaterial());
        assertEquals(5, resultado.getItens().getFirst().getQuantidade());
        assertSame(material2, resultado.getItens().get(1).getMaterial());
        assertEquals(2, resultado.getItens().get(1).getQuantidade());
    }

    @Test
    void criarMaterial_quandoRepetidoMaterialNoPedido_deveManterUltimaQuantidade() {
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
                1L,
                null,
                List.of(
                        new CriarRequisicaoMaterialRequest.ItemMaterialRequest(3L, 5),
                        new CriarRequisicaoMaterialRequest.ItemMaterialRequest(3L, 8)));

        RequisicaoMaterial resultado = requisicaoService.criarMaterial(request, request.criadoPorId());

        assertEquals(1, resultado.getItens().size());
        assertEquals(8, resultado.getItens().getFirst().getQuantidade());
    }

    @Test
    void criarMaterial_quandoMaterialNaoExiste_deveLancarExcecao() {
        Funcionario criadoPor = funcionarioComId(1L);
        when(funcionarioRepository.findById(1L)).thenReturn(Optional.of(criadoPor));
        when(materialRepository.findById(30L)).thenReturn(Optional.empty());

        CriarRequisicaoMaterialRequest request = new CriarRequisicaoMaterialRequest(
                "Pedido",
                RequisicaoPrioridade.BAIXA,
                1L,
                null,
                List.of(new CriarRequisicaoMaterialRequest.ItemMaterialRequest(30L, 1)));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> requisicaoService.criarMaterial(request, request.criadoPorId()));

        assertEquals("Material não encontrado: 30", exception.getMessage());
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
                10L,
                20L,
                "Centro de Dia",
                LocalDateTime.of(2026, 4, 11, 9, 0),
                LocalDateTime.of(2026, 4, 11, 13, 0),
                8,
                "Condutor 1",
                List.of(30L),
                null);

        RequisicaoTransporte resultado = requisicaoService.criarTransporte(request, request.criadoPorId());

        assertEquals(RequisicaoTipo.TRANSPORTE, resultado.getTipo());
        assertSame(criadoPor, resultado.getCriadoPor());
                assertNull(resultado.getGeridoPor());
        assertSame(transporte, resultado.getTransporte());
        assertEquals("Centro de Dia", resultado.getDestino());
        assertEquals(8, resultado.getNumeroPassageiros());
        assertEquals("Condutor 1", resultado.getCondutor());
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
                10L,
                null,
                "Hospital",
                LocalDateTime.of(2026, 4, 12, 9, 0),
                LocalDateTime.of(2026, 4, 12, 11, 0),
                3,
                "Condutor Teste",
                List.of(90L),
                null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> requisicaoService.criarTransporte(request, request.criadoPorId()));

        assertEquals("Transporte não encontrado: 90", exception.getMessage());
    }

    @Test
    void criarTransporte_quandoApenasTransporteId_deveAceitarCompatibilidade() {
        Funcionario criadoPor = funcionarioComId(10L);
        Transporte transporte = new Transporte();
        transporte.setId(30L);

        when(funcionarioRepository.findById(10L)).thenReturn(Optional.of(criadoPor));
        when(transporteRepository.findById(30L)).thenReturn(Optional.of(transporte));
        when(requisicaoTransporteRepository.save(any(RequisicaoTransporte.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CriarRequisicaoTransporteRequest request = new CriarRequisicaoTransporteRequest(
                "Pedido compatível",
                RequisicaoPrioridade.MEDIA,
                10L,
                null,
                "Centro",
                LocalDateTime.of(2026, 4, 13, 9, 0),
                LocalDateTime.of(2026, 4, 13, 10, 0),
                2,
                "Condutor Teste",
                null,
                30L);

        RequisicaoTransporte resultado = requisicaoService.criarTransporte(request, request.criadoPorId());

        assertSame(transporte, resultado.getTransporte());
        assertEquals(1, resultado.getTransportes().size());
    }

    @Test
    void criarTransporte_quandoTransporteIdsETransporteId_fornecidos_deveLancarErro() {
        CriarRequisicaoTransporteRequest request = new CriarRequisicaoTransporteRequest(
                "Pedido inválido",
                RequisicaoPrioridade.MEDIA,
                10L,
                null,
                "Centro",
                LocalDateTime.of(2026, 4, 13, 9, 0),
                LocalDateTime.of(2026, 4, 13, 10, 0),
                2,
                "Condutor Teste",
                List.of(30L),
                31L);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> requisicaoService.criarTransporte(request, request.criadoPorId()));

        assertEquals("Pedido inválido: forneça apenas 'transporteIds' ou 'transporteId', não ambos.",
                exception.getMessage());
    }

    @Test
    void criarTransporte_quandoNenhumTransporteFornecido_deveLancarErro() {
        CriarRequisicaoTransporteRequest request = new CriarRequisicaoTransporteRequest(
                "Pedido inválido",
                RequisicaoPrioridade.MEDIA,
                10L,
                null,
                "Centro",
                LocalDateTime.of(2026, 4, 13, 9, 0),
                LocalDateTime.of(2026, 4, 13, 10, 0),
                2,
                "Condutor Teste",
                null,
                null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> requisicaoService.criarTransporte(request, request.criadoPorId()));

        assertEquals("É obrigatório indicar pelo menos um transporte.", exception.getMessage());
    }

    @Test
    void criarTransporte_quandoRegressoAntesDaSaida_deveLancarErro() {
        CriarRequisicaoTransporteRequest request = new CriarRequisicaoTransporteRequest(
                "Pedido inválido",
                RequisicaoPrioridade.MEDIA,
                10L,
                null,
                "Centro",
                LocalDateTime.of(2026, 4, 13, 10, 0),
                LocalDateTime.of(2026, 4, 13, 9, 0),
                2,
                "Condutor Teste",
                List.of(30L),
                null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> requisicaoService.criarTransporte(request, request.criadoPorId()));

        assertEquals("A data/hora de regresso deve ser posterior à data/hora de saída.", exception.getMessage());
    }

    @Test
    void criarTransporte_quandoRegressoIgualSaida_deveLancarErro() {
        LocalDateTime dataHora = LocalDateTime.of(2026, 4, 13, 10, 0);

        CriarRequisicaoTransporteRequest request = new CriarRequisicaoTransporteRequest(
                "Pedido inválido",
                RequisicaoPrioridade.MEDIA,
                10L,
                null,
                "Centro",
                dataHora,
                dataHora,
                2,
                "Condutor Teste",
                List.of(30L),
                null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> requisicaoService.criarTransporte(request, request.criadoPorId()));

        assertEquals("A data/hora de regresso deve ser posterior à data/hora de saída.", exception.getMessage());
    }

    @Test
    void criarTransporte_quandoSaidaNoPassado_deveLancarErro() {
        LocalDateTime agora = LocalDateTime.now();

        CriarRequisicaoTransporteRequest request = new CriarRequisicaoTransporteRequest(
                "Pedido inválido",
                RequisicaoPrioridade.MEDIA,
                10L,
                null,
                "Centro",
                agora.minusDays(1),
                agora.plusDays(1),
                2,
                "Condutor Teste",
                List.of(30L),
                null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> requisicaoService.criarTransporte(request, request.criadoPorId()));

        assertEquals("A data/hora de saída não pode estar no passado.", exception.getMessage());
    }

    @Test
    void criarTransporte_quandoRegressoNoPassado_deveLancarErro() {
        LocalDateTime agora = LocalDateTime.now();

        CriarRequisicaoTransporteRequest request = new CriarRequisicaoTransporteRequest(
                "Pedido inválido",
                RequisicaoPrioridade.MEDIA,
                10L,
                null,
                "Centro",
                agora.plusDays(1),
                agora.minusDays(1),
                2,
                "Condutor Teste",
                List.of(30L),
                null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> requisicaoService.criarTransporte(request, request.criadoPorId()));

        assertEquals("A data/hora de regresso não pode estar no passado.", exception.getMessage());
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
                100L,
                200L,
                List.of());

        RequisicaoManutencao resultado = requisicaoService.criarManutencao(request, request.criadoPorId());

        assertNotNull(resultado);
        assertEquals(RequisicaoTipo.MANUTENCAO, resultado.getTipo());
        assertSame(criadoPor, resultado.getCriadoPor());
        assertNull(resultado.getGeridoPor());
    }

    @Test
    void criarManutencao_quandoCriadoPorNaoExiste_deveLancarExcecao() {
        when(funcionarioRepository.findById(404L)).thenReturn(Optional.empty());

        CriarRequisicaoManutencaoRequest request = new CriarRequisicaoManutencaoRequest(
                "Teste",
                RequisicaoPrioridade.MEDIA,
                404L,
                null,
                List.of());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> requisicaoService.criarManutencao(request, request.criadoPorId()));

        assertEquals("Funcionário não encontrado: 404", exception.getMessage());
    }

    private Funcionario funcionarioComId(Long id) {
        Funcionario funcionario = new Funcionario();
        funcionario.setId(id);
        return funcionario;
    }
}