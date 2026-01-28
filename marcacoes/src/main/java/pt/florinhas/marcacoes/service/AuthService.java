package pt.florinhas.marcacoes.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import pt.florinhas.marcacoes.domain.Funcionario;
import pt.florinhas.marcacoes.domain.FuncionarioTipo;
import pt.florinhas.marcacoes.domain.Utente;
import pt.florinhas.marcacoes.domain.Utilizador;
import pt.florinhas.marcacoes.dto.AuthResponse;
import pt.florinhas.marcacoes.dto.FuncionarioRegisterRequest;
import pt.florinhas.marcacoes.dto.LoginFuncionarioRequest;
import pt.florinhas.marcacoes.dto.LoginUtenteRequest;
import pt.florinhas.marcacoes.dto.UtenteRegisterRequest;
import pt.florinhas.marcacoes.exception.BadRequestException;
import pt.florinhas.marcacoes.repository.FuncionarioRepository;
import pt.florinhas.marcacoes.repository.UtenteRepository;
import pt.florinhas.marcacoes.repository.UtilizadorRepository;
import pt.florinhas.marcacoes.security.JwtService;

import java.time.LocalDateTime;

/**
 * Serviço responsável por autenticação e registo de utilizadores.
 *
 * Responsabilidades principais:
 * - Autenticação de Funcionários e Utentes (login).
 * - Registo de novos Utentes e Funcionários.
 * - Geração de tokens JWT após autenticação/registo.
 *
 * Integra:
 * - Spring Security (AuthenticationManager).
 * - Persistência JPA (repositórios).
 * - Segurança (PasswordEncoder + JwtService).
 */
@Service
@Slf4j
public class AuthService {

        private final UtilizadorRepository utilizadorRepository;
        private final FuncionarioRepository funcionarioRepository;
        private final UtenteRepository utenteRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;
        private final AuthenticationManager authenticationManager;

        public AuthService(
                        UtilizadorRepository utilizadorRepository,
                        FuncionarioRepository funcionarioRepository,
                        UtenteRepository utenteRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService,
                        AuthenticationManager authenticationManager) {
                this.utilizadorRepository = utilizadorRepository;
                this.funcionarioRepository = funcionarioRepository;
                this.utenteRepository = utenteRepository;
                this.passwordEncoder = passwordEncoder;
                this.jwtService = jwtService;
                this.authenticationManager = authenticationManager;
        }

        /**
         * Autenticação de Funcionário.
         *
         * Fluxo:
         * 1) Autentica credenciais via AuthenticationManager.
         * 2) Valida que o utilizador existe e é do tipo Funcionario.
         * 3) Gera token JWT.
         * 4) Devolve AuthResponse com dados do utilizador e expiração.
         */
        public AuthResponse loginFuncionario(LoginFuncionarioRequest request) {
                log.debug("Login funcionario started for: {}", request.email());

                // 1. Carregar utilizador primeiro para verificar estado "Ativo"
                // Isto previne que func. inativos validem credenciais (timing attack /
                // enumeração)
                var user = utilizadorRepository.findByEmail(request.email())
                                .orElseThrow(() -> new BadRequestException("Credenciais inválidas"));

                if (!(user instanceof Funcionario funcionario)) {
                        // Nota: Mensagem genérica para segurança, mas log específico
                        log.debug("User found but not Funcionario: {}", user.getId());
                        throw new BadRequestException("Credenciais inválidas");
                }

                if (!funcionario.isActivo()) {
                        throw new BadRequestException("Conta pendente de aprovação ou inativa. Contacte a secretaria.");
                }

                // 2. Autenticar credenciais só depois de confirmar que está ativo
                try {
                        authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(
                                                        request.email(),
                                                        request.password()));
                } catch (Exception e) {
                        throw new BadRequestException("Credenciais inválidas");
                }

                log.debug("Authentication successful");

