package pt.florinhas.candidaturas.service;

// Java
import java.util.List;

// Spring
import org.springframework.stereotype.Service;

// From this project
import pt.florinhas.candidaturas.repository.FormRepository;
import pt.florinhas.candidaturas.domain.Form;

// Lombok
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class FormService {
    private final FormRepository formRepository;

    public Form createForm(Form form) {
        String formName = form.getName();

        if (!isFormValid(form)) {
            throw new IllegalArgumentException("Form is not valid. Name and schema are required.");
        }

        if (formRepository.existsByName(formName)) {
            throw new IllegalArgumentException("Form with name " + formName + " already exists.");
        }

        return formRepository.save(form);
    }

    public Form updateForm(String id, Form form) {
        String formName = form.getName();

        if (!formRepository.existsById(id)) {
            throw new IllegalArgumentException("Form with id " + id + " does not exist.");
        }

        if (!isFormValid(form)) {
            throw new IllegalArgumentException("Form is not valid. Name and schema are required.");
        }

        // Verifico o caso de dar update ao nome do form para um nome que já existe mas com id diferente (outro form)
        if (!formRepository.findByName(formName).getId().equals(id)) {
            throw new IllegalArgumentException("Form with name " + formName + " already exists.");
        }

        return formRepository.save(form);
    }

    public void deleteForm(String id) {
        if (!formRepository.existsById(id)) {
            throw new IllegalArgumentException("Form with id " + id + " does not exist.");
        }

        formRepository.deleteById(id);
    }

    public Form getFormById(String id) {
        return formRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Form with id " + id + " does not exist."));
    }

    public List<Form> getForms() {
        return formRepository.findAll();
    }

    
    private boolean isFormValid(Form form) {
        if (form.getName() == null || form.getName().isEmpty()) {
            return false;
        }

        if (form.getSchema() == null || form.getSchema().isEmpty()) {
            return false;
        }

        return true;
    }
}
