package pt.florinhas.marcacoes.dto;

import java.time.LocalDateTime;

public class ReagendarMarcacaoRequest {
    private LocalDateTime novaDataHora;

    public LocalDateTime getNovaDataHora() {
        return novaDataHora;
    }

    public void setNovaDataHora(LocalDateTime novaDataHora) {
        this.novaDataHora = novaDataHora;
    }
}
