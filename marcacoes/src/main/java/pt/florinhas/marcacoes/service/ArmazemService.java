package pt.florinhas.marcacoes.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pt.florinhas.marcacoes.domain.EventoEstado;
import pt.florinhas.marcacoes.domain.ItemArmazem;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.domain.MarcacaoBalneario;
import pt.florinhas.marcacoes.domain.Roupa;
import pt.florinhas.marcacoes.dto.ConsumoEstatisticaDTO;
import pt.florinhas.marcacoes.dto.ItemArmazemDTO;
import pt.florinhas.marcacoes.repository.ItemArmazemRepository;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;
import pt.florinhas.marcacoes.repository.RoupaRepository;

/**
 * Serviço de gestão do armazém do Balneário.
 * 
 * Responsabilidades:
 * - CRUD de itens do armazém
 * - Desconto automático de stock ao marcar presença
 * - Restauração de stock ao cancelar/faltar
 * - Verificação de níveis de stock
 * - Estatísticas de consumo por período
 * - Inicialização de dados padrão
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArmazemService {

    private final ItemArmazemRepository itemArmazemRepository;
    private final MarcacaoRepository marcacaoRepository;
    private final RoupaRepository roupaRepository;

    // =====================================================================
    // MAPEAMENTO: Opções do formulário → Itens do armazém
    // =====================================================================

    private static final String KEY_TRACKED = "tracked";
    private static final String STATUS_BAIXO = "BAIXO";

    private static final String VAL_CHAMPO = "Champô";
    private static final String VAL_GEL_BANHO = "Gel de Banho";
    private static final String VAL_TOALHA = "Toalha";
    private static final String VAL_SABONETE_CREME = "Sabonete/Creme";
    private static final String VAL_LAVAR_SECA = "Lavar Roupa Seca";
    private static final String VAL_LAVAR_MOLHADA = "Lavar Roupa Molhada";
    private static final String VAL_ROUPA_INTERIOR = "Roupa Interior";
    private static final String VAL_MEIAS = "Meias";
    private static final String VAL_AGASALHO = "Agasalho/Casaco";
    private static final String VAL_DETERGENTE = "Detergente Roupa";
    private static final String VAL_TSHIRT = "T-shirt/Camisola";
    private static final String VAL_CALCAS = "Calças";

    private static final String CAT_HIGIENE = "HIGIENE";
    private static final String CAT_DETERGENTES = "DETERGENTES";
    private static final String CAT_VESTUARIO = "VESTUARIO";
    private static final String CAT_CALCADO = "CALCADO";
    private static final String BRAND_GENERATIVE = "Generative";
    private static final String STATUS_OK = "OK";
    private static final String UNIT_UN = "un";
    private static final String UNIT_PARES = "pares";
    private static final String UNIT_L = "L";
    private static final String BRAND_STANDARD = "Standard";

    /**
     * Mapeia a categoria do formulário (Roupa.categoria) para o nome do item no armazém.
     * Para calçado, o mapeamento é feito pelo tamanho (Roupa.tamanho → ItemArmazem.nome).
     */
    private static final Map<String, String> FORM_TO_ARMAZEM = Map.ofEntries(
        Map.entry("Shampoo", VAL_CHAMPO),
        Map.entry(VAL_CHAMPO, VAL_CHAMPO),
        Map.entry(VAL_GEL_BANHO, VAL_GEL_BANHO),
        Map.entry(VAL_TOALHA, VAL_TOALHA),
        Map.entry(VAL_SABONETE_CREME, VAL_SABONETE_CREME),
        Map.entry(VAL_LAVAR_SECA, VAL_DETERGENTE),
        Map.entry(VAL_LAVAR_MOLHADA, VAL_DETERGENTE),
        Map.entry(VAL_TSHIRT, VAL_TSHIRT),
        Map.entry(VAL_CALCAS, VAL_CALCAS),
        Map.entry(VAL_ROUPA_INTERIOR, VAL_ROUPA_INTERIOR),
        Map.entry(VAL_MEIAS, VAL_MEIAS),
        Map.entry(VAL_AGASALHO, VAL_AGASALHO)
    );

    /**
     * Mapeia a categoria do formulário para a categoria do armazém.
     */
    private static final Map<String, String> FORM_TO_CATEGORIA = Map.ofEntries(
        Map.entry("Shampoo", CAT_HIGIENE),
        Map.entry(VAL_CHAMPO, CAT_HIGIENE),
        Map.entry(VAL_GEL_BANHO, CAT_HIGIENE),
        Map.entry(VAL_TOALHA, CAT_HIGIENE),
        Map.entry(VAL_SABONETE_CREME, CAT_HIGIENE),
        Map.entry("Lixívia", CAT_DETERGENTES),
        Map.entry("Amaciador", CAT_DETERGENTES),
        Map.entry("Detergente Chão", CAT_DETERGENTES),
        Map.entry(VAL_LAVAR_SECA, CAT_DETERGENTES),
        Map.entry(VAL_LAVAR_MOLHADA, CAT_DETERGENTES),
        Map.entry(VAL_ROUPA_INTERIOR, CAT_VESTUARIO),
        Map.entry(VAL_MEIAS, CAT_VESTUARIO),
        Map.entry(VAL_AGASALHO, CAT_VESTUARIO)
    );

    // =====================================================================
    // CRUD
    // =====================================================================

    /**
     * Lista todos os itens do armazém, ordenados por categoria e nome.
     */
    public List<ItemArmazemDTO> listarTodos() {
        return itemArmazemRepository.findAllByOrderByCategoriaAscNomeAsc()
            .stream()
            .map(this::toDTO)
            .toList();
    }

    /**
     * Lista itens por categoria.
     */
    public List<ItemArmazemDTO> listarPorCategoria(String categoria) {
        return itemArmazemRepository.findByCategoria(categoria)
            .stream()
            .map(this::toDTO)
            .toList();
    }

    /**
     * Cria um novo item no armazém.
     */
    @Transactional
    public ItemArmazemDTO criarItem(ItemArmazemDTO dto) {
        if (dto.getCategoria() == null || dto.getCategoria().trim().isBlank()) {
            throw new IllegalArgumentException("A categoria é obrigatória.");
        }
        if (dto.getNome() == null || dto.getNome().trim().isBlank()) {
            throw new IllegalArgumentException("O nome do item é obrigatório.");
        }

        String categoriaNorm = dto.getCategoria().trim().toUpperCase();
        String nomeNorm = dto.getNome().trim();

        if (itemArmazemRepository.findByCategoriaAndNome(categoriaNorm, nomeNorm).isPresent()) {
            throw new IllegalArgumentException("Já existe um item com esta categoria e nome.");
        }

        ItemArmazem item = new ItemArmazem();
        item.setCategoria(categoriaNorm);
        item.setNome(nomeNorm);
        item.setQuantidade(dto.getQuantidade() != null ? Math.max(0, dto.getQuantidade()) : 0);
        item.setQuantidadeMinima(dto.getQuantidadeMinima() != null ? Math.max(0, dto.getQuantidadeMinima()) : 0);
        item.setUnidade(dto.getUnidade() != null ? dto.getUnidade() : UNIT_UN);
        item.setMarca(dto.getMarca());
        item.setTamanho(dto.getTamanho());
        item.setVolume(dto.getVolume());
        item.setDescricao(dto.getDescricao());

        item = itemArmazemRepository.save(item);
        return toDTO(item);
    }

    /**
     * Atualiza um item do armazém.
     * Pode atualizar quantidade, mínimos, categoria, nome e unidade.
     */
    @Transactional
    public ItemArmazemDTO atualizarItem(Long id, ItemArmazemDTO dto) {
        ItemArmazem item = itemArmazemRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Item do armazém não encontrado com ID: " + id));

        if (dto.getCategoria() != null) {
            if (dto.getCategoria().trim().isBlank()) {
                throw new IllegalArgumentException("A categoria não pode ser vazia.");
            }
            item.setCategoria(dto.getCategoria().trim().toUpperCase());
        }
        if (dto.getNome() != null) {
            String nomeTrim = dto.getNome().trim();
            if (nomeTrim.isBlank()) {
                throw new IllegalArgumentException("O nome do item não pode ser vazio.");
            }
            item.setNome(nomeTrim);
        }
        if (dto.getQuantidade() != null) {
            item.setQuantidade(Math.max(0, dto.getQuantidade()));
        }
        if (dto.getQuantidadeMinima() != null) {
            item.setQuantidadeMinima(Math.max(0, dto.getQuantidadeMinima()));
        }
        if (dto.getUnidade() != null) {
            item.setUnidade(dto.getUnidade());
        }
        if (dto.getMarca() != null) {
            item.setMarca(dto.getMarca());
        }
        if (dto.getTamanho() != null) {
            item.setTamanho(dto.getTamanho());
        }
        if (dto.getVolume() != null) {
            item.setVolume(dto.getVolume());
        }
        if (dto.getDescricao() != null) {
            item.setDescricao(dto.getDescricao());
        }

        item = itemArmazemRepository.save(item);
        return toDTO(item);
    }

    /**
     * Elimina um item do armazém.
     */
    @Transactional
    public void eliminarItem(Long id) {
        if (!itemArmazemRepository.existsById(id)) {
            throw new IllegalArgumentException("Item do armazém não encontrado com ID: " + id);
        }
        
        // Verificar se existem referências em marcações (Roupa)
        if (roupaRepository.existsByItemId(id)) {
            throw new IllegalStateException("Não é possível eliminar o item porque ele já foi utilizado em marcações. Considere alterar apenas os níveis de stock.");
        }
        
        itemArmazemRepository.deleteById(id);
    }

    // =====================================================================
    // DESCONTO E RESTAURAÇÃO DE STOCK
    // =====================================================================

    /**
     * Desconta os itens de uma marcação de balneário do stock do armazém.
     * Chamado quando a marcação transita para EM_PROGRESSO (Marcar Presente).
     *
     * @param marcacao a marcação com detalhes de balneário
     * @return lista de itens com stock insuficiente (vazia se tudo OK)
     */
    @Transactional
    public List<String> descontarItens(Marcacao marcacao) {
        MarcacaoBalneario detalhes = marcacao.getMarcacaoBalneario();
        if (detalhes == null || detalhes.getRoupas() == null) {
            return List.of();
        }

        List<String> avisos = new ArrayList<>();

        for (Roupa roupa : detalhes.getRoupas()) {
            // Tentar pelo ID primeiro (Novo modo Objeto)
            Optional<ItemArmazem> itemOpt = Optional.empty();
            if (roupa.getItem() != null) {
                itemOpt = itemArmazemRepository.findById(roupa.getItem().getId());
            }

            // Fallback para nome/categoria se o ID não estiver presente
            if (itemOpt.isEmpty()) {
                String formCategoria = roupa.getCategoria();
                if (formCategoria == null || formCategoria.trim().isBlank()) {
                    log.warn("Item da marcação ignorado no desconto: Categoria/Nome nulo.");
                    continue;
                }
                String armazemCategoria = FORM_TO_CATEGORIA.get(formCategoria);
                String armazemNome;

                if ("Sapatos/Sapatilhas".equals(formCategoria)) {
                    armazemNome = roupa.getTamanho();
                    armazemCategoria = "CALCADO";
                } else {
                    armazemNome = FORM_TO_ARMAZEM.get(formCategoria);
                }

                if (armazemNome == null) armazemNome = formCategoria;

                if (armazemCategoria != null) {
                    itemOpt = itemArmazemRepository.findByCategoriaAndNome(armazemCategoria, armazemNome);
                } else {
                    List<String> managedCats = List.of(CAT_HIGIENE, CAT_DETERGENTES, CAT_VESTUARIO, CAT_CALCADO);
                    for (String cat : managedCats) {
                        itemOpt = itemArmazemRepository.findByCategoriaAndNome(cat, armazemNome);
                        if (itemOpt.isPresent()) break;
                    }
                }
            }

            if (itemOpt.isPresent()) {
                ItemArmazem item = itemOpt.get();
                int novaQuantidade = item.getQuantidade() - roupa.getQuantidade();
                if (novaQuantidade < 0) {
                    avisos.add(String.format("Stock insuficiente para '%s' (disponível: %d, necessário: %d)",
                        item.getNome(), item.getQuantidade(), roupa.getQuantidade()));
                    item.setQuantidade(0);
                } else {
                    item.setQuantidade(novaQuantidade);
                }
                itemArmazemRepository.save(item);
            } else {
                log.warn("Item do armazém não encontrado para desconto: {}", roupa.getCategoria());
            }
        }

        return avisos;
    }

    /**
     * Restaura os itens de uma marcação ao stock do armazém.
     * Chamado quando a marcação transita de EM_PROGRESSO para CANCELADO ou NAO_COMPARECIDO.
     */
    @Transactional
    public void restaurarItens(Marcacao marcacao) {
        MarcacaoBalneario detalhes = marcacao.getMarcacaoBalneario();
        if (detalhes == null || detalhes.getRoupas() == null) {
            return;
        }

        for (Roupa roupa : detalhes.getRoupas()) {
            Optional<ItemArmazem> itemOpt = Optional.empty();
            if (roupa.getItem() != null) {
                itemOpt = itemArmazemRepository.findById(roupa.getItem().getId());
            }

            if (itemOpt.isEmpty()) {
                String formCategoria = roupa.getCategoria();
                if (formCategoria == null || formCategoria.trim().isBlank()) {
                    log.warn("Item da marcação ignorado no restauro: Categoria/Nome nulo.");
                    continue;
                }
                String armazemCategoria = FORM_TO_CATEGORIA.get(formCategoria);
                String armazemNome;

                if ("Sapatos/Sapatilhas".equals(formCategoria)) {
                    armazemNome = roupa.getTamanho();
                    armazemCategoria = CAT_CALCADO;
                } else {
                    armazemNome = FORM_TO_ARMAZEM.get(formCategoria);
                }

                if (armazemNome == null) armazemNome = formCategoria;

                if (armazemCategoria != null) {
                    itemOpt = itemArmazemRepository.findByCategoriaAndNome(armazemCategoria, armazemNome);
                } else {
                    List<String> managedCats = List.of(CAT_HIGIENE, CAT_DETERGENTES, CAT_VESTUARIO, CAT_CALCADO);
                    for (String cat : managedCats) {
                        itemOpt = itemArmazemRepository.findByCategoriaAndNome(cat, armazemNome);
                        if (itemOpt.isPresent()) break;
                    }
                }
            }

            if (itemOpt.isPresent()) {
                ItemArmazem item = itemOpt.get();
                item.setQuantidade(item.getQuantidade() + roupa.getQuantidade());
                itemArmazemRepository.save(item);
                log.debug("Stock restaurado para '{}': +{}", item.getNome(), roupa.getQuantidade());
            }
        }
    }

    // =====================================================================
    // VERIFICAÇÃO DE STOCK (para avisos no formulário)
    // =====================================================================

    /**
     * Verifica os níveis de stock para uma lista de itens do formulário.
     * Retorna o estado de cada item do formulário com stock disponível.
     *
     * @param formItems lista de categorias do formulário (ex: ["Shampoo", "Toalha"])
     * @return mapa com categoria → {quantidade, estado}
     */
    public Map<String, Map<String, Object>> verificarStock(List<String> formItems) {
        Map<String, Map<String, Object>> resultado = new LinkedHashMap<>();

        for (String formItem : formItems) {
            String armazemCategoria = FORM_TO_CATEGORIA.get(formItem);
            String armazemNome = FORM_TO_ARMAZEM.get(formItem);

            if (armazemNome == null || armazemCategoria == null) {
                // Sem mapeamento: não trackado no armazém
                resultado.put(formItem, Map.of(KEY_TRACKED, false));
                continue;
            }

            Optional<ItemArmazem> itemOpt = itemArmazemRepository.findByCategoriaAndNome(armazemCategoria, armazemNome);
            if (itemOpt.isPresent()) {
                ItemArmazem item = itemOpt.get();
                String estado = item.getQuantidade() >= item.getQuantidadeMinima() ? STATUS_OK : STATUS_BAIXO;
                resultado.put(formItem, Map.of(
                    KEY_TRACKED, true,
                    "quantidade", item.getQuantidade(),
                    "quantidadeMinima", item.getQuantidadeMinima(),
                    "estado", estado,
                    "esgotado", item.getQuantidade() <= 0
                ));
            } else {
                resultado.put(formItem, Map.of(KEY_TRACKED, false));
            }
        }

        return resultado;
    }

    /**
     * Verifica stock de calçado por tamanho.
     */
    public Map<String, Map<String, Object>> verificarStockCalcado(List<String> tamanhos) {
        Map<String, Map<String, Object>> resultado = new LinkedHashMap<>();

        for (String tamanho : tamanhos) {
            Optional<ItemArmazem> itemOpt = itemArmazemRepository.findByCategoriaAndNome(CAT_CALCADO, tamanho);
            if (itemOpt.isPresent()) {
                ItemArmazem item = itemOpt.get();
                String estado = item.getQuantidade() >= item.getQuantidadeMinima() ? STATUS_OK : STATUS_BAIXO;
                resultado.put(tamanho, Map.of(
                    KEY_TRACKED, true,
                    "quantidade", item.getQuantidade(),
                    "quantidadeMinima", item.getQuantidadeMinima(),
                    "estado", estado,
                    "esgotado", item.getQuantidade() <= 0
                ));
            } else {
                resultado.put(tamanho, Map.of(KEY_TRACKED, false));
            }
        }

        return resultado;
    }

    // =====================================================================
    // ESTATÍSTICAS DE CONSUMO
    // =====================================================================

    /**
     * Obtém estatísticas de consumo baseadas nas marcações concluídas.
     * Agrega os dados de Roupa de marcações com estado EM_PROGRESSO ou CONCLUIDO.
     *
     * @param periodo "DIA", "SEMANA", "MES"
     * @return DTO com dados agregados
     */
    public ConsumoEstatisticaDTO obterEstatisticas(String periodo) {
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime inicio;
        LocalDateTime fim; // Definido nas branches do switch

        switch (periodo.toUpperCase()) {
            case "DIA":
                inicio = agora.truncatedTo(ChronoUnit.DAYS);
                fim = agora.toLocalDate().atTime(23, 59, 59);
                break;
            case "SEMANA":
                inicio = agora.minusWeeks(1).truncatedTo(ChronoUnit.DAYS);
                fim = agora.toLocalDate().plusDays(30).atTime(23, 59, 59);
                break;
            case "MES":
            default:
                inicio = agora.minusMonths(1).truncatedTo(ChronoUnit.DAYS);
                fim = agora.toLocalDate().plusDays(30).atTime(23, 59, 59);
                break;
        }

        // Buscar marcações de balneário com estado EM_PROGRESSO ou CONCLUIDO no período
        List<EventoEstado> estadosConsumo = List.of(EventoEstado.EM_PROGRESSO, EventoEstado.CONCLUIDO);
        List<Marcacao> marcacoes = marcacaoRepository.findMarcacoesBetweenDates(inicio, fim, "BALNEARIO");

        List<ConsumoEstatisticaDTO.ConsumoItemDTO> itensConsumo = new ArrayList<>();
        Map<String, Integer> totaisPorCategoria = new HashMap<>();
        int totalGeral = 0;

        for (Marcacao m : marcacoes) {
            if (!estadosConsumo.contains(m.getEstado())) continue;
            MarcacaoBalneario bal = m.getMarcacaoBalneario();
            if (bal == null || bal.getRoupas() == null) continue;

            String dataStr = m.getData().toLocalDate().toString();

            for (Roupa roupa : bal.getRoupas()) {
                String armazemCategoria;
                String armazemNome;

                // Modo novo: item ligado diretamente ao armazém (inclui calçado com itemId)
                if (roupa.getItem() != null) {
                    ItemArmazem itemRef = roupa.getItem();
                    armazemCategoria = itemRef.getCategoria(); // ex: "CALCADO", "HIGIENE", etc.
                    armazemNome = itemRef.getNome();           // ex: "38", "Champô", etc.
                } else {
                    // Modo legado: usar campos de texto
                    String formCategoria = roupa.getCategoria();
                    if ("Sapatos/Sapatilhas".equalsIgnoreCase(formCategoria)) {
                        armazemCategoria = "CALCADO";
                        armazemNome = roupa.getTamanho() != null ? roupa.getTamanho() : formCategoria;
                    } else {
                        armazemCategoria = FORM_TO_CATEGORIA.getOrDefault(formCategoria, "OUTRO");
                        armazemNome = FORM_TO_ARMAZEM.getOrDefault(formCategoria, formCategoria);
                    }
                }

                ConsumoEstatisticaDTO.ConsumoItemDTO item = new ConsumoEstatisticaDTO.ConsumoItemDTO();
                item.setCategoria(armazemCategoria);
                item.setNome(armazemNome);
                item.setQuantidade(roupa.getQuantidade());
                item.setData(dataStr);
                itensConsumo.add(item);

                totaisPorCategoria.merge(armazemCategoria, roupa.getQuantidade(), Integer::sum);
                totalGeral += roupa.getQuantidade();
            }
        }

        ConsumoEstatisticaDTO dto = new ConsumoEstatisticaDTO();
        dto.setPeriodo(periodo);
        dto.setItens(itensConsumo);
        dto.setTotaisPorCategoria(totaisPorCategoria);
        dto.setTotalGeral(totalGeral);

        return dto;
    }

    // =====================================================================
    // INICIALIZAÇÃO DE DADOS PADRÃO
    // =====================================================================

    /**
     * Inicializa itens padrão do armazém se não existirem.
     * Executado ao arranque da aplicação.
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void inicializarDadosPadrao() {
        if (itemArmazemRepository.count() > 0) {
            log.debug("Armazém já contém dados, inicialização ignorada.");
            return;
        }

        log.info("A inicializar dados padrão do armazém...");

        // Detergentes
        criarItemSeNaoExiste(CAT_DETERGENTES, "Amaciador", 20, 5, UNIT_L, "Florinhas", null, 5.0, "Amaciador de roupa");
        criarItemSeNaoExiste(CAT_DETERGENTES, "Lixívia", 10, 5, UNIT_L, "Domestos", null, 2.0, "Lixívia desinfetante");
        criarItemSeNaoExiste(CAT_DETERGENTES, "Detergente Chão", 15, 8, UNIT_L, "Ajax", null, 5.0, "Detergente para o chão");
        criarItemSeNaoExiste(CAT_DETERGENTES, VAL_DETERGENTE, 50, 10, UNIT_L, "Florinhas", null, 10.0, "Detergente para a roupa");

        // Higiene
        criarItemSeNaoExiste(CAT_HIGIENE, "Sabonete Líquido", 100, 15, UNIT_UN, "Nivea", null, 0.5, "Sabonete para mãos");
        criarItemSeNaoExiste(CAT_HIGIENE, VAL_CHAMPO, 40, 8, UNIT_UN, "Garnier", null, 0.4, "Champô para cabelo");
        criarItemSeNaoExiste(CAT_HIGIENE, VAL_GEL_BANHO, 40, 10, UNIT_UN, "Dove", null, 0.5, "Gel de banho hidratante");
        criarItemSeNaoExiste(CAT_HIGIENE, VAL_TOALHA, 100, 20, UNIT_UN, BRAND_STANDARD, null, null, "Toalha de banho branca");
        criarItemSeNaoExiste(CAT_HIGIENE, VAL_SABONETE_CREME, 50, 15, UNIT_UN, "Dove", null, null, "Sabonete em barra ou creme");
        criarItemSeNaoExiste(CAT_HIGIENE, "Toalhetes", 60, 30, "pk", "Dodot", null, null, "Toalhetes de limpeza");

        // Vestuário
        criarItemSeNaoExiste(CAT_VESTUARIO, VAL_TSHIRT, 20, 10, UNIT_UN, BRAND_GENERATIVE, "M", null, "T-shirt básica");
        criarItemSeNaoExiste(CAT_VESTUARIO, VAL_CALCAS, 15, 10, UNIT_UN, BRAND_GENERATIVE, "L", null, "Calças confortáveis");
        criarItemSeNaoExiste(CAT_VESTUARIO, VAL_ROUPA_INTERIOR, 40, 20, UNIT_UN, BRAND_GENERATIVE, "M", null, "Boxers/Cuecas");
        criarItemSeNaoExiste(CAT_VESTUARIO, VAL_MEIAS, 50, 20, UNIT_PARES, BRAND_GENERATIVE, "39-42", null, "Meias de algodão");
        criarItemSeNaoExiste(CAT_VESTUARIO, VAL_AGASALHO, 10, 5, UNIT_UN, BRAND_GENERATIVE, "XL", null, "Casaco de inverno");

        // Calçado (tamanhos 35 a 46)
        for (int tamanho = 35; tamanho <= 46; tamanho++) {
            criarItemSeNaoExiste(CAT_CALCADO, String.valueOf(tamanho), 10, 3, UNIT_PARES, BRAND_STANDARD, String.valueOf(tamanho), null, "Sapatos tamanho " + tamanho);
        }

        log.info("Dados padrão do armazém inicializados com sucesso.");
    }

    private void criarItemSeNaoExiste(String categoria, String nome, int quantidade, int minimo, String unidade, String marca, String tamanho, Double volume, String descricao) {
        String catNorm = categoria.trim().toUpperCase();
        String nomeNorm = nome.trim();
        
        if (itemArmazemRepository.findByCategoriaAndNome(catNorm, nomeNorm).isEmpty()) {
            ItemArmazem item = new ItemArmazem();
            item.setCategoria(catNorm);
            item.setNome(nomeNorm);
            item.setQuantidade(quantidade);
            item.setQuantidadeMinima(minimo);
            item.setUnidade(unidade);
            item.setMarca(marca);
            item.setTamanho(tamanho);
            item.setVolume(volume);
            item.setDescricao(descricao);
            itemArmazemRepository.save(item);
        }
    }

    // =====================================================================
    // CONVERSÃO
    // =====================================================================

    private ItemArmazemDTO toDTO(ItemArmazem item) {
        ItemArmazemDTO dto = new ItemArmazemDTO();
        dto.setId(item.getId());
        dto.setCategoria(item.getCategoria());
        dto.setNome(item.getNome());
        dto.setQuantidade(item.getQuantidade());
        dto.setQuantidadeMinima(item.getQuantidadeMinima());
        dto.setUnidade(item.getUnidade());
        dto.setMarca(item.getMarca());
        dto.setTamanho(item.getTamanho());
        dto.setVolume(item.getVolume());
        dto.setDescricao(item.getDescricao());
        dto.setEstado(item.getQuantidade() >= item.getQuantidadeMinima() ? STATUS_OK : STATUS_BAIXO);
        return dto;
    }
}