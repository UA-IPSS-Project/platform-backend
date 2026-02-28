package pt.florinhas.requisicoes.service;

import java.util.List;

import org.springframework.stereotype.Service;

import pt.florinhas.requisicoes.domain.Funcionario;
import pt.florinhas.requisicoes.domain.Material;
import pt.florinhas.requisicoes.domain.Requisicao;
import pt.florinhas.requisicoes.domain.RequisicaoEstado;
import pt.florinhas.requisicoes.domain.RequisicaoManutencao;
import pt.florinhas.requisicoes.domain.RequisicaoPrioridade;
import pt.florinhas.requisicoes.domain.RequisicaoMaterial;
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

@Service
public class RequisicaoService {

    private final RequisicaoRepository requisicaoRepository;
    private final RequisicaoMaterialRepository requisicaoMaterialRepository;
    private final RequisicaoTransporteRepository requisicaoTransporteRepository;
    private final RequisicaoManutencaoRepository requisicaoManutencaoRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final MaterialRepository materialRepository;
    private final TransporteRepository transporteRepository;

    public RequisicaoService(
            RequisicaoRepository requisicaoRepository,
            RequisicaoMaterialRepository requisicaoMaterialRepository,
            RequisicaoTransporteRepository requisicaoTransporteRepository,
            RequisicaoManutencaoRepository requisicaoManutencaoRepository,
            FuncionarioRepository funcionarioRepository,
            MaterialRepository materialRepository,
            TransporteRepository transporteRepository) {
        this.requisicaoRepository = requisicaoRepository;
        this.requisicaoMaterialRepository = requisicaoMaterialRepository;
        this.requisicaoTransporteRepository = requisicaoTransporteRepository;
        this.requisicaoManutencaoRepository = requisicaoManutencaoRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.materialRepository = materialRepository;
        this.transporteRepository = transporteRepository;
    }

    public List<Requisicao> listarTodas() {
        return requisicaoRepository.findAll();
    }

    public Requisicao obterPorId(Long id) {
        return requisicaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Requisição não encontrada: " + id));
    }

    public List<Requisicao> listarPorEstado(RequisicaoEstado estado) {
        return requisicaoRepository.findByEstado(estado);
    }

    public List<Requisicao> procurar(
            RequisicaoEstado estado,
            RequisicaoTipo tipo,
            RequisicaoPrioridade prioridade,
            Long criadoPorId,
            Long geridoPorId) {
        return requisicaoRepository.findWithFilters(estado, tipo, prioridade, criadoPorId, geridoPorId);
    }

    public RequisicaoMaterial criarMaterial(CriarRequisicaoMaterialRequest request) {
        Funcionario criadoPor = obterFuncionario(request.criadoPorId());
        Material material = materialRepository.findById(request.materialId())
                .orElseThrow(() -> new ResourceNotFoundException("Material não encontrado: " + request.materialId()));

        RequisicaoMaterial requisicao = new RequisicaoMaterial();
        requisicao.setDescricao(request.descricao());
        requisicao.setPrioridade(request.prioridade());
        requisicao.setTempoLimite(request.tempoLimite());
        requisicao.setTipo(RequisicaoTipo.MATERIAL);
        requisicao.setCriadoPor(criadoPor);
        requisicao.setGeridoPor(request.geridoPorId() != null ? obterFuncionario(request.geridoPorId()) : null);
        requisicao.setMaterial(material);
        requisicao.setQuantidade(request.quantidade());

        return requisicaoMaterialRepository.save(requisicao);
    }

    public RequisicaoTransporte criarTransporte(CriarRequisicaoTransporteRequest request) {
        Funcionario criadoPor = obterFuncionario(request.criadoPorId());
        Transporte transporte = transporteRepository.findById(request.transporteId())
                .orElseThrow(() -> new ResourceNotFoundException("Transporte não encontrado: " + request.transporteId()));

        RequisicaoTransporte requisicao = new RequisicaoTransporte();
        requisicao.setDescricao(request.descricao());
        requisicao.setPrioridade(request.prioridade());
        requisicao.setTempoLimite(request.tempoLimite());
        requisicao.setTipo(RequisicaoTipo.TRANSPORTE);
        requisicao.setCriadoPor(criadoPor);
        requisicao.setGeridoPor(request.geridoPorId() != null ? obterFuncionario(request.geridoPorId()) : null);
        requisicao.setTransporte(transporte);

        return requisicaoTransporteRepository.save(requisicao);
    }

    public RequisicaoManutencao criarManutencao(CriarRequisicaoManutencaoRequest request) {
        Funcionario criadoPor = obterFuncionario(request.criadoPorId());

        RequisicaoManutencao requisicao = new RequisicaoManutencao();
        requisicao.setDescricao(request.descricao());
        requisicao.setPrioridade(request.prioridade());
        requisicao.setTempoLimite(request.tempoLimite());
        requisicao.setTipo(RequisicaoTipo.MANUTENCAO);
        requisicao.setCriadoPor(criadoPor);
        requisicao.setGeridoPor(request.geridoPorId() != null ? obterFuncionario(request.geridoPorId()) : null);
        requisicao.setAssunto(request.assunto());

        return requisicaoManutencaoRepository.save(requisicao);
    }

    private Funcionario obterFuncionario(Long id) {
        return funcionarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado: " + id));
    }
}
