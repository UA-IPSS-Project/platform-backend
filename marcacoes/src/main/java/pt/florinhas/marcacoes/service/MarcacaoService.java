package pt.florinhas.marcacoes.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.florinhas.marcacoes.domain.AtendimentoTipo;
import pt.florinhas.marcacoes.domain.EventoEstado;
import pt.florinhas.marcacoes.domain.Funcionario;
import pt.florinhas.marcacoes.domain.FuncionarioTipo;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.domain.MarcacaoSecretaria;
import pt.florinhas.marcacoes.domain.Utente;
import pt.florinhas.marcacoes.domain.Utilizador;
import pt.florinhas.marcacoes.dto.*;
import pt.florinhas.marcacoes.repository.FuncionarioRepository;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;
import pt.florinhas.marcacoes.repository.MarcacaoSecretariaRepository;
import pt.florinhas.marcacoes.repository.UtenteRepository;
import pt.florinhas.marcacoes.repository.UtilizadorRepository;

@Service
@Transactional
public class MarcacaoService{

    @Autowired
    private MarcacaoRepository marcacaoRepository;
    
    @Autowired
    private MarcacaoSecretariaRepository marcacaoSecretariaRepository;
    
    @Autowired
    private UtenteRepository utenteRepository;
    
    @Autowired
    private FuncionarioRepository funcionarioRepository;
    
    @Autowired
    private UtilizadorRepository utilizadorRepository;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    //@Autowired
    //private FuncionarioRepository funcionarioRepository;
    
    //@Autowired
    //private EmailService emailService;

