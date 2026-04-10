package pt.florinhas.api_gateway.dto;

import lombok.Data;

@Data
public class SendReportRequest {
    private String to;
    private String subject;
    private String body;
    private String pdfBase64;
    private String fileName;
}
