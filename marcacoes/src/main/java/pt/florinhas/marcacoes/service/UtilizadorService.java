package pt.florinhas.marcacoes.service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import pt.florinhas.common_data.repository.FuncionarioRepository;
import pt.florinhas.common_data.repository.UtenteRepository;
import pt.florinhas.common_data.repository.UtilizadorRepository;
import pt.florinhas.common_data.validation.NifValidator;
import pt.florinhas.common_data.domain.Funcionario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pt.florinhas.common_data.domain.FuncionarioTipo;
import pt.florinhas.common_data.domain.Utente;
import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.common_data.dto.UtilizadorInfoDTO;
import pt.florinhas.common_data.dto.UtilizadorResponseDTO;
import pt.florinhas.common_data.exception.BadRequestException;
import pt.florinhas.common_data.security.CryptoUtils;

import pt.florinhas.marcacoes.dto.CreateUserRequestDTO;
import pt.florinhas.marcacoes.dto.RecoverAccountDTO;
import pt.florinhas.marcacoes.exception.ConflictException;
import pt.florinhas.marcacoes.exception.NotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import pt.florinhas.marcacoes.repository.DocumentoRepository;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;
import pt.florinhas.marcacoes.service.email.EmailService;

/**
 * Serviço responsável pela gestão de utilizadores e utentes.
 *
 * Responsabilidades:
 * - Pesquisa de utilizadores (por ID, NIF).
 * - Criação automática de utentes (on-demand).
 * - Atualização de dados pessoais e profissionais.
 * - Gestão de funcionários (aprovações).
 * - Funcionalidades de secretaria (criação e recuperação de conta).
 * - Contagem de utentes ativos.
 */
@Service
@Slf4j
public class UtilizadorService {

    @Autowired
    private UtilizadorRepository utilizadorRepository;

    @Autowired
    private UtenteRepository utenteRepository;

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private NotificacaoService notificacaoService;

    @Autowired
    private NifValidator nifValidator;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private DocumentoRepository documentoRepository;
    
    @Autowired
    private MarcacaoRepository marcacaoRepository;

    @Autowired
    private CryptoUtils cryptoUtils;

    /*
     * =========================================================
     * CONSULTAS
     * =========================================================
     */

    public Utilizador buscarPorEmail(String email) {
        List<Utilizador> users = utilizadorRepository.findByEmail(email);
        if (users.isEmpty()) {
            throw new NotFoundException("Utilizador não encontrado com email: " + email);
        }
        return users.get(0);
    }

    /**
     * Procura um utilizador por NIF.
     *
     * @param nif NIF a pesquisar
     * @return Optional com Utilizador, se existir
     */
    public Optional<Utilizador> buscarPorNif(String nif) {
        if (nif == null || nif.trim().isEmpty()) {
            return Optional.empty();
        }
        String searchNif = nif.trim();
        log.info("Searching for user with NIF: '{}'", searchNif);

        List<Utilizador> results = utilizadorRepository.findByNifHash(cryptoUtils.generateBlindIndex(searchNif));
        if (!results.isEmpty()) {
            log.trace("Found user: ID={}", results.get(0).getId());
            return Optional.of(results.get(0));
        } else {
            log.debug("User with NIF '{}' NOT FOUND in database.", searchNif);
            return Optional.empty();
        }
    }

    /*
     * =========================================================
     * GESTÃO DE UTENTES (AUTO-CRIAÇÃO)
     * =========================================================
     */

