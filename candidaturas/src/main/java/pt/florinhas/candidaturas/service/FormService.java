package pt.florinhas.candidaturas.service;

import java.time.Instant;
// Java
import java.util.List;

// Spring
import org.springframework.stereotype.Service;

// From this project
import pt.florinhas.candidaturas.repository.FormRepository;
import pt.florinhas.candidaturas.domain.Form;

// Lombok
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class FormService {
    private final FormRepository formRepository;

    public Form createForm(Form form) {
        String formName = form.getName();

        if (!isFormValid(form)) {
            return null;
        }

        if (formRepository.existsByName(formName)) {
            log.warn("Form with name {} already exists.", formName);
            return null;
        }

        if (form.getCriadoEm() == null) {
            log.info("Form created at time is null, setting it to current time.");
            form.setCriadoEm(Instant.now());
        }

        return formRepository.save(form);
    }

    public Form updateForm(String id, Form form) {
        String formName = form.getName();

        if (!formRepository.existsById(id)) {
            log.error("Form with id {} does not exist.", id);
            return null;
        }

        if (!isFormValid(form)) {
            log.warn("Form is not valid. Name and schema are required.");
            return null;
        }

        Form beforeForm = formRepository.findById(id).get();

        if (!beforeForm.getId().equals(form.getId())) {
            log.error("Form id changed. Access Denied.");
            return null;
        }

        // Check if updating the form name to an already existing name with a different
        // id
        if (formName != null && !formName.equals(beforeForm.getName()) &&
                formRepository.existsByName(formName)) {
            log.warn("Form with name {} already exists.", formName);
            return null;
        }

        if (form.getAtualizadoPor() != null) {
            log.warn("Form without user whose update the form");
            return null;
        }

        if (form.getAtualizadoEm() != null) {
            log.info("Form updated at time is null, setting it to current time.");
            form.setAtualizadoEm(Instant.now());
        }

        return formRepository.save(form);
    }

    public boolean deleteForm(String id) {
        Form form = formRepository.findById(id).orElse(null);

        if (form == null) {
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

    private boolean isFormValid(Form form) {
        if (form.getName() == null || form.getName().isEmpty()) {
            log.warn("Form name is required.");
            return false;
        }

        if (form.getSchema() == null || form.getSchema().isEmpty()) {
            log.warn("Form schema is required.");
            return false;
        }

        if (form.getCriadoPor() == null || form.getCriadoPor() <= 0) {
            log.warn("Form creator is required.");
            return false;
        }

        return true;
    }

}
