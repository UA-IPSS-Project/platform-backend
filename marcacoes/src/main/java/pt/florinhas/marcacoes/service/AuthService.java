package pt.florinhas.marcacoes.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import pt.florinhas.marcacoes.domain.Funcionario;
import pt.florinhas.marcacoes.domain.FuncionarioTipo;
import pt.florinhas.marcacoes.domain.Utente;
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
                System.out.println("DEBUG: loginFuncionario started for " + request.email());

                // Autenticação delegada ao Spring Security
                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                request.email(),
                                                request.password()));
                System.out.println("DEBUG: Authentication successful");

                // Obter utilizador pelo email
                var user = utilizadorRepository.findByEmail(request.email())
                                .orElseThrow(() -> new BadRequestException("Funcionário não encontrado"));
                System.out.println("DEBUG: User found: " + user.getId());

                // Garantir que é efetivamente um Funcionário
                if (!(user instanceof Funcionario)) {
                        throw new BadRequestException("Credenciais inválidas para funcionário");
                }

                // Geração de token JWT
                var jwtToken = jwtService.generateToken(user);
                long expiresAt = System.currentTimeMillis() + jwtService.getJwtExpiration();

                return new AuthResponse(
                                jwtToken,
                                user.getId(),
                                user.getEmail(),
                                user.getNome(),
                                "FUNCIONARIO",
                                user.getNif(),
                                user.getTelefone(),
                                expiresAt,
                                true // Funcionários ativos por defeito
                );
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

                // Geração de token JWT
                var jwtToken = jwtService.generateToken(user);
                long expiresAt = System.currentTimeMillis() + jwtService.getJwtExpiration();

                return new AuthResponse(
                                jwtToken,
                                user.getId(),
                                user.getEmail(),
                                user.getNome(),
                                "UTENTE",
                                user.getNif(),
                                user.getTelefone(),
                                expiresAt,
                                ((Utente) user).isActivo());
        }

        /**
         * Registo de um novo Utente.
         *
         * Validações:
         * - Email único.
         * - NIF único.
         *
         * Após criação:
         * - Password é guardada com hash (BCrypt).
         * - Utente é marcado como ativo.
         * - JWT é gerado automaticamente.
         */
        public AuthResponse registerUtente(UtenteRegisterRequest request) {

                // Verificar unicidade de email e NIF
                if (utilizadorRepository.existsByEmail(request.email())) {
                        throw new BadRequestException("Email já está em uso");
                }
                if (utilizadorRepository.existsByNif(request.nif())) {
                        throw new BadRequestException("NIF já está em uso");
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

                // Persistência
                utente = utenteRepository.save(utente);

                // Token JWT pós-registo
                var jwtToken = jwtService.generateToken(utente);
                long expiresAt = System.currentTimeMillis() + jwtService.getJwtExpiration();

                return new AuthResponse(
                                jwtToken,
                                utente.getId(),
                                utente.getEmail(),
                                utente.getNome(),
                                "UTENTE",
                                utente.getNif(),
                                utente.getTelefone(),
                                expiresAt,
                                true);
        }

        /**
         * Registo de um novo Funcionário.
         *
         * Particularidades:
         * - Tipo de funcionário é inferido a partir da string "funcao".
         * - Password é armazenada com hash.
         * - JWT é devolvido após criação.
         */
        public AuthResponse registerFuncionario(FuncionarioRegisterRequest request) {

                // Verificar unicidade de email e NIF
                if (utilizadorRepository.existsByEmail(request.email())) {
                        throw new BadRequestException("Email já está em uso");
                }
                if (utilizadorRepository.existsByNif(request.nif())) {
                        throw new BadRequestException("NIF já está em uso");
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

                // Persistência
                funcionario = funcionarioRepository.save(funcionario);

                // Token JWT pós-registo
                var jwtToken = jwtService.generateToken(funcionario);
                long expiresAt = System.currentTimeMillis() + jwtService.getJwtExpiration();

                return new AuthResponse(
                                jwtToken,
                                funcionario.getId(),
                                funcionario.getEmail(),
                                funcionario.getNome(),
                                "FUNCIONARIO",
                                funcionario.getNif(),
                                funcionario.getTelefone(),
                                expiresAt,
                                true);
        }

        public void updatePassword(Long userId, String newPassword) {
                var user = utilizadorRepository.findById(userId)
                                .orElseThrow(() -> new BadRequestException("Utilizador não encontrado"));

                user.setPassHash(passwordEncoder.encode(newPassword));

                if (user instanceof Utente utente) {
                        utente.setActivo(true);
                        utenteRepository.save(utente);
                } else {
                        // Funcionários assumimos que já estão ativos ou lógica diferente
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
