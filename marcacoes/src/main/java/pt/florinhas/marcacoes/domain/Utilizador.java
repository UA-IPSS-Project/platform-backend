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

/**
 * Entidade JPA base para todos os utilizadores do sistema.
 *
 * Estratégia de herança:
 * - @Inheritance(strategy = JOINED): cada subclasse (ex.: Utente, Funcionario)
 * tem a sua tabela,
 * partilhando a mesma PK com a tabela "Utilizador". Garante normalização e
 * evita colunas nulas.
 *
 * Integração com Spring Security:
 * - Implementa UserDetails para integração direta no mecanismo de
 * autenticação/autorização.
 * - As authorities (roles) são derivadas dinamicamente do tipo concreto
 * (Funcionario vs Utente).
 */
@Entity
@Table(name = "Utilizador")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Utilizador implements UserDetails {

    /**
     * Chave primária autogerada (IDENTITY).
     * Partilhada pelas tabelas filhas na estratégia JOINED.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========= Atributos de identidade e contacto =========

    /**
     * Número de Identificação Fiscal (9 dígitos).
     * Único e obrigatório.
     */
    @Column(name = "nif", nullable = false, unique = true, length = 9)
    private String nif;

    // Nome completo do utilizador. Obrigatório.
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    /**
     * Email do utilizador. Usado como username no Spring Security.
     * Nota: se o sistema permitir login por NIF/telefone, adequar getUsername().
     */
    @Column(name = "email", length = 100)
    private String email;

    // Telefone do utilizador (texto para acomodar prefixos e espaços).
    @Column(name = "telefone", length = 20)
    private String telefone;

    @Column(name = "dataNasc")
    private LocalDate dataNasc;

    /**
     * Hash da palavra-passe (ex.: BCrypt -> 60 chars).
     * Nunca armazenar passwords em claro.
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    @Column(name = "passHash", length = 60)
    private String passHash;

    // ========= Informações de morada (residencial) =========

    // Morada residencial (texto livre).
    private String morada;

    // Código postal (texto para manter formatação com hífen).
    private String codigoPostal;

    // Freguesia de residência.
    private String freguesia;

    // ========= Informação profissional (opcional) =========

    // Telefone do local de trabalho.
    private String telefoneEmprego;

    // Nome/local do emprego.
    private String localEmprego;

    // Morada do emprego.
    private String moradaEmprego;

    // Profissão do utilizador.
    private String profissao;

    // ========= Implementação de UserDetails (Spring Security) =========

    /**
     * Devolve as authorities (roles) do utilizador autenticado.
     * Lógica: se for instância de Funcionario => ROLE_FUNCIONARIO, caso contrário
     * => ROLE_UTENTE.
     * Em cenários com perfis mais finos, complementar com perfis/valências.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Se for Funcionario retorna ROLE_FUNCIONARIO, se for Utente retorna
        // ROLE_UTENTE
        String role = this instanceof Funcionario ? "ROLE_FUNCIONARIO" : "ROLE_UTENTE";
        return List.of(new SimpleGrantedAuthority(role));
    }

    // Hash da palavra-passe usado pelo provider (ex.: DaoAuthenticationProvider +
    // BCrypt).
    @Override
    @com.fasterxml.jackson.annotation.JsonIgnore
    public String getPassword() {
        return passHash;
    }

    /**
     * Username usado para login. Aqui mapeado para email.
     * Atenção: garantir unicidade e não-nulidade se o email é o identificador de
     * login.
     */
    @Override
    public String getUsername() {
        return email;
    }

    // Indica se a conta não expirou. Sem política de expiração, mantém-se true.
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indica se a conta não está bloqueada. Sem bloqueios lógicos, mantém-se true.
     * Caso o domínio exija bloqueio (ex.: tentativas falhadas), adaptar.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // Indica se as credenciais não expiraram. Sem política de rotação, mantém-se
    // true.
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Ativação lógica da conta no contexto do Spring Security.
     * Atualmente devolve sempre true. Se desejar refletir o estado de 'ativo' do
     * Utente,
     * pode ser sobreposto nas subclasses ou controlado no AuthenticationProvider.
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
