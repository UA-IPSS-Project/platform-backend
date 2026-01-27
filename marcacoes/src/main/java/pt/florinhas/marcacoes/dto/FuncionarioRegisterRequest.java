package pt.florinhas.marcacoes.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.AssertTrue;

import jakarta.validation.constraints.Pattern;

/**
 * DTO (Java record) para pedido de registo de Funcionário.
 *
 * Características:
 * - Imutável por definição (record): ideal para transporte de dados entre
 * camadas.
 * - Integra Bean Validation nas componentes, permitindo validação automática em
 * controllers
 * com @Valid (ex.: mensagens de erro personalizadas em português).
 */
public record FuncionarioRegisterRequest(

        // Nome completo do funcionário (obrigatório).
        @NotBlank(message = "Nome é obrigatório") String nome,

        // Email institucional/pessoal do funcionário (obrigatório e válido).
        @NotBlank(message = "Email é obrigatório") @Email(message = "Email deve ser válido") String email,

        /**
         * Palavra-passe em claro recebida do frontend (obrigatória; mínimo 6).
         * Será posteriormente cifrada (ex.: BCrypt) antes de persistência.
         */
        @NotBlank(message = "Password é obrigatória") @Size(min = 6, message = "Password deve ter pelo menos 6 caracteres") String password,

        // NIF com exatamente 9 dígitos (obrigatório).
        @NotBlank(message = "NIF é obrigatório") @Pattern(regexp = "\\d{9}", message = "NIF deve ter 9 dígitos") String nif,

        // Contacto telefónico (obrigatório). Formato livre; validar formato se
        // necessário.
        @NotBlank(message = "Contacto é obrigatório") @Pattern(regexp = "\\d{9}", message = "Contacto deve ter 9 dígitos") String contacto,

        // Função/cargo do funcionário (obrigatório). Ex.: "SECRETARIA", "BALNEARIO".
        @NotBlank(message = "Função é obrigatória") String funcao,

        // Data de nascimento (obrigatória).
        @NotNull(message = "Data de nascimento é obrigatória") LocalDate dataNasc,

        /**
         * Indicador de aceitação dos termos de uso (RGPD).
         * Obrigatório ser true para completar o registo.
         */
        @NotNull(message = "Deve aceitar os termos de uso") @AssertTrue(message = "Deve aceitar os termos de uso para se registar") Boolean termsAccepted) {
}
