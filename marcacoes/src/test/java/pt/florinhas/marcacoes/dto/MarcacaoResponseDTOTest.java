package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import pt.florinhas.marcacoes.domain.AtendimentoTipo;
import pt.florinhas.marcacoes.domain.EventoEstado;

class MarcacaoResponseDTOTest {

    @Test
    void deveCriarDTO() {

        MarcacaoResponseDTO.UtenteDTO utente =
                new MarcacaoResponseDTO.UtenteDTO(
                        1L,
                        "Nuno",
                        "test@test.com",
                        "123456789",
                        "912345678"
                );

        MarcacaoResponseDTO.MarcacaoSecretariaDTO secretaria =
                new MarcacaoResponseDTO.MarcacaoSecretariaDTO(
                        "Consulta",
                        "Descricao",
                        AtendimentoTipo.PRESENCIAL,
                        utente
                );

        MarcacaoResponseDTO.MarcacaoBalnearioDTO balneario =
                new MarcacaoResponseDTO.MarcacaoBalnearioDTO(
                        "Nuno",
                        true,
                        true,
                        "Admin",
                        "Obs",
                        List.of()
                );

        MarcacaoResponseDTO dto =
                new MarcacaoResponseDTO(
                        1L,
                        1L,
                        LocalDateTime.now(),
                        EventoEstado.AGENDADO,
                        "Funcionario",
                        "Nenhum",
                        secretaria,
                        balneario
                );

        assertEquals(1L, dto.getId());
        assertEquals(EventoEstado.AGENDADO, dto.getEstado());
        assertEquals("Funcionario", dto.getAtendenteNome());
    }
}