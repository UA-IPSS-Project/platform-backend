package pt.florinhas.marcacoes.dto;

import lombok.Data;

@Data
public class SendReportRequest {
    private String to;
    private String subject;
    private String body;
}
