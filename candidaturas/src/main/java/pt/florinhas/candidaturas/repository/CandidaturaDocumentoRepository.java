package pt.florinhas.candidaturas.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import pt.florinhas.candidaturas.domain.CandidaturaDocumento;

public interface CandidaturaDocumentoRepository extends MongoRepository<CandidaturaDocumento, String> {
    List<CandidaturaDocumento> findByCandidaturaId(String candidaturaId);
    void deleteByCandidaturaId(String candidaturaId);
}
