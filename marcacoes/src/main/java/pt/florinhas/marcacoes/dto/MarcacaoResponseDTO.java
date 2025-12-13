package pt.florinhas.marcacoes.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pt.florinhas.marcacoes.domain.AtendimentoTipo;
import pt.florinhas.marcacoes.domain.EventoEstado;

/**
 * DTO de resposta para expor marcações ao frontend.
 *
 * Estrutura:
 *  - Dados da própria marcação (id, version, data, estado, atendenteNome).
 *  - Subdocumento opcional com detalhes de secretaria (MarcacaoSecretariaDTO),
 *    incluindo identificação do utente.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarcacaoResponseDTO {

    // Identificador único da marcação. 
    private Long id;

    // Versão do registo (para controlo de concorrência otimista no cliente). 
    private Long version;

    //  Instante agendado (data/hora local) da marcação. 
    private LocalDateTime data;

    // Estado atual do ciclo de vida da marcação. 
    private EventoEstado estado;

    // Nome do funcionário que atendeu/concluiu a marcação (se aplicável). 
    private String atendenteNome; // Nome do funcionário que atendeu

    // Detalhes específicos do fluxo de secretaria, se existirem. 
    private MarcacaoSecretariaDTO marcacaoSecretaria;

    /**
     * Subdocumento com metadados de secretaria associados à marcação.
     * Reflete campos da entidade MarcacaoSecretaria, já preparados para o frontend.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarcacaoSecretariaDTO {

        // Assunto curto da marcação. 
        private String assunto;

        // Descrição detalhada do atendimento. 
        private String descricao;

        // Tipo de atendimento: PRESENCIAL ou REMOTO. 
        private AtendimentoTipo tipoAtendimento;

        // Dados resumidos do utente associado. 
        private UtenteDTO utente;
    }

    /**
     * Subdocumento resumido com os dados públicos do utente ligados à marcação.
     * Útil para renderização direta em listas e detalhes sem novas chamadas.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UtenteDTO {

        // Identificador do utente. 
        private Long id;

        // Nome do utente. 
        private String nome;

        // Email do utente. 
        private String email;

        // NIF do utente. 
        private String nif;

        // Telefone de contacto do utente. 
        private String telefone;
    }
}
