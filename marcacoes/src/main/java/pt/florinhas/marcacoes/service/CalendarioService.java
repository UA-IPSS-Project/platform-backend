package pt.florinhas.marcacoes.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import pt.florinhas.marcacoes.domain.BloqueioAgenda;
import pt.florinhas.marcacoes.domain.ConfiguracaoAgenda;
import pt.florinhas.marcacoes.domain.EventoEstado;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.dto.ConfiguracaoSlotDTO;
import pt.florinhas.marcacoes.dto.FeriadoDTO;
import pt.florinhas.marcacoes.repository.BloqueioRepository;
import pt.florinhas.marcacoes.repository.ConfiguracaoAgendaRepository;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;

import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.common_data.exception.BadRequestException;

/**
 * Serviço responsável pela gestão do calendário e disponibilidade.
 *
 * Responsabilidades principais:
 * - Gestão de bloqueios de agenda (parciais ou de dia inteiro).
 * - Verificação de disponibilidade de slots horários.
 * - Integração com API externa de feriados nacionais.
 * - Aplicação das regras de negócio de calendário:
 * Horário de funcionamento
 * Fins de semana
 * Feriados
 * Sobreposição de bloqueios
 * Conflitos com marcações existentes
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarioService {

    private final BloqueioRepository bloqueioRepository;
    private final MarcacaoRepository marcacaoRepository;
    private final ConfiguracaoAgendaRepository configuracaoAgendaRepository;
    private final NotificacaoService notificacaoService;
    private final AuditLogService auditLogService;

    /**
     * Limites de horário do sistema.
     * Todos os bloqueios e marcações devem respeitar este intervalo.
     */
    private static final LocalTime HORA_ABERTURA = LocalTime.of(9, 0);
    private static final LocalTime HORA_FECHO = LocalTime.of(17, 0);

    /**
     * Cache em memória dos feriados nacionais.
     * Evita chamadas repetidas à API externa.
     */
    private final Map<Integer, List<LocalDate>> feriadosCache = new ConcurrentHashMap<>();

    /**
     * Cache em memória da capacidade por slot.
     * @Cacheable não funciona em self-invocation (proxy AOP é ignorado),
     * por isso usamos ConcurrentHashMap diretamente — mesmo padrão dos feriados.
     * Invalidado explicitamente em atualizarCapacidadePorSlot.
     */
    private final Map<String, Integer> capacidadeCache = new ConcurrentHashMap<>();

    /**
     * Endpoint público para feriados nacionais (Portugal).
     */
    private static final String API_PT_HOLIDAYS = "https://date.nager.at/api/v3/publicholidays/%d/PT";
    private static final int CAPACIDADE_SLOT_DEFAULT_SECRETARIA = 1;
    private static final int CAPACIDADE_SLOT_DEFAULT_BALNEARIO = 2;
    private static final String TIPO_SECRETARIA = "SECRETARIA";
    private static final String TIPO_BALNEARIO = "BALNEARIO";

    private int capacidadeSlotDefault(String tipo) {
        return TIPO_BALNEARIO.equals(tipo) ? CAPACIDADE_SLOT_DEFAULT_BALNEARIO : CAPACIDADE_SLOT_DEFAULT_SECRETARIA;
    }

    /**
     * Carrega os feriados do ano corrente no arranque da aplicação.
     */
    @PostConstruct
    public void carregarFeriados() {
        new Thread(() -> {
            try {
                int currentYear = LocalDate.now().getYear();
                List<LocalDate> feriados = fetchFeriados(currentYear);
                feriadosCache.put(currentYear, feriados);
            } catch (Exception e) {
                log.error("Erro ao carregar feriados (API externa): {}", e.getMessage());
            }
        }).start();
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void inicializarConfiguracoes() {
        inicializarTipoSeNaoExistir(TIPO_SECRETARIA, CAPACIDADE_SLOT_DEFAULT_SECRETARIA);
        inicializarTipoSeNaoExistir(TIPO_BALNEARIO, CAPACIDADE_SLOT_DEFAULT_BALNEARIO);
    }

    private void inicializarTipoSeNaoExistir(String tipo, int capacidade) {
        if (configuracaoAgendaRepository.findByTipo(tipo).isEmpty()) {
            try {
                ConfiguracaoAgenda cfg = new ConfiguracaoAgenda();
                cfg.setTipo(tipo);
                cfg.setCapacidadePorSlot(capacidade);
                configuracaoAgendaRepository.save(cfg);
                log.info("Configuração para {} inicializada com capacidade {}.", tipo, capacidade);
            } catch (Exception e) {
                log.info("Configuração para {} já foi inicializada por outra réplica.", tipo);
            }
        }
    }

    @Cacheable(value = "feriados", key = "#ano")
    public List<LocalDate> getFeriadosDoAno(int ano) {
        return feriadosCache.computeIfAbsent(ano, this::fetchFeriados);
    }

    private List<LocalDate> fetchFeriados(int ano) {
        try {
            String url = String.format(API_PT_HOLIDAYS, ano);
            RestTemplate restTemplate = new RestTemplate();
            FeriadoDTO[] response = restTemplate.getForObject(url, FeriadoDTO[].class);

            if (response == null) {
                return List.of();
            }

            List<LocalDate> result = new ArrayList<>();
            for (FeriadoDTO h : response) {
                result.add(LocalDate.parse(h.getDate()));
            }
            return result;
        } catch (Exception e) {
            log.error("Erro ao obter feriados (API externa) para ano {}: {}", ano, e.getMessage());
            return List.of();
        }
    }

    /**
     * Verifica se um slot horário específico está bloqueado.
     * Filtra bloqueios pelo tipo de agenda (SECRETARIA / BALNEARIO).
     */
    public boolean isSlotBloqueado(LocalDate data, LocalTime hora, String tipo) {

        String tipoNormalizado = normalizarTipo(tipo);

        // Se o dia for indisponível por completo (feriados e fins de semana)
        if (isDiaInteiroIndisponivel(data))
            return true;

        // Verificar bloqueios parciais do dia — filtrados por tipo
        List<BloqueioAgenda> bloqueiosDoDia;
        if (tipoNormalizado != null && !tipoNormalizado.isEmpty()) {
            bloqueiosDoDia = bloqueioRepository.findByDataAndTipo(data, tipoNormalizado);
        } else {
            bloqueiosDoDia = bloqueioRepository.findByData(data);
        }

        boolean bloqueadoPorAgenda = bloqueiosDoDia.stream()
                .anyMatch(b -> (hora.equals(b.getHoraInicio()) || hora.isAfter(b.getHoraInicio())) &&
                        hora.isBefore(b.getHoraFim()));

        if (bloqueadoPorAgenda)
            return true;

        // Verificar capacidade dinâmica de marcações ativas neste slot
        LocalDateTime slotStart = LocalDateTime.of(data, hora);
        int capacidade = getCapacidadePorSlot(tipoNormalizado);
        long emUso = marcacaoRepository.countByDataAndTipo(slotStart, tipoNormalizado);

        return emUso >= capacidade;
    }

    public int getCapacidadePorSlot(String tipo) {
        String tipoNormalizado = normalizarTipo(tipo);
        if (tipoNormalizado == null) {
            return CAPACIDADE_SLOT_DEFAULT_SECRETARIA;
        }

        return capacidadeCache.computeIfAbsent(tipoNormalizado, t ->
                configuracaoAgendaRepository.findByTipo(t)
                        .map(ConfiguracaoAgenda::getCapacidadePorSlot)
                        .orElse(capacidadeSlotDefault(t)));
    }

    public int previewReducaoCapacidade(String tipo, int novaCapacidade) {
        String tipoNormalizado = normalizarTipoObrigatorio(tipo);
        int atual = getCapacidadePorSlot(tipoNormalizado);
        if (novaCapacidade >= atual) return 0;

        LocalDateTime agora = LocalDateTime.now();
        List<Marcacao> futuras = marcacaoRepository.findActiveFutureMarcacoes(agora, tipoNormalizado);

        return (int) futuras.stream()
                .collect(java.util.stream.Collectors.groupingBy(Marcacao::getData))
                .values().stream()
                .mapToLong(list -> Math.max(0, list.size() - novaCapacidade))
                .sum();
    }

    @Transactional
    @CacheEvict(value = "config-slots", allEntries = true)
    public ConfiguracaoSlotDTO atualizarCapacidadePorSlot(String tipo, Integer capacidadePorSlot) {
        String tipoNormalizado = normalizarTipoObrigatorio(tipo);

        if (capacidadePorSlot == null || capacidadePorSlot < 1) {
            throw new BadRequestException("A capacidade por slot deve ser um número inteiro maior ou igual a 1.");
        }

        ConfiguracaoAgenda cfg = configuracaoAgendaRepository.findByTipo(tipoNormalizado)
                .orElseGet(() -> {
                    ConfiguracaoAgenda created = new ConfiguracaoAgenda();
                    created.setTipo(tipoNormalizado);
                    return created;
                });

        int oldCapacidade = getCapacidadePorSlot(tipoNormalizado);

        cfg.setCapacidadePorSlot(capacidadePorSlot);
        ConfiguracaoAgenda saved = configuracaoAgendaRepository.save(cfg);
        
        capacidadeCache.put(tipoNormalizado, capacidadePorSlot);

        if (capacidadePorSlot < oldCapacidade) {
            cancelarMarcacoesExcedentesNoFuturo(tipoNormalizado, oldCapacidade, capacidadePorSlot);
        }

        return new ConfiguracaoSlotDTO(saved.getTipo(), saved.getCapacidadePorSlot());
    }

    private void cancelarMarcacoesExcedentesNoFuturo(String tipo, int oldCapacidade, int novaCapacidade) {
        LocalDateTime agora = LocalDateTime.now();
        List<Marcacao> activeFutureMarcacoes = marcacaoRepository.findActiveFutureMarcacoes(agora, tipo);

        // Group by LocalDateTime of the slot
        java.util.Map<LocalDateTime, List<Marcacao>> bookingsBySlot = activeFutureMarcacoes.stream()
                .collect(java.util.stream.Collectors.groupingBy(Marcacao::getData));

        for (java.util.Map.Entry<LocalDateTime, List<Marcacao>> entry : bookingsBySlot.entrySet()) {
            List<Marcacao> bookingsInSlot = entry.getValue();
            if (bookingsInSlot.size() > novaCapacidade) {
                int excessCount = bookingsInSlot.size() - novaCapacidade;

                // Sort bookings by criadoEm descending (newest first), so we cancel the newest bookings first
                bookingsInSlot.sort((m1, m2) -> {
                    LocalDateTime c1 = m1.getCriadoEm();
                    LocalDateTime c2 = m2.getCriadoEm();
                    if (c1 != null && c2 != null) {
                        return c2.compareTo(c1);
                    }
                    if (m1.getId() != null && m2.getId() != null) {
                        return m2.getId().compareTo(m1.getId());
                    }
                    return 0;
                });

                String motivo = "Cancelada devido à redução de capacidade máxima de vagas de " + oldCapacidade + " para " + novaCapacidade + ".";

                for (int i = 0; i < excessCount; i++) {
                    Marcacao m = bookingsInSlot.get(i);
                    m.setEstado(EventoEstado.CANCELADO);
                    m.setMotivoCancelamento(motivo);
                    marcacaoRepository.save(m);

                    auditLogService.log(
                        "ATUALIZAR_ESTADO_MARCACAO",
                        "MARCACAO",
                        m.getId(),
                        "Cancelamento automático devido a redução de capacidade do slot. Motivo: " + motivo
                    );

                    // Notify the utente / creator
                    Long utenteId = null;
                    if (m.getMarcacaoSecretaria() != null && m.getMarcacaoSecretaria().getUtente() != null) {
                        utenteId = m.getMarcacaoSecretaria().getUtente().getId();
                    } else if (m.getCriadoPor() != null) {
                        utenteId = m.getCriadoPor().getId();
                    }

                    if (utenteId != null) {
                        try {
                            notificacaoService.notificarCancelamento(utenteId, m.getData(), motivo);
                        } catch (Exception e) {
                            log.error("Erro ao enviar notificação de cancelamento automático para o utilizador {}", utenteId, e);
                        }
                    }
                }
            }
        }
    }

    @Cacheable("config-slots")
    public List<ConfiguracaoSlotDTO> listarConfiguracoesSlot() {
        return List.of(
                new ConfiguracaoSlotDTO(TIPO_SECRETARIA, getCapacidadePorSlot(TIPO_SECRETARIA)),
                new ConfiguracaoSlotDTO(TIPO_BALNEARIO, getCapacidadePorSlot(TIPO_BALNEARIO)));
    }

    /**
     * Indica se um dia está totalmente indisponível.
     */
    private boolean isDiaInteiroIndisponivel(LocalDate data) {
        return isFimDeSemana(data) || getFeriadosDoAno(data.getYear()).contains(data);
    }

    /**
     * Cria um bloqueio de horário no calendário.
     * Filtra verificações de sobreposição pelo tipo de agenda.
     */
    @Transactional
    public BloqueioAgenda bloquearHorario(
            LocalDate data,
            LocalTime inicio,
            LocalTime fim,
            String motivo,
            Utilizador funcionario,
            String tipo) {

        // Validação 1: Data no futuro
        if (data.isBefore(LocalDate.now())) {
            throw new BadRequestException("Não é possível bloquear datas no passado.");
        }

        // Validação 2: Limites de horário
        if (inicio.isBefore(HORA_ABERTURA) || fim.isAfter(HORA_FECHO)) {
            throw new BadRequestException(
                    "O bloqueio deve estar dentro do horário de funcionamento (09:00 às 17:00).");
        }

        // Validação 3: Ordem temporal
        if (!inicio.isBefore(fim)) {
            throw new BadRequestException("A hora de início deve ser anterior à hora de fim.");
        }

        // Validação 4: Feriados e fins de semana
        if (isDiaInteiroIndisponivel(data)) {
            throw new BadRequestException("Este dia já é um Feriado ou Fim de Semana.");
        }

        // Validação 5: Sobreposição com outros bloqueios do mesmo tipo
        if (bloqueioRepository.countConflictingWithLockByTipo(data, inicio, fim, tipo) > 0) {
            throw new BadRequestException("Já existe um bloqueio registado para este período (Conflito detetado).");
        }

        // Validação 6: Não permitir bloqueios se existirem marcações ativas no
        // intervalo
        LocalDateTime inicioBloqueio = LocalDateTime.of(data, inicio);
        LocalDateTime fimBloqueio = LocalDateTime.of(data, fim);

        List<Marcacao> marcacoesNoPeriodo = marcacaoRepository.findMarcacoesBetweenDates(inicioBloqueio, fimBloqueio,
                tipo);

        boolean temMarcacaoAtiva = marcacoesNoPeriodo.stream()
                .filter(m -> m.getEstado() != EventoEstado.CANCELADO)
                .anyMatch(m -> m.getData().isBefore(fimBloqueio));

        if (temMarcacaoAtiva) {
            throw new BadRequestException(
                    "Impossível bloquear: Existem marcações agendadas neste intervalo.");
        }

        // Criação e persistência do bloqueio com tipo
        BloqueioAgenda bloqueio = BloqueioAgenda.builder()
                .data(data)
                .horaInicio(inicio)
                .horaFim(fim)
                .motivo(motivo)
                .bloqueadoPor(funcionario)
                .tipo(tipo)
                .build();

        return bloqueioRepository.save(bloqueio);
    }

    /**
     * Remove um bloqueio de agenda pelo seu ID.
     */
    public void removerBloqueio(Long id) {
        bloqueioRepository.deleteById(id);
    }

    /**
     * Obtém bloqueios de um determinado mês, filtrados por tipo.
     */
    public List<BloqueioAgenda> getBloqueiosDoMes(int ano, int mes, String tipo) {
        LocalDate inicio = LocalDate.of(ano, mes, 1);
        LocalDate fim = inicio.withDayOfMonth(inicio.lengthOfMonth());

        if (tipo != null && !tipo.isEmpty()) {
            return bloqueioRepository.findByDataBetweenAndTipo(inicio, fim, tipo);
        }
        return bloqueioRepository.findByDataBetween(inicio, fim);
    }

    /**
     * Obtém todos os bloqueios, opcionalmente filtrados por tipo.
     */
    public List<BloqueioAgenda> getTodosBloqueios(String tipo) {
        if (tipo != null && !tipo.isEmpty()) {
            return bloqueioRepository.findByTipo(tipo);
        }
        return bloqueioRepository.findAll();
    }

    // Verifica se uma data corresponde a fim de semana.
    private boolean isFimDeSemana(LocalDate data) {
        DayOfWeek d = data.getDayOfWeek();
        return d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY;
    }

    private String normalizarTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            return TIPO_SECRETARIA;
        }

        String normalizado = tipo.trim().toUpperCase();
        if (TIPO_SECRETARIA.equals(normalizado) || TIPO_BALNEARIO.equals(normalizado)) {
            return normalizado;
        }

        return null;
    }

    private String normalizarTipoObrigatorio(String tipo) {
        String normalizado = normalizarTipo(tipo);
        if (normalizado == null) {
            throw new BadRequestException("Tipo de agenda inválido. Use SECRETARIA ou BALNEARIO.");
        }
        return normalizado;
    }
}
