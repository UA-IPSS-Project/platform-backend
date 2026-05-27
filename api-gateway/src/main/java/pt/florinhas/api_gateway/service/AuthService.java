package pt.florinhas.api_gateway.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;

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

        // Constantes para roles
        private static final String ROLE_UTENTE = "UTENTE";
        private static final String ROLE_FUNCIONARIO = "FUNCIONARIO";
        private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        private static final SecureRandom SECURE_RANDOM = new SecureRandom();

        private final UtilizadorRepository utilizadorRepository;
        private final FuncionarioRepository funcionarioRepository;
        private final UtenteRepository utenteRepository;
        private final PasswordEncoder passwordEncoder;
        private final NifValidator nifValidator;
        private final WebClient notificacoesWebClient;

        @Value("${jwt.expiration:86400000}")
        private long jwtExpiration;

        private final CryptoUtils cryptoUtils;

        public AuthService(
                        UtilizadorRepository utilizadorRepository,
                        FuncionarioRepository funcionarioRepository,
                        UtenteRepository utenteRepository,
                        PasswordEncoder passwordEncoder,
                        NifValidator nifValidator,
                        CryptoUtils cryptoUtils,
                        WebClient.Builder webClientBuilder,
                        @Value("${gateway.notificacoes.base-url:http://notificacoes:8083}") String notificacoesUrl,
                        @Value("${gateway.shared-secret:}") String gatewaySecret) {
                this.utilizadorRepository = utilizadorRepository;
                this.funcionarioRepository = funcionarioRepository;
                this.utenteRepository = utenteRepository;
                this.passwordEncoder = passwordEncoder;
                this.nifValidator = nifValidator;
                this.cryptoUtils = cryptoUtils;
                this.notificacoesWebClient = webClientBuilder
                        .baseUrl(notificacoesUrl)
                        .defaultHeader("X-Gateway-Secret", gatewaySecret)
                        .build();
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

                // 2. Verificar password diretamente via BCrypt.
                // isAccountNonLocked, isAccountNonExpired e isCredentialsNonExpired estão
                // hardcoded a true em Utilizador.java, pelo que o DaoAuthenticationProvider
                // não acrescenta nenhuma validação útil — evita-se o segundo findByEmail.
                if (!passwordEncoder.matches(request.password(), funcionario.getPassHash())) {
                        throw new BadRequestException("Credenciais inválidas");
                }

                // Verificar se a OTP expirou
                if (user.getOtpExpiresAt() != null && user.getOtpExpiresAt().isBefore(java.time.LocalDateTime.now())) {
                        throw new BadRequestException("Código expirado. Solicite um novo código na secretaria.");
                }

                log.debug("Authentication successful");

                String role = funcionario.getTipo() != null ? funcionario.getTipo().name() : ROLE_FUNCIONARIO;
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

                // Obter utilizador pelo NIF primeiro (antes era feito depois do authenticate)
                var users = utilizadorRepository.findByNifHash(cryptoUtils.generateBlindIndex(request.nif()));
                if (users.isEmpty()) {
                        throw new BadRequestException("Utente não encontrado");
                }
                var user = users.get(0);

                // Garantir que é efetivamente um Utente
                if (!(user instanceof Utente utente)) {
                        throw new BadRequestException("Credenciais inválidas para utente");
                }

                // Verificar password diretamente via BCrypt.
                // isAccountNonLocked, isAccountNonExpired e isCredentialsNonExpired estão
                // hardcoded a true em Utilizador.java, pelo que o DaoAuthenticationProvider
                // não acrescenta nenhuma validação útil — evita-se o segundo findByNifHash.
                // Antes: authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.nif(), request.password()))
                if (!passwordEncoder.matches(request.password(), utente.getPassHash())) {
                        log.error("Authentication failed for NIF: {}", request.nif());
                        throw new BadRequestException("Credenciais inválidas");
                }

                // Verificar se a OTP expirou
                if (user.getOtpExpiresAt() != null && user.getOtpExpiresAt().isBefore(java.time.LocalDateTime.now())) {
                        throw new BadRequestException("Código expirado. Solicite um novo código na secretaria.");
                }

                log.debug("User found: {}, Active: {}", user.getEmail(), utente.isActivo());
                return generateAuthResponse(user, ROLE_UTENTE, utente.isActivo());
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

                String role = funcionario.getTipo() != null ? funcionario.getTipo().name() : ROLE_FUNCIONARIO;
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
                user.setOtpExpiresAt(null); // Limpar expiração da OTP

                if (user instanceof Utente utente) {
                        utente.setActivo(true);
                        utenteRepository.save(utente);
                } else if (user instanceof Funcionario funcionario) {
                        // Ativa funcionário se aceitou termos agora OU já tinha termos aceites
                        if (acceptedTermsNow || funcionario.getTermsAcceptedAt() != null) {
                                funcionario.setActivo(true);
                        }
                        funcionarioRepository.save(funcionario);
                } else {
                        utilizadorRepository.save(user);
                }
        }

        /**
         * Recuperação de password.
         * Gera nova OTP com expiração de 15 min e envia por email.
         * Aceita email ou NIF como identificador.
         */
        public void recoverPassword(String identifier) {
                Utilizador user = findUserByIdentifier(identifier);
                if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
                        // Não revelar se o utilizador existe ou não
                        log.debug("Recover password: user not found or no email for identifier: {}", identifier);
                        return;
                }

                String rawPassword = generateRandomPassword();

                // Enviar email primeiro — se falhar, não alteramos o estado da conta
                sendPasswordEmail(user.getEmail(), rawPassword);

                user.setPassHash(passwordEncoder.encode(rawPassword));
                user.setOtpExpiresAt(LocalDateTime.now().plusMinutes(15));
                user.setTermsAcceptedAt(null);

                if (user instanceof Utente utente) {
                        utente.setActivo(false);
                        utenteRepository.save(utente);
                } else if (user instanceof Funcionario funcionario) {
                        funcionario.setActivo(false);
                        funcionarioRepository.save(funcionario);
                } else {
                        utilizadorRepository.save(user);
                }

                log.info("Password recovery initiated for: {}", user.getEmail());
        }

        private Utilizador findUserByIdentifier(String identifier) {
                if (identifier == null || identifier.isBlank()) return null;
                String trimmed = identifier.trim();

                // Tentar como email
                if (trimmed.contains("@")) {
                        List<Utilizador> users = utilizadorRepository.findByEmail(trimmed);
                        return users.isEmpty() ? null : users.get(0);
                }

                // Tentar como NIF (9 dígitos)
                if (trimmed.matches("\\d{9}")) {
                        List<Utilizador> users = utilizadorRepository.findByNifHash(
                                        cryptoUtils.generateBlindIndex(trimmed));
                        return users.isEmpty() ? null : users.get(0);
                }

                return null;
        }

        private String generateRandomPassword() {
                StringBuilder password = new StringBuilder(22);
                for (int i = 0; i < 22; i++) {
                        password.append(ALPHANUMERIC.charAt(SECURE_RANDOM.nextInt(ALPHANUMERIC.length())));
                }
                return password.toString();
        }

        private void sendPasswordEmail(String to, String password) {
                notificacoesWebClient.post()
                        .uri("/api/internal/notificacoes/email/password")
                        .bodyValue(Map.of("to", to, "password", password))
                        .retrieve()
                        .toBodilessEntity()
                        .doOnError(e -> log.error("Erro ao enviar email de recuperação para {}", to, e))
                        .block();
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

                var persistedUser = utilizadorRepository.findById(principal.getId());
                if (persistedUser.isEmpty()) {
                        return Optional.empty();
                }

                Utilizador user = persistedUser.get();

                // Derive role from the persisted entity to avoid stale principal authorities in WebFlux
                String role;
                if (user instanceof Funcionario f) {
                        role = f.getTipo() != null ? f.getTipo().name() : ROLE_FUNCIONARIO;
                } else {
                        role = ROLE_UTENTE;
                }

                boolean active = true;
                if (user instanceof Utente u) {
                        active = u.isActivo();
                } else if (user instanceof Funcionario f) {
                        active = f.isActivo();
                }

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
