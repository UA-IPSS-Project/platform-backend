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
            AuthenticationManager authenticationManager) 
    {
        this.utilizadorRepository = utilizadorRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.utenteRepository = utenteRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }
    
    public AuthResponse loginFuncionario(LoginFuncionarioRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        var user = utilizadorRepository.findByEmail(request.email()).orElseThrow(() -> new BadRequestException("Funcionário não encontrado"));
        
        if (!(user instanceof Funcionario)) {
            throw new BadRequestException("Credenciais inválidas para funcionário");
        }

        var jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken, user.getId(), user.getEmail(), user.getNome(), "FUNCIONARIO", user.getNif(), user.getTelefone());
    }
    
    public AuthResponse loginUtente(LoginUtenteRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.nif(),
                        request.password()
                )
        );

        var user = utilizadorRepository.findByNif(request.nif()).orElseThrow(() -> new BadRequestException("Utente não encontrado"));
        
        if (!(user instanceof Utente)) {
            throw new BadRequestException("Credenciais inválidas para utente");
        }

        var jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken, user.getId(), user.getEmail(), user.getNome(), "UTENTE", user.getNif(), user.getTelefone());
    }

    public AuthResponse registerUtente(UtenteRegisterRequest request) {
        // Verificar se já existe utilizador com este email ou NIF
        if (utilizadorRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email já está em uso");
        }
        if (utilizadorRepository.existsByNif(request.nif())) {
            throw new BadRequestException("NIF já está em uso");
        }

        // Criar Utente com info básica
        Utente utente = new Utente();
        utente.setNome(request.nome());
        utente.setEmail(request.email());
        utente.setNif(request.nif());
        utente.setTelefone(request.telefone());
        utente.setPassHash(passwordEncoder.encode(request.password()));
        utente.setDataNasc(request.dataNasc());
        utente.setActivo(true);

        utente = utenteRepository.save(utente);

        var jwtToken = jwtService.generateToken(utente);
        return new AuthResponse(jwtToken, utente.getId(), utente.getEmail(), utente.getNome(), "UTENTE", utente.getNif(), utente.getTelefone());
    }

    public AuthResponse registerFuncionario(FuncionarioRegisterRequest request) {
        // Verificar se já existe utilizador com este email ou NIF
        if (utilizadorRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email já está em uso");
        }
        if (utilizadorRepository.existsByNif(request.nif())) {
            throw new BadRequestException("NIF já está em uso");
        }

        // Criar Funcionário com info básica
        Funcionario funcionario = new Funcionario();
        funcionario.setNome(request.nome());
        funcionario.setEmail(request.email());
        funcionario.setNif(request.nif());
        funcionario.setTelefone(request.contacto());
        funcionario.setPassHash(passwordEncoder.encode(request.password()));
        FuncionarioTipo tipo = mapFuncaoToTipo(request.funcao());
        funcionario.setTipo(tipo);
        funcionario.setDataNasc(request.dataNasc());

        funcionario = funcionarioRepository.save(funcionario);

        var jwtToken = jwtService.generateToken(funcionario);
        return new AuthResponse(jwtToken, funcionario.getId(), funcionario.getEmail(), funcionario.getNome(), "FUNCIONARIO", funcionario.getNif(), funcionario.getTelefone());
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
