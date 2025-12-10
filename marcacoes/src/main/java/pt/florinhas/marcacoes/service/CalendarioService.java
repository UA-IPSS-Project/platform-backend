package pt.florinhas.marcacoes.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

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

@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarioService {

    private final BloqueioRepository bloqueioRepository;
    private final MarcacaoRepository marcacaoRepository;
    
    // Limites de horário do sistema
    private static final LocalTime HORA_ABERTURA = LocalTime.of(9, 0);
    private static final LocalTime HORA_FECHO = LocalTime.of(17, 0); 

    private final List<LocalDate> feriadosCache = new ArrayList<>();
    private static final String API_URL_TEMPLATE = "https://date.nager.at/api/v3/publicholidays/%d/PT";

    @PostConstruct
    public void carregarFeriados() {
        try {
            int currentYear = LocalDate.now().getYear();
            String url = String.format(API_URL_TEMPLATE, currentYear);
            RestTemplate restTemplate = new RestTemplate();
            FeriadoDTO[] response = restTemplate.getForObject(url, FeriadoDTO[].class);
            if (response != null) {
                feriadosCache.clear();
                for (FeriadoDTO h : response) {
                    feriadosCache.add(LocalDate.parse(h.getDate()));
                }
            }
        } catch (RestClientException e) {
            log.error("Erro feriados: {}", e.getMessage());
        }
    }

    public boolean isSlotBloqueado(LocalDate data, LocalTime slotTime) {
        if (isDiaInteiroIndisponivel(data)) return true;

        List<BloqueioAgenda> bloqueiosDoDia = bloqueioRepository.findByData(data);
        
        return bloqueiosDoDia.stream().anyMatch(b -> 
            (slotTime.equals(b.getHoraInicio()) || slotTime.isAfter(b.getHoraInicio())) && 
            slotTime.isBefore(b.getHoraFim())
        );
    }

private boolean isDiaInteiroIndisponivel(LocalDate data) {
    return isFimDeSemana(data) || feriadosCache.contains(data);
}

public BloqueioAgenda bloquearHorario(LocalDate data, LocalTime inicio, LocalTime fim, String motivo, Utilizador funcionario) {
        
        // Validação 1: Data Futura
        if (data.isBefore(LocalDate.now())) {
            throw new BadRequestException("Não é possível bloquear datas no passado.");
        }

        // Validação 2: Limites de Horário (09:00 - 17:00)
        if (inicio.isBefore(HORA_ABERTURA) || fim.isAfter(HORA_FECHO)) {
            throw new BadRequestException("O bloqueio deve estar dentro do horário de funcionamento (09:00 às 17:00).");
        }
        if (!inicio.isBefore(fim)) {
            throw new BadRequestException("A hora de início deve ser anterior à hora de fim.");
        }

        // Validação 3: Feriados/Fim de Semana
        if (isDiaInteiroIndisponivel(data)) {
            throw new BadRequestException("Este dia já é um Feriado ou Fim de Semana.");
        }

        // Validação 4: Sobreposição com outros bloqueios
        if (bloqueioRepository.existeSobreposicao(data, inicio, fim)) {
            throw new BadRequestException("Já existe um bloqueio registado para este período.");
        }

        // Validação 5: Sobreposição com MARCAÇÕES existentes (A Regra de Ouro)
        LocalDateTime inicioBloqueio = LocalDateTime.of(data, inicio);
        LocalDateTime fimBloqueio = LocalDateTime.of(data, fim);

        // No repository: findByDataBetweenAndEstadoNot...
        List<Marcacao> marcacoesNoPeriodo = marcacaoRepository.findByDataBetween(inicioBloqueio, fimBloqueio);
        
        boolean temMarcacaoAtiva = marcacoesNoPeriodo.stream().anyMatch(m -> m.getEstado() != EventoEstado.CANCELADO);

        if (temMarcacaoAtiva) {
            throw new BadRequestException("Impossível bloquear: Existem marcações agendadas neste intervalo.");
        }

        // Criar Bloqueio
        BloqueioAgenda bloqueio = BloqueioAgenda.builder()
                .data(data)
                .horaInicio(inicio)
                .horaFim(fim)
                .motivo(motivo)
                .bloqueadoPor(funcionario)
                .build();

        return bloqueioRepository.save(bloqueio);
    }

    public void removerBloqueio(Long id) {
        bloqueioRepository.deleteById(id);
    }
    
    // Método auxiliar para obter lista de bloqueios para o frontend desenhar áreas cinzentas
    public List<BloqueioAgenda> getBloqueiosDoMes(int ano, int mes) {
        LocalDate inicio = LocalDate.of(ano, mes, 1);
        LocalDate fim = inicio.withDayOfMonth(inicio.lengthOfMonth());
        // Aqui terias de ter um findByDataBetween no BloqueioRepository, 
        // ou filtrar em memória se forem poucos. Vou simplificar:
        return bloqueioRepository.findAll().stream()
                .filter(b -> !b.getData().isBefore(inicio) && !b.getData().isAfter(fim))
                .toList();
    }

    private boolean isFimDeSemana(LocalDate data) {
        DayOfWeek d = data.getDayOfWeek();
        return d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY;
    }
}