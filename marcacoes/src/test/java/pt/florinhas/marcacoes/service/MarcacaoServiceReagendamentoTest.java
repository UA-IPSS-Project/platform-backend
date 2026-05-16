package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityNotFoundException;

import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.dto.MarcacaoResponseDTO;
import pt.florinhas.marcacoes.dto.ReagendarMarcacaoRequest;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;
import pt.florinhas.marcacoes.validation.MarcacaoValidator;

@ExtendWith(MockitoExtension.class)
class MarcacaoServiceReagendamentoTest {

    @Mock
    private MarcacaoRepository marcacaoRepository;

    @Mock
    private CalendarioService calendarioService;

    @Mock
    private MarcacaoValidator marcacaoValidator;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private AuthorizationService authorizationService;

    @InjectMocks
    private MarcacaoService service;

    @Test
    void reagendarMarcacao_DeveReagendar() {

        Marcacao marcacao = new Marcacao();
        marcacao.setId(1L);
        marcacao.setData(LocalDateTime.now());

        when(marcacaoRepository.findById(1L))
                .thenReturn(Optional.of(marcacao));

        when(calendarioService.getCapacidadePorSlot(any()))
                .thenReturn(5);

        when(marcacaoRepository.countByDataAndEstadoInAndTipo(any(), any(), any()))
                .thenReturn(0L);

        when(marcacaoRepository.save(any()))
                .thenReturn(marcacao);

        ReagendarMarcacaoRequest request =
                new ReagendarMarcacaoRequest();

        request.setNovaDataHora(
                LocalDateTime.now().plusDays(1)
        );

        MarcacaoResponseDTO dto =
                service.reagendarMarcacao(1L, request);

        assertNotNull(dto);

        verify(auditLogService).log(
                eq("REAGENDAR_MARCACAO"),
                eq("MARCACAO"),
                eq(1L),
                anyString()
        );
    }

    @Test
    void reagendarMarcacao_DeveLancarExcecaoQuandoMarcacaoNaoExiste() {

        when(marcacaoRepository.findById(1L))
                .thenReturn(Optional.empty());

        ReagendarMarcacaoRequest request =
                new ReagendarMarcacaoRequest();

        assertThrows(
                EntityNotFoundException.class,
                () -> service.reagendarMarcacao(1L, request)
        );
    }

    @Test
    void reagendarMarcacao_DeveLancarExcecaoQuandoSlotCheio() {

        Marcacao marcacao = new Marcacao();
        marcacao.setId(1L);
        marcacao.setData(LocalDateTime.now());

        when(marcacaoRepository.findById(1L))
                .thenReturn(Optional.of(marcacao));

        when(calendarioService.getCapacidadePorSlot(any()))
                .thenReturn(1);

        when(marcacaoRepository.countByDataAndEstadoInAndTipo(any(), any(), any()))
                .thenReturn(1L);

        ReagendarMarcacaoRequest request =
                new ReagendarMarcacaoRequest();

        request.setNovaDataHora(
                LocalDateTime.now().plusDays(1)
        );

        assertThrows(
                IllegalStateException.class,
                () -> service.reagendarMarcacao(1L, request)
        );
    }
}