package pt.florinhas.marcacoes.service;

import pt.florinhas.marcacoes.domain.*;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;
import pt.florinhas.marcacoes.repository.MarcacaoSecretariaRepository;
import pt.florinhas.marcacoes.repository.UtenteRepository;
import pt.florinhas.marcacoes.repository.FuncionarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional
public class MarcacaoServiceImpl implements MarcacaoService {

    @Autowired
    private MarcacaoRepository marcacaoRepository;
    
    @Autowired
    private MarcacaoSecretariaRepository marcacaoSecretariaRepository;
    
    @Autowired
    private UtenteRepository utenteRepository;
    
    @Autowired
    private FuncionarioRepository funcionarioRepository;
    
    //@Autowired
    //private EmailService emailService;

    @Override
    public Marcacao criarMarcacaoPresencial(LocalDateTime data, String assunto, Utente utente, Funcionario criadoPor) {
        
        validarDisponibilidade(data);
        validarFuncionarioSecretaria(criadoPor); // Apenas secretaria pode criar
        
        // Criar Marcacao
        Marcacao marcacao = new Marcacao();
        marcacao.setData(data);
        marcacao.setEstado(EventoEstado.AGENDADO);
        marcacao.setCriadoPor(criadoPor);
        
        Marcacao savedMarcacao = marcacaoRepository.save(marcacao);
        
        // Criar MarcacaoSecretaria com OneToOne
        MarcacaoSecretaria marcacaoSecretaria = new MarcacaoSecretaria();
        marcacaoSecretaria.setMarcacao(savedMarcacao);
        marcacaoSecretaria.setAssunto(assunto);
        marcacaoSecretaria.setTipoAtendimento(AtendimentoTipo.PRESENCIAL);

        // TODO: Se utente for null, criar utente automático
        if (utente == null) {
            utente = criarUtenteAutomatico(assunto, assunto, assunto, assunto);
        }

        marcacaoSecretaria.setUtente(utente);
        
        marcacaoSecretariaRepository.save(marcacaoSecretaria);
        
        notificarUtenteMarcacao(savedMarcacao, "NOVA_MARCACAO");
        
        return savedMarcacao;
    }

    @Override
    public Marcacao criarMarcacaoRemota(LocalDateTime data, String assunto, Utente utente) {
        
        validarDisponibilidade(data);
        
        // Criar Marcacao
        Marcacao marcacao = new Marcacao();
        marcacao.setData(data);
        marcacao.setEstado(EventoEstado.AGENDADO);
        marcacao.setCriadoPor(utente); // Criado pelo próprio utente

        Marcacao savedMarcacao = marcacaoRepository.save(marcacao);
        
        // Criar MarcacaoSecretaria com OneToOne
        MarcacaoSecretaria marcacaoSecretaria = new MarcacaoSecretaria();
        marcacaoSecretaria.setMarcacao(savedMarcacao);
        marcacaoSecretaria.setAssunto(assunto);
        marcacaoSecretaria.setTipoAtendimento(AtendimentoTipo.REMOTO);
        marcacaoSecretaria.setUtente(utente);
        
        marcacaoSecretariaRepository.save(marcacaoSecretaria);
        
        notificarUtenteMarcacao(savedMarcacao, "NOVA_MARCACAO");
        
        return savedMarcacao;
    }

    @Override
    public void cancelarMarcacao(Long marcacaoId, String motivo, Utilizador canceladoPor) {
        Marcacao marcacao = marcacaoRepository.findById(marcacaoId).orElseThrow(() -> new RuntimeException("Marcação não encontrada"));
        
        // Verificar permissões: apenas secretaria ou funcionário responsável pode cancelar
        validarPermissaoCancelamento(marcacao, canceladoPor);
        
        marcacao.setEstado(EventoEstado.CANCELADO);
        marcacaoRepository.save(marcacao);
        
        notificarUtenteMarcacao(marcacao, "CANCELAMENTO");
    }

    @Override
    public List<Marcacao> consultarAgenda(LocalDateTime dataInicio, LocalDateTime dataFim) {
        return marcacaoRepository.findMarcacoesBetweenDates(dataInicio, dataFim);
    }

    @Override
    public List<Marcacao> procurarAgenda(LocalDateTime dataInicio, LocalDateTime dataFim, Long criadoPorId, Long utenteId, EventoEstado estado) {
        List<Marcacao> marcacoes = marcacaoRepository.findMarcacoesBetweenDates(dataInicio, dataFim);
        
        return marcacoes.stream()
                .filter(m -> criadoPorId == null || m.getCriadoPor().getId().equals(criadoPorId))
                .filter(m -> utenteId == null || (m.getMarcacaoSecretaria() != null && m.getMarcacaoSecretaria().getUtente().getId().equals(utenteId)))
                .filter(m -> estado == null || m.getEstado().equals(estado))
                .toList();
    }

