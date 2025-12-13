package pt.florinhas.marcacoes.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.florinhas.marcacoes.domain.Utente;
import pt.florinhas.marcacoes.domain.Utilizador;
import pt.florinhas.marcacoes.dto.UtilizadorInfoDTO;
import pt.florinhas.marcacoes.repository.UtenteRepository;
import pt.florinhas.marcacoes.repository.UtilizadorRepository;

/**
 * Serviço responsável pela gestão de utilizadores e utentes.
 *
 * Responsabilidades:
 *  - Pesquisa de utilizadores (por ID, NIF).
 *  - Criação automática de utentes (on-demand).
 *  - Atualização de dados pessoais e profissionais.
 *  - Contagem de utentes ativos.
 *
 * É utilizado por vários serviços, nomeadamente:
 *  - MarcacaoService (criação de marcações presenciais).
 *  - Controllers de perfil/utilizador.
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
    
    /*
     * Encoder usado para gerar passwords temporárias de utentes
     * criados automaticamente pela secretaria.
     */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /* =========================================================
     *  CONSULTAS
     * ========================================================= */

    /**
     * Procura um utilizador por NIF.
     *
     * param nif NIF a pesquisar
     * return Optional com Utilizador, se existir
     * throws IllegalArgumentException se o NIF for inválido
     */
    public Optional<Utilizador> buscarPorNif(String nif) {
        if (nif == null || nif.trim().isEmpty()) {
            throw new IllegalArgumentException("NIF não pode ser nulo ou vazio");
        }
        return utilizadorRepository.findByNif(nif);
    }

    /* =========================================================
     *  CRIAÇÃO AUTOMÁTICA DE UTENTE
     * ========================================================= */

    /**
     * Obtém um utente existente ou cria automaticamente um novo.
     *
     * Este método é usado principalmente quando a secretaria cria
     * uma marcação para um utente que ainda não existe no sistema.
     *
     * Regras:
     *  - O NIF é obrigatório e identifica univocamente o utente.
     *  - Se o utente não existir:
     *      • Nome, email e telefone tornam-se obrigatórios.
     *      • O utente é criado como INATIVO.
     *      • É atribuída uma password temporária (NIF).
     *
     * param nif NIF do utente
     * param nome nome do utente
     * param email email do utente
     * param telefone telefone do utente
     * return utente existente ou recém-criado
     */
    public Utente obterOuCriarUtente(String nif, String nome, String email, String telefone) {

        // Validação mínima
        if (nif == null || nif.trim().isEmpty()) {
            throw new RuntimeException("NIF do utente é obrigatório");
        }
        
        // Procura utente existente
        return utenteRepository.findByNif(nif).orElseGet(() -> {

            System.out.println("Utente com NIF " + nif + " não encontrado. Criando novo utente...");

            // Validações obrigatórias para criação
            if (nome == null || nome.trim().isEmpty()) {
                throw new RuntimeException("Nome do utente é obrigatório para criar novo registo");
            }
            if (email == null || email.trim().isEmpty()) {
                throw new RuntimeException("Email do utente é obrigatório para criar novo registo");
            }
            if (telefone == null || telefone.trim().isEmpty()) {
                throw new RuntimeException("Telefone do utente é obrigatório para criar novo registo");
            }

            // Garantir unicidade do email
            if (utenteRepository.existsByEmail(email)) {
                throw new RuntimeException("Email já está registado no sistema");
            }

            // Criação do novo utente
            Utente novoUtente = new Utente();
            novoUtente.setNif(nif);
            novoUtente.setNome(nome);
            novoUtente.setEmail(email);
            novoUtente.setTelefone(telefone);
            novoUtente.setActivo(false); // só fica ativo após primeiro login

            // Password temporária = NIF
            String passwordTemporaria = nif;
            novoUtente.setPassHash(passwordEncoder.encode(passwordTemporaria));

            System.out.println("Novo utente criado com password temporária = NIF");

            // Aqui poderia ser enviado um email/token de acesso
            // enviarTokenAcesso(novoUtente);

            return utenteRepository.save(novoUtente);
        });
    }

    /* =========================================================
     *  OBTENÇÃO E ATUALIZAÇÃO DE UTILIZADOR
     * ========================================================= */


    // Obtém um utilizador pelo seu ID.

    public Utilizador obterUtilizadorPorId(Long utilizadorId) {
        return utilizadorRepository.findById(utilizadorId)
            .orElseThrow(() ->
                new RuntimeException("Utilizador não encontrado com ID: " + utilizadorId)
            );
    }

    /**
     * Atualiza dados pessoais e profissionais de um utilizador.
     *
     * Apenas os campos fornecidos no DTO são alterados.
     * Campos não enviados permanecem inalterados.
     *
     * Regras:
     *  - Email tem de ser único.
     *  - Data de nascimento deve seguir o formato YYYY-MM-DD.
     */
    public Utilizador atualizarUtilizador(Long utilizadorId, UtilizadorInfoDTO request) {

        Utilizador utilizador = utilizadorRepository.findById(utilizadorId)
            .orElseThrow(() ->
                new RuntimeException("Utilizador não encontrado com ID: " + utilizadorId)
            );

        if (request.getNome() != null && !request.getNome().trim().isEmpty()) {
            utilizador.setNome(request.getNome());
        }

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            utilizadorRepository.findByEmail(request.getEmail()).ifPresent(u -> {
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
                        DateTimeFormatter.ISO_LOCAL_DATE
                );
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

    /* =========================================================
     *  ESTATÍSTICAS
     * ========================================================= */

    
    // Conta o número de utentes ativos no sistema.

    public long contarUtentesAtivos() {
        return utenteRepository.countByActivo(true);
    }

    /* =========================================================
     *  MÉTODOS AUXILIARES (FUTURO)
     * ========================================================= */

    /**
     * Validação simples de NIF.
     * Pode ser melhorada com APIs externas oficiais.
     */
    private boolean validarNIF(String nif) {
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

    /**
     * Envio de token de acesso (não usado atualmente).
     * Poderá ser utilizado para primeiro login de utentes criados automaticamente.
     */
    private void enviarTokenAcesso(Utente utente) {
        String token = gerarToken();
        String mensagem = (
            "Foi criada uma conta automática para si. " +
            "Use o token %s para aceder à plataforma. " +
            "Será obrigatório definir uma nova palavra-passe no primeiro acesso."
        ).formatted(token);

        if (utente.getEmail() != null) {
            // emailService.enviarEmail(...)
            System.out.println("Email enviado para " + utente.getEmail() + " com token: " + token);
        }
    }

    //Gera um token numérico aleatório de 6 dígitos.

    private String gerarToken() {
        return String.valueOf(
            (int) ((ThreadLocalRandom.current().nextDouble() * 900000) + 100000)
        );
    }
}
