package pt.florinhas.candidaturas.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import pt.florinhas.candidaturas.domain.CandidaturaSecretaryDraft;

public interface CandidaturaSecretaryDraftRepository extends MongoRepository<CandidaturaSecretaryDraft, String> {
    Optional<CandidaturaSecretaryDraft> findByCandidaturaId(String candidaturaId);
    void deleteByCandidaturaId(String candidaturaId);
}
