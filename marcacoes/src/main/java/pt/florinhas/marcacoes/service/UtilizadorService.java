package pt.florinhas.marcacoes.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.security.SecureRandom;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.florinhas.marcacoes.domain.Funcionario;
import pt.florinhas.marcacoes.domain.Utente;
import pt.florinhas.marcacoes.domain.Utilizador;
import pt.florinhas.marcacoes.dto.UtilizadorInfoDTO;
import pt.florinhas.marcacoes.dto.UtilizadorResponseDTO;
import pt.florinhas.marcacoes.repository.FuncionarioRepository;
import pt.florinhas.marcacoes.repository.UtenteRepository;
import pt.florinhas.marcacoes.repository.UtilizadorRepository;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço responsável pela gestão de utilizadores e utentes.
 *
 * Responsabilidades:
 * - Pesquisa de utilizadores (por ID, NIF).
 * - Criação automática de utentes (on-demand).
 * - Atualização de dados pessoais e profissionais.
 * - Contagem de utentes ativos.
 *
 * É utilizado por vários serviços, nomeadamente:
 * - MarcacaoService (criação de marcações presenciais).
 * - Controllers de perfil/utilizador.
 *
 * Transactional garante consistência em operações de escrita.
 */
@Service
@Transactional
@Slf4j
public class UtilizadorService {

    @Autowired
    private UtilizadorRepository utilizadorRepository;

    @Autowired
    private UtenteRepository utenteRepository;

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private pt.florinhas.marcacoes.service.email.EmailService emailService;

    @Autowired
    private pt.florinhas.marcacoes.service.nif.NifValidationService nifValidationService;

    /*
     * Encoder usado para gerar passwords temporárias de utentes
     * criados automaticamente pela secretaria.
     */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Utilizador buscarPorEmail(String email) {
        return utilizadorRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado com email: " + email));
    }

    /*
     * =========================================================
     * CONSULTAS
     * =========================================================
     */

    /**
     * Procura um utilizador por NIF.
     *
     * param nif NIF a pesquisar
     * return Optional com Utilizador, se existir
     * throws IllegalArgumentException se o NIF for inválido
     */
    public Optional<Utilizador> buscarPorNif(String nif) {
        if (nif == null || nif.trim().isEmpty()) {
            return Optional.empty();
        }
        String searchNif = nif.trim();
        log.info("Searching for user with NIF: '{}'", searchNif);

        Optional<Utilizador> result = utilizadorRepository.findByNif(searchNif);
        if (result.isPresent()) {
            log.trace("Found user: ID={}", result.get().getId());
        } else {
            log.debug("User with NIF '{}' NOT FOUND in database.", searchNif);
        }
        return result;
    }

    /*
     * =========================================================
     * CRIAÇÃO AUTOMÁTICA DE UTENTE
     * =========================================================
     */

    /**
     * Obtém um utente existente ou cria automaticamente um novo.
     *
     * Este método é usado principalmente quando a secretaria cria
     * uma marcação para um utente que ainda não existe no sistema.
     *
     * Regras:
     * - O NIF é obrigatório e identifica univocamente o utente.
     * - Se o utente não existir:
     * • Nome, email e telefone tornam-se obrigatórios.
     * • O utente é criado como INATIVO.
     * • É atribuída uma password temporária (NIF).
     *
     * param nif NIF do utente
     * param nome nome do utente
     * param email email do utente
     * param telefone telefone do utente
     * return utente existente ou recém-criado
     */
    public Utente obterOuCriarUtente(String nif, String nome, String email, String telefone) {

        // Validação Apenas de Formato (para aceitar legados válidos mas tecnicamente
        // incorretos, se já existirem)
        if (nif == null || !nif.matches("\\d{9}")) {
            throw new RuntimeException("NIF inválido (deve ter 9 dígitos numéricos).");
        }

        // 1. Verificar se JÁ existe algum utilizador com este NIF (Seja Utente ou
        // Funcionario)
        Optional<Utilizador> existingUser = utilizadorRepository.findByNif(nif);

        if (existingUser.isPresent()) {
            Utilizador u = existingUser.get();
            if (u instanceof Utente) {
                return (Utente) u;
            } else {
                throw new RuntimeException(
                        "Este NIF (" + nif + ") já está registado como Funcionário. Não pode ser usado como Utente.");
            }
        }

        // 2. Se não existir, criar novo utente

        // Validação RIGOROSA para NOVOS utilizadores
        if (!validarNIF(nif)) {
            throw new RuntimeException("NIF inválido/inexistente. Verifique o número ou utilize um NIF válido.");
        }

        log.info("Utente com NIF {} não encontrado. Criando novo utente...", nif);

        // Validar campos obrigatórios
        validarCampoObrigatorio(nome, "Nome do utente é obrigatório para criar novo registo");
        validarCampoObrigatorio(email, "Email do utente é obrigatório para criar novo registo");
        validarCampoObrigatorio(telefone, "Telefone do utente é obrigatório para criar novo registo");

        // Verificar se email já existe
        if (utenteRepository.existsByEmail(email)) {
            // Pode ser que o email pertença a um funcionário também
            throw new RuntimeException("Email já está registado no sistema");
        }

        // Criar novo utente
        Utente novoUtente = new Utente();
        novoUtente.setNif(nif);
        novoUtente.setNome(nome);
        novoUtente.setEmail(email);
        novoUtente.setTelefone(telefone);
        novoUtente.setActivo(false); // Inactivo até dar login pela primeira vez

        // Geração de password segura e envio por email
        String passwordInicial = gerarPasswordSegura();
        novoUtente.setPassHash(passwordEncoder.encode(passwordInicial));
        novoUtente.setDataNasc(java.time.LocalDate.now());

        log.info("Novo utente criado. Enviando password para: {}", email);

        try {
            emailService.sendPassword(email, passwordInicial);
        } catch (Exception e) {
            log.error("Erro ao enviar email: {}", e.getMessage());
            // Não falhamos a criação, mas logamos o erro. O utilizador terá de pedir
            // recuperação ou contactar admin.
        }

        return utenteRepository.save(novoUtente);
    }

