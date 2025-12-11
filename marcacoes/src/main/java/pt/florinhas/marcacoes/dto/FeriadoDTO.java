package pt.florinhas.marcacoes.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeriadoDTO {
    private String date;
    private String localName;
}