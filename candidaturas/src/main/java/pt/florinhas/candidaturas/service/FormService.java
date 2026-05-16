package pt.florinhas.candidaturas.service;

import java.time.Instant;
// Java
import java.util.List;

// Spring
import org.springframework.stereotype.Service;

// From this project
import pt.florinhas.candidaturas.repository.FormRepository;
import pt.florinhas.candidaturas.repository.FormDraftRepository;
import pt.florinhas.candidaturas.domain.Form;
import pt.florinhas.candidaturas.domain.FormDraft;
import pt.florinhas.candidaturas.dto.FormCreate;
import pt.florinhas.candidaturas.dto.FormUpdate;
import pt.florinhas.candidaturas.dto.FormDraftSave;

// Lombok
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class FormService {
    private final FormRepository formRepository;
    private final FormDraftRepository formDraftRepository;

    public Form createForm(FormCreate dto, Long userId) {
        if (formRepository.existsByName(dto.getName())) {
            log.warn("Form with name {} already exists.", dto.getName());
            return null;
        }

        Form form = new Form();
        form.setName(dto.getName());
        form.setStatus(dto.getStatus() != null ? dto.getStatus() : pt.florinhas.candidaturas.domain.FormStatus.RASCUNHO);
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
        if (dto.getStatus() != null) {
            form.setStatus(dto.getStatus());
        }
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

    public FormDraft saveOrUpdateDraft(String formId, FormDraftSave dto, Long userId) {
        if (!formRepository.existsById(formId)) {
            log.error("Form with id {} does not exist.", formId);
            return null;
        }
        FormDraft draft = formDraftRepository.findByFormId(formId).orElse(new FormDraft());
        draft.setFormId(formId);
        draft.setName(dto.getName());
        draft.setPages(dto.getPages());
        draft.setAtualizadoPor(userId);
        draft.setAtualizadoEm(Instant.now());
        return formDraftRepository.save(draft);
    }

    public FormDraft getDraftByFormId(String formId) {
        return formDraftRepository.findByFormId(formId).orElse(null);
    }

    public boolean deleteDraftByFormId(String formId) {
        if (formDraftRepository.findByFormId(formId).isEmpty()) {
            return false;
        }
        formDraftRepository.deleteByFormId(formId);
        return true;
    }
}
