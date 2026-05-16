package pt.florinhas.api_gateway.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import lombok.extern.slf4j.Slf4j;
import pt.florinhas.common_data.repository.FuncionarioRepository;
import pt.florinhas.common_data.repository.UtenteRepository;
import pt.florinhas.common_data.repository.UtilizadorRepository;

import pt.florinhas.api_gateway.dto.*;

import pt.florinhas.common_data.domain.Funcionario;
import pt.florinhas.common_data.domain.FuncionarioTipo;
import pt.florinhas.common_data.domain.Utente;
import pt.florinhas.common_data.domain.Utilizador;

import pt.florinhas.common_data.validation.NifValidator;
import pt.florinhas.common_data.exception.BadRequestException;
import pt.florinhas.common_data.security.CryptoUtils;

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

        private static final String ROLE_UTENTE = "UTENTE";

        private final UtilizadorRepository utilizadorRepository;
        private final FuncionarioRepository funcionarioRepository;
        private final UtenteRepository utenteRepository;
        private final PasswordEncoder passwordEncoder;
        private final AuthenticationManager authenticationManager;
        private final NifValidator nifValidator;

        @Value("${jwt.expiration:86400000}")
        private long jwtExpiration;

        private final CryptoUtils cryptoUtils;

        public AuthService(
                        UtilizadorRepository utilizadorRepository,
                        FuncionarioRepository funcionarioRepository,
                        UtenteRepository utenteRepository,
                        PasswordEncoder passwordEncoder,
                        AuthenticationManager authenticationManager,
                        NifValidator nifValidator,
                        CryptoUtils cryptoUtils) {
                this.utilizadorRepository = utilizadorRepository;
                this.funcionarioRepository = funcionarioRepository;
                this.utenteRepository = utenteRepository;
                this.passwordEncoder = passwordEncoder;
                this.authenticationManager = authenticationManager;
                this.nifValidator = nifValidator;
                this.cryptoUtils = cryptoUtils;
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
        public AuthResult loginFuncionario(LoginFuncionarioRequest request) {
                log.debug("Login funcionario started for: {}", request.email());

                // 1. Carregar utilizador primeiro para verificar estado "Ativo"
                List<Utilizador> users = utilizadorRepository.findByEmail(request.email());
                if (users.isEmpty()) {
                        throw new BadRequestException("Credenciais inválidas");
                }
                var user = users.get(0);

                if (!(user instanceof Funcionario funcionario)) {
                        // Nota: Mensagem genérica para segurança, mas log específico
                        log.debug("User found but not Funcionario: {}", user.getId());
                        throw new BadRequestException("Credenciais inválidas");
                }

                // Funcionário inativo pode significar dois cenários distintos:
                // 1) Criado pela secretaria (primeiro login com password temporária) -> permitir autenticar
                // 2) Auto-registo pendente de aprovação (termos já aceites) -> bloquear login
                if (!funcionario.isActivo() && funcionario.getTermsAcceptedAt() != null) {
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

                String role = funcionario.getTipo() != null ? funcionario.getTipo().name() : "FUNCIONARIO";
                return generateAuthResponse(user, role, funcionario.isActivo());
        }

        /**
         * Autenticação de Utente.
         *
         * Diferença principal:
         * - Username usado na autenticação é o NIF.
         */
        public AuthResult loginUtente(LoginUtenteRequest request) {
                log.debug("Login utente attempt for NIF: {}", request.nif());

                // Autenticação via NIF + password
                try {
                        authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(
                                                        request.nif(),
                                                        request.password()));
                } catch (Exception e) {
                        log.error("Authentication failed for NIF: " + request.nif(), e);
                        throw new BadRequestException("Credenciais inválidas");
                }

                // Obter utilizador pelo NIF (handle duplicate data by taking first)
                var users = utilizadorRepository.findByNifHash(cryptoUtils.generateBlindIndex(request.nif()));

                if (users.isEmpty()) {
                        throw new BadRequestException("Utente não encontrado");
                }
                var user = users.get(0);

                log.debug("User found: {}, Active: {}", user.getEmail(), ((Utente) user).isActivo());

                // Garantir que é efetivamente um Utente
                if (!(user instanceof Utente)) {
                        throw new BadRequestException("Credenciais inválidas para utente");
                }

                return generateAuthResponse(user, ROLE_UTENTE, ((Utente) user).isActivo());
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
        public AuthResult registerUtente(UtenteRegisterRequest request) {
                nifValidator.validateRequiredOrThrow(request.nif());
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

                return generateAuthResponse(utente, ROLE_UTENTE, true);
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
        public AuthResult registerFuncionario(FuncionarioRegisterRequest request) {
                nifValidator.validateRequiredOrThrow(request.nif());
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

                String role = funcionario.getTipo() != null ? funcionario.getTipo().name() : "FUNCIONARIO";
                return generateAuthResponse(funcionario, role, false);
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

                boolean acceptedTermsNow = false;

                // Verificar se precisa aceitar termos
                if (user.getTermsAcceptedAt() == null) {
                        if (termsAccepted == null || !termsAccepted) {
                                throw new BadRequestException("Deve aceitar os termos de uso para ativar a conta");
                        }
                        user.setTermsAcceptedAt(LocalDateTime.now());
                        acceptedTermsNow = true;
                }

                user.setPassHash(passwordEncoder.encode(newPassword));

                switch (user) {
                        case Utente utente -> {
                                utente.setActivo(true);
                                utenteRepository.save(utente);
                        }
                        case Funcionario funcionario -> {
                                // Ativa funcionário se aceitou termos agora OU já tinha termos aceites
                                if (acceptedTermsNow || funcionario.getTermsAcceptedAt() != null) {
                                        funcionario.setActivo(true);
                                }
                                funcionarioRepository.save(funcionario);
                        }
                        default -> utilizadorRepository.save(user);
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
                if (utilizadorRepository.existsByNifHash(cryptoUtils.generateBlindIndex(nif))) {
                        throw new BadRequestException("NIF já está em uso");
                }
        }

        public AuthResult generateAuthResponse(Utilizador user, String role, boolean isActive) {
                long expiresAt = System.currentTimeMillis() + jwtExpiration;
                boolean requiresPasswordSetup = requiresPasswordSetup(user, isActive);

                AuthResponse response = new AuthResponse(
                                user.getId(),
                                user.getEmail(),
                                user.getNome(),
                                role,
                                user.getNif(),
                                user.getTelefone(),
                                expiresAt,
                                isActive,
                                requiresPasswordSetup);

                return new AuthResult(response);
        }

        /**
         * Regra centralizada para saber se o utilizador precisa de definir password.
         *
         * Cenário esperado:
         * - Conta inativa + sem termos aceites -> requer configuração de password.
         */
        public boolean requiresPasswordSetup(Utilizador user, boolean isActive) {
                return !isActive && user.getTermsAcceptedAt() == null;
        }

        public Optional<AuthResponse> getCurrentUserResponse(Utilizador principal) {
                if (principal == null) {
                        return Optional.empty();
                }

                String role = principal.getAuthorities().stream()
                                .findFirst()
                                .map(a -> a.getAuthority().replace("ROLE_", ""))
                                .orElse(ROLE_UTENTE);

                var persistedUser = utilizadorRepository.findById(principal.getId());
                if (persistedUser.isEmpty()) {
                        return Optional.empty();
                }

                Utilizador user = persistedUser.get();
                boolean active = switch (user) {
                        case Utente u -> u.isActivo();
                        case Funcionario f -> f.isActivo();
                        default -> true;
                };

                boolean requiresPasswordSetup = requiresPasswordSetup(user, active);

                return Optional.of(new AuthResponse(
                                principal.getId(),
                                principal.getEmail(),
                                principal.getNome(),
                                role,
                                principal.getNif(),
                                principal.getTelefone(),
                                System.currentTimeMillis() + jwtExpiration,
                                active,
                                requiresPasswordSetup));
        }

        public record AuthResult(AuthResponse response) {
        }

        private FuncionarioTipo mapFuncaoToTipo(String funcao) {
                if (funcao == null) {
                        throw new BadRequestException("A função do funcionário é obrigatória");
                }
                
                return switch (funcao.toUpperCase()) {
                        case "SECRETARIA", "SECRETÁRIA" -> FuncionarioTipo.SECRETARIA;
                        case "BALNEARIO", "BALNEÁRIO", "BALNEARIO SOCIAL", "BALNEÁRIO SOCIAL" ->
                                FuncionarioTipo.BALNEARIO;
                        case "ESCOLA" -> FuncionarioTipo.ESCOLA;
                        case "INTERNOS", "INTERNO", "SERVIÇOS INTERNOS", "SERVICOS INTERNOS" -> FuncionarioTipo.INTERNO;
                        default -> {
                                log.warn("Tentativa de registo com função desconhecida: {}", funcao);
                                throw new BadRequestException("Função desconhecida: " + funcao);
                        }
                };
        }

        /**
         * Obtém o ID do utilizador autenticado a partir do SecurityContext.
         */
        public Long getCurrentUserId() {
                var authentication = SecurityContextHolder.getContext()
                                .getAuthentication();
                if (authentication == null || !authentication.isAuthenticated()
                                || authentication instanceof AnonymousAuthenticationToken) {
                        return null;
                }

                Object principal = authentication.getPrincipal();

                if (principal instanceof Utilizador utilizador) {
                        return utilizador.getId();
                } else if (principal instanceof UserDetails userDetails) {
                        // Em alguns casos pode ser apenas UserDetails, tentamos buscar pelo email
                        var users = utilizadorRepository.findByEmail(userDetails.getUsername());
                        if (!users.isEmpty()) {
                                return users.get(0).getId();
                        }
                }

                return null;
        }

        /**
         * Verifica se o utilizador autenticado tem privilégios administrativos
         * (apenas SECRETARIA).
         */
        public boolean isAdmin() {
                var authentication = SecurityContextHolder.getContext()
                                .getAuthentication();
                if (authentication == null || !authentication.isAuthenticated()) {
                        return false;
                }

                return authentication.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_SECRETARIA"));
        }
}
