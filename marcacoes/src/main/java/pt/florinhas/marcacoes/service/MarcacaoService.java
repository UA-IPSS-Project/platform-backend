package pt.florinhas.marcacoes.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import pt.florinhas.marcacoes.domain.EventoEstado;
import pt.florinhas.marcacoes.domain.Funcionario;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.domain.Utente;
import pt.florinhas.marcacoes.domain.Utilizador;

public interface MarcacaoService {
    
    // RF1.2.1 - Criar marcações presenciais (criado por Funcionario da secretaria)
    Marcacao criarMarcacaoPresencial(LocalDateTime data, String assunto, Utente utente, Funcionario criadoPor);
    
    // RF1.2.10 - Criar marcações remotamente (criado pelo próprio Utente)
    Marcacao criarMarcacaoRemota(LocalDateTime data, String assunto, Utente utente);
    
    // RF1.2.2 - Cancelar marcações
    void cancelarMarcacao(Long marcacaoId, String motivo, Utilizador canceladoPor);
    
    // RF1.2.3 - Agenda central - mostra todas as marcações
    List<Marcacao> consultarAgenda(LocalDateTime dataInicio, LocalDateTime dataFim);
    
    // Procurar marcações com filtros
    List<Marcacao> procurarAgenda(LocalDateTime dataInicio, LocalDateTime dataFim, 
                                 Long criadoPorId, Long utenteId, EventoEstado estado);
    
    // RF1.2.4 - Atualizar estado das marcações
    Marcacao atualizarEstadoMarcacao(Long marcacaoId, EventoEstado novoEstado, Utilizador atualizadoPor);
    
    // RF1.2.14 - Consultar e filtrar marcações passadas
    List<Marcacao> consultarMarcacoesPassadas(LocalDateTime dataInicio, LocalDateTime dataFim, 
                                             Long utenteId, EventoEstado estado);
    
    
    // RF1.2.12 - Criar conta automaticamente com NIF válido
    Utente criarUtenteAutomatico(String nome, String nif, String telefone, String email);
    
    // Métodos auxiliares
    Optional<Marcacao> findById(Long id);
    List<Marcacao> findAll();
    void deleteById(Long id);
    
    // RF1.2.11 - Notificar documentos inválidos
    void notificarDocumentosInvalidos(Long marcacaoId, String observacoes, Funcionario notificadoPor);
    
    // Métodos específicos por tipo de utilizador
    List<Marcacao> consultarMarcacoesUtente(Utente utente);
    List<Marcacao> consultarMarcacoesFuncionario(Funcionario funcionario);
}