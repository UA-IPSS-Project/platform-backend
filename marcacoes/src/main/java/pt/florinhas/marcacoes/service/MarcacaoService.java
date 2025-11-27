package pt.florinhas.marcacoes.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import pt.florinhas.marcacoes.domain.Funcionario;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.domain.MarcacaoBalneario;
import pt.florinhas.marcacoes.domain.Utente;
import pt.florinhas.marcacoes.domain.Valencia;

public interface MarcacaoService {
    
    // RF1.2.1 - Criar marcações presenciais (criado por Funcionario da secretaria)
    Marcacao criarMarcacaoPresencial(LocalDate data, LocalTime hora, String tipoAtendimento, 
                                    Utente utente, Funcionario funcionario, Valencia valencia, 
                                    Funcionario criadoPor);
    
    // RF1.2.10 - Criar marcações remotamente (criado pelo próprio Utente)
    Marcacao criarMarcacaoRemota(LocalDate data, LocalTime hora, String tipoAtendimento, 
                                Utente utente, Funcionario funcionario, Valencia valencia);
    
    // RF1.2.2 - Cancelar marcações
    void cancelarMarcacao(Long marcacaoId, String motivo, Funcionario canceladoPor);
    
    // RF1.2.3 - Agenda central com filtros
    List<Marcacao> consultarAgenda(LocalDate dataInicio, LocalDate dataFim, 
                                  Long funcionarioId, Long valenciaId, String estado);
    
    // RF1.2.4 - Atualizar estado das marcações
    Marcacao atualizarEstadoMarcacao(Long marcacaoId, String novoEstado, Funcionario atualizadoPor);
    
    // RF1.2.14 - Consultar e filtrar marcações passadas
    List<Marcacao> consultarMarcacoesPassadas(LocalDate dataInicio, LocalDate dataFim, 
                                             Long utenteId, String estado);
    
    // RF2.1 - Técnicos marcarem atendimento para balneário
    MarcacaoBalneario criarMarcacaoBalnearioTecnico(LocalDate data, LocalTime hora, Utente utente, 
                                                   Funcionario tecnico, Boolean produtosHigiene, 
                                                   Boolean lavagemRoupa, String roupaDescricao);
    
    // RF2.2 - Responsável do balneário marcar atendimento
    MarcacaoBalneario criarMarcacaoBalnearioResponsavel(LocalDate data, LocalTime hora, Utente utente, 
                                                       Funcionario responsavel, Boolean produtosHigiene, 
                                                       Boolean lavagemRoupa, String roupaDescricao);
    
    // RF2.3 - Confirmar presenças no balneário
    void confirmarPresencaBalneario(Long marcacaoId, Boolean presencaConfirmada, Funcionario confirmadoPor);
    
    // RF2.4 - Atualizar consumos no balneário
    MarcacaoBalneario atualizarConsumosBalneario(Long marcacaoId, Integer quantidadeProdutos, 
                                                Integer quantidadeRoupa, String observacoesConsumo,
                                                Funcionario atualizadoPor);
    
    // RF2.5 - Consultar e cancelar marcações do balneário
    List<MarcacaoBalneario> consultarMarcacoesBalneario(LocalDate data, String estado);
    void cancelarMarcacaoBalneario(Long marcacaoId, String motivo, Funcionario canceladoPor);
    
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