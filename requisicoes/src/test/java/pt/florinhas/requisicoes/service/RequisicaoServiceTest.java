package pt.florinhas.requisicoes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

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
        when(requisicaoRepository.findByEstado(RequisicaoEstado.EM_ANALISE)).thenReturn(List.of());

        requisicaoService.listarPorEstado(RequisicaoEstado.EM_ANALISE);

        verify(requisicaoRepository).findByEstado(RequisicaoEstado.EM_ANALISE);
    }

    @Test
    void procurar_deveDelegarFindWithFilters() {
        Requisicao requisicao = new RequisicaoManutencao();
        requisicao.setEstado(RequisicaoEstado.ENVIADA);
        requisicao.setTipo(RequisicaoTipo.MATERIAL);
        requisicao.setPrioridade(RequisicaoPrioridade.ALTA);
        Funcionario criadoPor = new Funcionario();
        criadoPor.setNome("Maria Silva");
        requisicao.setCriadoPor(criadoPor);
        Funcionario geridoPor = new Funcionario();
        geridoPor.setNome("João Costa");
        requisicao.setGeridoPor(geridoPor);

        when(requisicaoRepository.findAll(Sort.by(Sort.Direction.DESC, "criadoEm")))
                .thenReturn(List.of(requisicao));

        List<Requisicao> resultado = requisicaoService.procurar(
                RequisicaoEstado.ENVIADA,
                RequisicaoTipo.MATERIAL,
                RequisicaoPrioridade.ALTA,
                "Maria",
                "João");

        assertEquals(1, resultado.size());
        assertSame(requisicao, resultado.getFirst());

        verify(requisicaoRepository).findAll(Sort.by(Sort.Direction.DESC, "criadoEm"));
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
                LocalDateTime.of(2026, 3, 1, 10, 0),
                1L,
                2L,
                List.of(
                        new CriarRequisicaoMaterialRequest.ItemMaterialRequest(3L, 5),
                        new CriarRequisicaoMaterialRequest.ItemMaterialRequest(4L, 2)));

        RequisicaoMaterial resultado = requisicaoService.criarMaterial(request);

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
                null,
                1L,
                null,
                List.of(
                        new CriarRequisicaoMaterialRequest.ItemMaterialRequest(3L, 5),
                        new CriarRequisicaoMaterialRequest.ItemMaterialRequest(3L, 8)));

        RequisicaoMaterial resultado = requisicaoService.criarMaterial(request);

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
                null,
                1L,
                null,
                List.of(new CriarRequisicaoMaterialRequest.ItemMaterialRequest(30L, 1)));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> requisicaoService.criarMaterial(request));

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
                LocalDateTime.of(2026, 4, 10, 8, 30),
                10L,
                20L,
                30L);

        RequisicaoTransporte resultado = requisicaoService.criarTransporte(request);

        assertEquals(RequisicaoTipo.TRANSPORTE, resultado.getTipo());
        assertSame(criadoPor, resultado.getCriadoPor());
                assertNull(resultado.getGeridoPor());
        assertSame(transporte, resultado.getTransporte());
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
                10L,
                null,
                90L);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> requisicaoService.criarTransporte(request));

        assertEquals("Transporte não encontrado: 90", exception.getMessage());
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
                LocalDateTime.of(2026, 5, 5, 9, 0),
                100L,
                200L,
                "Janela partida na sala 2");

        RequisicaoManutencao resultado = requisicaoService.criarManutencao(request);

        assertNotNull(resultado);
        assertEquals(RequisicaoTipo.MANUTENCAO, resultado.getTipo());
        assertSame(criadoPor, resultado.getCriadoPor());
                assertNull(resultado.getGeridoPor());
        assertEquals("Janela partida na sala 2", resultado.getAssunto());
    }

    @Test
    void criarManutencao_quandoCriadoPorNaoExiste_deveLancarExcecao() {
        when(funcionarioRepository.findById(404L)).thenReturn(Optional.empty());

        CriarRequisicaoManutencaoRequest request = new CriarRequisicaoManutencaoRequest(
                "Teste",
                RequisicaoPrioridade.MEDIA,
                null,
                404L,
                null,
                "Assunto");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> requisicaoService.criarManutencao(request));

        assertEquals("Funcionário não encontrado: 404", exception.getMessage());
    }

    private Funcionario funcionarioComId(Long id) {
        Funcionario funcionario = new Funcionario();
        funcionario.setId(id);
        return funcionario;
    }
}