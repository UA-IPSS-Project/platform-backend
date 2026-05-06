package pt.florinhas.candidaturas.domain;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.bson.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FieldDefinition {
    private String key;
    private String componentType;
    private int order;
    private Document config; // Ainda podemos trocar @TypeAlias
    private FieldAudience audience; // Ainda a considerar se vamos transitar para Form Pages
}