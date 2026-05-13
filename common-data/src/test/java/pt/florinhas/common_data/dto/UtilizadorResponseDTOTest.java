package pt.florinhas.common_data.dto;

import static org.junit.jupiter.api.Assertions.*;

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
        funcionario.setNome("Funcionario");
        funcionario.setEmail("func@teste.com");
        funcionario.setNif("123456789");
        funcionario.setTelefone("912345678");
        funcionario.setDataNasc(LocalDate.of(1990, 1, 1));
        funcionario.setActivo(true);
        funcionario.setTipo(FuncionarioTipo.SECRETARIA);

        UtilizadorResponseDTO dto =
                UtilizadorResponseDTO
                        .fromUtilizador(funcionario);

        assertEquals(1L, dto.getId());
        assertEquals("Funcionario", dto.getNome());
        assertEquals("SECRETARIA", dto.getFuncao());
        assertTrue(dto.isActive());
    }

    @Test
    void fromUtilizador_DeveConverterUtente() {

        Utente utente =
                new Utente();

        utente.setId(2L);
        utente.setNome("Utente");
        utente.setEmail("utente@teste.com");
        utente.setNif("987654321");
        utente.setTelefone("911111111");
        utente.setActivo(true);

        UtilizadorResponseDTO dto =
                UtilizadorResponseDTO
                        .fromUtilizador(utente);

        assertEquals(2L, dto.getId());
        assertEquals("UTENTE", dto.getFuncao());
        assertTrue(dto.isActive());
    }
}