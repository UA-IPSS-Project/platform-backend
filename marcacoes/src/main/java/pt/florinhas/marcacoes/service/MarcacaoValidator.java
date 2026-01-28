package pt.florinhas.marcacoes.service;

import org.springframework.stereotype.Component;

import pt.florinhas.marcacoes.dto.CriarMarcacaoRequest;
import pt.florinhas.marcacoes.dto.ReagendarMarcacaoRequest;

@Component
public class MarcacaoValidator {

    public void validarCriacao(CriarMarcacaoRequest request) {
        if (request.getData() == null) {
            throw new IllegalArgumentException("A data da marcação é obrigatória.");
        }
        // Adicionar mais validações conforme necessário
    }

    public void validarReagendamento(ReagendarMarcacaoRequest request) {
        if (request.getNovaDataHora() == null) {
            throw new IllegalArgumentException("A nova data é obrigatória.");
        }
    }
}