    public Marcacao criarMarcacaoPresencial(CriarMarcacaoRequest request) {
        // Validar campos obrigatórios
        if (request.getUtenteNif() == null || request.getUtenteNif().trim().isEmpty()) {
            throw new RuntimeException("NIF do utente é obrigatório");
        }
        
        validarDisponibilidade(request.getData());
        
        // Obter funcionário que está a criar
        Funcionario criadoPor = funcionarioRepository.findById(request.getFuncionarioId()).orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));
        
        validarFuncionarioSecretaria(criadoPor); // Apenas secretaria pode criar

        // Procurar ou criar utente por NIF
        Utente utente = utenteRepository.findByNif(request.getUtenteNif()).orElseGet(() -> {
                    // Se não existir, criar novo utente
                    System.out.println("Utente com NIF " + request.getUtenteNif() + " não encontrado. Criando novo utente...");
                    
                    // Validar campos necessários para criar utente
                    if (request.getUtenteNome() == null || request.getUtenteNome().trim().isEmpty()) {
                        throw new RuntimeException("Nome do utente é obrigatório para criar novo registo");
                    }
                    if (request.getUtenteEmail() == null || request.getUtenteEmail().trim().isEmpty()) {
                        throw new RuntimeException("Email do utente é obrigatório para criar novo registo");
                    }
                    if (request.getUtenteTelefone() == null || request.getUtenteTelefone().trim().isEmpty()) {
                        throw new RuntimeException("Telefone do utente é obrigatório para criar novo registo");
                    }
                    
                    // Verificar se email já existe
                    if (utenteRepository.existsByEmail(request.getUtenteEmail())) {
                        throw new RuntimeException("Email já está registado no sistema");
                    }
                    
                    // Criar novo utente
                    Utente novoUtente = new Utente();
                    novoUtente.setNif(request.getUtenteNif());
                    novoUtente.setNome(request.getUtenteNome());
                    novoUtente.setEmail(request.getUtenteEmail());
                    novoUtente.setTelefone(request.getUtenteTelefone());
                    novoUtente.setActivo(false); // Inactivo até dar login pela primeira vez
                    String passwordTemporaria = request.getUtenteNif();
                    novoUtente.setPassHash(passwordEncoder.encode(passwordTemporaria)); // TODO GERAR TOKEN e TROCAR PASSWORD
                    
                    System.out.println("Novo utente criado com password temporária = NIF: " + passwordTemporaria);
                    
                    return utenteRepository.save(novoUtente);
                });
        
        // Verificar se os dados do request coincidem com os dados do utente existente
        if (request.getUtenteNome() != null && !request.getUtenteNome().trim().isEmpty() 
            && !utente.getNome().equalsIgnoreCase(request.getUtenteNome())) {
            throw new RuntimeException("O nome fornecido não coincide com o utente registado com o NIF " + request.getUtenteNif());
        }
        
        if (request.getUtenteEmail() != null && !request.getUtenteEmail().trim().isEmpty() 
            && !utente.getEmail().equalsIgnoreCase(request.getUtenteEmail())) {
            throw new RuntimeException("O email fornecido não coincide com o utente registado com o NIF " + request.getUtenteNif());
        }
        
        if (request.getUtenteTelefone() != null && !request.getUtenteTelefone().trim().isEmpty() 
            && !utente.getTelefone().equals(request.getUtenteTelefone())) {
            throw new RuntimeException("O telefone fornecido não coincide com o utente registado com o NIF " + request.getUtenteNif());
        }

        // Criar Marcacao
        Marcacao marcacao = new Marcacao();
        marcacao.setData(request.getData());
        marcacao.setEstado(EventoEstado.AGENDADO);
        marcacao.setCriadoPor(criadoPor);
        
        Marcacao savedMarcacao = marcacaoRepository.save(marcacao);
        
        // Criar MarcacaoSecretaria com OneToOne
        MarcacaoSecretaria marcacaoSecretaria = new MarcacaoSecretaria();
        marcacaoSecretaria.setMarcacao(savedMarcacao);
        marcacaoSecretaria.setAssunto(request.getAssunto());
        marcacaoSecretaria.setTipoAtendimento(AtendimentoTipo.PRESENCIAL);
        marcacaoSecretaria.setUtente(utente);
        
        // Se tiver descrição, adicionar
        if (request.getDescricao() != null && !request.getDescricao().trim().isEmpty()) {
            marcacaoSecretaria.setDescricao(request.getDescricao());
        }
        
        MarcacaoSecretaria savedMarcacaoSecretaria = marcacaoSecretariaRepository.save(marcacaoSecretaria);
        
        // Estabelecer relação bidirecional
        savedMarcacao.setMarcacaoSecretaria(savedMarcacaoSecretaria);
        
        notificarUtenteMarcacao(savedMarcacao, "NOVA_MARCACAO");
        
        System.out.println("Marcação presencial criada com sucesso: " + savedMarcacao.getId());
        
        return savedMarcacao;
    }

    
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
        
        MarcacaoSecretaria savedMarcacaoSecretaria = marcacaoSecretariaRepository.save(marcacaoSecretaria);
        
        // Estabelecer relacionamento bidirecional
        savedMarcacao.setMarcacaoSecretaria(savedMarcacaoSecretaria);
        
        notificarUtenteMarcacao(savedMarcacao, "NOVA_MARCACAO");
        
        return savedMarcacao;
    }

    
    public List<Marcacao> consultarAgenda(LocalDateTime dataInicio, LocalDateTime dataFim) {
        return marcacaoRepository.findMarcacoesBetweenDates(dataInicio, dataFim);
    }

    
    public List<Marcacao> procurarAgenda(LocalDateTime dataInicio, LocalDateTime dataFim, Long criadoPorId, Long utenteId, EventoEstado estado) {
        List<Marcacao> marcacoes = marcacaoRepository.findMarcacoesBetweenDates(dataInicio, dataFim);
        
        return marcacoes.stream()
                .filter(m -> criadoPorId == null || m.getCriadoPor().getId().equals(criadoPorId))
                .filter(m -> utenteId == null || (m.getMarcacaoSecretaria() != null && m.getMarcacaoSecretaria().getUtente().getId().equals(utenteId)))
                .filter(m -> estado == null || m.getEstado().equals(estado))
                .toList();
    }

    
    public Marcacao atualizarEstadoMarcacao(Long marcacaoId, EventoEstado novoEstado, Utilizador atualizadoPor) {
        if (marcacaoId == null || novoEstado == null || atualizadoPor == null) {
            throw new IllegalArgumentException(" Argumento não pode ser nulo");
        }   
        Marcacao marcacao = marcacaoRepository.findById(marcacaoId).orElseThrow(() -> new RuntimeException("Marcação não encontrada"));

        if (atualizadoPor instanceof Utente utente && utente.equals(marcacao.getCriadoPor()) && novoEstado.equals(EventoEstado.CANCELADO)) {
            // Utente que criou a marcação pode apenas marcar com cancelado
            marcacao.setEstado(novoEstado);
            return marcacaoRepository.save(marcacao);
        }

        if (atualizadoPor instanceof Funcionario funcionario) {
            validarFuncionarioSecretaria(funcionario); // Apenas secretaria pode atualizar estado
            marcacao.setEstado(novoEstado);
            return marcacaoRepository.save(marcacao);
        }
        else {
            throw new RuntimeException("Apenas funcionários podem atualizar o estado da marcação");
        }
        
    }

    
    public List<Marcacao> consultarMarcacoesPassadas(LocalDateTime dataInicio) {
        // Se não foram fornecidas datas, buscar desde o início até agora
        LocalDateTime inicio = dataInicio != null ? dataInicio : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime fim = LocalDateTime.now();
        
        List<Marcacao> marcacoes = marcacaoRepository.findMarcacoesBetweenDates(inicio, fim);
        
        return marcacoes.stream()
                .filter(m -> m.getData().isBefore(LocalDateTime.now()))
                // Apenas estados finalizados: CONCLUIDO, CANCELADO, AVISO (não compareceu)
                .filter(m -> m.getEstado() == EventoEstado.CONCLUIDO || 
                            m.getEstado() == EventoEstado.CANCELADO || 
                            m.getEstado() == EventoEstado.AVISO)
                .toList();
    }



    
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

    
    public Optional<Marcacao> findById(Long id) {
        if (id == null) { throw new IllegalArgumentException("ID não pode ser nulo"); }
        return marcacaoRepository.findById(id);
    }

    
    public List<Marcacao> findAll() { return marcacaoRepository.findAll(); }

    
    public void deleteById(Long id) {
        if (id == null) { throw new IllegalArgumentException("ID não pode ser nulo"); }
        marcacaoRepository.deleteById(id);
    }

    
    public void notificarDocumentosInvalidos(Long marcacaoId, String observacoes, Funcionario notificadoPor) {
        if (marcacaoId == null || notificadoPor == null || observacoes == null) { throw new IllegalArgumentException(" Argumento não pode ser nulo"); }

        Marcacao marcacao = marcacaoRepository.findById(marcacaoId).orElseThrow(() -> new RuntimeException("Marcação não encontrada"));
        
        // Apenas secretaria pode notificar documentos inválidos
        validarFuncionarioSecretaria(notificadoPor);
        
        notificarUtenteMarcacao(marcacao, "DOCUMENTOS_INVALIDOS");
    }

    
    public List<Marcacao> consultarMarcacoesUtente(Utente utente) { return marcacaoRepository.findByUtente(utente); }

    
    public List<Marcacao> consultarMarcacoesFuncionario(Funcionario funcionario) { return marcacaoRepository.findByCriadoPor(funcionario); }

    // Métodos privados auxiliares
    private void validarDisponibilidade(LocalDateTime data) {
        if (data.isBefore(LocalDateTime.now())) { throw new RuntimeException("Não é possível agendar marcações para datas passadas"); }
        // TODO: Lógica de matriz de horários pode ser adicionada aqui
    }
    
    private void validarFuncionarioSecretaria(Funcionario funcionario) {
        // Verificar se o funcionário pertence à secretaria
        if (funcionario.getTipo() != FuncionarioTipo.SECRETARIA) { throw new RuntimeException("Apenas funcionários da secretaria podem realizar esta ação"); }
    }
    
    
    private void notificarUtenteMarcacao(Marcacao marcacao, String tipoNotificacao) {
        String mensagem = "";
        String assunto = "";
        
        switch (tipoNotificacao) {
            case "NOVA_MARCACAO" -> {
                assunto = "Nova Marcação Criada";
                mensagem = "A sua marcação para %s foi agendada com sucesso.".formatted(
                        marcacao.getData());
            }
            case "CANCELAMENTO" -> {
                assunto = "Marcação Cancelada";
                mensagem = "A sua marcação foi cancelada.";
            }
            case "DOCUMENTOS_INVALIDOS" -> {
                assunto = "Documentos Inválidos";
                mensagem = "Os documentos apresentados são inválidos. Por favor, contacte a secretaria.";
            }
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
            Integer.valueOf(nif);
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
            System.out.println("Email enviado para " + utente.getEmail() + " com token: " + token + " e mensagem: " + mensagem);
        }
    }
    
    private String gerarToken() { return String.valueOf((int) ((ThreadLocalRandom.current().nextDouble() * 900000) + 100000)); }
    
    // ======================== Métodos de lógica de negócio complexa (delegados do controller) ========================
    
    
    public Marcacao criarMarcacaoRemotaComRequest(CriarMarcacaoRequest request) {
        Utilizador utilizador = utilizadorRepository.findById(request.getUtenteId())
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado com ID: " + request.getUtenteId()));

        if (!(utilizador instanceof Utente)) {
            throw new RuntimeException("O utilizador com ID " + request.getUtenteId() + 
                " é um " + utilizador.getClass().getSimpleName() + ", não um Utente. " +
                "Apenas utentes podem criar marcações remotas.");
        }
        
        Utente utente = (Utente) utilizador;
        Marcacao marcacao = criarMarcacaoRemota(request.getData(), request.getAssunto(), utente);

        System.out.println("Marcação remota criada com sucesso: " + marcacao.getId());
        
        return marcacao;
    }
    
    
    public MarcacaoResponseDTO atualizarEstadoMarcacaoDTO(Long id, AtualizarEstadoRequest request) {
        Utilizador atualizadoPor = utilizadorRepository.findById(request.getFuncionarioId())
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado"));

        Marcacao marcacao = atualizarEstadoMarcacao(id, request.getNovoEstadoEnum(), atualizadoPor);
        return converterParaDTO(marcacao);
    }
    
    
    public MarcacaoResponseDTO notificarDocumentosInvalidosDTO(Long id, NotificarDocumentosRequest request) {
        Funcionario notificadoPor = funcionarioRepository.findById(request.getFuncionarioId())
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));

        notificarDocumentosInvalidos(id, request.getObservacoes(), notificadoPor);
        
        Marcacao marcacao = findById(id)
                .orElseThrow(() -> new RuntimeException("Marcação não encontrada"));
                
        return converterParaDTO(marcacao);
    }
    
    
    public List<MarcacaoResponseDTO> consultarMarcacoesUtenteDTO(Long utenteId) {
        Utente utente = utenteRepository.findById(utenteId)
                .orElseThrow(() -> new RuntimeException("Utente não encontrado"));
        
        return consultarMarcacoesUtente(utente).stream().map(this::converterParaDTO).toList();
    }
    
    
    public List<java.util.Map<String, Object>> consultarMarcacoesBloqueadas(Long utenteId) {
        // Verificar se utente existe
        if (!utenteRepository.existsById(utenteId)) {
            throw new RuntimeException("Utente não encontrado");
        }
        
        List<Marcacao> todasMarcacoes = findAll();
        
        // Filtrar marcações que NÃO são do utente
        return todasMarcacoes.stream()
            .filter(m -> m.getMarcacaoSecretaria() != null && 
                       m.getMarcacaoSecretaria().getUtente() != null &&
                       !m.getMarcacaoSecretaria().getUtente().getId().equals(utenteId))
            .map(m -> {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("id", m.getId());
                map.put("data", m.getData());
                return map;
            })
            .toList();
    }
    
    
    public List<MarcacaoResponseDTO> consultarMarcacoesFuncionarioDTO(Long funcionarioId) {
        Funcionario funcionario = funcionarioRepository.findById(funcionarioId).orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));
        
        return consultarMarcacoesFuncionario(funcionario).stream().map(this::converterParaDTO).toList();
    }
    
    // ======================== Métodos que retornam DTOs (delegados do controller) ========================
    
    
    public MarcacaoResponseDTO converterParaDTO(Marcacao marcacao) {
        MarcacaoResponseDTO dto = new MarcacaoResponseDTO();
        dto.setId(marcacao.getId());
        dto.setData(marcacao.getData());
        dto.setEstado(marcacao.getEstado());
        
        if (marcacao.getMarcacaoSecretaria() != null) {
            MarcacaoResponseDTO.MarcacaoSecretariaDTO secDTO = new MarcacaoResponseDTO.MarcacaoSecretariaDTO();
            secDTO.setAssunto(marcacao.getMarcacaoSecretaria().getAssunto());
            secDTO.setDescricao(marcacao.getMarcacaoSecretaria().getDescricao());
            secDTO.setTipoAtendimento(marcacao.getMarcacaoSecretaria().getTipoAtendimento());
            
            if (marcacao.getMarcacaoSecretaria().getUtente() != null) {
                Utente utente = marcacao.getMarcacaoSecretaria().getUtente();
                MarcacaoResponseDTO.UtenteDTO utenteDTO = new MarcacaoResponseDTO.UtenteDTO();
                utenteDTO.setId(utente.getId());
                utenteDTO.setNome(utente.getNome());
                utenteDTO.setEmail(utente.getEmail());
                utenteDTO.setNif(utente.getNif());
                utenteDTO.setTelefone(utente.getTelefone());
                secDTO.setUtente(utenteDTO);
            }
            
            dto.setMarcacaoSecretaria(secDTO);
        }
        
        return dto;
    }
    
    
    public List<MarcacaoResponseDTO> listarTodasMarcacoesDTO() {
        return findAll().stream().map(this::converterParaDTO).toList();
    }
    
    
    public MarcacaoResponseDTO obterMarcacaoDTO(Long id) {
        Marcacao marcacao = findById(id).orElseThrow(() -> new RuntimeException("Marcação não encontrada"));
        return converterParaDTO(marcacao);
    }
    
    
    public List<MarcacaoResponseDTO> consultarAgendaDTO(LocalDateTime dataInicio, LocalDateTime dataFim) {
        return consultarAgenda(dataInicio, dataFim).stream().map(this::converterParaDTO).toList();
    }
    
    
    public List<MarcacaoResponseDTO> procurarAgendaDTO(LocalDateTime dataInicio, LocalDateTime dataFim, Long criadoPorId, Long utenteId, EventoEstado estado) {
        return procurarAgenda(dataInicio, dataFim, criadoPorId, utenteId, estado).stream().map(this::converterParaDTO).toList();
    }
    
    
    public List<MarcacaoResponseDTO> consultarMarcacoesPassadasDTO(LocalDateTime dataInicio, LocalDateTime dataFim, Long utenteId, EventoEstado estado) {
        return consultarMarcacoesPassadas(dataInicio, dataFim, utenteId, estado).stream().map(this::converterParaDTO).toList();
    }
}