                return generateAuthResponse(user, "FUNCIONARIO", true);
        }

        /**
         * Autenticação de Utente.
         *
         * Diferença principal:
         * - Username usado na autenticação é o NIF.
         */
        public AuthResponse loginUtente(LoginUtenteRequest request) {

                // Autenticação via NIF + password
                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                request.nif(),
                                                request.password()));

                // Obter utilizador pelo NIF
                var user = utilizadorRepository.findByNif(request.nif())
                                .orElseThrow(() -> new BadRequestException("Utente não encontrado"));

                // Garantir que é efetivamente um Utente
                if (!(user instanceof Utente)) {
                        throw new BadRequestException("Credenciais inválidas para utente");
                }

                return generateAuthResponse(user, "UTENTE", ((Utente) user).isActivo());
        }

        /**
         * Registo de um novo Utente.
         *
         * Validações:
         * - Email único.
         * - NIF único.
         * - Termos de uso aceites (termsAccepted = true).
         *
         * Após criação:
         * - Password é guardada com hash (BCrypt).
         * - Utente é marcado como ativo.
         * - termsAcceptedAt é definido com timestamp atual.
         * - JWT é gerado automaticamente.
         */
        public AuthResponse registerUtente(UtenteRegisterRequest request) {
                checkUserExists(request.email(), request.nif());

                if (!request.termsAccepted()) {
                        throw new BadRequestException("Deve aceitar os termos de uso para se registar");
                }

                // Construção da entidade Utente
                Utente utente = new Utente();
                utente.setNome(request.nome());
                utente.setEmail(request.email());
                utente.setNif(request.nif());
                utente.setTelefone(request.telefone());
                utente.setPassHash(passwordEncoder.encode(request.password()));
                utente.setDataNasc(request.dataNasc());
                utente.setActivo(true);
                utente.setTermsAcceptedAt(LocalDateTime.now());

                utente = utenteRepository.save(utente);

                return generateAuthResponse(utente, "UTENTE", true);
        }

        /**
         * Registo de um novo Funcionário.
         *
         * Particularidades:
         * - Tipo de funcionário é inferido a partir da string "funcao".
         * - Password é armazenada com hash.
         * - Termos de uso devem ser aceites (similar a utentes).
         * - termsAcceptedAt é definido com timestamp atual.
         * - JWT é devolvido após criação.
         */
        public AuthResponse registerFuncionario(FuncionarioRegisterRequest request) {
                checkUserExists(request.email(), request.nif());

                if (!request.termsAccepted()) {
                        throw new BadRequestException("Deve aceitar os termos de uso para se registar");
                }

                // Construção da entidade Funcionário
                Funcionario funcionario = new Funcionario();
                funcionario.setNome(request.nome());
                funcionario.setEmail(request.email());
                funcionario.setNif(request.nif());
                funcionario.setTelefone(request.contacto());
                funcionario.setPassHash(passwordEncoder.encode(request.password()));
                funcionario.setTipo(mapFuncaoToTipo(request.funcao()));
                funcionario.setDataNasc(request.dataNasc());
                funcionario.setActivo(false);
                funcionario.setTermsAcceptedAt(LocalDateTime.now());

                funcionario = funcionarioRepository.save(funcionario);

                return generateAuthResponse(funcionario, "FUNCIONARIO", false);
        }

        /**
         * Atualiza a password de um utilizador.
         * 
         * Cenários:
         * 1. Utente criado pela secretaria define password pela primeira vez
         * -> Aceita termos (obrigatório) e ativa a conta
         * 2. Utilizador faz reset de password
         * -> Se já tinha termos aceites, mantém; se não, exige aceitação
         * 
         * @param userId        ID do utilizador
         * @param newPassword   Nova password
         * @param termsAccepted Aceitação dos termos (obrigatório se ainda não aceites)
         */
        public void updatePassword(Long userId, String newPassword, Boolean termsAccepted) {
                var user = utilizadorRepository.findById(userId)
                                .orElseThrow(() -> new BadRequestException("Utilizador não encontrado"));

                // Verificar se precisa aceitar termos
                if (user.getTermsAcceptedAt() == null) {
                        // Conta criada pela secretaria ou sem termos aceites
                        if (termsAccepted == null || !termsAccepted) {
                                throw new BadRequestException("Deve aceitar os termos de uso para ativar a conta");
                        }
                        // Define timestamp de aceitação dos termos
                        user.setTermsAcceptedAt(LocalDateTime.now());
                }

                // Atualiza a password
                user.setPassHash(passwordEncoder.encode(newPassword));

                if (user instanceof Utente utente) {
                        // Ativa a conta do utente (caso ainda não estivesse ativa)
                        utente.setActivo(true);
                        utenteRepository.save(utente);
                } else if (user instanceof Funcionario funcionario) {
                        // Para funcionários, define timestamp de termos se fornecido
                        if (termsAccepted != null && termsAccepted && funcionario.getTermsAcceptedAt() == null) {
                                funcionario.setTermsAcceptedAt(LocalDateTime.now());
                        }
                        funcionarioRepository.save(funcionario);
                } else {
                        utilizadorRepository.save(user);
                }
        }

        /**
         * Converte a string "funcao" recebida no registo
         * para o enum FuncionarioTipo.
         *
         * Aceita variantes com/sem acentos.
         * Valor default: SECRETARIA.
         */

        private void checkUserExists(String email, String nif) {
                if (utilizadorRepository.existsByEmail(email)) {
                        throw new BadRequestException("Email já está em uso");
                }
                if (utilizadorRepository.existsByNif(nif)) {
                        throw new BadRequestException("NIF já está em uso");
                }
        }

        private AuthResponse generateAuthResponse(Utilizador user, String role, boolean isActive) {
                var jwtToken = jwtService.generateToken(user);
                long expiresAt = System.currentTimeMillis() + jwtService.getJwtExpiration();

                return new AuthResponse(
                                jwtToken,
                                user.getId(),
                                user.getEmail(),
                                user.getNome(),
                                role,
                                user.getNif(),
                                user.getTelefone(),
                                expiresAt,
                                isActive);
        }

        private FuncionarioTipo mapFuncaoToTipo(String funcao) {
                return switch (funcao.toUpperCase()) {
                        case "SECRETARIA", "SECRETÁRIA" -> FuncionarioTipo.SECRETARIA;
                        case "BALNEARIO", "BALNEÁRIO" -> FuncionarioTipo.BALNEARIO;
                        case "ESCOLA" -> FuncionarioTipo.ESCOLA;
                        case "INTERNOS" -> FuncionarioTipo.INTERNOS;
                        default -> FuncionarioTipo.SECRETARIA;
                };
        }
}
