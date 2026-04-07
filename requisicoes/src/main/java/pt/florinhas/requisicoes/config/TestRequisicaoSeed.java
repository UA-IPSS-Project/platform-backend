package pt.florinhas.requisicoes.config;

import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import pt.florinhas.requisicoes.domain.Funcionario;
import pt.florinhas.requisicoes.domain.ManutencaoItem;
import pt.florinhas.requisicoes.domain.Material;
import pt.florinhas.requisicoes.domain.RequisicaoEstado;
import pt.florinhas.requisicoes.domain.RequisicaoManutencao;
import pt.florinhas.requisicoes.domain.RequisicaoManutencaoItem;
import pt.florinhas.requisicoes.domain.RequisicaoMaterial;
import pt.florinhas.requisicoes.domain.RequisicaoMaterialItem;
import pt.florinhas.requisicoes.domain.RequisicaoPrioridade;
import pt.florinhas.requisicoes.domain.RequisicaoTipo;
import pt.florinhas.requisicoes.domain.RequisicaoTransporte;
import pt.florinhas.requisicoes.domain.RequisicaoTransporteItem;
import pt.florinhas.requisicoes.domain.Transporte;
import pt.florinhas.requisicoes.repository.FuncionarioRepository;
import pt.florinhas.requisicoes.repository.ManutencaoItemRepository;
import pt.florinhas.requisicoes.repository.MaterialRepository;
import pt.florinhas.requisicoes.repository.RequisicaoManutencaoRepository;
import pt.florinhas.requisicoes.repository.RequisicaoMaterialRepository;
import pt.florinhas.requisicoes.repository.RequisicaoTransporteRepository;
import pt.florinhas.requisicoes.repository.TransporteRepository;

@Slf4j
@Component
@Order(4)
public class TestRequisicaoSeed implements CommandLineRunner {

    private final RequisicaoMaterialRepository requisicaoMaterialRepository;
    private final RequisicaoTransporteRepository requisicaoTransporteRepository;
    private final RequisicaoManutencaoRepository requisicaoManutencaoRepository;
    private final MaterialRepository materialRepository;
    private final TransporteRepository transporteRepository;
    private final ManutencaoItemRepository manutencaoItemRepository;
    private final FuncionarioRepository funcionarioRepository;

    public TestRequisicaoSeed(
            RequisicaoMaterialRepository requisicaoMaterialRepository,
            RequisicaoTransporteRepository requisicaoTransporteRepository,
            RequisicaoManutencaoRepository requisicaoManutencaoRepository,
            MaterialRepository materialRepository,
            TransporteRepository transporteRepository,
            ManutencaoItemRepository manutencaoItemRepository,
            FuncionarioRepository funcionarioRepository) {
        this.requisicaoMaterialRepository = requisicaoMaterialRepository;
        this.requisicaoTransporteRepository = requisicaoTransporteRepository;
        this.requisicaoManutencaoRepository = requisicaoManutencaoRepository;
        this.materialRepository = materialRepository;
        this.transporteRepository = transporteRepository;
        this.manutencaoItemRepository = manutencaoItemRepository;
        this.funcionarioRepository = funcionarioRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("--- SEEDING TEST REQUISITIONS ---");
        
        Funcionario creator = funcionarioRepository.findByNif("999999998").orElseGet(() -> {
            Funcionario f = new Funcionario();
            f.setNif("999999998");
            f.setNome("Funcionário Secretaria");
            f.setEmail("secretaria@florinhasdovouga.pt");
            f.setTelefone("999999998");
            f.setTipo(pt.florinhas.requisicoes.domain.FuncionarioTipo.SECRETARIA);
            f.setActivo(true);
            return funcionarioRepository.save(f);
        });

        Material material = materialRepository.findAll().stream().findFirst().orElse(null);
        Transporte transporte = transporteRepository.findAll().stream().findFirst().orElse(null);
        ManutencaoItem manutencaoItem = manutencaoItemRepository.findAll().stream().findFirst().orElse(null);

        if (material == null || transporte == null || manutencaoItem == null) {
            log.error("--- MISSING BASE DATA (Material/Transporte/ManutencaoItem). SEEDING CANCELLED. ---");
            return;
        }

        // 1. Requisicao Material - 31 dias
        String descMat = "Teste Relatório Material - 31 dias";
        if (!requisicaoMaterialRepository.existsByDescricao(descMat)) {
            RequisicaoMaterial req = new RequisicaoMaterial();
            req.setDescricao(descMat);
            req.setEstado(RequisicaoEstado.ABERTO);
            req.setPrioridade(RequisicaoPrioridade.URGENTE);
            req.setTipo(RequisicaoTipo.MATERIAL);
            req.setCriadoEm(LocalDateTime.now().minusDays(31));
            req.setUltimaAlteracaoEstadoEm(req.getCriadoEm());
            req.setCriadoPor(creator);

            RequisicaoMaterialItem item = new RequisicaoMaterialItem();
            item.setMaterial(material);
            item.setQuantidade(10);
            req.getItens().add(item);
            requisicaoMaterialRepository.save(req);
        }

        // 2. Requisicao Transporte - 91 dias
        String descTransp = "Teste Relatório Transporte - 91 dias";
        if (!requisicaoTransporteRepository.existsByDescricao(descTransp)) {
            RequisicaoTransporte req = new RequisicaoTransporte();
            req.setDescricao(descTransp);
            req.setEstado(RequisicaoEstado.ABERTO);
            req.setPrioridade(RequisicaoPrioridade.ALTA);
            req.setTipo(RequisicaoTipo.TRANSPORTE);
            req.setCriadoEm(LocalDateTime.now().minusDays(91));
            req.setUltimaAlteracaoEstadoEm(req.getCriadoEm());
            req.setCriadoPor(creator);
            req.setDestino("Aveiro, UA");
            req.setDataHoraSaida(LocalDateTime.now().plusDays(1));
            req.setDataHoraRegresso(LocalDateTime.now().plusDays(1).plusHours(2));
            req.setNumeroPassageiros(5);
            req.setTransporte(transporte);

            RequisicaoTransporteItem item = new RequisicaoTransporteItem();
            item.setTransporte(transporte);
            item.setRequisicao(req);
            req.getTransportes().add(item);
            requisicaoTransporteRepository.save(req);
        }

        // 3. Requisicao Manutencao - 181 dias
        String descManut = "Teste Relatório Manutenção - 181 dias";
        if (!requisicaoManutencaoRepository.existsByDescricao(descManut)) {
            RequisicaoManutencao req = new RequisicaoManutencao();
            req.setDescricao(descManut);
            req.setEstado(RequisicaoEstado.ABERTO);
            req.setPrioridade(RequisicaoPrioridade.MEDIA);
            req.setTipo(RequisicaoTipo.MANUTENCAO);
            req.setCriadoEm(LocalDateTime.now().minusDays(181));
            req.setUltimaAlteracaoEstadoEm(req.getCriadoEm());
            req.setCriadoPor(creator);

            RequisicaoManutencaoItem item = new RequisicaoManutencaoItem();
            item.setManutencaoItem(manutencaoItem);
            item.setRequisicao(req);
            item.setObservacoes("Verificar infiltração");
            req.getItens().add(item);
            requisicaoManutencaoRepository.save(req);
        }

        log.info("--- TEST REQUISITIONS SEEDED SUCCESSFULLY ---");
    }
}
