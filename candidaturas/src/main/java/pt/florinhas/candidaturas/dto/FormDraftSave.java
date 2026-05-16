package pt.florinhas.candidaturas.dto;

import java.util.List;

import lombok.Data;

import pt.florinhas.candidaturas.domain.FormPage;

@Data
public class FormDraftSave {
    private String name;
    private List<FormPage> pages;
}
