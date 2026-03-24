package pt.florinhas.marcacoes.validation;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pt.florinhas.marcacoes.dto.CriarMarcacaoBalnearioRequest;
import pt.florinhas.marcacoes.dto.CriarMarcacaoRequest;
import pt.florinhas.marcacoes.dto.ReagendarMarcacaoRequest;
import pt.florinhas.marcacoes.service.CalendarioService;

/**
 * Validador de regras de negócio para marcações.
 * 
 * Responsável por validar:
 * - Datas e horários (dentro de um intervalo válido, não no passado)
 * - Campos obrigatórios e tamanho
 * - Regras de agendamento (slots disponíveis, conflitos)
 * - Bloqueios de agenda, feriados e fins de semana
 * - IDs de utilizadores e funcionários
 */
@Component
@RequiredArgsConstructor
public class MarcacaoValidator {

    private final CalendarioService calendarioService;

    // Constantes de validação
    private static final long DIAS_ANTECEDENCIA_MAXIMA = 365;

    /**
     * Valida uma requisição de criação de marcação.
     * 
     * @param request DTO com dados da marcação
     * @throws IllegalArgumentException se a validação falhar
     */
    public void validarCriacao(CriarMarcacaoRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Requisição de criação de marcação não pode ser nula.");
        }

        // Validar data, hora e sobreposição
        validarDataHora(request.getData(), "SECRETARIA");

        // Validar campos de texto
        validarAssunto(request.getAssunto());
    }

    public void validarCriacaoBalneario(CriarMarcacaoBalnearioRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Requisição de criação de marcação de balneário não pode ser nula.");
        }

        validarDataHora(request.getData(), "BALNEARIO");
    }

    /**
     * Valida uma requisição de reagendamento de marcação.
     * 
     * Realiza as mesmas validações que criação:
     * - Data/hora (passado, máximo 365 dias, fins de semana, feriados, bloqueios,
     * sobreposição)
     * 
     * @param request       DTO com dados do reagendamento
     * @param funcionarioId ID do funcionário responsável (para verificar
     *                      sobreposição)
     * @throws IllegalArgumentException se a validação falhar
     */
    public void validarReagendamento(ReagendarMarcacaoRequest request, String tipoAgenda) {
        if (request == null) {
            throw new IllegalArgumentException("Requisição de reagendamento não pode ser nula.");
        }

        if (request.getNovaDataHora() == null) {
            throw new IllegalArgumentException("A nova data e hora são obrigatórias.");
        }

        // Validar nova data/hora com mesmas regras de criação (incluindo sobreposição
        // com mesmo funcionário)
        validarDataHora(request.getNovaDataHora(), tipoAgenda);
    }

    /**
     * Valida data e hora da marcação.
     * 
     * Regras verificadas:
     * - Não pode ser no passado
     * - Não pode estar marcado para mais de 1 ano no futuro
     * - Não pode ser em fim de semana (sábado ou domingo)
     * - Não pode ser em feriado nacional
     * - Não pode ser bloqueado manualmente (bloqueios de agenda)
     * - Não pode haver sobreposição com outras marcações confirmadas
     * 
     * @param dataHora Data e hora a validar
     * @param request  The request context
     * @throws IllegalArgumentException se a validação falhar
     */
    private void validarDataHora(LocalDateTime dataHora, String tipoAgenda) {
        if (dataHora == null) {
            throw new IllegalArgumentException("A data e hora da marcação são obrigatórias.");
        }

        LocalDateTime agora = LocalDateTime.now();

        // Verificar se está no passado
        if (dataHora.isBefore(agora)) {
            throw new IllegalArgumentException("A data e hora da marcação não podem estar no passado.");
        }

        // Verificar limite máximo de dias no futuro
        long diasAteAgendamento = ChronoUnit.DAYS.between(agora, dataHora);
        if (diasAteAgendamento > DIAS_ANTECEDENCIA_MAXIMA) {
            throw new IllegalArgumentException(
                    String.format("A marcação não pode ser agendada para mais de %d dias no futuro.",
                            DIAS_ANTECEDENCIA_MAXIMA));
        }

        // Extrair data e hora para validações adicionais
        LocalDate data = dataHora.toLocalDate();
        LocalTime hora = dataHora.toLocalTime();

        // Verificar se é fim de semana
        if (isFimDeSemana(data)) {
            throw new IllegalArgumentException(
                    "A marcação não pode ser agendada para fim de semana (sábado ou domingo).");
        }

        // Verificar se é feriado
        if (isFeriado(data)) {
            throw new IllegalArgumentException(
                    "A marcação não pode ser agendada para um feriado nacional.");
        }

        // Verificar bloqueios manuais e dinâmicos (agenda cheia)
        if (existeBloqueio(data, hora, tipoAgenda)) {
            throw new IllegalArgumentException(
                    "O horário escolhido está bloqueado ou preenchido. Por favor, escolha outro horário.");
        }
    }

    /**
     * Valida o assunto da marcação.
     * 
     * @param assunto Assunto a validar
     * @throws IllegalArgumentException se a validação falhar
     */
    private void validarAssunto(String assunto) {
        if (assunto == null || assunto.trim().isBlank()) {
            throw new IllegalArgumentException("O assunto da marcação é obrigatório.");
        }
    }

    /**
     * Verifica se uma data é fim de semana (sábado ou domingo).
     * 
     * @param data Data a verificar
     * @return true se for sábado ou domingo
     */
    private boolean isFimDeSemana(LocalDate data) {
        DayOfWeek dayOfWeek = data.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    /**
     * Verifica se uma data é feriado nacional.
     * 
     * @param data Data a verificar
     * @return true se for feriado
     */
    private boolean isFeriado(LocalDate data) {
        return calendarioService.getFeriadosDoAno(data.getYear()).contains(data);
    }

    /**
     * Verifica se existe bloqueio para uma data e hora específica.
     * 
     * @param data   Data a verificar
     * @param hora   Hora a verificar
     * @param record O request que contém os dados da marcação para identificar tipo
     * @return true se existir bloqueio
     */
    private boolean existeBloqueio(LocalDate data, LocalTime hora, String tipoAgenda) {
        return calendarioService.isSlotBloqueado(data, hora, tipoAgenda);
    }

}
