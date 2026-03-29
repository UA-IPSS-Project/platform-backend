package pt.florinhas.requisicoes.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.florinhas.requisicoes.domain.Funcionario;
import pt.florinhas.requisicoes.domain.ManutencaoItem;
import pt.florinhas.requisicoes.domain.Material;
import pt.florinhas.requisicoes.domain.Requisicao;
import pt.florinhas.requisicoes.domain.RequisicaoEstado;
import pt.florinhas.requisicoes.domain.RequisicaoManutencao;
import pt.florinhas.requisicoes.domain.RequisicaoManutencaoItem;
import pt.florinhas.requisicoes.domain.RequisicaoMaterial;
import pt.florinhas.requisicoes.domain.RequisicaoMaterialItem;
import pt.florinhas.requisicoes.domain.RequisicaoPrioridade;
import pt.florinhas.requisicoes.domain.RequisicaoTipo;
import pt.florinhas.requisicoes.domain.RequisicaoTransporte;
import pt.florinhas.requisicoes.domain.RequisicaoTransporteItem;
import pt.florinhas.requisicoes.domain.TipoManutencao;
import pt.florinhas.requisicoes.domain.Transporte;
import pt.florinhas.requisicoes.dto.CriarMaterialRequest;
import pt.florinhas.requisicoes.dto.CriarRequisicaoManutencaoRequest;
import pt.florinhas.requisicoes.dto.CriarRequisicaoMaterialRequest;
import pt.florinhas.requisicoes.dto.CriarRequisicaoTransporteRequest;
import pt.florinhas.requisicoes.dto.CriarTipoManutencaoRequest;
import pt.florinhas.requisicoes.dto.CriarManutencaoItemRequest;
import pt.florinhas.requisicoes.dto.CriarTransporteRequest;
import pt.florinhas.requisicoes.exception.ResourceNotFoundException;
import pt.florinhas.requisicoes.repository.FuncionarioRepository;
import pt.florinhas.requisicoes.repository.ManutencaoItemRepository;
import pt.florinhas.requisicoes.repository.MaterialRepository;
import pt.florinhas.requisicoes.repository.RequisicaoManutencaoItemRepository;
import pt.florinhas.requisicoes.repository.RequisicaoManutencaoRepository;
import pt.florinhas.requisicoes.repository.RequisicaoMaterialRepository;
import pt.florinhas.requisicoes.repository.RequisicaoRepository;
import pt.florinhas.requisicoes.repository.RequisicaoTransporteRepository;
import pt.florinhas.requisicoes.repository.TipoManutencaoRepository;
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
    private final TipoManutencaoRepository tipoManutencaoRepository;
    private final ManutencaoItemRepository manutencaoItemRepository;
    private final RequisicaoManutencaoItemRepository requisicaoManutencaoItemRepository;

    public RequisicaoService(
            RequisicaoRepository requisicaoRepository,
            RequisicaoMaterialRepository requisicaoMaterialRepository,
            RequisicaoTransporteRepository requisicaoTransporteRepository,
            RequisicaoManutencaoRepository requisicaoManutencaoRepository,
            FuncionarioRepository funcionarioRepository,
            MaterialRepository materialRepository,
            TransporteRepository transporteRepository,
            TipoManutencaoRepository tipoManutencaoRepository,
            ManutencaoItemRepository manutencaoItemRepository,
            RequisicaoManutencaoItemRepository requisicaoManutencaoItemRepository) {
        this.requisicaoRepository = requisicaoRepository;
        this.requisicaoMaterialRepository = requisicaoMaterialRepository;
        this.requisicaoTransporteRepository = requisicaoTransporteRepository;
        this.requisicaoManutencaoRepository = requisicaoManutencaoRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.materialRepository = materialRepository;
        this.transporteRepository = transporteRepository;
        this.tipoManutencaoRepository = tipoManutencaoRepository;
        this.manutencaoItemRepository = manutencaoItemRepository;
        this.requisicaoManutencaoItemRepository = requisicaoManutencaoItemRepository;
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
            String dataInicioStr,
            String dataFimStr) {
        String criadoPorPattern = prepararPadraoLike(criadoPorNome);

        LocalDateTime dataInicio = null;
        if (dataInicioStr != null && !dataInicioStr.isBlank()) {
            dataInicio = LocalDate.parse(dataInicioStr).atStartOfDay();
        }

        LocalDateTime dataFim = null;
        if (dataFimStr != null && !dataFimStr.isBlank()) {
            dataFim = LocalDate.parse(dataFimStr).atTime(LocalTime.MAX);
        }

        return requisicaoRepository.findWithFilters(
            estado,
            tipo,
            prioridade,
            criadoPorPattern,
            dataInicio,
            dataFim
        );
    }

    private String prepararPadraoLike(String filtro) {
        String normalizado = normalizarFiltroTexto(filtro);
        if (normalizado == null) {
            return null;
        }
        return "%" + normalizado + "%";
    }

    private String normalizarFiltroTexto(String filtro) {
        if (filtro == null || filtro.isBlank()) {
            return null;
        }
        return filtro.toLowerCase(Locale.ROOT).trim();
    }

    @Transactional
    public RequisicaoMaterial criarMaterial(CriarRequisicaoMaterialRequest request) {
        Funcionario criadoPor = obterFuncionario(request.criadoPorId());

        Map<Long, Integer> itensNormalizados = new LinkedHashMap<>();
        for (CriarRequisicaoMaterialRequest.ItemMaterialRequest item : request.itens()) {
            itensNormalizados.put(item.materialId(), item.quantidade());
        }

        RequisicaoMaterial requisicao = new RequisicaoMaterial();
        requisicao.setDescricao(normalizarDescricao(request.descricao()));
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

    @Transactional
    public RequisicaoTransporte criarTransporte(CriarRequisicaoTransporteRequest request) {
        List<Long> transporteIds = resolverIdsTransporte(request.transporteIds(), request.transporteId());
        validarPeriodoTransporte(request.dataHoraSaida(), request.dataHoraRegresso());
        Funcionario criadoPor = obterFuncionario(request.criadoPorId());

        List<Transporte> transportesSelecionados = transporteIds.stream()
                .distinct()
                .map(id -> transporteRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Transporte não encontrado: " + id)))
                .toList();

        RequisicaoTransporte requisicao = new RequisicaoTransporte();
        requisicao.setDescricao(normalizarDescricao(request.descricao()));
        requisicao.setPrioridade(request.prioridade());
        requisicao.setTempoLimite(request.tempoLimite());
        requisicao.setTipo(RequisicaoTipo.TRANSPORTE);
        requisicao.setCriadoPor(criadoPor);
        requisicao.setGeridoPor(null);
        requisicao.setDestino(request.destino());
        requisicao.setDataHoraSaida(request.dataHoraSaida());
        requisicao.setDataHoraRegresso(request.dataHoraRegresso());
        requisicao.setNumeroPassageiros(request.numeroPassageiros());
        requisicao.setCondutor(normalizarTextoOpcional(request.condutor()));
        requisicao.setTransporte(transportesSelecionados.getFirst());

        for (Transporte transporte : transportesSelecionados) {
            RequisicaoTransporteItem item = new RequisicaoTransporteItem();
            item.setTransporte(transporte);
            item.setRequisicao(requisicao);
            requisicao.getTransportes().add(item);
        }

        return requisicaoTransporteRepository.save(requisicao);
    }

    private List<Long> resolverIdsTransporte(List<Long> transporteIds, Long transporteId) {
        boolean temLista = transporteIds != null && !transporteIds.isEmpty();
        boolean temSingular = transporteId != null;

        if (temLista && temSingular) {
            throw new IllegalArgumentException("Pedido inválido: forneça apenas 'transporteIds' ou 'transporteId', não ambos.");
        }

        if (!temLista && !temSingular) {
            throw new IllegalArgumentException("É obrigatório indicar pelo menos um transporte.");
        }

        return temLista ? transporteIds : List.of(transporteId);
    }

    private void validarPeriodoTransporte(LocalDateTime dataHoraSaida, LocalDateTime dataHoraRegresso) {
        LocalDateTime agora = LocalDateTime.now();

        if (dataHoraSaida.isBefore(agora)) {
            throw new IllegalArgumentException("A data/hora de saída não pode estar no passado.");
        }

        if (dataHoraRegresso.isBefore(agora)) {
            throw new IllegalArgumentException("A data/hora de regresso não pode estar no passado.");
        }

        if (!dataHoraRegresso.isAfter(dataHoraSaida)) {
            throw new IllegalArgumentException("A data/hora de regresso deve ser posterior à data/hora de saída.");
        }
    }

    @Transactional
    public RequisicaoManutencao criarManutencao(CriarRequisicaoManutencaoRequest request) {
        Funcionario criadoPor = obterFuncionario(request.criadoPorId());

        RequisicaoManutencao requisicao = new RequisicaoManutencao();
        requisicao.setDescricao(normalizarDescricao(request.descricao()));
        requisicao.setPrioridade(request.prioridade());
        requisicao.setTempoLimite(request.tempoLimite());
        requisicao.setTipo(RequisicaoTipo.MANUTENCAO);
        requisicao.setCriadoPor(criadoPor);
        requisicao.setGeridoPor(null);
        requisicao.setAssunto(request.assunto());

        RequisicaoManutencao savedRequisicao = requisicaoManutencaoRepository.save(requisicao);

        // Process maintenance items if provided
        if (request.manutencaoItens() != null && !request.manutencaoItens().isEmpty()) {
            for (var itemRequest : request.manutencaoItens()) {
                ManutencaoItem item = manutencaoItemRepository.findById(itemRequest.itemId())
                        .orElseThrow(() -> new IllegalArgumentException("ManutencaoItem not found: " + itemRequest.itemId()));
                
                RequisicaoManutencaoItem requisicaoItem = new RequisicaoManutencaoItem();
                requisicaoItem.setRequisicao(savedRequisicao);
                requisicaoItem.setManutencaoItem(item);
                requisicaoItem.setObservacoes(itemRequest.observacoes());
                
                requisicaoManutencaoItemRepository.save(requisicaoItem);
            }
        }

        return savedRequisicao;
    }

    @Transactional
    public Requisicao atualizarEstado(Long id, RequisicaoEstado novoEstado, Long alteradoPorId) {
        Requisicao requisicao = obterPorId(id);
        validarTransicaoEstado(requisicao.getEstado(), novoEstado);

        requisicao.setEstado(novoEstado);
        requisicao.setGeridoPor(obterFuncionario(alteradoPorId));
        requisicao.setUltimaAlteracaoEstadoEm(LocalDateTime.now());

        return requisicaoRepository.save(requisicao);
    }

    public List<Material> listarMateriais() {
        return materialRepository.findAllByOrderByCategoriaAscNomeAscAtributoAscValorAtributoAsc();
    }

    @Transactional
    public Material criarMaterialCatalogo(CriarMaterialRequest request) {
        Material material = new Material();
        material.setNome(request.nome().trim());
        material.setCategoria(request.categoria());
        material.setAtributo(request.atributo().trim());
        material.setValorAtributo(request.valorAtributo().trim());
        return materialRepository.save(material);
    }

    @Transactional
    public Material atualizarMaterialCatalogo(Long id, CriarMaterialRequest request) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material não encontrado: " + id));

        material.setNome(request.nome().trim());
        material.setCategoria(request.categoria());
        material.setAtributo(request.atributo().trim());
        material.setValorAtributo(request.valorAtributo().trim());

        return materialRepository.save(material);
    }

    @Transactional
    public void apagarMaterialCatalogo(Long id) {
        if (requisicaoMaterialRepository.existsByItensMaterialId(id)) {
            throw new IllegalArgumentException("Não é possível apagar: material está associado a requisições.");
        }

        if (!materialRepository.existsById(id)) {
            throw new ResourceNotFoundException("Material não encontrado: " + id);
        }

        materialRepository.deleteById(id);
    }

    public List<Transporte> listarTransportes() {
        return transporteRepository.findAll(
                Sort.by(Sort.Order.asc("codigo"), Sort.Order.asc("tipo"), Sort.Order.asc("matricula")));
    }

    @Transactional
    public Transporte criarTransporteCatalogo(CriarTransporteRequest request) {
        String codigoNormalizado = normalizarTextoObrigatorio(request.codigo(), "Código interno");
        String tipoNormalizado = normalizarTextoObrigatorio(request.tipo(), "Tipo");
        String marcaNormalizada = normalizarTextoObrigatorio(request.marca(), "Marca");
        String modeloNormalizado = normalizarTextoObrigatorio(request.modelo(), "Modelo");

        if (request.categoria() == null) {
            throw new IllegalArgumentException("A categoria do transporte é obrigatória.");
        }
        if (request.lotacao() == null || request.lotacao() <= 0) {
            throw new IllegalArgumentException("A lotação do transporte é obrigatória e deve ser maior que zero.");
        }
        if (request.dataMatricula() == null) {
            throw new IllegalArgumentException("A data de matrícula do transporte é obrigatória.");
        }

        String matriculaNormalizada = normalizarTextoObrigatorio(request.matricula(), "Matrícula")
                .toUpperCase(Locale.ROOT);

        transporteRepository.findByMatricula(matriculaNormalizada)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Já existe transporte com a matrícula: " + request.matricula());
                });

        Transporte transporte = new Transporte();
        transporte.setCodigo(codigoNormalizado);
        transporte.setTipo(tipoNormalizado);
        transporte.setCategoria(request.categoria());
        transporte.setMatricula(matriculaNormalizada);
        transporte.setMarca(marcaNormalizada);
        transporte.setModelo(modeloNormalizado);
        transporte.setLotacao(request.lotacao());
        transporte.setDataMatricula(request.dataMatricula());

        return transporteRepository.save(transporte);
    }

    @Transactional
    public Transporte atualizarTransporteCatalogo(Long id, CriarTransporteRequest request) {
        Transporte transporte = transporteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transporte não encontrado: " + id));

        String matriculaNormalizada = request.matricula().trim().toUpperCase(Locale.ROOT);
        transporteRepository.findByMatricula(matriculaNormalizada)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Já existe transporte com a matrícula: " + request.matricula());
                });

        transporte.setCodigo(normalizarTextoOpcional(request.codigo()));
        transporte.setTipo(request.tipo().trim());
        transporte.setCategoria(request.categoria());
        transporte.setMatricula(matriculaNormalizada);
        transporte.setMarca(request.marca() != null ? request.marca().trim() : null);
        transporte.setModelo(request.modelo() != null ? request.modelo().trim() : null);
        transporte.setLotacao(request.lotacao());
        transporte.setDataMatricula(request.dataMatricula());

        return transporteRepository.save(transporte);
    }

    @Transactional
    public void apagarTransporteCatalogo(Long id) {
        if (requisicaoTransporteRepository.existsByTransporteId(id)
                || requisicaoTransporteRepository.existsByTransportesTransporteId(id)) {
            throw new IllegalArgumentException("Não é possível apagar: transporte está associado a requisições.");
        }

        if (!transporteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Transporte não encontrado: " + id);
        }

        transporteRepository.deleteById(id);
    }

    public List<TipoManutencao> listarTiposManutencao() {
        return tipoManutencaoRepository.findAllByOrderByNomeAsc();
    }

    @Transactional
    public TipoManutencao criarTipoManutencao(CriarTipoManutencaoRequest request) {
        String nomeNormalizado = request.nome().trim();
        tipoManutencaoRepository.findByNomeIgnoreCase(nomeNormalizado)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Já existe tipo de manutenção com o nome: " + request.nome());
                });

        TipoManutencao tipo = new TipoManutencao();
        tipo.setNome(nomeNormalizado);
        tipo.setDescricao(request.descricao() != null ? request.descricao().trim() : null);
        return tipoManutencaoRepository.save(tipo);
    }

    @Transactional
    public TipoManutencao atualizarTipoManutencao(Long id, CriarTipoManutencaoRequest request) {
        TipoManutencao tipo = tipoManutencaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de manutenção não encontrado: " + id));

        String nomeNormalizado = request.nome().trim();
        tipoManutencaoRepository.findByNomeIgnoreCase(nomeNormalizado)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Já existe tipo de manutenção com o nome: " + request.nome());
                });

        tipo.setNome(nomeNormalizado);
        tipo.setDescricao(request.descricao() != null ? request.descricao().trim() : null);
        return tipoManutencaoRepository.save(tipo);
    }

    @Transactional
    public void apagarTipoManutencao(Long id) {
        TipoManutencao tipo = tipoManutencaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de manutenção não encontrado: " + id));

        if (requisicaoManutencaoRepository.existsByAssuntoIgnoreCase(tipo.getNome())) {
            throw new IllegalArgumentException("Não é possível apagar: tipo está associado a requisições de manutenção.");
        }

        tipoManutencaoRepository.delete(tipo);
    }

    public List<ManutencaoItem> listarManutencaoItems() {
        return manutencaoItemRepository.findAllByOrderByCategoriaAscEspacoAsc();
    }

    public List<ManutencaoItem> listarManutencaoItemsPorCategoria(String categoria) {
        return manutencaoItemRepository.findByCategoria(categoria);
    }

    @Transactional
    public ManutencaoItem criarManutencaoItem(CriarManutencaoItemRequest request) {
        ManutencaoItem item = new ManutencaoItem();
        item.setCategoria(request.categoria().trim());
        item.setEspaco(request.espaco().trim());
        item.setItemVerificacao(request.itemVerificacao().trim());
        return manutencaoItemRepository.save(item);
    }

    @Transactional
    public ManutencaoItem atualizarManutencaoItem(Long id, CriarManutencaoItemRequest request) {
        ManutencaoItem item = manutencaoItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item de manutenção não encontrado: " + id));

        item.setCategoria(request.categoria().trim());
        item.setEspaco(request.espaco().trim());
        item.setItemVerificacao(request.itemVerificacao().trim());
        return manutencaoItemRepository.save(item);
    }

    @Transactional
    public void apagarManutencaoItem(Long id) {
        ManutencaoItem item = manutencaoItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item de manutenção não encontrado: " + id));

        if (requisicaoManutencaoItemRepository.existsByManutencaoItemId(id)) {
            throw new IllegalArgumentException("Não é possível apagar: item de manutenção está associado a requisições.");
        }

        manutencaoItemRepository.delete(item);
    }

    private void validarTransicaoEstado(RequisicaoEstado estadoAtual, RequisicaoEstado novoEstado) {
        if (estadoAtual == novoEstado) return;

        if (estadoAtual == RequisicaoEstado.FECHADO || estadoAtual == RequisicaoEstado.RECUSADO) {
            throw new IllegalArgumentException("Não é possível alterar o estado de uma requisição finalizada.");
        }

        if (estadoAtual == RequisicaoEstado.ABERTO) {
            if (novoEstado == RequisicaoEstado.EM_PROGRESSO || novoEstado == RequisicaoEstado.FECHADO || novoEstado == RequisicaoEstado.RECUSADO) {
                return;
            }
        }

        if (estadoAtual == RequisicaoEstado.EM_PROGRESSO) {
            if (novoEstado == RequisicaoEstado.FECHADO || novoEstado == RequisicaoEstado.RECUSADO) {
                return;
            }
        }

        throw new IllegalArgumentException("Transição de estado inválida do estado " + estadoAtual + " para " + novoEstado);
    }

    private Funcionario obterFuncionario(Long id) {
        return funcionarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado: " + id));
    }

    private String normalizarTextoOpcional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizarTextoObrigatorio(String value, String campo) {
        String normalized = normalizarTextoOpcional(value);
        if (normalized == null) {
            throw new IllegalArgumentException(campo + " é obrigatório.");
        }
        return normalized;
    }

    private String normalizarDescricao(String descricao) {
        String normalized = normalizarTextoOpcional(descricao);
        return normalized != null ? normalized : "";
    }
}
