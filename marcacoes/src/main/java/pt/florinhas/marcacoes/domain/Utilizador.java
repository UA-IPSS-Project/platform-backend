package pt.florinhas.marcacoes.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "utilizador")
public class Utilizador {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    private String password;
    private String email;
    private String role; // SECRETARIA, BALNEARIO, TECNICO, UTENTE
    
    private String nif;
    private Boolean nifValidado;
    
    private String tokenRecuperacao;
    private LocalDateTime tokenExpiracao;
    
    private Boolean passwordTemporario;
    
    @OneToOne
    @JoinColumn(name = "funcionario_id")
    private Funcionario funcionario;
    
    @OneToOne
    @JoinColumn(name = "utente_id")
    private Utente utente;
    
    public Utilizador() {}
    
    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public String getNif() { return nif; }
    public void setNif(String nif) { this.nif = nif; }
    
    public Boolean getNifValidado() { return nifValidado; }
    public void setNifValidado(Boolean nifValidado) { this.nifValidado = nifValidado; }
    
    public String getTokenRecuperacao() { return tokenRecuperacao; }
    public void setTokenRecuperacao(String tokenRecuperacao) { this.tokenRecuperacao = tokenRecuperacao; }
    
    public LocalDateTime getTokenExpiracao() { return tokenExpiracao; }
    public void setTokenExpiracao(LocalDateTime tokenExpiracao) { this.tokenExpiracao = tokenExpiracao; }
    
    public Boolean getPasswordTemporario() { return passwordTemporario; }
    public void setPasswordTemporario(Boolean passwordTemporario) { this.passwordTemporario = passwordTemporario; }
    
    public Funcionario getFuncionario() { return funcionario; }
    public void setFuncionario(Funcionario funcionario) { this.funcionario = funcionario; }
    
    public Utente getUtente() { return utente; }
    public void setUtente(Utente utente) { this.utente = utente; }
}