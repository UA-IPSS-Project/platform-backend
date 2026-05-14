package pt.florinhas.requisicoes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pt.florinhas.common_data.domain.Funcionario;
import pt.florinhas.common_data.repository.FuncionarioRepository;
import pt.florinhas.requisicoes.domain.Requisicao;
import pt.florinhas.requisicoes.domain.RequisicaoEstado;
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

    private RequisicaoService service;

    @BeforeEach
    void setUp() {

        requisicaoRepository = mock(RequisicaoRepository.class);

        service = new RequisicaoService(
                requisicaoRepository,
                mock(RequisicaoMaterialRepository.class),
                mock(RequisicaoTransporteRepository.class),
                mock(RequisicaoManutencaoRepository.class),
                mock(FuncionarioRepository.class),
                mock(MaterialRepository.class),
                mock(TransporteRepository.class),
                mock(TipoManutencaoRepository.class),
                mock(ManutencaoItemRepository.class),
                mock(RequisicaoManutencaoItemRepository.class),
                mock(NotificacaoService.class));
    }

    @Test
    void listarTodas_DeveRetornarLista() {

        when(requisicaoRepository.findAll())
                .thenReturn(List.of(mock(Requisicao.class)));

        assertEquals(1, service.listarTodas().size());
    }

    @Test
    void obterPorId_DeveRetornarRequisicao() {

        Requisicao req = mock(Requisicao.class);

        when(requisicaoRepository.findById(1L))
                .thenReturn(Optional.of(req));

        assertNotNull(service.obterPorId(1L));
    }

    @Test
    void obterPorId_DeveLancarException() {

        when(requisicaoRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.obterPorId(1L));
    }

    @Test
    void listarPorEstado_DeveRetornarLista() {

        when(requisicaoRepository.findByEstado(
                RequisicaoEstado.ABERTO))
                .thenReturn(List.of(mock(Requisicao.class)));

        assertEquals(
                1,
                service.listarPorEstado(
                        RequisicaoEstado.ABERTO).size());
    }
}