    /**
     * Obtém um utente existente ou cria automaticamente um novo.
     * Usado quando a secretaria cria uma marcação para um utente que ainda não
     * existe.
     *
     * @param nif      NIF do utente
     * @param nome     nome do utente
     * @param email    email do utente
     * @param telefone telefone do utente
     * @return utente existente ou recém-criado
     */
    @Transactional
    public Utente obterOuCriarUtente(String nif, String nome, String email, String telefone) {
        // Verificar se já existe
        List<Utilizador> existingUsers = utilizadorRepository.findByNifHash(cryptoUtils.generateBlindIndex(nif));
        Optional<Utilizador> existingUser = existingUsers.isEmpty() ? Optional.empty()
                : Optional.of(existingUsers.get(0));

        if (existingUser.isPresent()) {
            Utilizador u = existingUser.get();
            if (u instanceof Utente) {
                return (Utente) u;
            } else {
                throw new ConflictException(
                        "Este NIF (" + nif + ") já está registado como Funcionário. Não pode ser usado como Utente.");
            }
        }

        // Se não existir, criar novo utente

        log.info("Utente com NIF {} não encontrado. Criando novo utente...", nif);
        nifValidator.validateRequiredOrThrow(nif);

        validarCampoObrigatorio(nome, "Nome do utente é obrigatório para criar novo registo");
        validarCampoObrigatorio(telefone, "Telefone do utente é obrigatório para criar novo registo");

        if (email != null && !email.isBlank() && utenteRepository.existsByEmail(email)) {
            throw new ConflictException("Email já está registado no sistema");
        }

        Utente novoUtente = new Utente();
        novoUtente.setNif(nif);
        novoUtente.setNome(nome);
        if (email != null && !email.isBlank()) novoUtente.setEmail(email);
        novoUtente.setTelefone(telefone);
        novoUtente.setActivo(false);

        log.info("[AUTO-CREATE] utente nif={} setActivo=false", nif);

        String passwordInicial = gerarPasswordSegura();
        novoUtente.setPassHash(passwordEncoder.encode(passwordInicial));
        novoUtente.setDataNasc(LocalDate.now());

        try {
            if (email != null && !email.isBlank()) emailService.sendPassword(email, passwordInicial);
        } catch (Exception e) {
            log.error("Erro ao enviar email: {}", e.getMessage());
        }

        Utente saved = utenteRepository.save(novoUtente);
        log.info("[AUTO-CREATE] utente id={} nif={} activo={}", saved.getId(), saved.getNif(), saved.isActivo());
        return saved;
    }

    /*
     * =========================================================
     * GESTÃO DE UTILIZADORES (PERFIL)
     * =========================================================
     */

    public Utilizador obterUtilizadorPorId(Long utilizadorId) {
        return utilizadorRepository.findById(utilizadorId)
                .orElseThrow(() -> new NotFoundException("Utilizador não encontrado com ID: " + utilizadorId));
    }

    /**
     * Atualiza dados pessoais e profissionais de um utilizador.
     * Apenas os campos fornecidos no DTO são alterados.
     */
    @Transactional
    public Utilizador atualizarUtilizador(Long utilizadorId, UtilizadorInfoDTO request) {

        Utilizador utilizador = obterUtilizadorPorId(utilizadorId);

        if (request.getNome() != null && !request.getNome().trim().isEmpty()) {
            utilizador.setNome(request.getNome());
        }

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            List<Utilizador> users = utilizadorRepository.findByEmail(request.getEmail());
            if (!users.isEmpty()) {
                Utilizador u = users.get(0);
                if (!u.getId().equals(utilizadorId)) {
                    throw new ConflictException("Email já está em uso por outro utilizador");
                }
            }
            utilizador.setEmail(request.getEmail());
        }

        if (request.getTelefone() != null) {
            utilizador.setTelefone(request.getTelefone());
        }

        if (request.getDataNasc() != null && !request.getDataNasc().trim().isEmpty()) {
            try {
                LocalDate dataNasc = LocalDate.parse(
                        request.getDataNasc(),
                        DateTimeFormatter.ISO_LOCAL_DATE);
                utilizador.setDataNasc(dataNasc);
            } catch (Exception e) {
                throw new BadRequestException("Formato de data inválido. Use YYYY-MM-DD");
            }
        }

        if (request.getMorada() != null) {
            utilizador.setMorada(request.getMorada());
        }

        if (request.getCodigoPostal() != null) {
            utilizador.setCodigoPostal(request.getCodigoPostal());
        }

        if (request.getFreguesia() != null) {
            utilizador.setFreguesia(request.getFreguesia());
        }