    /*
     * =========================================================
     * OBTENÇÃO E ATUALIZAÇÃO DE UTILIZADOR
     * =========================================================
     */

    // Obtém um utilizador pelo seu ID.

    public Utilizador obterUtilizadorPorId(Long utilizadorId) {
        return utilizadorRepository.findById(utilizadorId)
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado com ID: " + utilizadorId));
    }

    /**
     * Atualiza dados pessoais e profissionais de um utilizador.
     *
     * Apenas os campos fornecidos no DTO são alterados.
     * Campos não enviados permanecem inalterados.
     *
     * Regras:
     * - Email tem de ser único.
     * - Data de nascimento deve seguir o formato YYYY-MM-DD.
     */
    public Utilizador atualizarUtilizador(Long utilizadorId, UtilizadorInfoDTO request) {

        Utilizador utilizador = obterUtilizadorPorId(utilizadorId);

        // Atualizar campos se forem fornecidos
        if (request.getNome() != null && !request.getNome().trim().isEmpty()) {
            utilizador.setNome(request.getNome());
        }

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            // Verificar se email já está em uso por outro utilizador
            utilizadorRepository.findByEmail(request.getEmail())
                    .ifPresent(u -> {
                        if (!u.getId().equals(utilizadorId)) {
                            throw new RuntimeException("Email já está em uso por outro utilizador");
                        }
                    });
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

        // Salvar e retornar
        return utilizadorRepository.save(utilizador);
    }

    /*
     * =========================================================
     * ESTATÍSTICAS
     * =========================================================
     */

    // Conta o número de utentes ativos no sistema.

    public long contarUtentesAtivos() {
        return utenteRepository.countByActivo(true);
    }

    /*
     * =========================================================
     * MÉTODOS AUXILIARES (FUTURO)
     * =========================================================
     */

    /**
     * Validação de NIF usando serviço externo.
     */
    private boolean validarNIF(String nif) {
        return nifValidationService.validate(nif);
    }

