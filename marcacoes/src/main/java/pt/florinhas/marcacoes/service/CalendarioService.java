package pt.florinhas.marcacoes.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pt.florinhas.marcacoes.domain.BloqueioAgenda;
import pt.florinhas.marcacoes.domain.EventoEstado;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.domain.Utilizador;
import pt.florinhas.marcacoes.dto.FeriadoDTO;
import pt.florinhas.marcacoes.exception.BadRequestException;
import pt.florinhas.marcacoes.repository.BloqueioRepository;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;

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
     * Endpoint público para feriados nacionais (Portugal).
     */
    private static final String API_PT_HOLIDAYS = "https://date.nager.at/api/v3/publicholidays/%d/PT";

    /**
     * Carrega os feriados do ano corrente no arranque da aplicação.
     *
     * É executado automaticamente após a injeção de dependências (@PostConstruct).
     * Em caso de falha da API externa, o sistema continua a funcionar
     * (apenas sem validação de feriados).
     */
    @PostConstruct
    public void carregarFeriados() {
        // Executar em thread separada para não bloquear o arranque
        new Thread(() -> {
            try {
                int currentYear = LocalDate.now().getYear();
                List<LocalDate> feriados = fetchFeriados(currentYear);
                feriadosCache.put(currentYear, feriados);
            } catch (Exception e) {
                // Em caso de erro, apenas regista no log
                log.error("Erro ao carregar feriados (API externa): {}", e.getMessage());
            }
        }).start();
    }

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
     *
     * Regras aplicadas:
     * - Dia inteiro indisponível (fim de semana ou feriado).
     * - Existência de um bloqueio que inclua o horário indicado.
     *
     * param data dia a verificar
     * param hora hora do slot (ex.: 14:00)
     * param tipo tipo de marcação a verificar
     * return true se o slot estiver bloqueado
     */
    public boolean isSlotBloqueado(LocalDate data, LocalTime hora, String tipo) {

        // Se o dia for indisponível por completo, o slot também é aka feriados e fins
        // de semana
        if (isDiaInteiroIndisponivel(data))
            return true;

        // Verificar bloqueios parciais do dia
        List<BloqueioAgenda> bloqueiosDoDia = bloqueioRepository.findByData(data);

        boolean bloqueadoPorAgenda = bloqueiosDoDia.stream()
                .anyMatch(b -> (hora.equals(b.getHoraInicio()) || hora.isAfter(b.getHoraInicio())) &&
                        hora.isBefore(b.getHoraFim()));

        if (bloqueadoPorAgenda)
            return true;

        // Verificar se já existe uma marcação ativa neste slot
        LocalDateTime slotStart = LocalDateTime.of(data, hora);
        LocalDateTime slotEnd = slotStart.plusMinutes(15);

        List<Marcacao> marcacoes = marcacaoRepository.findMarcacoesBetweenDates(slotStart, slotEnd, tipo);

        return marcacoes.stream()
                .anyMatch(m -> m.getEstado() != EventoEstado.CANCELADO &&
                        m.getData().isEqual(slotStart)); // Match exato no início do slot
    }

    /**
     * Indica se um dia está totalmente indisponível.
     *
     * Um dia é considerado indisponível se:
     * - For fim de semana
     * - For feriado nacional
     */
    private boolean isDiaInteiroIndisponivel(LocalDate data) {
        return isFimDeSemana(data) || getFeriadosDoAno(data.getYear()).contains(data);
    }

    /**
     * Cria um bloqueio de horário no calendário.
     *
     * Validações de negócio aplicadas:
     * 1) Não permitir datas no passado.
     * 2) Horário dentro do funcionamento (09:00–17:00).
     * 3) Hora de início anterior à hora de fim.
     * 4) Dia não pode ser feriado nem fim de semana.
     * 5) Não pode existir sobreposição com outros bloqueios.
     * 6) Não pode existir qualquer marcação ativa no intervalo.
     *
     * param data dia do bloqueio
     * param inicio hora de início
     * param fim hora de fim
     * param motivo motivo opcional do bloqueio
     * param funcionario utilizador que cria o bloqueio
     * return bloqueio persistido
     */
    @Transactional
    public BloqueioAgenda bloquearHorario(
            LocalDate data,
            LocalTime inicio,
            LocalTime fim,
            String motivo,
            Utilizador funcionario) {

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

        // Validação 5: Sobreposição com outros bloqueios
        // Usamos countConflictingWithLock para garantir serialização no DB
        if (bloqueioRepository.countConflictingWithLock(data, inicio, fim) > 0) {
            throw new BadRequestException("Já existe um bloqueio registado para este período (Conflito detetado).");
        }

        /**
         * Validação 6 (Regra de Ouro):
         * Não permitir bloqueios se existirem marcações ativas no intervalo.
         */
        LocalDateTime inicioBloqueio = LocalDateTime.of(data, inicio);
        LocalDateTime fimBloqueio = LocalDateTime.of(data, fim);

        List<Marcacao> marcacoesNoPeriodo = marcacaoRepository.findByDataBetween(inicioBloqueio, fimBloqueio);

        boolean temMarcacaoAtiva = marcacoesNoPeriodo.stream()
                .filter(m -> m.getEstado() != EventoEstado.CANCELADO)
                // Ignorar marcações que começam exatamente no fim do bloqueio (m.data ==
                // fimBloqueio)
                // O método findByDataBetween inclui os limites, mas queremos comportamento
                // [start, end[
                .anyMatch(m -> m.getData().isBefore(fimBloqueio));

        if (temMarcacaoAtiva) {
            throw new BadRequestException(
                    "Impossível bloquear: Existem marcações agendadas neste intervalo.");
        }

        // Criação e persistência do bloqueio
        BloqueioAgenda bloqueio = BloqueioAgenda.builder()
                .data(data)
                .horaInicio(inicio)
                .horaFim(fim)
                .motivo(motivo)
                .bloqueadoPor(funcionario)
                .build();

        return bloqueioRepository.save(bloqueio);
    }

    /**
     * Remove um bloqueio de agenda pelo seu ID.
     * param id identificador do bloqueio
     */
    public void removerBloqueio(Long id) {
        bloqueioRepository.deleteById(id);
    }

    /**
     * Obtém todos os bloqueios de um determinado mês.
     *
     * Utilizado pelo frontend para desenhar áreas indisponíveis
     * (ex.: caixas cinzentas no calendário).
     *
     * Nota:
     * - Atualmente filtra em memória.
     * - Poderia ser otimizado com um findByDataBetween no repositório.
     */
    public List<BloqueioAgenda> getBloqueiosDoMes(int ano, int mes) {
        LocalDate inicio = LocalDate.of(ano, mes, 1);
        LocalDate fim = inicio.withDayOfMonth(inicio.lengthOfMonth());

        return bloqueioRepository.findByDataBetween(inicio, fim);
    }

    /**
     * Obtém todos os bloqueios registados no sistema.
     * Útil para listagens de gestão.
     */
    public List<BloqueioAgenda> getTodosBloqueios() {
        return bloqueioRepository.findAll();
    }

    // Verifica se uma data corresponde a fim de semana.
    private boolean isFimDeSemana(LocalDate data) {
        DayOfWeek d = data.getDayOfWeek();
        return d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY;
    }
}
