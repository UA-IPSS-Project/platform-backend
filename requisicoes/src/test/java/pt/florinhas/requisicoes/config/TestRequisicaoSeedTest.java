package pt.florinhas.requisicoes.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pt.florinhas.common_data.repository.FuncionarioRepository;
import pt.florinhas.common_data.security.CryptoUtils;
import pt.florinhas.requisicoes.repository.ManutencaoItemRepository;
import pt.florinhas.requisicoes.repository.MaterialRepository;
import pt.florinhas.requisicoes.repository.RequisicaoManutencaoItemRepository;
import pt.florinhas.requisicoes.repository.RequisicaoManutencaoRepository;
import pt.florinhas.requisicoes.repository.RequisicaoMaterialRepository;
import pt.florinhas.requisicoes.repository.RequisicaoRepository;
import pt.florinhas.requisicoes.repository.RequisicaoTransporteRepository;
import pt.florinhas.requisicoes.repository.TransporteRepository;

class TestRequisicaoSeedTest {

    private TestRequisicaoSeed seed;

    private RequisicaoRepository requisicaoRepository;

    @BeforeEach
    void setUp() {

        requisicaoRepository =
                mock(RequisicaoRepository.class);

        seed =
                new TestRequisicaoSeed(
                        requisicaoRepository,
                        mock(RequisicaoMaterialRepository.class),
                        mock(RequisicaoTransporteRepository.class),
                        mock(RequisicaoManutencaoRepository.class),
                        mock(FuncionarioRepository.class),
                        mock(MaterialRepository.class),
                        mock(TransporteRepository.class),
                        mock(ManutencaoItemRepository.class),
                        mock(RequisicaoManutencaoItemRepository.class),
                        mock(CryptoUtils.class));

        when(requisicaoRepository.count())
                .thenReturn(1L);
    }

    @Test
    void run_DeveIgnorarQuandoJaExistemDados() {

        assertDoesNotThrow(() ->
                seed.run());
    }
}