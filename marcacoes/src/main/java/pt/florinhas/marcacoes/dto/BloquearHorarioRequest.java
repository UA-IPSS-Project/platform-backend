package pt.florinhas.marcacoes.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Data;

/**
 * DTO usado para solicitar o bloqueio de um intervalo horário na agenda.
 *
 * Usos típicos:
 * - Criar bloqueios parciais (horaInicio/horaFim) ou de dia completo
 * (convenção: horaInicio=00:00 e horaFim=23:59, conforme a regra do serviço).
 */
@Data
public class BloquearHorarioRequest {

    // Data civil do bloqueio (YYYY-MM-DD). Obrigatória.
    private LocalDate data;

    // Hora local de início do bloqueio (ex.: 14:00). Obrigatória para bloqueio
    // parcial.
    private LocalTime horaInicio; // Ex: 14:00

    // Hora local de fim do bloqueio (ex.: 16:00). Obrigatória para bloqueio
    // parcial.
    private LocalTime horaFim; // Ex: 16:00

    // Motivo/justificação do bloqueio (texto livre, opcional).
    private String motivo;

    // Identificador do funcionário/admin que regista o bloqueio.
    private Long funcionarioId;

    // Tipo de agenda: "SECRETARIA" ou "BALNEARIO" (default: SECRETARIA).
    private String tipo = "SECRETARIA";
}