        if (request.getTelefoneEmprego() != null) {
            utilizador.setTelefoneEmprego(request.getTelefoneEmprego());
        }

        if (request.getLocalEmprego() != null) {
            utilizador.setLocalEmprego(request.getLocalEmprego());
        }

        if (request.getMoradaEmprego() != null) {
            utilizador.setMoradaEmprego(request.getMoradaEmprego());
        }

        if (request.getProfissao() != null) {
            utilizador.setProfissao(request.getProfissao());
        }

        Utilizador atualizado = utilizadorRepository.save(utilizador);

        auditLogService.log(
            "ATUALIZAR_PERFIL",
            "UTILIZADOR",
            utilizadorId,
            String.format("Perfil atualizado: %s", utilizador.getNome())
        );

        return atualizado;
    }

    /*
     * =========================================================
     * GESTÃO DE FUNCIONÁRIOS
     * =========================================================
     */

    public List<UtilizadorResponseDTO> listarTodosFuncionarios() {
        return funcionarioRepository.findAll().stream()
                .map(UtilizadorResponseDTO::fromUtilizador)
                .collect(Collectors.toList());
    }

    public Page<UtilizadorResponseDTO> pesquisarFuncionarios(String nome, FuncionarioTipo tipo, String nif, Pageable pageable) {
        String nifHash = (nif != null && !nif.isBlank()) ? cryptoUtils.generateBlindIndex(nif) : null;
        return funcionarioRepository.findByNomeAndTipoFilter(nome, tipo, nifHash, pageable)
                .map(UtilizadorResponseDTO::fromUtilizador);
    }

    public List<UtilizadorResponseDTO> listarFuncionariosPendentes() {
        return funcionarioRepository.findByActivoFalse().stream()
                .map(UtilizadorResponseDTO::fromUtilizador)
                .collect(Collectors.toList());
    }

    /**
     * Lista todos os utentes (ativos e inativos).
     */
    public List<UtilizadorResponseDTO> listarTodosUtentes() {
        return utenteRepository.findAll().stream()
                .map(UtilizadorResponseDTO::fromUtilizador)
                .collect(Collectors.toList());
    }

    public Page<UtilizadorResponseDTO> pesquisarUtentes(String nome, String nif, Pageable pageable) {
        String nifHash = (nif != null && !nif.isBlank()) ? cryptoUtils.generateBlindIndex(nif) : null;
        return utenteRepository.findByNomeFilter(nome, nifHash, pageable)
                .map(UtilizadorResponseDTO::fromUtilizador);
    }

    @Transactional
    public void aprovarFuncionario(Long id) {
        Funcionario funcionario = funcionarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Funcionário não encontrado com ID: " + id));

        funcionario.setActivo(true);
        funcionarioRepository.save(funcionario);

        auditLogService.log(
            "APROVAR_FUNCIONARIO",
            "FUNCIONARIO",
            id,
            String.format("Funcionário aprovado: %s (%s) - Tipo: %s", 
                funcionario.getNome(), funcionario.getEmail(), funcionario.getTipo())
        );
    }

    /*
     * =========================================================
     * FUNCIONALIDADES DE SECRETARIA
     * =========================================================
     */

    /**
     * Cria um utilizador (Utente ou Funcionário) pela secretaria.
     */
    @Transactional
    public Utilizador criarUtilizadorPelaSecretaria(CreateUserRequestDTO request) {
        if (utilizadorRepository.existsByNifHash(cryptoUtils.generateBlindIndex(request.getNif()))) {
            throw new ConflictException("Já existe um utilizador com este NIF.");
        }
        boolean hasEmail = request.getEmail() != null && !request.getEmail().isBlank();
        if (hasEmail && utilizadorRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Já existe um utilizador com este Email.");
        }

        Utilizador novoUtilizador;

        boolean employeeByRole = request.getRole() != null
                && !request.getRole().trim().isEmpty()
                && !"UTENTE".equalsIgnoreCase(request.getRole().trim());
        boolean shouldCreateEmployee = request.isEmployee() || employeeByRole;

        if (shouldCreateEmployee) {
            Funcionario f = new Funcionario();
            try {
                String roleStr = request.getRole();
                FuncionarioTipo tipo = FuncionarioTipo.OUTRO;
                if (roleStr != null) {
                    String normalizedRole = roleStr.trim().toUpperCase();
                    if (normalizedRole.equals("SECRETARIA") || normalizedRole.equals("SECRETARY")) {
                        tipo = FuncionarioTipo.SECRETARIA;
                    } else if (normalizedRole.equals("BALNEARIO") || normalizedRole.equals("BALNEÁRIO")
                            || normalizedRole.contains("BALNE")) {
                        tipo = FuncionarioTipo.BALNEARIO;
                    } else if (normalizedRole.equals("ESCOLA") || normalizedRole.equals("SCHOOL")) {
                        tipo = FuncionarioTipo.ESCOLA;
                    } else if (normalizedRole.equals("INTERNO") || normalizedRole.equals("INTERNOS")
                            || normalizedRole.contains("INTERNO")) {
                        tipo = FuncionarioTipo.INTERNO;
                    }
                }
                f.setTipo(tipo);
            } catch (Exception e) {
                f.setTipo(FuncionarioTipo.OUTRO);
            }
            f.setActivo(false);
            novoUtilizador = f;
        } else {
            Utente u = new Utente();
            u.setActivo(false);
            novoUtilizador = u;
        }

        novoUtilizador.setNif(request.getNif());
        novoUtilizador.setNome(request.getName());
        novoUtilizador.setTelefone(request.getContact());
        if (hasEmail) novoUtilizador.setEmail(request.getEmail());

        try {
            LocalDate dataNasc = LocalDate.parse(request.getBirthDate(), DateTimeFormatter.ISO_LOCAL_DATE);
            novoUtilizador.setDataNasc(dataNasc);
        } catch (Exception e) {
            novoUtilizador.setDataNasc(LocalDate.now());
        }

        String passwordInicial = gerarPasswordSegura();
        novoUtilizador.setPassHash(passwordEncoder.encode(passwordInicial));

        Utilizador salvo = utilizadorRepository.save(novoUtilizador);

        auditLogService.log(
            "CRIAR_CONTA",
            "UTILIZADOR",
            salvo.getId(),
            String.format("Conta criada pela secretaria: %s (%s) - Tipo: %s", 
                salvo.getNome(), salvo.getEmail(), 
                salvo instanceof Funcionario ? ((Funcionario)salvo).getTipo() : "UTENTE")
        );

        try {
            if (hasEmail) emailService.sendPassword(novoUtilizador.getEmail(), passwordInicial);
        } catch (Exception e) {
            log.error("Erro ao enviar email de criação: {}", e.getMessage());
        }
        return salvo;
    }

    /**
     * Inicia o processo de recuperação de conta pela secretaria.
     */
    @Transactional
    public void recuperarConta(RecoverAccountDTO request) {
        List<Utilizador> users = utilizadorRepository.findByNifHash(cryptoUtils.generateBlindIndex(request.getNif()));
        if (users.isEmpty()) {
            throw new NotFoundException("Utilizador não encontrado com NIF: " + request.getNif());
        }
        Utilizador utilizador = users.get(0);

        if (request.getUpdatedEmail() != null && !request.getUpdatedEmail().isEmpty()
                && !request.getUpdatedEmail().equals(utilizador.getEmail())) {

            if (utilizadorRepository.existsByEmail(request.getUpdatedEmail())) {
                throw new ConflictException("O novo email já está associado a outra conta.");
            }
            utilizador.setEmail(request.getUpdatedEmail());
        }

        if (request.getUpdatedContact() != null && !request.getUpdatedContact().isEmpty()
                && !request.getUpdatedContact().equals(utilizador.getTelefone())) {
            utilizador.setTelefone(request.getUpdatedContact());
        }

        if (utilizador instanceof Utente) {
            ((Utente) utilizador).setActivo(false);
        } else if (utilizador instanceof Funcionario) {
            ((Funcionario) utilizador).setActivo(false);
        }

        String novaPassword = gerarPasswordSegura();
        utilizador.setPassHash(passwordEncoder.encode(novaPassword));
        utilizador.setOtpExpiresAt(LocalDateTime.now().plusMinutes(15));

        utilizadorRepository.save(utilizador);
        
        auditLogService.log(
            "RECUPERAR_CONTA",
            "UTILIZADOR",
            utilizador.getId(),
            String.format("Conta recuperada pela secretaria: %s (%s)", 
                utilizador.getNome(), utilizador.getNif())
        );

        try {
            emailService.sendPassword(utilizador.getEmail(), novaPassword);
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao enviar email de recuperação: " + e.getMessage(), e);
        }
    }

    /*
     * =========================================================
     * ESTATÍSTICAS
     * =========================================================
     */

    public long contarUtentesAtivos() {
        return utenteRepository.countByActivo(true);
    }

    public long contarFuncionariosAtivos() {
        return funcionarioRepository.countByActivo(true);
    }

    /**
     * Gera um código presencial curto (6 dígitos) com validade de 10 minutos.
     * O código é definido como password temporária do utilizador.
     */
    @Transactional
    public String gerarCodigoPresencial(String nif) {
        List<Utilizador> users = utilizadorRepository.findByNifHash(cryptoUtils.generateBlindIndex(nif));
        if (users.isEmpty()) {
            throw new NotFoundException("Utilizador não encontrado com NIF: " + nif);
        }
        Utilizador utilizador = users.get(0);

        String codigo = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
        utilizador.setPassHash(passwordEncoder.encode(codigo));
        utilizador.setOtpExpiresAt(LocalDateTime.now().plusMinutes(10));

        if (utilizador instanceof Utente) {
            ((Utente) utilizador).setActivo(false);
        } else if (utilizador instanceof Funcionario) {
            ((Funcionario) utilizador).setActivo(false);
        }

        utilizadorRepository.save(utilizador);

        auditLogService.log(
            "GERAR_CODIGO_PRESENCIAL",
            "UTILIZADOR",
            utilizador.getId(),
            String.format("Código presencial gerado para: %s", utilizador.getNome())
        );

        return codigo;
    }

    /*
     * =========================================================
     * DIREITO AO ESQUECIMENTO (RGPD Art.º 17)
     * =========================================================
     */

    /**
     * Solicita eliminação de conta pelo próprio utilizador.
     * Marca flag na BD e notifica secretaria para processar anonimização.
     */
    @Transactional
    public void solicitarEliminacaoConta() {
        Utilizador utilizador = buscarUtilizadorAutenticado();
        
        if (Boolean.TRUE.equals(utilizador.getDeleteRequested())) {
            throw new BadRequestException("Já existe um pedido de eliminação pendente para esta conta.");
        }

        utilizador.setDeleteRequested(true);
        utilizador.setDeleteRequestedAt(LocalDateTime.now());
        utilizadorRepository.save(utilizador);

        log.info("Pedido de eliminação registado para utilizador ID: {}", utilizador.getId());

        auditLogService.log(
            "SOLICITAR_ELIMINACAO",
            "UTILIZADOR",
            utilizador.getId(),
            String.format("Pedido de eliminação registado para utilizador: %s (%s)", 
                utilizador.getNome(), utilizador.getNif())
        );

        // Notificar secretaria
        try {
            List<Funcionario> secretarias = funcionarioRepository.findByTipo(FuncionarioTipo.SECRETARIA);
            String titulo = "Pedido de Eliminação de Conta (RGPD)";
            String mensagem = String.format(
                "O utilizador %s (NIF: %s) solicitou a eliminação da sua conta. " +
                "Por favor, processe a anonimização dos dados no prazo de 1 mês conforme RGPD Art.º 17.",
                utilizador.getNome(), utilizador.getNif()
            );
            
            // Criar notificação para cada secretária
            for (Funcionario secretaria : secretarias) {
                try {
                    notificacaoService.criarNotificacao(
                        secretaria.getId(),
                        titulo,
                        mensagem,
                        "ALERTA"
                    );
                } catch (Exception e) {
                    log.error("Erro ao criar notificação para secretaria ID {}: {}", secretaria.getId(), e.getMessage());
                }
            }

            // Enviar email à secretaria
            emailService.sendGenericEmail(
                "secretaria@florinhasdovouga.pt",
                titulo,
                mensagem
            );
        } catch (Exception e) {
            log.error("Erro ao notificar secretaria sobre pedido de eliminação: {}", e.getMessage());
        }
    }

    /**
     * Anonimiza dados de um utilizador (RGPD Art.º 17).
     * Substitui dados pessoais por valores genéricos mantendo registos históricos.
     */
    @Transactional
    public void anonimizarUtilizador(Long id) {
        Utilizador utilizador = obterUtilizadorPorId(id);
        
        String nifOriginal = utilizador.getNif();
        String timestamp = String.valueOf(System.currentTimeMillis());
        
        // Anonimizar dados pessoais
        utilizador.setNome("Utilizador Anónimo #" + id);
        utilizador.setEmail("anonimo." + timestamp + "@anonimizado.local");
        utilizador.setTelefone("000000000");
        utilizador.setNif(String.format("%09d", id)); // NIF fictício único baseado no ID
        utilizador.setMorada(null);
        utilizador.setCodigoPostal(null);
        utilizador.setFreguesia(null);
        utilizador.setTelefoneEmprego(null);
        utilizador.setLocalEmprego(null);
        utilizador.setMoradaEmprego(null);
        utilizador.setProfissao(null);
        utilizador.setDataNasc(null);
        
        // Invalidar password
        utilizador.setPassHash(passwordEncoder.encode("ANONIMIZADO_" + timestamp));
        
        // Marcar como processado
        utilizador.setDeleteRequested(false);
        utilizador.setDeleteRequestedAt(null);
        
        // Desativar conta
        if (utilizador instanceof Utente) {
            ((Utente) utilizador).setActivo(false);
        } else if (utilizador instanceof Funcionario) {
            ((Funcionario) utilizador).setActivo(false);
        }
        
        utilizadorRepository.save(utilizador);
        
        auditLogService.log(
            "ANONIMIZAR_UTILIZADOR",
            "UTILIZADOR",
            id,
            String.format("Utilizador anonimizado (NIF original: %s) - RGPD Art.º 17", nifOriginal)
        );
        
        log.info("Utilizador ID {} (NIF original: {}) foi anonimizado com sucesso", id, nifOriginal);
    }

    /**
     * Anonimiza um utilizador e desativa a conta (RGPD Art.º 17).
     * O registo é mantido para preservar integridade referencial do histórico
     * (Marcacao, BloqueioAgenda, etc.). Os dados pessoais são substituídos por
     * valores genéricos e a conta é desativada permanentemente.
     */
    @Transactional
    public void anonimizarEEliminarUtilizador(Long id) {
        Utilizador utilizador = obterUtilizadorPorId(id);
        String nifOriginal = utilizador.getNif();
        String nomeOriginal = utilizador.getNome();

        anonimizarUtilizador(id);

        auditLogService.log(
            "ELIMINAR_UTILIZADOR",
            "UTILIZADOR",
            id,
            String.format("Utilizador anonimizado e desativado (Nome: %s, NIF: %s) - RGPD Art.º 17",
                nomeOriginal, nifOriginal)
        );

        log.info("Utilizador ID {} (NIF original: {}) foi anonimizado e desativado", id, nifOriginal);
    }

    /**
     * Obtém o utilizador autenticado do contexto de segurança.
     */
    private Utilizador buscarUtilizadorAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new BadRequestException("Utilizador não autenticado");
        }
        
        String email = auth.getName();
        return buscarPorEmail(email);
    }

    /** Alias público para uso no controller. */
    public Utilizador getUtilizadorAutenticado() {
        return buscarUtilizadorAutenticado();
    }

    /*
     * =========================================================
     * DIREITO DE PORTABILIDADE (RGPD Art.º 20)
     * =========================================================
     */

    /**
     * Exporta todos os dados pessoais do utilizador autenticado.
     * Retorna JSON com dados de utilizador, documentos, marcações e requisições.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> exportarDadosUtilizador() {
        Utilizador utilizador = buscarUtilizadorAutenticado();
        
        Map<String, Object> dados = new HashMap<>();
        
        // Dados pessoais
        Map<String, Object> dadosPessoais = new HashMap<>();
        dadosPessoais.put("id", utilizador.getId());
        dadosPessoais.put("nome", utilizador.getNome());
        dadosPessoais.put("email", utilizador.getEmail());
        dadosPessoais.put("nif", utilizador.getNif());
        dadosPessoais.put("telefone", utilizador.getTelefone());
        dadosPessoais.put("dataNascimento", utilizador.getDataNasc());
        dadosPessoais.put("morada", utilizador.getMorada());
        dadosPessoais.put("codigoPostal", utilizador.getCodigoPostal());
        dadosPessoais.put("freguesia", utilizador.getFreguesia());
        dadosPessoais.put("profissao", utilizador.getProfissao());
        dadosPessoais.put("localEmprego", utilizador.getLocalEmprego());
        dadosPessoais.put("moradaEmprego", utilizador.getMoradaEmprego());
        dadosPessoais.put("telefoneEmprego", utilizador.getTelefoneEmprego());
        dadosPessoais.put("dataCriacao", utilizador.getCreatedAt());
        dados.put("dadosPessoais", dadosPessoais);
        
        // Documentos
        try {
            if (utilizador instanceof Utente) {
                dados.put("documentos", documentoRepository.findByUtente((Utente) utilizador)
                        .stream()
                        .map(pt.florinhas.marcacoes.dto.DocumentoDTO::fromDocumento)
                        .collect(Collectors.toList()));
            } else {
                dados.put("documentos", new ArrayList<>());
            }
        } catch (Exception e) {
            log.error("Erro ao exportar documentos: {}", e.getMessage());
            dados.put("documentos", new ArrayList<>());
        }

        // Marcações
        try {
            if (utilizador instanceof Utente) {
                dados.put("marcacoes", marcacaoRepository.findByUtente((Utente) utilizador)
                        .stream()
                        .map(m -> {
                            Map<String, Object> entry = new HashMap<>();
                            entry.put("id", m.getId());
                            entry.put("data", m.getData());
                            entry.put("estado", m.getEstado());
                            entry.put("motivoCancelamento", m.getMotivoCancelamento());
                            if (m.getMarcacaoSecretaria() != null) {
                                entry.put("assunto", m.getMarcacaoSecretaria().getAssunto());
                                entry.put("descricao", m.getMarcacaoSecretaria().getDescricao());
                            }
                            return entry;
                        })
                        .collect(Collectors.toList()));
            } else {
                dados.put("marcacoes", new ArrayList<>());
            }
        } catch (Exception e) {
            log.error("Erro ao exportar marcações: {}", e.getMessage());
            dados.put("marcacoes", new ArrayList<>());
        }
        
        // Requisições
        dados.put("requisicoes", new ArrayList<>());
        
        dados.put("dataExportacao", LocalDateTime.now());
        dados.put("formatoRGPD", "Art.º 20 - Direito de Portabilidade");
        
        log.info("Dados exportados para utilizador ID: {}", utilizador.getId());
        
        return dados;
    }

    /*
     * =========================================================
     * MÉTODOS AUXILIARES
     * =========================================================
     */

    // Character set aligned with the updated flow used in other account creation
    // paths.
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Gera uma password temporária segura com ~130 bits de entropia.
     * Usa apenas caracteres alfanuméricos para evitar ambiguidades na cópia do
     * email
     * no primeiro login.
     */
    private String gerarPasswordSegura() {
        int length = 22;
        StringBuilder password = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = SECURE_RANDOM.nextInt(ALPHANUMERIC.length());
            password.append(ALPHANUMERIC.charAt(index));
        }

        return password.toString();
    }

    private void validarCampoObrigatorio(String valor, String mensagemErro) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new BadRequestException(mensagemErro);
        }
    }
}
