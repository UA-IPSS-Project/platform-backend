package pt.florinhas.candidaturas.service;

import java.time.Instant;
// Java
import java.util.List;

// Spring
import org.springframework.stereotype.Service;

// From this project
import pt.florinhas.candidaturas.repository.FormRepository;
import pt.florinhas.candidaturas.domain.Form;
import pt.florinhas.candidaturas.dto.FormCreate;
import pt.florinhas.candidaturas.dto.FormUpdate;

// Lombok
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class FormService {
    private final FormRepository formRepository;

    public Form createForm(FormCreate dto, Long userId) {
        if (formRepository.existsByName(dto.getName())) {
            log.warn("Form with name {} already exists.", dto.getName());
            return null;
        }

        Form form = new Form();
        form.setName(dto.getName());
        form.setSchema(dto.getSchema());
        form.setUiSchema(dto.getUiSchema());
        form.setPages(dto.getPages());
        form.setCriadoPor(userId);
        form.setCriadoEm(Instant.now());

        return formRepository.save(form);
    }

    public Form updateForm(String id, FormUpdate dto, Long userId) {
        Form form = formRepository.findById(id).orElse(null);
        if (form == null) {
            log.error("Form with id {} does not exist.", id);
            return null;
        }

        if (!dto.getName().equals(form.getName()) && formRepository.existsByName(dto.getName())) {
            log.warn("Form with name {} already exists.", dto.getName());
            return null;
        }

        form.setName(dto.getName());
        form.setSchema(dto.getSchema());
        form.setUiSchema(dto.getUiSchema());
        form.setPages(dto.getPages());
        form.setAtualizadoPor(userId);
        form.setAtualizadoEm(Instant.now());

        return formRepository.save(form);
    }

    public boolean deleteForm(String id) {
        if (!formRepository.existsById(id)) {
            log.error("Form with id {} does not exist.", id);
            return false;
        }
        formRepository.deleteById(id);
        return true;
    }

    public Form getFormById(String id) {
        Form form = formRepository.findById(id).orElse(null);
        if (form == null) {
            log.warn("Form with id {} does not exist.", id);
        }
        return form;
    }

    public List<Form> getForms() {
        return formRepository.findAll();
    }
}
