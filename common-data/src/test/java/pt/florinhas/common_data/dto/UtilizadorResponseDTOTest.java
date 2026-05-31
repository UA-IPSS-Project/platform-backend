package pt.florinhas.common_data.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import pt.florinhas.common_data.domain.Funcionario;
import pt.florinhas.common_data.domain.FuncionarioTipo;
import pt.florinhas.common_data.domain.Utente;

class UtilizadorResponseDTOTest {

    @Test
    void fromUtilizador_DeveConverterFuncionario() {

        Funcionario funcionario =
                new Funcionario();

        funcionario.setId(1L);
        funcionario.setNome("Teste");
        funcionario.setEmail("teste@teste.com");
        funcionario.setNif("123456789");
        funcionario.setTelefone("912345678");
        funcionario.setDataNasc(
                LocalDate.of(2000, 1, 1));
        funcionario.setTipo(
                FuncionarioTipo.SECRETARIA);
        funcionario.setActivo(true);

        UtilizadorResponseDTO dto =
                UtilizadorResponseDTO
                        .fromUtilizador(funcionario);

        assertEquals(1L, dto.getId());
        assertEquals("Teste", dto.getNome());
        assertEquals("SECRETARIA", dto.getFuncao());
        assertTrue(dto.isActive());
    }

    @Test
    void fromUtilizador_DeveConverterUtente() {

        Utente utente =
                new Utente();

        utente.setId(1L);
        utente.setNome("Utente");
        utente.setActivo(true);

        UtilizadorResponseDTO dto =
                UtilizadorResponseDTO
                        .fromUtilizador(utente);

        assertEquals(
                "UTENTE",
                dto.getFuncao());

        assertTrue(
                dto.isActive());
    }
}