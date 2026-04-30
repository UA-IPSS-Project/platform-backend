package pt.florinhas.common_data.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    @Column(name = "nif", nullable = false, unique = true, length = 64)
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
     * Timestamp de aceitação dos termos de uso (RGPD).
     * NULL = termos não aceites (conta criada pela secretaria ou ainda não
     * ativada).
     * NOT NULL = termos aceites, conta totalmente ativa.
     */
    @Column(name = "terms_accepted_at")
    private LocalDateTime termsAcceptedAt;

    /**
     * Timestamp de criação da conta.
     * Preenchido automaticamente na primeira persistência.
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Hash da palavra-passe (ex.: BCrypt -> 60 chars).
     * Nunca armazenar passwords em claro.
     */
    @JsonIgnore
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
     * Mapeia os tipos de funcionários para ROLE_SECRETARIA, ROLE_BALNEARIO, etc.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this instanceof Funcionario f) {
            String role = f.getTipo() != null ? "ROLE_" + f.getTipo().name() : "ROLE_FUNCIONARIO";
            return List.of(new SimpleGrantedAuthority(role));
        }
        return List.of(new SimpleGrantedAuthority("ROLE_UTENTE"));
    }

    // Hash da palavra-passe usado pelo provider (ex.: DaoAuthenticationProvider +
    // BCrypt).
    @Override
    @JsonIgnore
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

    /**
     * Lifecycle callback executado antes da primeira persistência.
     * Define o timestamp de criação.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
