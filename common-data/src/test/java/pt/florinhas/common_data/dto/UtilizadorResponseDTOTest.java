package pt.florinhas.common_data.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import pt.florinhas.common_data.domain.Funcionario;
import pt.florinhas.common_data.domain.FuncionarioTipo;
import pt.florinhas.common_data.domain.Utente;
import pt.florinhas.common_data.domain.Utilizador;

class UtilizadorResponseDTOTest {

    @Test
    void fromUtilizador_DeveConverterFuncionario() {

        Funcionario funcionario =
                new Funcionario();

        funcionario.setId(1L);
        funcionario.setNome("Funcionario");
        funcionario.setEmail("func@email.com");
        funcionario.setNif("123456789");
        funcionario.setTelefone("912345678");
        funcionario.setDataNasc(
                LocalDate.of(2000, 1, 1));
        funcionario.setMorada("Rua");
        funcionario.setCodigoPostal("1234");
        funcionario.setFreguesia("Aveiro");
        funcionario.setProfissao("Admin");
        funcionario.setLocalEmprego("Florinhas");
        funcionario.setMoradaEmprego("Rua Trabalho");
        funcionario.setTelefoneEmprego("999999999");
        funcionario.setActivo(true);
        funcionario.setTipo(
                FuncionarioTipo.SECRETARIA);

        UtilizadorResponseDTO dto =
                UtilizadorResponseDTO.fromUtilizador(
                        funcionario);

        assertEquals(1L, dto.getId());
        assertEquals("Funcionario", dto.getNome());
        assertEquals("SECRETARIA", dto.getFuncao());
        assertTrue(dto.isActive());
        assertTrue(dto.isCreatedBySecretaria());
    }

    @Test
    void fromUtilizador_DeveConverterUtente() {

        Utente utente =
                new Utente();

        utente.setActivo(true);

        UtilizadorResponseDTO dto =
                UtilizadorResponseDTO.fromUtilizador(
                        utente);

        assertEquals(
                "UTENTE",
                dto.getFuncao());

        assertTrue(dto.isActive());
    }

    @Test
    void fromUtilizador_DeveMarcarCreatedBySecretariaFalse() {

        Funcionario funcionario =
                new Funcionario();

        funcionario.setTermsAcceptedAt(
                LocalDateTime.now());

        funcionario.setActivo(true);

        UtilizadorResponseDTO dto =
                UtilizadorResponseDTO.fromUtilizador(
                        funcionario);

        assertEquals(
                false,
                dto.isCreatedBySecretaria());
    }

    @Test
    void fromUtilizador_DeveConverterUtilizadorBase() {

        Utilizador utilizador =
                new Utilizador();

        UtilizadorResponseDTO dto =
                UtilizadorResponseDTO.fromUtilizador(
                        utilizador);

        assertTrue(dto.isActive());
    }
}