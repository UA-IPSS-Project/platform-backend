package pt.florinhas.marcacoes.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO para criação de uma marcação associada ao Balneário.
 *
 * Usos típicos:
 *  - Recolher do frontend a data/hora do atendimento,
 *    o utente e o funcionário envolvidos, e opções de serviço (produtos/roupa).
 */
public class CriarBalnearioRequest {

    // Data civil do atendimento. 
    private LocalDate data;

    // Hora local do atendimento (slot). 
    private LocalTime hora;

    // Identificador do utente alvo do atendimento. 
    private Long utenteId;

    // Identificador do funcionário que cria/regista a marcação. 
    private Long funcionarioId;

    // Tipo de criador da marcação (ex.: "TECNICO" ou "RESPONSAVEL"). 
    private String tipoCriador; // TECNICO ou RESPONSAVEL

    // Indica se foram fornecidos produtos de higiene. 
    private Boolean produtosHigiene;

    // Indica se houve lavagem de roupa associada ao atendimento. 
    private Boolean lavagemRoupa;

    // Descrição livre sobre a roupa (ex.: peças, observações). 
    private String roupaDescricao;

    // ================= Getters e Setters =================

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