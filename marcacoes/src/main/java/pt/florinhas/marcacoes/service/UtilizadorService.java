package pt.florinhas.marcacoes.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.florinhas.marcacoes.domain.Funcionario;
import pt.florinhas.marcacoes.domain.FuncionarioTipo;
import pt.florinhas.marcacoes.domain.Utente;
import pt.florinhas.marcacoes.domain.Utilizador;
import pt.florinhas.marcacoes.dto.CreateUserRequestDTO;
import pt.florinhas.marcacoes.dto.RecoverAccountDTO;
import pt.florinhas.marcacoes.dto.UtilizadorInfoDTO;
import pt.florinhas.marcacoes.dto.UtilizadorResponseDTO;
import pt.florinhas.marcacoes.repository.FuncionarioRepository;
import pt.florinhas.marcacoes.repository.UtenteRepository;
import pt.florinhas.marcacoes.repository.UtilizadorRepository;
import pt.florinhas.marcacoes.service.email.EmailService;
import pt.florinhas.marcacoes.validation.NifValidator;

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
    private NifValidator nifValidator;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /*
     * =========================================================
     * CONSULTAS
     * =========================================================
     */

    public Utilizador buscarPorEmail(String email) {
        List<Utilizador> users = utilizadorRepository.findByEmail(email);
        if (users.isEmpty()) {
            throw new RuntimeException("Utilizador não encontrado com email: " + email);
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

        List<Utilizador> results = utilizadorRepository.findByNif(searchNif);
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
        nifValidator.validateRequiredOrThrow(nif);

        // Verificar se já existe
        List<Utilizador> existingUsers = utilizadorRepository.findByNif(nif);
        Optional<Utilizador> existingUser = existingUsers.isEmpty() ? Optional.empty()
                : Optional.of(existingUsers.get(0));

        if (existingUser.isPresent()) {
            Utilizador u = existingUser.get();
            if (u instanceof Utente) {
                return (Utente) u;
            } else {
                throw new RuntimeException(
                        "Este NIF (" + nif + ") já está registado como Funcionário. Não pode ser usado como Utente.");
            }
        }

        // Se não existir, criar novo utente

        log.info("Utente com NIF {} não encontrado. Criando novo utente...", nif);

        validarCampoObrigatorio(nome, "Nome do utente é obrigatório para criar novo registo");
        validarCampoObrigatorio(email, "Email do utente é obrigatório para criar novo registo");
        validarCampoObrigatorio(telefone, "Telefone do utente é obrigatório para criar novo registo");

        if (utenteRepository.existsByEmail(email)) {
            throw new RuntimeException("Email já está registado no sistema");
        }

        Utente novoUtente = new Utente();
        novoUtente.setNif(nif);
        novoUtente.setNome(nome);
        novoUtente.setEmail(email);
        novoUtente.setTelefone(telefone);
        novoUtente.setActivo(false); // Inactivo até dar login pela primeira vez

        log.info("[AUTO-CREATE] utente nif={} setActivo=false", nif);

        String passwordInicial = gerarPasswordSegura();
        novoUtente.setPassHash(passwordEncoder.encode(passwordInicial));
        novoUtente.setDataNasc(LocalDate.now());

        log.info("Novo utente criado. Enviando password para: {}", email);

        try {
            emailService.sendPassword(email, passwordInicial);
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
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado com ID: " + utilizadorId));
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
                    throw new RuntimeException("Email já está em uso por outro utilizador");
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
                throw new RuntimeException("Formato de data inválido. Use YYYY-MM-DD");
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

        return utilizadorRepository.save(utilizador);
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

    public List<UtilizadorResponseDTO> listarFuncionariosPendentes() {
        return funcionarioRepository.findByActivoFalse().stream()
                .map(UtilizadorResponseDTO::fromUtilizador)
                .collect(Collectors.toList());
    }

    @Transactional
    public void aprovarFuncionario(Long id) {
        Funcionario funcionario = funcionarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado com ID: " + id));

        funcionario.setActivo(true);
        funcionarioRepository.save(funcionario);
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
        nifValidator.validateRequiredOrThrow(request.getNif());
        if (utilizadorRepository.existsByNif(request.getNif())) {
            throw new RuntimeException("Já existe um utilizador com este NIF.");
        }
        if (utilizadorRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Já existe um utilizador com este Email.");
        }

        Utilizador novoUtilizador;

        if (request.isEmployee()) {
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
        novoUtilizador.setEmail(request.getEmail());

        try {
            LocalDate dataNasc = LocalDate.parse(request.getBirthDate(), DateTimeFormatter.ISO_LOCAL_DATE);
            novoUtilizador.setDataNasc(dataNasc);
        } catch (Exception e) {
            novoUtilizador.setDataNasc(LocalDate.now());
        }

        String passwordInicial = gerarPasswordSegura();
        novoUtilizador.setPassHash(passwordEncoder.encode(passwordInicial));

        Utilizador salvo = utilizadorRepository.save(novoUtilizador);

        try {
            emailService.sendPassword(novoUtilizador.getEmail(), passwordInicial);
        } catch (Exception e) {
            log.error("Erro ao enviar email de criação: {}", e.getMessage());
        }

        System.out.println(">>> DEBUG PASSWORD (CREATE): " + passwordInicial);
        return salvo;
    }

    /**
     * Inicia o processo de recuperação de conta pela secretaria.
     */
    @Transactional
    public void recuperarConta(RecoverAccountDTO request) {
        List<Utilizador> users = utilizadorRepository.findByNif(request.getNif());
        if (users.isEmpty()) {
            throw new RuntimeException("Utilizador não encontrado com NIF: " + request.getNif());
        }
        Utilizador utilizador = users.get(0);

        if (request.getUpdatedEmail() != null && !request.getUpdatedEmail().isEmpty()
                && !request.getUpdatedEmail().equals(utilizador.getEmail())) {

            if (utilizadorRepository.existsByEmail(request.getUpdatedEmail())) {
                throw new RuntimeException("O novo email já está associado a outra conta.");
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

        utilizadorRepository.save(utilizador);

        try {
            System.out.println(">>> DEBUG PASSWORD (RECOVER): " + novaPassword);
            emailService.sendPassword(utilizador.getEmail(), novaPassword);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao enviar email de recuperação: " + e.getMessage());
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

    /*
     * =========================================================
     * MÉTODOS AUXILIARES
     * =========================================================
     */

    // Character set aligned with the updated flow used in other account creation paths.
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Gera uma password temporária segura com ~130 bits de entropia.
     * Usa apenas caracteres alfanuméricos para evitar ambiguidades na cópia do email
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
            throw new RuntimeException(mensagemErro);
        }
    }
}
