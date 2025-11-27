package pt.florinhas.marcacoes.service;

import pt.florinhas.marcacoes.domain.*;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;
import pt.florinhas.marcacoes.repository.UtenteRepository;
import pt.florinhas.marcacoes.repository.FuncionarioRepository;
import pt.florinhas.marcacoes.repository.ValenciaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MarcacaoServiceImpl implements MarcacaoService {

    @Autowired
    private MarcacaoRepository marcacaoRepository;
    
    @Autowired
    private UtenteRepository utenteRepository;
    
    @Autowired
    private FuncionarioRepository funcionarioRepository;
    
    @Autowired
    private ValenciaRepository valenciaRepository;
    
    //@Autowired
    //private EmailService emailService;

    @Override
    public Marcacao criarMarcacaoPresencial(LocalDate data, LocalTime hora, String tipoAtendimento,
                                          Utente utente, Funcionario funcionario, Valencia valencia,
                                          Funcionario criadoPor) {
        
        validarDisponibilidade(data, hora, funcionario);
        validarFuncionarioSecretaria(criadoPor); // RF1.2.1 - Apenas secretaria pode criar
        
        Marcacao marcacao = new Marcacao();
        marcacao.setData(data);
        marcacao.setHora(hora);
        marcacao.setTipoAtendimento("PRESENCIAL");
        marcacao.setUtente(utente);
        marcacao.setFuncionario(funcionario);
        marcacao.setValencia(valencia);
        marcacao.setEstado("AGENDADO");
        marcacao.setPresencaConfirmada(false);
        marcacao.setDocumentosInvalidos(false);
        
        Marcacao savedMarcacao = marcacaoRepository.save(marcacao);
        
        notificarUtenteMarcacao(savedMarcacao, "NOVA_MARCACAO");
        
        return savedMarcacao;
    }

    @Override
    public Marcacao criarMarcacaoRemota(LocalDate data, LocalTime hora, String tipoAtendimento, Utente utente, Funcionario funcionario, Valencia valencia) {
        
        validarDisponibilidade(data, hora, funcionario);
        
        Marcacao marcacao = new Marcacao();
        marcacao.setData(data);
        marcacao.setHora(hora);
        marcacao.setTipoAtendimento("REMOTO");
        marcacao.setUtente(utente);
        marcacao.setFuncionario(funcionario);
        marcacao.setValencia(valencia);
        marcacao.setEstado("AGENDADO");
        marcacao.setPresencaConfirmada(false);
        marcacao.setDocumentosInvalidos(false);
        
        Marcacao savedMarcacao = marcacaoRepository.save(marcacao);
        
        notificarUtenteMarcacao(savedMarcacao, "NOVA_MARCACAO");
        
        return savedMarcacao;
    }

    @Override
    public void cancelarMarcacao(Long marcacaoId, String motivo, Funcionario canceladoPor) {
        Marcacao marcacao = marcacaoRepository.findById(marcacaoId)
                .orElseThrow(() -> new RuntimeException("Marcação não encontrada"));
        
        // Verificar permissões: apenas secretaria ou funcionário responsável pode cancelar
        validarPermissaoCancelamento(marcacao, canceladoPor);
        
        marcacao.setEstado("CANCELADO");
        marcacao.setDataAtualizacao(LocalDateTime.now());
        marcacaoRepository.save(marcacao);
        
        notificarUtenteMarcacao(marcacao, "CANCELAMENTO");
    }

    @Override
    public List<Marcacao> consultarAgenda(LocalDate dataInicio, LocalDate dataFim, Long funcionarioId, Long valenciaId, String estado) {
        return marcacaoRepository.findByFilters(dataInicio, dataFim, funcionarioId, valenciaId, estado);
    }

    @Override
    public Marcacao atualizarEstadoMarcacao(Long marcacaoId, String novoEstado, Funcionario atualizadoPor) {
        Marcacao marcacao = marcacaoRepository.findById(marcacaoId)
                .orElseThrow(() -> new RuntimeException("Marcação não encontrada"));
        
        // RF1.2.4 - Apenas secretaria pode atualizar estados
        validarFuncionarioSecretaria(atualizadoPor);
        
        marcacao.setEstado(novoEstado);
        marcacao.setDataAtualizacao(LocalDateTime.now());
        
        return marcacaoRepository.save(marcacao);
    }

    @Override
    public List<Marcacao> consultarMarcacoesPassadas(LocalDate dataInicio, LocalDate dataFim, Long utenteId, String estado) {
        return marcacaoRepository.findMarcacoesPassadas(dataInicio, dataFim, utenteId, estado);
    }

    @Override
    public MarcacaoBalneario criarMarcacaoBalnearioTecnico(LocalDate data, LocalTime hora, Utente utente,
                                                        Funcionario tecnico, Boolean produtosHigiene,
                                                        Boolean lavagemRoupa, String roupaDescricao) {
        
        validarDisponibilidade(data, hora, tecnico);
        validarFuncionarioBalneario(tecnico); // RF2.1 - Apenas técnicos do balneário
        
        return criarMarcacaoBalneario(data, hora, utente, tecnico, produtosHigiene, lavagemRoupa, roupaDescricao);
    }

    @Override
    public MarcacaoBalneario criarMarcacaoBalnearioResponsavel(LocalDate data, LocalTime hora, Utente utente,
                                                            Funcionario responsavel, Boolean produtosHigiene,
                                                            Boolean lavagemRoupa, String roupaDescricao) {
        
        validarDisponibilidade(data, hora, responsavel);
        validarResponsavelBalneario(responsavel); // RF2.2 - Apenas responsável do balneário
        
        return criarMarcacaoBalneario(data, hora, utente, responsavel, produtosHigiene, lavagemRoupa, roupaDescricao);
    }

    @Override
    public void confirmarPresencaBalneario(Long marcacaoId, Boolean presencaConfirmada, Funcionario confirmadoPor) {
        MarcacaoBalneario marcacao = (MarcacaoBalneario) marcacaoRepository.findById(marcacaoId)
                .orElseThrow(() -> new RuntimeException("Marcação de balneário não encontrada"));
        
        // RF2.3 - Apenas responsável do balneário pode confirmar presenças
        validarResponsavelBalneario(confirmadoPor);
        
        marcacao.setPresencaConfirmada(presencaConfirmada);
        marcacao.setDataAtualizacao(LocalDateTime.now());
        
        if (presencaConfirmada) {
            marcacao.setEstado("CONFIRMADO");
        }
        
        marcacaoRepository.save(marcacao);
    }

    @Override
    public MarcacaoBalneario atualizarConsumosBalneario(Long marcacaoId, Integer quantidadeProdutos,
                                                    Integer quantidadeRoupa, String observacoesConsumo,
                                                    Funcionario atualizadoPor) {
        MarcacaoBalneario marcacao = (MarcacaoBalneario) marcacaoRepository.findById(marcacaoId)
                .orElseThrow(() -> new RuntimeException("Marcação de balneário não encontrada"));
        
        // RF2.4 - Apenas responsável do balneário pode atualizar consumos
        validarResponsavelBalneario(atualizadoPor);
        
        marcacao.setQuantidadeProdutos(quantidadeProdutos);
        marcacao.setQuantidadeRoupa(quantidadeRoupa);
        marcacao.setObservacoesConsumo(observacoesConsumo);
        marcacao.setConsumoRegistado(true);
        marcacao.setDataAtualizacao(LocalDateTime.now());
        
        return marcacaoRepository.save(marcacao);
    }

    @Override
    public List<MarcacaoBalneario> consultarMarcacoesBalneario(LocalDate data, String estado) {
        return marcacaoRepository.findMarcacoesBalnearioByDataAndEstado(data, estado);
    }

    @Override
    public void cancelarMarcacaoBalneario(Long marcacaoId, String motivo, Funcionario canceladoPor) {
        // RF2.5 - Apenas técnicos ou responsável do balneário podem cancelar
        MarcacaoBalneario marcacao = (MarcacaoBalneario) marcacaoRepository.findById(marcacaoId)
                .orElseThrow(() -> new RuntimeException("Marcação de balneário não encontrada"));
        
        validarFuncionarioBalneario(canceladoPor);
        
        cancelarMarcacao(marcacaoId, motivo, canceladoPor);
    }

    @Override
    public Utente criarUtenteAutomatico(String nome, String nif, String telefone, String email) {
        if (!validarNIF(nif)) {
            throw new RuntimeException("NIF inválido");
        }
        
        if (utenteRepository.existsByNif(nif)) {
            throw new RuntimeException("NIF já existe");
        }
        
        Utente utente = new Utente();
        utente.setNome(nome);
        utente.setNif(nif);
        utente.setTelefone(telefone);
        utente.setEmail(email);
        utente.setContaCriadaAutomaticamente(true);
        utente.setPasswordDefinida(false);
        
        Utente savedUtente = utenteRepository.save(utente);
        
        enviarTokenAcesso(savedUtente);
        
        return savedUtente;
    }

    @Override
    public Optional<Marcacao> findById(Long id) {
        return marcacaoRepository.findById(id);
    }

    @Override
    public List<Marcacao> findAll() {
        return marcacaoRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        marcacaoRepository.deleteById(id);
    }

    @Override
    public void notificarDocumentosInvalidos(Long marcacaoId, String observacoes, Funcionario notificadoPor) {
        Marcacao marcacao = marcacaoRepository.findById(marcacaoId)
                .orElseThrow(() -> new RuntimeException("Marcação não encontrada"));
        
        // RF1.2.11 - Apenas secretaria pode notificar documentos inválidos
        validarFuncionarioSecretaria(notificadoPor);
        
        marcacao.setDocumentosInvalidos(true);
        marcacao.setDocumentosObservacoes(observacoes);
        marcacao.setDataAtualizacao(LocalDateTime.now());
        marcacaoRepository.save(marcacao);
        
        notificarUtenteMarcacao(marcacao, "DOCUMENTOS_INVALIDOS");
    }

    @Override
    public List<Marcacao> consultarMarcacoesUtente(Utente utente) {
        return marcacaoRepository.findByUtente(utente);
    }

    @Override
    public List<Marcacao> consultarMarcacoesFuncionario(Funcionario funcionario) {
        return marcacaoRepository.findByFuncionario(funcionario);
    }

    // Métodos privados auxiliares
    private void validarDisponibilidade(LocalDate data, LocalTime hora, Funcionario funcionario) {
        boolean existeMarcacao = marcacaoRepository.existsByDataAndHoraAndFuncionarioAndEstadoNot(
            data, hora, funcionario, "CANCELADO");
        
        if (existeMarcacao) {
            throw new RuntimeException("Já existe uma marcação para este funcionário no horário selecionado");
        }
    }
    
    private void validarFuncionarioSecretaria(Funcionario funcionario) {
        // Verificar se o funcionário pertence à secretaria
        // Implementação depende da estrutura de roles/permissões
        if (!funcionario.getTipo().equals("SECRETARIA")) {
            throw new RuntimeException("Apenas funcionários da secretaria podem realizar esta ação");
        }
    }
    
    private void validarFuncionarioBalneario(Funcionario funcionario) {
        // Verificar se o funcionário é do balneário (técnico ou responsável)
        if (!funcionario.getTipo().equals("BALNEARIO") && !funcionario.getTipo().equals("TECNICO")) {
            throw new RuntimeException("Apenas funcionários do balneário podem realizar esta ação");
        }
    }
    
    private void validarResponsavelBalneario(Funcionario funcionario) {
        // Verificar se o funcionário é responsável do balneário
        if (!funcionario.getTipo().equals("BALNEARIO")) {
            throw new RuntimeException("Apenas o responsável do balneário pode realizar esta ação");
        }
    }
    
    private void validarPermissaoCancelamento(Marcacao marcacao, Funcionario canceladoPor) {
        // Secretaria pode cancelar qualquer marcação
        if (canceladoPor.getTipo().equals("SECRETARIA")) {
            return;
        }
        
        // Funcionário só pode cancelar suas próprias marcações
        if (marcacao.getFuncionario().getId().equals(canceladoPor.getId())) {
            return;
        }
        
        throw new RuntimeException("Não tem permissão para cancelar esta marcação");
    }
    
    private MarcacaoBalneario criarMarcacaoBalneario(LocalDate data, LocalTime hora, Utente utente,
                                                   Funcionario funcionario, Boolean produtosHigiene,
                                                   Boolean lavagemRoupa, String roupaDescricao) {
        
        MarcacaoBalneario marcacao = new MarcacaoBalneario();
        marcacao.setData(data);
        marcacao.setHora(hora);
        marcacao.setTipoAtendimento("PRESENCIAL");
        marcacao.setUtente(utente);
        marcacao.setFuncionario(funcionario);
        marcacao.setEstado("AGENDADO");
        marcacao.setProdutosHigiene(produtosHigiene);
        marcacao.setLavagemRoupa(lavagemRoupa);
        marcacao.setRoupaDescricao(roupaDescricao);
        marcacao.setConsumoRegistado(false);
        marcacao.setPresencaConfirmada(false);
        
        return marcacaoRepository.save(marcacao);
    }
    
    private void notificarUtenteMarcacao(Marcacao marcacao, String tipoNotificacao) {
        String mensagem = "";
        String assunto = "";
        
        switch (tipoNotificacao) {
            case "NOVA_MARCACAO":
                assunto = "Nova Marcação Criada";
                mensagem = String.format("A sua marcação para %s às %s foi agendada com sucesso.", 
                    marcacao.getData(), marcacao.getHora());
                break;
            case "CANCELAMENTO":
                assunto = "Marcação Cancelada";
                mensagem = "A sua marcação foi cancelada.";
                break;
            case "DOCUMENTOS_INVALIDOS":
                assunto = "Documentos Inválidos";
                mensagem = "Os documentos apresentados são inválidos. Por favor, contacte a secretaria.";
                break;
        }
        
        if (marcacao.getUtente().getEmail() != null) {
            //emailService.enviarEmail(marcacao.getUtente().getEmail(), assunto, mensagem);
        }
    }
    
    private boolean validarNIF(String nif) {
        if (nif == null || nif.length() != 9) {
            return false;
        }
        
        try {
            Integer.parseInt(nif);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private void enviarTokenAcesso(Utente utente) {
        String token = gerarToken();
        String mensagem = String.format(
            "Foi criada uma conta automática para si. Use o token %s para aceder à plataforma. " +
            "Será obrigatório definir uma nova palavra-passe no primeiro acesso.", token);
        
        if (utente.getEmail() != null) {
            //emailService.enviarEmail(utente.getEmail(), "Token de Acesso - Plataforma", mensagem);
        }
    }
    
    private String gerarToken() {
        return String.valueOf((int) ((Math.random() * 900000) + 100000));
    }
}