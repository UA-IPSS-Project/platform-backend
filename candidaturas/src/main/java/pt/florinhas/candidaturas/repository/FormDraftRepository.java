package pt.florinhas.candidaturas.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import pt.florinhas.candidaturas.domain.FormDraft;

public interface FormDraftRepository extends MongoRepository<FormDraft, String> {
    Optional<FormDraft> findByFormId(String formId);
    void deleteByFormId(String formId);
}
