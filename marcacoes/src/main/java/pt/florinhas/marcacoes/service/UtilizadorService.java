package pt.florinhas.marcacoes.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.security.SecureRandom;

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
        if (!validarNIF(nif)) {
            throw new IllegalArgumentException("NIF inválido (deve ter 9 dígitos numéricos).");
        }
        return utilizadorRepository.findByNif(nif);
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

        // Validação mínima
        if (!validarNIF(nif)) {
            throw new RuntimeException("NIF do utente inválido (deve ter 9 dígitos numéricos).");
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
        System.out.println("Utente com NIF " + nif + " não encontrado. Criando novo utente...");

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

        System.out.println("Novo utente criado. Enviando password para: " + email);

        try {
            emailService.sendPassword(email, passwordInicial);
        } catch (Exception e) {
            System.err.println("Erro ao enviar email: " + e.getMessage());
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
}
