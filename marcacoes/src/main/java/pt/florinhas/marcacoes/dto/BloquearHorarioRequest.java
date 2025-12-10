package pt.florinhas.marcacoes.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Data;

@Data
public class BloquearHorarioRequest {
    private LocalDate data;
    private LocalTime horaInicio; // Ex: 14:00
    private LocalTime horaFim;    // Ex: 16:00
    private String motivo;
    private Long funcionarioId;
}