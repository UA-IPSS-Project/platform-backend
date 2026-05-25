package pt.florinhas.marcacoes.dto;

import pt.florinhas.marcacoes.domain.BloqueioAgenda;

public record BloqueioAgendaDTO(
        Long id,
        String data,
        String horaInicio,
        String horaFim,
        String motivo,
        String tipo) {

    public static BloqueioAgendaDTO from(BloqueioAgenda b) {
        return new BloqueioAgendaDTO(
                b.getId(),
                b.getData().toString(),
                b.getHoraInicio().toString(),
                b.getHoraFim().toString(),
                b.getMotivo(),
                b.getTipo());
    }
}
