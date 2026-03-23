package pt.florinhas.marcacoes.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    // =====================================================================
    // MAPEAMENTO: Opções do formulário → Itens do armazém
    // =====================================================================

    /**
     * Mapeia a categoria do formulário (Roupa.categoria) para o nome do item no armazém.
     * Para calçado, o mapeamento é feito pelo tamanho (Roupa.tamanho → ItemArmazem.nome).
     */
    private static final Map<String, String> FORM_TO_ARMAZEM = Map.ofEntries(
        Map.entry("Shampoo", "Champô"),
        Map.entry("Champô", "Champô"),
        Map.entry("Gel de Banho", "Gel de Banho"),
        Map.entry("Toalha", "Toalha"),
        Map.entry("Sabonete/Creme", "Sabonete/Creme"),
        Map.entry("Lavar Roupa Seca", "Detergente Roupa"),
        Map.entry("Lavar Roupa Molhada", "Detergente Roupa"),
        Map.entry("T-shirt/Camisola", "T-shirt/Camisola"),
        Map.entry("Calças", "Calças"),
        Map.entry("Roupa Interior", "Roupa Interior"),
        Map.entry("Meias", "Meias"),
        Map.entry("Agasalho/Casaco", "Agasalho/Casaco")
    );

    /**
     * Mapeia a categoria do formulário para a categoria do armazém.
     */
    private static final Map<String, String> FORM_TO_CATEGORIA = Map.ofEntries(
        Map.entry("Shampoo", "HIGIENE"),
        Map.entry("Champô", "HIGIENE"),
        Map.entry("Gel de Banho", "HIGIENE"),
        Map.entry("Toalha", "HIGIENE"),
        Map.entry("Sabonete/Creme", "HIGIENE"),
        Map.entry("Lavar Roupa Seca", "DETERGENTES"),
        Map.entry("Lavar Roupa Molhada", "DETERGENTES"),
        Map.entry("Sapatos/Sapatilhas", "CALCADO"),
        Map.entry("T-shirt/Camisola", "VESTUARIO"),
        Map.entry("Calças", "VESTUARIO"),
        Map.entry("Roupa Interior", "VESTUARIO"),
        Map.entry("Meias", "VESTUARIO"),
        Map.entry("Agasalho/Casaco", "VESTUARIO")
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
            .collect(Collectors.toList());
    }

    /**
     * Lista itens por categoria.
     */
    public List<ItemArmazemDTO> listarPorCategoria(String categoria) {
        return itemArmazemRepository.findByCategoria(categoria)
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Atualiza a quantidade e/ou quantidade mínima de um item.
     */
    @Transactional
    public ItemArmazemDTO atualizarItem(Long id, Integer quantidade, Integer quantidadeMinima) {
        ItemArmazem item = itemArmazemRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Item do armazém não encontrado com ID: " + id));

        if (quantidade != null) {
            item.setQuantidade(Math.max(0, quantidade));
        }
        if (quantidadeMinima != null) {
            item.setQuantidadeMinima(Math.max(0, quantidadeMinima));
        }

        item = itemArmazemRepository.save(item);
        return toDTO(item);
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
            String formCategoria = roupa.getCategoria();
            String armazemCategoria = FORM_TO_CATEGORIA.get(formCategoria);
            String armazemNome;

            if ("Sapatos/Sapatilhas".equals(formCategoria)) {
                // Para calçado, o nome no armazém é o tamanho
                armazemNome = roupa.getTamanho();
                armazemCategoria = "CALCADO";
            } else {
                armazemNome = FORM_TO_ARMAZEM.get(formCategoria);
            }

            if (armazemNome == null || armazemCategoria == null) {
                // Item do formulário sem mapeamento no armazém (ex: vestuário)
                log.debug("Item '{}' sem mapeamento no armazém, ignorado.", formCategoria);
                continue;
            }

            Optional<ItemArmazem> itemOpt = itemArmazemRepository.findByCategoriaAndNome(armazemCategoria, armazemNome);
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
                log.warn("Item do armazém não encontrado: categoria={}, nome={}", armazemCategoria, armazemNome);
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
            String formCategoria = roupa.getCategoria();
            String armazemCategoria = FORM_TO_CATEGORIA.get(formCategoria);
            String armazemNome;

            if ("Sapatos/Sapatilhas".equals(formCategoria)) {
                armazemNome = roupa.getTamanho();
                armazemCategoria = "CALCADO";
            } else {
                armazemNome = FORM_TO_ARMAZEM.get(formCategoria);
            }

            if (armazemNome == null || armazemCategoria == null) {
                continue;
            }

            Optional<ItemArmazem> itemOpt = itemArmazemRepository.findByCategoriaAndNome(armazemCategoria, armazemNome);
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
                resultado.put(formItem, Map.of("tracked", false));
                continue;
            }

            Optional<ItemArmazem> itemOpt = itemArmazemRepository.findByCategoriaAndNome(armazemCategoria, armazemNome);
            if (itemOpt.isPresent()) {
                ItemArmazem item = itemOpt.get();
                String estado = item.getQuantidade() >= item.getQuantidadeMinima() ? "OK" : "BAIXO";
                resultado.put(formItem, Map.of(
                    "tracked", true,
                    "quantidade", item.getQuantidade(),
                    "quantidadeMinima", item.getQuantidadeMinima(),
                    "estado", estado,
                    "esgotado", item.getQuantidade() <= 0
                ));
            } else {
                resultado.put(formItem, Map.of("tracked", false));
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
            Optional<ItemArmazem> itemOpt = itemArmazemRepository.findByCategoriaAndNome("CALCADO", tamanho);
            if (itemOpt.isPresent()) {
                ItemArmazem item = itemOpt.get();
                String estado = item.getQuantidade() >= item.getQuantidadeMinima() ? "OK" : "BAIXO";
                resultado.put(tamanho, Map.of(
                    "tracked", true,
                    "quantidade", item.getQuantidade(),
                    "quantidadeMinima", item.getQuantidadeMinima(),
                    "estado", estado,
                    "esgotado", item.getQuantidade() <= 0
                ));
            } else {
                resultado.put(tamanho, Map.of("tracked", false));
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
        LocalDateTime fim;

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
                String armazemCategoria = FORM_TO_CATEGORIA.getOrDefault(roupa.getCategoria(), "OUTRO");
                String armazemNome = FORM_TO_ARMAZEM.getOrDefault(roupa.getCategoria(), roupa.getCategoria());

                ConsumoEstatisticaDTO.ConsumoItemDTO item = new ConsumoEstatisticaDTO.ConsumoItemDTO();
                item.setCategoria(armazemCategoria);
                item.setNome(roupa.getCategoria().equalsIgnoreCase("Sapatos/Sapatilhas") && roupa.getTamanho() != null
                        ? roupa.getTamanho()
                        : armazemNome);
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
        criarItemSeNaoExiste("DETERGENTES", "Detergente Roupa", 0, 10, "L");
        criarItemSeNaoExiste("DETERGENTES", "Amaciador", 0, 5, "L");
        criarItemSeNaoExiste("DETERGENTES", "Lixívia", 0, 5, "L");
        criarItemSeNaoExiste("DETERGENTES", "Detergente Chão", 0, 8, "L");

        // Higiene
        criarItemSeNaoExiste("HIGIENE", "Sabonete Líquido", 0, 15, "un");
        criarItemSeNaoExiste("HIGIENE", "Champô", 0, 8, "un");
        criarItemSeNaoExiste("HIGIENE", "Gel de Banho", 0, 8, "un");
        criarItemSeNaoExiste("HIGIENE", "Toalha", 0, 10, "un");
        criarItemSeNaoExiste("HIGIENE", "Sabonete/Creme", 0, 10, "un");
        criarItemSeNaoExiste("HIGIENE", "Toalhetes", 0, 30, "pk");
        criarItemSeNaoExiste("HIGIENE", "Fraldas Adulto", 0, 20, "pk");
        criarItemSeNaoExiste("HIGIENE", "Papel Higiénico", 0, 30, "rolos");

        // Calçado (tamanhos 35 a 46)
        for (int tamanho = 35; tamanho <= 46; tamanho++) {
            criarItemSeNaoExiste("CALCADO", String.valueOf(tamanho), 0, 3, "pares");
        }
        
        // Vestuário
        criarItemSeNaoExiste("VESTUARIO", "T-shirt/Camisola", 0, 5, "un");
        criarItemSeNaoExiste("VESTUARIO", "Calças", 0, 5, "un");
        criarItemSeNaoExiste("VESTUARIO", "Roupa Interior", 0, 10, "un");
        criarItemSeNaoExiste("VESTUARIO", "Meias", 0, 10, "pares");
        criarItemSeNaoExiste("VESTUARIO", "Agasalho/Casaco", 0, 3, "un");

        log.info("Dados padrão do armazém inicializados com sucesso.");
    }

    private void criarItemSeNaoExiste(String categoria, String nome, int quantidade, int minimo, String unidade) {
        if (itemArmazemRepository.findByCategoriaAndNome(categoria, nome).isEmpty()) {
            ItemArmazem item = new ItemArmazem();
            item.setCategoria(categoria);
            item.setNome(nome);
            item.setQuantidade(quantidade);
            item.setQuantidadeMinima(minimo);
            item.setUnidade(unidade);
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
        dto.setEstado(item.getQuantidade() >= item.getQuantidadeMinima() ? "OK" : "BAIXO");
        return dto;
    }
}