    @Override
    public Marcacao atualizarEstadoMarcacao(Long marcacaoId, EventoEstado novoEstado, Utilizador atualizadoPor) {
        Marcacao marcacao = marcacaoRepository.findById(marcacaoId).orElseThrow(() -> new RuntimeException("Marcação não encontrada"));
        
        // TODO: Ver se vale a pena por aqui a parte de cancelar

        if (atualizadoPor instanceof Funcionario) {
            validarFuncionarioSecretaria((Funcionario) atualizadoPor); // Apenas secretaria pode atualizar estado
    }
        else {
            throw new RuntimeException("Apenas funcionários podem atualizar o estado da marcação");
        }
        
        marcacao.setEstado(novoEstado);
        
        return marcacaoRepository.save(marcacao);
    }

    @Override
    public List<Marcacao> consultarMarcacoesPassadas(LocalDateTime dataInicio, LocalDateTime dataFim, Long utenteId, EventoEstado estado) {
        List<Marcacao> marcacoes = marcacaoRepository.findMarcacoesBetweenDates(dataInicio, dataFim);
        
        return marcacoes.stream()
                .filter(m -> m.getData().isBefore(LocalDateTime.now()))
                .filter(m -> utenteId == null || m.getMarcacaoSecretaria().getUtente().getId().equals(utenteId))
                .filter(m -> estado == null || m.getEstado().equals(estado))
                .toList();
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
        
        Utente savedUtente = utenteRepository.save(utente);
        
        enviarTokenAcesso(savedUtente);
        
        return savedUtente;
    }

    @Override
    public Optional<Marcacao> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return marcacaoRepository.findById(id);
    }

    @Override
    public List<Marcacao> findAll() {
        return marcacaoRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        marcacaoRepository.deleteById(id);
    }

    @Override
    public void notificarDocumentosInvalidos(Long marcacaoId, String observacoes, Funcionario notificadoPor) {
        if (marcacaoId == null || notificadoPor == null || observacoes == null) {
            throw new IllegalArgumentException(" Argumento não pode ser nulo");
        }

        Marcacao marcacao = marcacaoRepository.findById(marcacaoId).orElseThrow(() -> new RuntimeException("Marcação não encontrada"));
        
        // Apenas secretaria pode notificar documentos inválidos
        validarFuncionarioSecretaria(notificadoPor);
        
        notificarUtenteMarcacao(marcacao, "DOCUMENTOS_INVALIDOS");
    }

    @Override
    public List<Marcacao> consultarMarcacoesUtente(Utente utente) {
        return marcacaoRepository.findByUtente(utente);
    }

    @Override
    public List<Marcacao> consultarMarcacoesFuncionario(Funcionario funcionario) {
        return marcacaoRepository.findByCriadoPor(funcionario);
    }

    // Métodos privados auxiliares
    private void validarDisponibilidade(LocalDateTime data) {
        if (data.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Não é possível agendar marcações para datas passadas");
        }
        // TODO: Lógica de matriz de horários pode ser adicionada aqui
    }
    
    private void validarFuncionarioSecretaria(Funcionario funcionario) {
        // Verificar se o funcionário pertence à secretaria
        if (funcionario.getTipo() != FuncionarioTipo.SECRETARIA) { throw new RuntimeException("Apenas funcionários da secretaria podem realizar esta ação"); }
    }
    

    private void validarPermissaoCancelamento(Marcacao marcacao, Utilizador canceladoPor) {
        if (canceladoPor.equals(marcacao.getCriadoPor())) {
            return; // Utilizador que criou a marcação pode cancelar    
        }

        if (canceladoPor instanceof Funcionario funcionario) {
            if (funcionario.getTipo() == FuncionarioTipo.SECRETARIA) {
                return; // Qualquer funcionário da secretaria pode cancelar
            }
        }
        
        throw new RuntimeException("Não tem permissão para cancelar esta marcação");
    }
    
    
    private void notificarUtenteMarcacao(Marcacao marcacao, String tipoNotificacao) {
        String mensagem = "";
        String assunto = "";
        
        switch (tipoNotificacao) {
            case "NOVA_MARCACAO":
                assunto = "Nova Marcação Criada";
                mensagem = "A sua marcação para %s foi agendada com sucesso.".formatted(
                        marcacao.getData());
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
        
        if (marcacao.getMarcacaoSecretaria().getUtente().getEmail() != null) {
            //emailService.enviarEmail(marcacao.getMarcacaoSecretaria().getUtente().getEmail(), assunto, mensagem);
            System.out.println("Email enviado para " + marcacao.getMarcacaoSecretaria().getUtente().getEmail() + " com assunto: " + assunto + " e mensagem: " + mensagem);
        }
    }
    
    private boolean validarNIF(String nif) { // TODO: melhorar validação de NIF com API Externa
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
        String mensagem = ( "Foi criada uma conta automática para si. Use o token %s para aceder à plataforma. " + "Será obrigatório definir uma nova palavra-passe no primeiro acesso.").formatted(token);
        
        if (utente.getEmail() != null) {
            //emailService.enviarEmail(utente.getEmail(), "Token de Acesso - Plataforma", mensagem);
            System.out.println("Email enviado para " + utente.getEmail() + " com token: " + token);
        }
    }
    
    private String gerarToken() { return String.valueOf((int) ((ThreadLocalRandom.current().nextDouble() * 900000) + 100000)); }
}