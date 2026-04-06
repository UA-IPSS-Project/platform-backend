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
            System.out.println("Form is not valid. Name and schema are required.");
            return null;
        }

        if (formRepository.existsByName(formName)) {
            System.out.println("Form with name " + formName + " already exists.");
            return null;
        }

        return formRepository.save(form);
    }

    public Form updateForm(String id, Form form) {
        String formName = form.getName();

        if (!formRepository.existsById(id)) {
            System.out.println("Form with id " + id + " does not exist.");
            return null;
        }

        if (!isFormValid(form)) {
            System.out.println("Form is not valid. Name and schema are required.");
            return null;
        }

        // Verifico o caso de dar update ao nome do form para um nome que já existe mas com id diferente (outro form)
        if (!formRepository.findByName(formName).getId().equals(id)) {
            System.out.println("Form with name " + formName + " already exists.");
            return null;
        }

        return formRepository.save(form);
    }

    public boolean deleteForm(String id) {
        Form form = formRepository.findById(id).orElse(null);

        if (form == null) {
            System.out.println("Form with id " + id + " does not exist.");
            return false;
        }

        formRepository.deleteById(id);
        
        return true;
    }

    public Form getFormById(String id) {
        Form form = formRepository.findById(id).orElse(null);
        
        if (form == null) {
            System.out.println("Form with id " + id + " does not exist.");
        }
        
        return form;
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
