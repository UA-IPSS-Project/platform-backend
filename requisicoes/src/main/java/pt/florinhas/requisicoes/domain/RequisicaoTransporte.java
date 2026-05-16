package pt.florinhas.requisicoes.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Requisicao_Transporte")
@PrimaryKeyJoinColumn(name = "requisicao_id")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RequisicaoTransporte extends Requisicao {

    @Column(nullable = true, length = 255)
    private String destino;

    @Column(name = "data_hora_saida", nullable = false)
    private LocalDateTime dataHoraSaida;

    @Column(name = "data_hora_regresso", nullable = false)
    private LocalDateTime dataHoraRegresso;

    @Column(name = "numero_passageiros", nullable = false)
    private Integer numeroPassageiros;

    @Column(length = 120)
    private String condutor;

    @ManyToOne
    @JoinColumn(name = "transporte_id", nullable = false)
    private Transporte transporte;

    @OneToMany(mappedBy = "requisicao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RequisicaoTransporteItem> transportes = new ArrayList<>();
}