package pt.florinhas.marcacoes.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import pt.florinhas.marcacoes.service.DocumentoStorageCleanupService;

/**
 * Entidade JPA que representa um documento anexado a uma marcação.
 * 
 * Cada documento está associado a uma marcação específica e armazena
 * metadados sobre o ficheiro enviado pelo utente.
 */
@Entity
@Table(name = "Documento")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Documento {

    /**
     * Chave primária autogerada (IDENTITY).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Número sequencial do documento dentro da marcação.
     * Substitui o UUID para identificação amigável.
     */
    @Column(name = "sequencia")
    private Integer sequencia;

    /**
     * Nome original do ficheiro quando foi enviado.
     */
    @Column(name = "nome_original", nullable = false, length = 255)
    private String nomeOriginal;

    /**
     * Nome do ficheiro armazenado no sistema de ficheiros (geralmente um UUID).
     * Este nome garante unicidade e evita conflitos.
     */
    @Column(name = "nome_armazenado", nullable = false, length = 255, unique = true)
    private String nomeArmazenado;

    /**
     * Caminho relativo onde o ficheiro está armazenado.
     * Exemplo: "uploads/documentos/2024/01/..."
     */
    @Column(name = "caminho", nullable = false, length = 500)
    private String caminho;

    /**
     * Tipo MIME do ficheiro (ex: application/pdf, image/jpeg).
     */
    @Column(name = "tipo", nullable = false, length = 100)
    private String tipo;

    /**
     * Tamanho do ficheiro em bytes.
     */
    @Column(name = "tamanho", nullable = false)
    private Long tamanho;

    /**
     * Finalidade do documento (RGPD - art.º 13.º).
     * Exemplo: "Comprovativo de residência", "Atestado médico", etc.
     */
    @Column(name = "finalidade", length = 255)
    private String finalidade;

    /**
     * Data/hora em que o documento foi enviado.
     */
    @Column(name = "uploaded_em", nullable = false, updatable = false)
    private LocalDateTime uploadedEm;

    /**
     * Data de expiração do documento (RGPD - retenção de dados).
     * Após esta data, o documento pode ser automaticamente removido.
     */
    @Column(name = "data_expiracao")
    private LocalDateTime dataExpiracao;

    /**
     * Marcação à qual este documento está associado.
     * Relação N:1 (vários documentos podem pertencer a uma marcação).
     */
    @ManyToOne
    @JoinColumn(name = "marcacao_id", nullable = false)
    private Marcacao marcacao;

    /**
     * Define a data de upload antes de persistir.
     */
    @PrePersist
    protected void onCreate() {
        uploadedEm = LocalDateTime.now();
    }

    /**
     * Remove o ficheiro físico do MinIO antes de eliminar o registo na BD.
     *
     * Garante limpeza também em remoções por cascata/orphanRemoval.
     */
    @PreRemove
    protected void onRemove() {
        DocumentoStorageCleanupService.removerDoArmazenamento(caminho, id);
    }
}