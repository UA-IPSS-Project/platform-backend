package pt.florinhas.marcacoes.service;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import pt.florinhas.marcacoes.domain.EventoEstado;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;

@Service
public class LimpezaService {

    private final MarcacaoRepository marcacaoRepository;

    public LimpezaService(MarcacaoRepository marcacaoRepository) {
        this.marcacaoRepository = marcacaoRepository;
    }

    // Corre a cada minuto
    @Scheduled(fixedRate = 60000)
    public void limparReservasExpiradas() {
        // Define o tempo limite (10 minutos atrás)
        LocalDateTime limite = LocalDateTime.now().minusMinutes(10);
        
        // Apaga tudo o que está EM_PREENCHIMENTO e foi criado há mais de 10 min
        marcacaoRepository.deleteByEstadoAndCriadoEmBefore( EventoEstado.EM_PREENCHIMENTO, limite );
    }
}