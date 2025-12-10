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

@Service
@Transactional
public class UtilizadorService{

    @Autowired
    private UtilizadorRepository utilizadorRepository;
    
    @Autowired
    private UtenteRepository utenteRepository;
    
    @Autowired
    private pt.florinhas.marcacoes.repository.FuncionarioRepository funcionarioRepository;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    
    public Optional<Utilizador> buscarPorNif(String nif) {
        if (nif == null || nif.trim().isEmpty()) {
            throw new IllegalArgumentException("NIF não pode ser nulo ou vazio");
        }
        return utilizadorRepository.findByNif(nif);
    }


    
    public Utente obterOuCriarUtente(String nif, String nome, String email, String telefone) {
        // Validar NIF obrigatório
        if (nif == null || nif.trim().isEmpty()) {
            throw new RuntimeException("NIF do utente é obrigatório");
        }
        
        // Procurar utente por NIF
        return utenteRepository.findByNif(nif).orElseGet(() -> {
            // Se não existir, criar novo utente
            System.out.println("Utente com NIF " + nif + " não encontrado. Criando novo utente...");
            
            // Validar campos necessários para criar utente
            if (nome == null || nome.trim().isEmpty()) {
                throw new RuntimeException("Nome do utente é obrigatório para criar novo registo");
            }
            if (email == null || email.trim().isEmpty()) {
                throw new RuntimeException("Email do utente é obrigatório para criar novo registo");
            }
            if (telefone == null || telefone.trim().isEmpty()) {
                throw new RuntimeException("Telefone do utente é obrigatório para criar novo registo");
            }
            
            // Verificar se email já existe
            if (utenteRepository.existsByEmail(email)) {
                throw new RuntimeException("Email já está registado no sistema");
            }
            
            // Criar novo utente
            Utente novoUtente = new Utente();
            novoUtente.setNif(nif);
            novoUtente.setNome(nome);
            novoUtente.setEmail(email);
            novoUtente.setTelefone(telefone);
            novoUtente.setActivo(false); // Inactivo até dar login pela primeira vez
            String passwordTemporaria = nif;
            novoUtente.setPassHash(passwordEncoder.encode(passwordTemporaria));
            
            System.out.println("Novo utente criado com password temporária = NIF: " + passwordTemporaria);
            
            return utenteRepository.save(novoUtente);
        });
    }

    public Utilizador obterUtilizadorPorId(Long utilizadorId) {
        return utilizadorRepository.findById(utilizadorId)
            .orElseThrow(() -> new RuntimeException("Utilizador não encontrado com ID: " + utilizadorId));
    }

    public Utilizador atualizarUtilizador(Long utilizadorId, UtilizadorInfoDTO request) {
        // Buscar utilizador existente
        Utilizador utilizador = utilizadorRepository.findById(utilizadorId)
            .orElseThrow(() -> new RuntimeException("Utilizador não encontrado com ID: " + utilizadorId));
        
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
                LocalDate dataNasc = LocalDate.parse(request.getDataNasc(), DateTimeFormatter.ISO_LOCAL_DATE);
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

    public long contarUtentesAtivos() {
        return utenteRepository.countByActivo(true);
    }

    // Métodos privados auxiliares
    
    private boolean validarNIF(String nif) {
        // TODO: melhorar validação de NIF com API Externa
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
    
    private void enviarTokenAcesso(Utente utente) {
        String token = gerarToken();
        String mensagem = ("Foi criada uma conta automática para si. Use o token %s para aceder à plataforma. " + 
                          "Será obrigatório definir uma nova palavra-passe no primeiro acesso.").formatted(token);
        
        if (utente.getEmail() != null) {
            //emailService.enviarEmail(utente.getEmail(), "Token de Acesso - Plataforma", mensagem);
            System.out.println("Email enviado para " + utente.getEmail() + " com token: " + token + " e mensagem: " + mensagem);
        }
    }
    
    private String gerarToken() {
        return String.valueOf((int) ((ThreadLocalRandom.current().nextDouble() * 900000) + 100000));
    }
}
