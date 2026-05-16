package pt.florinhas.requisicoes.config;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.security.SecureRandom;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
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
import pt.florinhas.requisicoes.repository.ManutencaoItemRepository;
import pt.florinhas.requisicoes.repository.MaterialRepository;
import pt.florinhas.requisicoes.repository.RequisicaoManutencaoItemRepository;
import pt.florinhas.requisicoes.repository.RequisicaoManutencaoRepository;
import pt.florinhas.requisicoes.repository.RequisicaoMaterialRepository;
import pt.florinhas.requisicoes.repository.RequisicaoRepository;
import pt.florinhas.requisicoes.repository.RequisicaoTransporteRepository;
import pt.florinhas.requisicoes.repository.TransporteRepository;

import pt.florinhas.common_data.domain.Funcionario;
import pt.florinhas.common_data.repository.FuncionarioRepository;
import pt.florinhas.common_data.security.CryptoUtils;

import org.springframework.context.annotation.Profile;

@Slf4j
@Component
@Profile({"dev", "test"})
@Order(4)
public class TestRequisicaoSeed implements CommandLineRunner {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final RequisicaoRepository requisicaoRepository;
    private final RequisicaoMaterialRepository requisicaoMaterialRepository;
    private final RequisicaoTransporteRepository requisicaoTransporteRepository;
    private final RequisicaoManutencaoRepository requisicaoManutencaoRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final MaterialRepository materialRepository;
    private final TransporteRepository transporteRepository;
    private final ManutencaoItemRepository manutencaoItemRepository;
    private final RequisicaoManutencaoItemRepository requisicaoManutencaoItemRepository;
    private final CryptoUtils cryptoUtils;

    public TestRequisicaoSeed(
            RequisicaoRepository requisicaoRepository,
            RequisicaoMaterialRepository requisicaoMaterialRepository,
            RequisicaoTransporteRepository requisicaoTransporteRepository,
            RequisicaoManutencaoRepository requisicaoManutencaoRepository,
            FuncionarioRepository funcionarioRepository,
            MaterialRepository materialRepository,
            TransporteRepository transporteRepository,
            ManutencaoItemRepository manutencaoItemRepository,
            RequisicaoManutencaoItemRepository requisicaoManutencaoItemRepository,
            CryptoUtils cryptoUtils) {
        this.requisicaoRepository = requisicaoRepository;
        this.requisicaoMaterialRepository = requisicaoMaterialRepository;
        this.requisicaoTransporteRepository = requisicaoTransporteRepository;
        this.requisicaoManutencaoRepository = requisicaoManutencaoRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.materialRepository = materialRepository;
        this.transporteRepository = transporteRepository;
        this.manutencaoItemRepository = manutencaoItemRepository;
        this.requisicaoManutencaoItemRepository = requisicaoManutencaoItemRepository;
        this.cryptoUtils = cryptoUtils;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("--- STARTING TEST REQUISICAO SEED ---");

        if (requisicaoRepository.count() > 0) {
            log.info("--- Test requisitions already exist, skipping ---");
            return;
        }

        // 1. Ensure employee for seeding exists
        Funcionario ana = funcionarioRepository.findByNifHash(cryptoUtils.generateBlindIndex("123456789"))
                .orElseGet(() -> {
                    Funcionario f = new Funcionario();
                    f.setNome("Ana Silva");
                    f.setNif("123456789");
                    f.setEmail("ana.silva@florinhas.pt");
                    f.setActivo(true);
                    return funcionarioRepository.save(f);
                });

        List<Material> materiais = materialRepository.findAll();
        List<Transporte> transportes = transporteRepository.findAll();
        List<ManutencaoItem> items = manutencaoItemRepository.findAll();

        if (materiais.isEmpty() || transportes.isEmpty() || items.isEmpty()) {
            log.warn("--- Basic catalogs are not seeded yet. Skipping test requisitions. ---");
            return;
        }


        // 2. Create Material Requisitions
        for (int i = 1; i <= 5; i++) {
            RequisicaoMaterial req = new RequisicaoMaterial();
            req.setTipo(RequisicaoTipo.MATERIAL);
            req.setDescricao("Requisição de material de teste " + i);
            req.setPrioridade(RequisicaoPrioridade.MEDIA);
            req.setEstado(RequisicaoEstado.values()[RANDOM.nextInt(RequisicaoEstado.values().length)]);
            req.setCriadoPor(ana);
            req.setCriadoEm(LocalDateTime.now().minusDays(RANDOM.nextInt(30)));
            req.setItens(new ArrayList<>());

            for (int j = 0; j < 2; j++) {
                RequisicaoMaterialItem item = new RequisicaoMaterialItem();
                item.setMaterial(materiais.get(RANDOM.nextInt(materiais.size())));
                item.setQuantidade(RANDOM.nextInt(10) + 1);
                item.setRequisicao(req);
                req.getItens().add(item);
            }
            requisicaoMaterialRepository.save(req);
        }

        // 3. Create Transport Requisitions
        for (int i = 1; i <= 5; i++) {
            RequisicaoTransporte req = new RequisicaoTransporte();
            req.setTipo(RequisicaoTipo.TRANSPORTE);
            req.setDescricao("Requisição de transporte de teste " + i);
            req.setPrioridade(RequisicaoPrioridade.MEDIA);
            req.setEstado(RequisicaoEstado.values()[RANDOM.nextInt(RequisicaoEstado.values().length)]);
            req.setCriadoPor(ana);
            req.setCriadoEm(LocalDateTime.now().minusDays(RANDOM.nextInt(30)));
            req.setDestino("Destino de teste " + i);
            req.setDataHoraSaida(req.getCriadoEm().plusDays(1));
            req.setDataHoraRegresso(req.getDataHoraSaida().plusHours(2));
            req.setNumeroPassageiros(RANDOM.nextInt(20) + 1);
            req.setTransporte(transportes.get(RANDOM.nextInt(transportes.size())));
            req.setTransportes(new ArrayList<>());

            for (int j = 0; j < 1; j++) {
                RequisicaoTransporteItem item = new RequisicaoTransporteItem();
                item.setTransporte(transportes.get(RANDOM.nextInt(transportes.size())));
                item.setRequisicao(req);
                req.getTransportes().add(item);
            }
            requisicaoTransporteRepository.save(req);
        }

        // 4. Create Maintenance Requisitions
        for (int i = 1; i <= 5; i++) {
            RequisicaoManutencao req = new RequisicaoManutencao();
            req.setTipo(RequisicaoTipo.MANUTENCAO);
            req.setDescricao("Requisição de manutenção de teste " + i);
            req.setPrioridade(RequisicaoPrioridade.ALTA);
            req.setEstado(RequisicaoEstado.values()[RANDOM.nextInt(RequisicaoEstado.values().length)]);
            req.setCriadoPor(ana);
            req.setCriadoEm(LocalDateTime.now().minusDays(RANDOM.nextInt(30)));

            RequisicaoManutencao saved = requisicaoManutencaoRepository.save(req);

            for (int j = 0; j < 3; j++) {
                RequisicaoManutencaoItem item = new RequisicaoManutencaoItem();
                item.setRequisicao(saved);
                item.setManutencaoItem(items.get(RANDOM.nextInt(items.size())));
                item.setObservacoes("Observação de teste " + j);
                requisicaoManutencaoItemRepository.save(item);
            }
        }
        
        log.info("--- TEST REQUISICAO SEED COMPLETED ---");
    }
}
