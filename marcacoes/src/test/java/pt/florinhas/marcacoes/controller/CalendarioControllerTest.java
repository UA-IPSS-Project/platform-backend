package pt.florinhas.marcacoes.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import pt.florinhas.common_data.domain.Funcionario;
import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.common_data.repository.UtilizadorRepository;
import pt.florinhas.marcacoes.domain.BloqueioAgenda;
import pt.florinhas.marcacoes.dto.AtualizarConfiguracaoSlotRequest;
import pt.florinhas.marcacoes.dto.BloquearHorarioRequest;
import pt.florinhas.marcacoes.dto.ConfiguracaoSlotDTO;
import pt.florinhas.marcacoes.exception.NotFoundException;
import pt.florinhas.marcacoes.service.CalendarioService;

class CalendarioControllerTest {

    private CalendarioService calendarioService;
    private UtilizadorRepository utilizadorRepository;
    private CalendarioController controller;

    @BeforeEach
    void setUp() {
        calendarioService = mock(CalendarioService.class);
        utilizadorRepository = mock(UtilizadorRepository.class);
        controller = new CalendarioController(calendarioService, utilizadorRepository);
    }

    @Test
    @DisplayName("CalendarioController deve ser criado")
    void deveCriarController() {
        assertNotNull(controller);
    }

    @Test
    @DisplayName("Classe CalendarioController deve carregar")
    void classeDeveCarregar() {
        assertNotNull(CalendarioController.class);
    }

    @Test
    @DisplayName("bloquearHorario deve lancar NotFoundException quando o funcionario nao existir")
    void bloquearHorario_DeveLancarNotFoundException_QuandoFuncionarioNaoExiste() {
        BloquearHorarioRequest request = new BloquearHorarioRequest();
        request.setFuncionarioId(99L);

        when(utilizadorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> controller.bloquearHorario(request));
        verifyNoInteractions(calendarioService);
    }

    @Test
    @DisplayName("bloquearHorario deve bloquear com sucesso quando o funcionario existir")
    void bloquearHorario_DeveBloquearComSucesso_QuandoFuncionarioExiste() {
        LocalDate date = LocalDate.of(2026, 5, 20);
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(12, 0);

        BloquearHorarioRequest request = new BloquearHorarioRequest();
        request.setFuncionarioId(1L);
        request.setData(date);
        request.setHoraInicio(start);
        request.setHoraFim(end);
        request.setMotivo("Reuniao");
        request.setTipo(null); // Should fallback to "SECRETARIA"

        Utilizador funcionario = new Funcionario();
        funcionario.setId(1L);

        when(utilizadorRepository.findById(1L)).thenReturn(Optional.of(funcionario));

        ResponseEntity<Map<String, String>> response = controller.bloquearHorario(request);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Bloqueio registado com sucesso", response.getBody().get("message"));

        verify(calendarioService).bloquearHorario(eq(date), eq(start), eq(end), eq("Reuniao"), eq(funcionario), eq("SECRETARIA"));
    }

    @Test
    @DisplayName("bloquearHorario deve usar o tipo especificado quando fornecido")
    void bloquearHorario_DeveUsarTipoEspecificado_QuandoTipoForFornecido() {
        LocalDate date = LocalDate.of(2026, 5, 20);
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(12, 0);

        BloquearHorarioRequest request = new BloquearHorarioRequest();
        request.setFuncionarioId(1L);
        request.setData(date);
        request.setHoraInicio(start);
        request.setHoraFim(end);
        request.setMotivo("Manutencao");
        request.setTipo("BALNEARIO");

        Utilizador funcionario = new Funcionario();
        funcionario.setId(1L);

        when(utilizadorRepository.findById(1L)).thenReturn(Optional.of(funcionario));

        ResponseEntity<Map<String, String>> response = controller.bloquearHorario(request);

        assertEquals(200, response.getStatusCode().value());
        verify(calendarioService).bloquearHorario(eq(date), eq(start), eq(end), eq("Manutencao"), eq(funcionario), eq("BALNEARIO"));
    }

