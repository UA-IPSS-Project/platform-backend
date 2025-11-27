package pt.florinhas.marcacoes.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class CriarMarcacaoRequest {
    private LocalDate data;
    private LocalTime hora;
    private String tipoAtendimento;
    private Long utenteId;
    private Long funcionarioId;
    private Long valenciaId;
    private Long criadoPorId;

    // Getters e Setters
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }

    public String getTipoAtendimento() { return tipoAtendimento; }
    public void setTipoAtendimento(String tipoAtendimento) { this.tipoAtendimento = tipoAtendimento; }

    public Long getUtenteId() { return utenteId; }
    public void setUtenteId(Long utenteId) { this.utenteId = utenteId; }

    public Long getFuncionarioId() { return funcionarioId; }
    public void setFuncionarioId(Long funcionarioId) { this.funcionarioId = funcionarioId; }

    public Long getValenciaId() { return valenciaId; }
    public void setValenciaId(Long valenciaId) { this.valenciaId = valenciaId; }

    public Long getCriadoPorId() { return criadoPorId; }
    public void setCriadoPorId(Long criadoPorId) { this.criadoPorId = criadoPorId; }
}