    // Gera uma password aleatória de 8 caracteres
    private String gerarPasswordSegura() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int randomIndex = random.nextInt(chars.length());
            sb.append(chars.charAt(randomIndex));
        }
        return sb.toString();
    }

    private void validarCampoObrigatorio(String valor, String mensagemErro) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new RuntimeException(mensagemErro);
        }
    }

    /*
     * =========================================================
     * GESTÃO DE APROVAÇÕES (FUNCIONÁRIOS)
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

    public void aprovarFuncionario(Long id) {
        Funcionario funcionario = funcionarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado com ID: " + id));

        funcionario.setActivo(true);
        funcionarioRepository.save(funcionario);
    }

    /*
     * =========================================================
     * GESTÃO PELA SECRETARIA (Novos Métodos)
     * =========================================================
     */

    /**
     * Cria um utilizador (Utente ou Funcionário) pela secretaria.
     * Gera password automática e envia por email.
     * A conta fica INATIVA até o utilizador fazer login (ou definir password).
     */
    public Utilizador criarUtilizadorPelaSecretaria(pt.florinhas.marcacoes.dto.CreateUserRequestDTO request) {
        // Validação básica
        if (!validarNIF(request.getNif())) {
            throw new RuntimeException("NIF inválido.");
        }
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
                // Tenta converter a string role para Enum
                // Mapeamento simples ou direto
                String roleStr = request.getRole();
                // Normalizar entrada comum do frontend se necessário, ou assumir match direto
                // Frontend envia: "Secretaria", "Balneário Social", etc.
                // FuncionarioTipo: SECRETARIA, BALNEARIO, OUTRO, ESCOLA, INTERNOS

                pt.florinhas.marcacoes.domain.FuncionarioTipo tipo = pt.florinhas.marcacoes.domain.FuncionarioTipo.OUTRO;
                if (roleStr != null) {
                    if (roleStr.equalsIgnoreCase("Secretaria"))
                        tipo = pt.florinhas.marcacoes.domain.FuncionarioTipo.SECRETARIA;
                    else if (roleStr.toUpperCase().contains("BALNE"))
                        tipo = pt.florinhas.marcacoes.domain.FuncionarioTipo.BALNEARIO;
                    else if (roleStr.equalsIgnoreCase("Escola"))
                        tipo = pt.florinhas.marcacoes.domain.FuncionarioTipo.ESCOLA;
                    else if (roleStr.toUpperCase().contains("INTERNOS"))
                        tipo = pt.florinhas.marcacoes.domain.FuncionarioTipo.INTERNOS;
                }
                f.setTipo(tipo);
            } catch (Exception e) {
                f.setTipo(pt.florinhas.marcacoes.domain.FuncionarioTipo.OUTRO);
            }
            f.setActivo(false); // Inativo à espera de login/reset
            novoUtilizador = f;
        } else {
            Utente u = new Utente();
            u.setActivo(false); // Inativo à espera de login/reset
            novoUtilizador = u;
        }

        novoUtilizador.setNif(request.getNif());
        novoUtilizador.setNome(request.getName());
        novoUtilizador.setTelefone(request.getContact());
        novoUtilizador.setEmail(request.getEmail());

        // Data de Nascimento
        try {
            LocalDate dataNasc = LocalDate.parse(request.getBirthDate(), DateTimeFormatter.ISO_LOCAL_DATE);
            novoUtilizador.setDataNasc(dataNasc);
        } catch (Exception e) {
            novoUtilizador.setDataNasc(LocalDate.now()); // Fallback ou erro? Melhor erro se for obrigatório
        }

        // Gerar Password
        String passwordInicial = gerarPasswordSegura();
        novoUtilizador.setPassHash(passwordEncoder.encode(passwordInicial));

        Utilizador salvo = utilizadorRepository.save(novoUtilizador);

        // Enviar Email
        try {
            emailService.sendPassword(novoUtilizador.getEmail(), passwordInicial);
        } catch (Exception e) {
            log.error("Erro ao enviar email de criação: {}", e.getMessage());
            // Não abortar transação, permitir retry de recuperação depois
        }

        return salvo;
    }

    /**
     * Inicia o processo de recuperação de conta pela secretaria.
     * Atualiza contactos se fornecidos e reenvia password (reset).
     */
    public void recuperarConta(pt.florinhas.marcacoes.dto.RecoverAccountDTO request) {
        Utilizador utilizador = utilizadorRepository.findByNif(request.getNif())
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado com NIF: " + request.getNif()));

        // Atualizar Email/Telefone se fornecidos e diferentes
        boolean changed = false;

        if (request.getUpdatedEmail() != null && !request.getUpdatedEmail().isEmpty()
                && !request.getUpdatedEmail().equals(utilizador.getEmail())) {

            // Validar unicidade se mudou
            if (utilizadorRepository.existsByEmail(request.getUpdatedEmail())) {
                throw new RuntimeException("O novo email já está associado a outra conta.");
            }
            utilizador.setEmail(request.getUpdatedEmail());
            changed = true;
        }

        if (request.getUpdatedContact() != null && !request.getUpdatedContact().isEmpty()
                && !request.getUpdatedContact().equals(utilizador.getTelefone())) {
            utilizador.setTelefone(request.getUpdatedContact());
            changed = true;
        }

        // Reset Status e Password
        if (utilizador instanceof Utente) {
            ((Utente) utilizador).setActivo(false);
        } else if (utilizador instanceof Funcionario) {
            ((Funcionario) utilizador).setActivo(false);
        }
        // User request: "a conta passa a Inativa de novo"

        String novaPassword = gerarPasswordSegura();
        utilizador.setPassHash(passwordEncoder.encode(novaPassword));

        utilizadorRepository.save(utilizador);

        // Enviar Email
        try {
            // Usar o email (potencialmente novo)
            emailService.sendPassword(utilizador.getEmail(), novaPassword);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao enviar email de recuperação: " + e.getMessage());
        }
    }
}
