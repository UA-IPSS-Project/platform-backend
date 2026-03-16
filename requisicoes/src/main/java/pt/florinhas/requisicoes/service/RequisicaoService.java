package pt.florinhas.requisicoes.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import pt.florinhas.requisicoes.domain.Funcionario;
import pt.florinhas.requisicoes.domain.Material;
import pt.florinhas.requisicoes.domain.Requisicao;
import pt.florinhas.requisicoes.domain.RequisicaoEstado;
import pt.florinhas.requisicoes.domain.RequisicaoManutencao;
import pt.florinhas.requisicoes.domain.RequisicaoMaterial;
import pt.florinhas.requisicoes.domain.RequisicaoMaterialItem;
import pt.florinhas.requisicoes.domain.RequisicaoPrioridade;
import pt.florinhas.requisicoes.domain.RequisicaoTipo;
import pt.florinhas.requisicoes.domain.RequisicaoTransporte;
import pt.florinhas.requisicoes.domain.RequisicaoTransporteItem;
import pt.florinhas.requisicoes.domain.Transporte;
import pt.florinhas.requisicoes.dto.CriarMaterialRequest;
import pt.florinhas.requisicoes.dto.CriarRequisicaoManutencaoRequest;
import pt.florinhas.requisicoes.dto.CriarRequisicaoMaterialRequest;
import pt.florinhas.requisicoes.dto.CriarRequisicaoTransporteRequest;
import pt.florinhas.requisicoes.dto.CriarTransporteRequest;
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
            String criadoPorNome,
            String geridoPorNome) {
        String criadoPorNomeNormalizado = normalizarFiltroTexto(criadoPorNome);
        String geridoPorNomeNormalizado = normalizarFiltroTexto(geridoPorNome);

        return requisicaoRepository.findAll(Sort.by(Sort.Direction.DESC, "criadoEm")).stream()
            .filter(requisicao -> estado == null || requisicao.getEstado() == estado)
            .filter(requisicao -> tipo == null || requisicao.getTipo() == tipo)
            .filter(requisicao -> prioridade == null || requisicao.getPrioridade() == prioridade)
            .filter(requisicao -> criadoPorNomeNormalizado == null
                || (requisicao.getCriadoPor() != null
                    && requisicao.getCriadoPor().getNome() != null
                    && requisicao.getCriadoPor().getNome().toLowerCase(Locale.ROOT)
                        .contains(criadoPorNomeNormalizado)))
            .filter(requisicao -> geridoPorNomeNormalizado == null
                || (requisicao.getGeridoPor() != null
                    && requisicao.getGeridoPor().getNome() != null
                    && requisicao.getGeridoPor().getNome().toLowerCase(Locale.ROOT)
                        .contains(geridoPorNomeNormalizado)))
            .toList();
    }

    private String normalizarFiltroTexto(String filtro) {
        if (filtro == null || filtro.isBlank()) {
            return null;
        }
        return filtro.toLowerCase(Locale.ROOT).trim();
    }

    public RequisicaoMaterial criarMaterial(CriarRequisicaoMaterialRequest request) {
        Funcionario criadoPor = obterFuncionario(request.criadoPorId());

        Map<Long, Integer> itensNormalizados = new LinkedHashMap<>();
        for (CriarRequisicaoMaterialRequest.ItemMaterialRequest item : request.itens()) {
            itensNormalizados.put(item.materialId(), item.quantidade());
        }

        RequisicaoMaterial requisicao = new RequisicaoMaterial();
        requisicao.setDescricao(request.descricao());
        requisicao.setPrioridade(request.prioridade());
        requisicao.setTempoLimite(request.tempoLimite());
        requisicao.setTipo(RequisicaoTipo.MATERIAL);
        requisicao.setCriadoPor(criadoPor);
        requisicao.setGeridoPor(null);

        for (Map.Entry<Long, Integer> item : itensNormalizados.entrySet()) {
            Material material = materialRepository.findById(item.getKey())
                    .orElseThrow(() -> new ResourceNotFoundException("Material não encontrado: " + item.getKey()));

            RequisicaoMaterialItem requisicaoMaterialItem = new RequisicaoMaterialItem();
            requisicaoMaterialItem.setMaterial(material);
            requisicaoMaterialItem.setQuantidade(item.getValue());
            requisicao.getItens().add(requisicaoMaterialItem);
        }

        return requisicaoMaterialRepository.save(requisicao);
    }

    public RequisicaoTransporte criarTransporte(CriarRequisicaoTransporteRequest request) {
        Funcionario criadoPor = obterFuncionario(request.criadoPorId());
        validarPeriodoTransporte(request.dataHoraSaida(), request.dataHoraRegresso());

        Set<Long> transporteIdsNormalizados = new LinkedHashSet<>(request.transporteIds());
        List<Transporte> transportesSelecionados = transporteIdsNormalizados.stream()
            .map(transporteId -> transporteRepository.findById(transporteId)
                .orElseThrow(() -> new ResourceNotFoundException("Transporte não encontrado: " + transporteId)))
            .toList();

        validarTransportesDisponiveis(
                transportesSelecionados,
                request.dataHoraSaida(),
                request.dataHoraRegresso(),
                null);

        int capacidadeTotal = transportesSelecionados.stream()
            .map(Transporte::getLotacao)
            .filter(lotacao -> lotacao != null && lotacao > 0)
            .mapToInt(lotacao -> Math.max(0, lotacao - 1))
            .sum();

        if (capacidadeTotal < request.numeroPassageiros()) {
            throw new IllegalArgumentException(
                "A lotação útil das viaturas selecionadas é insuficiente para o número de passageiros indicado.");
        }

        RequisicaoTransporte requisicao = new RequisicaoTransporte();
        requisicao.setDescricao(request.descricao());
        requisicao.setPrioridade(request.prioridade());
        requisicao.setTempoLimite(request.tempoLimite());
        requisicao.setTipo(RequisicaoTipo.TRANSPORTE);
        requisicao.setCriadoPor(criadoPor);
        requisicao.setGeridoPor(null);
        requisicao.setDestino(normalizarTextoObrigatorio(request.destino(), false));
        requisicao.setDataHoraSaida(request.dataHoraSaida());
        requisicao.setDataHoraRegresso(request.dataHoraRegresso());
        requisicao.setNumeroPassageiros(request.numeroPassageiros());
        requisicao.setCondutor(normalizarTextoOpcional(request.condutor(), false));
        requisicao.setTransporte(transportesSelecionados.getFirst());

        for (Transporte transporte : transportesSelecionados) {
            RequisicaoTransporteItem item = new RequisicaoTransporteItem();
            item.setTransporte(transporte);
            requisicao.getTransportes().add(item);
        }

        return requisicaoTransporteRepository.save(requisicao);
    }

    private void validarPeriodoTransporte(LocalDateTime dataHoraSaida, LocalDateTime dataHoraRegresso) {
        if (!dataHoraRegresso.isAfter(dataHoraSaida)) {
            throw new IllegalArgumentException("A data/hora de regresso deve ser posterior à data/hora de saída.");
        }
    }

    private List<Transporte> extrairTransportes(RequisicaoTransporte requisicao) {
        List<Transporte> transportesSelecionados = new ArrayList<>();

        if (requisicao.getTransporte() != null) {
            transportesSelecionados.add(requisicao.getTransporte());
        }

        requisicao.getTransportes().stream()
                .map(RequisicaoTransporteItem::getTransporte)
                .filter(Objects::nonNull)
                .forEach(transportesSelecionados::add);

        return transportesSelecionados.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Transporte::getId, transporte -> transporte, (left, right) -> left, LinkedHashMap::new))
                .values()
                .stream()
                .toList();
    }

    private void validarTransportesDisponiveis(
            List<Transporte> transportesSelecionados,
            LocalDateTime dataHoraSaida,
            LocalDateTime dataHoraRegresso,
            Long requisicaoIgnoradaId) {
        List<Long> transporteIds = transportesSelecionados.stream()
                .map(Transporte::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (transporteIds.isEmpty()) {
            return;
        }

        List<RequisicaoTransporte> conflitos = requisicaoTransporteRepository.findConflitosTransporte(
                RequisicaoEstado.ACEITE,
                transporteIds,
                dataHoraSaida,
                dataHoraRegresso,
                requisicaoIgnoradaId);

        if (conflitos == null || conflitos.isEmpty()) {
            return;
        }

        Set<Long> idsEmConflito = conflitos.stream()
                .flatMap(requisicao -> extrairTransportes(requisicao).stream())
                .map(Transporte::getId)
                .filter(Objects::nonNull)
                .filter(transporteIds::contains)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        String transportesIndisponiveis = transportesSelecionados.stream()
                .filter(transporte -> transporte.getId() != null && idsEmConflito.contains(transporte.getId()))
                .map(this::formatarIdentificacaoTransporte)
                .distinct()
                .collect(Collectors.joining(", "));

        throw new IllegalArgumentException(
                transportesIndisponiveis.isBlank()
                        ? "Uma ou mais viaturas selecionadas já estão indisponíveis no período indicado."
                        : "As seguintes viaturas já estão indisponíveis no período indicado: " + transportesIndisponiveis + ".");
    }

    private String formatarIdentificacaoTransporte(Transporte transporte) {
        if (transporte.getCodigo() != null && !transporte.getCodigo().isBlank()) {
            return transporte.getCodigo().trim();
        }
        if (transporte.getMatricula() != null && !transporte.getMatricula().isBlank()) {
            return transporte.getMatricula().trim();
        }
        return "#" + transporte.getId();
    }

    public RequisicaoManutencao criarManutencao(CriarRequisicaoManutencaoRequest request) {
        Funcionario criadoPor = obterFuncionario(request.criadoPorId());

        RequisicaoManutencao requisicao = new RequisicaoManutencao();
        requisicao.setDescricao(request.descricao());
        requisicao.setPrioridade(request.prioridade());
        requisicao.setTempoLimite(request.tempoLimite());
        requisicao.setTipo(RequisicaoTipo.MANUTENCAO);
        requisicao.setCriadoPor(criadoPor);
        requisicao.setGeridoPor(null);
        requisicao.setAssunto(request.assunto());

        return requisicaoManutencaoRepository.save(requisicao);
    }

    public Requisicao atualizarEstado(Long id, RequisicaoEstado novoEstado, Long alteradoPorId) {
        Requisicao requisicao = obterPorId(id);
        validarTransicaoEstado(requisicao.getEstado(), novoEstado);

        if (novoEstado == RequisicaoEstado.ACEITE && requisicao instanceof RequisicaoTransporte requisicaoTransporte) {
            validarTransportesDisponiveis(
                    extrairTransportes(requisicaoTransporte),
                    requisicaoTransporte.getDataHoraSaida(),
                    requisicaoTransporte.getDataHoraRegresso(),
                    requisicaoTransporte.getId());
        }

        requisicao.setEstado(novoEstado);
        requisicao.setGeridoPor(obterFuncionario(alteradoPorId));
        requisicao.setUltimaAlteracaoEstadoEm(LocalDateTime.now());

        return requisicaoRepository.save(requisicao);
    }

    public List<Material> listarMateriais() {
        return materialRepository.findAllByOrderByCategoriaAscNomeAscAtributoAscValorAtributoAsc();
    }

    public Material criarMaterialCatalogo(CriarMaterialRequest request) {
        Material material = new Material();
        material.setNome(request.nome().trim());
        material.setDescricao(request.descricao() != null ? request.descricao().trim() : null);
        material.setCategoria(request.categoria());
        material.setAtributo(request.atributo().trim());
        material.setValorAtributo(request.valorAtributo().trim());
        return materialRepository.save(material);
    }

    public List<Transporte> listarTransportes() {
        return transporteRepository.findAll(
                Sort.by(Sort.Order.asc("codigo"), Sort.Order.asc("tipo"), Sort.Order.asc("matricula")));
    }

    public Transporte criarTransporteCatalogo(CriarTransporteRequest request) {
        String codigoNormalizado = normalizarTextoOpcional(request.codigo(), true);
        String matriculaNormalizada = normalizarTextoObrigatorio(request.matricula(), true);

        if (codigoNormalizado != null) {
            transporteRepository.findByCodigo(codigoNormalizado)
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException("Já existe transporte com o código: " + request.codigo());
                    });
        }

        transporteRepository.findByMatricula(matriculaNormalizada)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Já existe transporte com a matrícula: " + request.matricula());
                });

        Transporte transporte = new Transporte();
        transporte.setCodigo(codigoNormalizado);
        transporte.setTipo(normalizarTextoObrigatorio(request.tipo(), false));
        transporte.setCategoria(request.categoria());
        transporte.setMatricula(matriculaNormalizada);
        transporte.setMarca(normalizarTextoOpcional(request.marca(), false));
        transporte.setModelo(normalizarTextoOpcional(request.modelo(), false));
        transporte.setLotacao(request.lotacao());
        transporte.setDataMatricula(request.dataMatricula());

        return transporteRepository.save(transporte);
    }

    private String normalizarTextoObrigatorio(String valor, boolean uppercase) {
        return uppercase ? valor.trim().toUpperCase(Locale.ROOT) : valor.trim();
    }

    private String normalizarTextoOpcional(String valor, boolean uppercase) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return uppercase ? valor.trim().toUpperCase(Locale.ROOT) : valor.trim();
    }

    private void validarTransicaoEstado(RequisicaoEstado estadoAtual, RequisicaoEstado novoEstado) {
        if (estadoAtual == novoEstado) {
            throw new IllegalArgumentException("O estado novo tem de ser diferente do estado atual.");
        }

        if (estadoAtual == RequisicaoEstado.ENVIADA && novoEstado == RequisicaoEstado.EM_ANALISE) {
            return;
        }

        if (estadoAtual == RequisicaoEstado.EM_ANALISE
            && (novoEstado == RequisicaoEstado.ACEITE || novoEstado == RequisicaoEstado.RECUSADA)) {
            return;
        }

        throw new IllegalArgumentException(
            "Transição de estado inválida. Fluxos permitidos: ENVIADA -> EM_ANALISE -> ACEITE ou ENVIADA -> EM_ANALISE -> RECUSADA.");
    }

    private Funcionario obterFuncionario(Long id) {
        return funcionarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado: " + id));
    }
}
