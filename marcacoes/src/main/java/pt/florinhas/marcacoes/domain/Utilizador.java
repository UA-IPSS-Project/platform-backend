package pt.florinhas.marcacoes.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "Utilizador")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Utilizador implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Atributos próprios do Utilizador
    @Column(name = "nif", nullable = false, unique = true, length = 9)
    private String nif;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "telefone", length = 20)
    private String telefone;

    @Column(name = "dataNasc")
    private LocalDate dataNasc;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @Column(name = "passHash", length = 60)
    private String passHash;

    // Informações de morada
    private String morada;
    private String codigoPostal;
    private String freguesia;

    // Informação profissional
    private String telefoneEmprego;
    private String localEmprego;
    private String moradaEmprego;
    private String profissao;

    // Implementação UserDetails
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Se for Funcionario retorna ROLE_FUNCIONARIO, se for Utente retorna
        // ROLE_UTENTE
        String role = this instanceof Funcionario ? "ROLE_FUNCIONARIO" : "ROLE_UTENTE";
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public String getPassword() {
        return passHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}