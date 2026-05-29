package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import pt.florinhas.marcacoes.domain.AtendimentoTipo;
import pt.florinhas.marcacoes.domain.EventoEstado;

class MarcacaoResponseDTOTest {

    @Test
    void marcacaoResponseDTO_DeveGuardarValores() {

        MarcacaoResponseDTO dto = new MarcacaoResponseDTO();

        dto.setId(1L);
        dto.setVersion(2L);
        dto.setData(LocalDateTime.now());
        dto.setEstado(EventoEstado.AGENDADO);
        dto.setAtendenteNome("Nuno");
        dto.setMotivoCancelamento("Teste");

        dto.setMarcacaoSecretaria(
                new MarcacaoResponseDTO.MarcacaoSecretariaDTO(
                        "Assunto",
                        "Descrição",
                        AtendimentoTipo.PRESENCIAL,
                        new MarcacaoResponseDTO.UtenteDTO(
                                3L,
                                "Utente",
                                "teste@teste.com",
                                "123456789",
                                "912345678")));

        dto.setMarcacaoBalneario(
                new MarcacaoResponseDTO.MarcacaoBalnearioDTO(
                        "Utente",
                        true,
                        false,
                        "Responsável",
                        "Obs",
                        List.of()));

        assertEquals(1L, dto.getId());
        assertEquals(2L, dto.getVersion());
        assertEquals(EventoEstado.AGENDADO, dto.getEstado());
        assertEquals("Nuno", dto.getAtendenteNome());
        assertEquals("Teste", dto.getMotivoCancelamento());
        assertEquals("Assunto", dto.getMarcacaoSecretaria().getAssunto());
        assertEquals("Utente", dto.getMarcacaoBalneario().getNomeUtente());
    }
}