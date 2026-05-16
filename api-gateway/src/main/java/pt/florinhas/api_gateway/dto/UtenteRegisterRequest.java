package pt.florinhas.api_gateway.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.AssertTrue;

import jakarta.validation.constraints.Pattern;

/**
 * DTO (Java record) para pedido de registo de Utente.
 *
 * Características:
 * - Imutável por definição (record), adequado para transporte de dados entre
 * camadas.
 * - Integra Bean Validation para validação automática em controllers
 * com @Valid.
 */
public record UtenteRegisterRequest(

        // Nome completo do utente (obrigatório).
        @NotBlank(message = "Nome é obrigatório") String nome,

        // Email do utente (obrigatório e com formato válido).
        @NotBlank(message = "Email é obrigatório") @Email(message = "Email deve ser válido") String email,

        /**
         * Palavra-passe em claro recebida do frontend (obrigatória; mínimo 6).
         * Será cifrada (ex.: BCrypt) antes de persistência.
         */
        @NotBlank(message = "Password é obrigatória") @Size(min = 6, message = "Password deve ter pelo menos 6 caracteres") String password,

        // NIF do utente (9 dígitos).
        @NotBlank(message = "NIF é obrigatório") @Pattern(regexp = "\\d{9}", message = "NIF deve ter 9 dígitos") String nif,

        // Telefone de contacto do utente (opcional; se preenchido, deve ter 9 dígitos).
        @Pattern(regexp = "\\d{9}", message = "Telefone deve ter 9 dígitos") String telefone,

        // Data de nascimento (obrigatória).
        @NotNull(message = "Data de nascimento é obrigatória") LocalDate dataNasc,

        /**
         * Indicador de aceitação dos termos de uso (RGPD).
         * Obrigatório ser true para completar o registo.
         */
        @NotNull(message = "Deve aceitar os termos de uso") @AssertTrue(message = "Deve aceitar os termos de uso para se registar") Boolean termsAccepted) {
}