    @Test
    @DisplayName("removerBloqueio deve remover o bloqueio e retornar OK")
    void removerBloqueio_DeveRemoverEretornarOk() {
        ResponseEntity<Map<String, String>> response = controller.removerBloqueio(123L);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Bloqueio removido", response.getBody().get("message"));
        verify(calendarioService).removerBloqueio(123L);
    }

    @Test
    @DisplayName("listarBloqueios deve retornar bloqueios do mes quando filtros mes/ano presentes")
    void listarBloqueios_DeveRetornarBloqueiosDoMes_QuandoFiltrosMesAnoPresentes() {
        List<BloqueioAgenda> mockList = List.of(new BloqueioAgenda());
        when(calendarioService.getBloqueiosDoMes(2026, 5, "SECRETARIA")).thenReturn(mockList);

        ResponseEntity<List<BloqueioAgenda>> response = controller.listarBloqueios(2026, 5, "SECRETARIA");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(mockList, response.getBody());
    }

    @Test
    @DisplayName("listarBloqueios deve retornar todos bloqueios quando filtros mes/ano ausentes")
    void listarBloqueios_DeveRetornarTodosBloqueios_QuandoFiltrosAnoMesAusentes() {
        List<BloqueioAgenda> mockList = List.of(new BloqueioAgenda());
        when(calendarioService.getTodosBloqueios("BALNEARIO")).thenReturn(mockList);

        ResponseEntity<List<BloqueioAgenda>> response = controller.listarBloqueios(null, null, "BALNEARIO");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(mockList, response.getBody());
    }

    @Test
    @DisplayName("listarFeriados deve retornar lista vazia quando o ano for nulo")
    void listarFeriados_DeveRetornarListaVazia_QuandoAnoForNulo() {
        ResponseEntity<List<String>> response = controller.listarFeriados(null);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verifyNoInteractions(calendarioService);
    }

    @Test
    @DisplayName("listarFeriados deve retornar feriados mapeados quando o ano for fornecido")
    void listarFeriados_DeveRetornarFeriadosMapeados_QuandoAnoForFornecido() {
        List<LocalDate> feriados = List.of(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 25));
        when(calendarioService.getFeriadosDoAno(2026)).thenReturn(feriados);

        ResponseEntity<List<String>> response = controller.listarFeriados(2026);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(List.of("2026-01-01", "2026-12-25"), response.getBody());
    }

    @Test
    @DisplayName("verificarSlot deve retornar status do slot")
    void verificarSlot_DeveRetornarStatusDoSlot() {
        LocalDate date = LocalDate.of(2026, 5, 20);
        LocalTime time = LocalTime.of(10, 0);
        when(calendarioService.isSlotBloqueado(date, time, "SECRETARIA")).thenReturn(true);

        ResponseEntity<Boolean> response = controller.verificarSlot("2026-05-20", "10:00", "SECRETARIA");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(true, response.getBody());
    }

    @Test
    @DisplayName("listarConfiguracaoSlots deve retornar configuracoes de capacidade")
    void listarConfiguracaoSlots_DeveRetornarListaConfiguracoes() {
        List<ConfiguracaoSlotDTO> mockList = List.of(new ConfiguracaoSlotDTO("SECRETARIA", 5));
        when(calendarioService.listarConfiguracoesSlot()).thenReturn(mockList);

        ResponseEntity<List<ConfiguracaoSlotDTO>> response = controller.listarConfiguracaoSlots();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(mockList, response.getBody());
    }

    @Test
    @DisplayName("atualizarConfiguracaoSlot deve retornar configuracao atualizada")
    void atualizarConfiguracaoSlot_DeveRetornarConfiguracaoAtualizada() {
        ConfiguracaoSlotDTO mockDto = new ConfiguracaoSlotDTO("BALNEARIO", 10);
        AtualizarConfiguracaoSlotRequest request = new AtualizarConfiguracaoSlotRequest(10);
        when(calendarioService.atualizarCapacidadePorSlot("BALNEARIO", 10)).thenReturn(mockDto);

        ResponseEntity<ConfiguracaoSlotDTO> response = controller.atualizarConfiguracaoSlot("BALNEARIO", request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(mockDto, response.getBody());
    }
}