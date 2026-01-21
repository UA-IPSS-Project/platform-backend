package pt.florinhas.marcacoes.service.nif;

public interface NifValidationService {
    /**
     * Valida um NIF.
     * 
     * @param nif NIF a validar
     * @return true se válido, false caso contrário
     */
    boolean validate(String nif);
}
