package pt.florinhas.marcacoes.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * DTO simples para representar um feriado (tipicamente vindo de uma API externa).
 *
 * Notas:
 *  - @JsonIgnoreProperties(ignoreUnknown = true) permite ignorar campos extra no JSON
 *    que não estejam mapeados aqui, tornando a desserialização resiliente a mudanças.
 *  - @Data (Lombok) gera getters/setters, equals, hashCode e toString automaticamente.
 *
 * Campos:
 *  - date: data do feriado (formato conforme a origem, p.ex. "YYYY-MM-DD").
 *  - localName: designação/local do feriado (ex.: "Carnaval", "Natal").
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeriadoDTO {
    // Data do feriado, tal como fornecida pela fonte externa (ex.: "2025-12-25").
    private String date;
    // Nome/local do feriado na língua/localização da fonte (ex.: "Natal"). 
    private String localName;
}
