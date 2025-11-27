package pt.florinhas.marcacoes.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class CriarBalnearioRequest {
    private LocalDate data;
    private LocalTime hora;
    private Long utenteId;
    private Long funcionarioId;
    private String tipoCriador; // TECNICO ou RESPONSAVEL
    private Boolean produtosHigiene;
    private Boolean lavagemRoupa;
    private String roupaDescricao;

    // Getters e Setters
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }

    public Long getUtenteId() { return utenteId; }
    public void setUtenteId(Long utenteId) { this.utenteId = utenteId; }

    public Long getFuncionarioId() { return funcionarioId; }
    public void setFuncionarioId(Long funcionarioId) { this.funcionarioId = funcionarioId; }

    public String getTipoCriador() { return tipoCriador; }
    public void setTipoCriador(String tipoCriador) { this.tipoCriador = tipoCriador; }

    public Boolean getProdutosHigiene() { return produtosHigiene; }
    public void setProdutosHigiene(Boolean produtosHigiene) { this.produtosHigiene = produtosHigiene; }

    public Boolean getLavagemRoupa() { return lavagemRoupa; }
    public void setLavagemRoupa(Boolean lavagemRoupa) { this.lavagemRoupa = lavagemRoupa; }

    public String getRoupaDescricao() { return roupaDescricao; }
    public void setRoupaDescricao(String roupaDescricao) { this.roupaDescricao = roupaDescricao; }
}