package pt.florinhas.marcacoes.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.common_data.repository.UtilizadorRepository;
import pt.florinhas.marcacoes.domain.BloqueioAgenda;
import pt.florinhas.marcacoes.dto.AtualizarConfiguracaoSlotRequest;
import pt.florinhas.marcacoes.dto.ConfiguracaoSlotDTO;
import pt.florinhas.marcacoes.dto.BloquearHorarioRequest;
import pt.florinhas.marcacoes.exception.NotFoundException;
import pt.florinhas.marcacoes.service.CalendarioService;

class CalendarioControllerTest {

    private CalendarioService service;
    private UtilizadorRepository repository;

    private CalendarioController controller;

    @BeforeEach
    void setUp() {

        service = mock(CalendarioService.class);

        repository = mock(UtilizadorRepository.class);

        controller = new CalendarioController(service, repository);
    }

    @Test
    void bloquearHorario_DeveRetornar200() {

        Utilizador user =
                new Utilizador();

        BloquearHorarioRequest request =
                mock(BloquearHorarioRequest.class);

        when(request.getFuncionarioId())
                .thenReturn(1L);

        when(repository.findById(1L))
                .thenReturn(Optional.of(user));

        ResponseEntity<?> result =
                controller.bloquearHorario(request);

        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    void bloquearHorario_DeveFalharSemFuncionario() {

        BloquearHorarioRequest request =
                mock(BloquearHorarioRequest.class);

        when(request.getFuncionarioId())
                .thenReturn(1L);

        when(repository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> controller.bloquearHorario(request));
    }

    @Test
    void listarBloqueios_DeveRetornarLista() {

        BloqueioAgenda bloqueio =
                new BloqueioAgenda();

        bloqueio.setId(1L);
        bloqueio.setData(java.time.LocalDate.now());
        bloqueio.setHoraInicio(java.time.LocalTime.of(10, 0));
        bloqueio.setHoraFim(java.time.LocalTime.of(11, 0));
        bloqueio.setMotivo("Teste");

        when(service.getTodosBloqueios(null))
                .thenReturn(List.of(bloqueio));

        ResponseEntity<?> result =
                controller.listarBloqueios(
                        null,
                        null,
                        null);

        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    void listarFeriados_DeveRetornarListaVazia() {

        ResponseEntity<List<String>> result =
                controller.listarFeriados(null);

        assertEquals(
                0,
                result.getBody().size());
    }

    @Test
    void verificarSlot_DeveRetornarBoolean() {

        when(service.isSlotBloqueado(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()))
                .thenReturn(true);

        ResponseEntity<Boolean> result =
                controller.verificarSlot(
                        "2026-01-01",
                        "10:00",
                        "SECRETARIA");

        assertEquals(true, result.getBody());
    }

    @Test
    void previewReducaoSlot_DeveFalhar() {

        assertThrows(
                ResponseStatusException.class,
                () -> controller.previewReducaoSlot(
                        "SECRETARIA",
                        0));
    }

    @Test
    void previewReducaoSlot_DeveRetornarMapa() {

        when(service.previewReducaoCapacidade(
                "SECRETARIA",
                5))
                .thenReturn(2);

        ResponseEntity<Map<String, Integer>> result =
                controller.previewReducaoSlot(
                        "SECRETARIA",
                        5);

        assertEquals(
                2,
                result.getBody()
                        .get("marcacoesAfetadas"));
    }

    @Test
    void atualizarConfiguracaoSlot_DeveRetornarDto() {

        ConfiguracaoSlotDTO dto =
                mock(ConfiguracaoSlotDTO.class);

        AtualizarConfiguracaoSlotRequest request =
                mock(AtualizarConfiguracaoSlotRequest.class);

        when(request.getCapacidadePorSlot())
                .thenReturn(5);

        when(service.atualizarCapacidadePorSlot(
                "SECRETARIA",
                5))
                .thenReturn(dto);

        ResponseEntity<ConfiguracaoSlotDTO> result =
                controller.atualizarConfiguracaoSlot(
                        "SECRETARIA",
                        request);

        assertEquals(dto, result.getBody());
    }
}