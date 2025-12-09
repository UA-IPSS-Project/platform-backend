package pt.florinhas.marcacoes.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pt.florinhas.marcacoes.domain.AtendimentoTipo;
import pt.florinhas.marcacoes.domain.EventoEstado;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarcacaoResponseDTO {
    private Long id;
    private LocalDateTime data;
    private EventoEstado estado;
    private MarcacaoSecretariaDTO marcacaoSecretaria;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarcacaoSecretariaDTO {
        private String assunto;
        private String descricao;
        private AtendimentoTipo tipoAtendimento;
        private UtenteDTO utente;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UtenteDTO {
        private Long id;
        private String nome;
        private String email;
        private String nif;
        private String telefone;
    }
}
