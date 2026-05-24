package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pt.florinhas.marcacoes.domain.ConfiguracaoAgenda;
import pt.florinhas.marcacoes.domain.EventoEstado;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.dto.ConfiguracaoSlotDTO;
import pt.florinhas.marcacoes.repository.BloqueioRepository;
import pt.florinhas.marcacoes.repository.ConfiguracaoAgendaRepository;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;
import pt.florinhas.common_data.domain.Utilizador;

@ExtendWith(MockitoExtension.class)
class CalendarioServiceTest {

    @Mock
    private BloqueioRepository bloqueioRepository;

    @Mock
    private MarcacaoRepository marcacaoRepository;

    @Mock
    private ConfiguracaoAgendaRepository configuracaoAgendaRepository;

    @Mock
    private NotificacaoService notificacaoService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private CalendarioService calendarioService;

    @Test
    void atualizarCapacidadePorSlot_quandoDiminuiCapacidade_deveCancelarMarcacoesExcedentesNoFuturo() {
        // Arrange
        String tipo = "SECRETARIA";
        int oldCapacidade = 3;
        int novaCapacidade = 1;

        // Configuração antiga
        ConfiguracaoAgenda cfg = new ConfiguracaoAgenda();
        cfg.setTipo(tipo);
        cfg.setCapacidadePorSlot(oldCapacidade);

        when(configuracaoAgendaRepository.findByTipo(tipo)).thenReturn(Optional.of(cfg));
        when(configuracaoAgendaRepository.save(any(ConfiguracaoAgenda.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Criar marcações futuras no mesmo slot para simular excesso
        LocalDateTime slotTime = LocalDateTime.now().plusDays(2);
        
        Marcacao m1 = new Marcacao();
        m1.setId(1L);
        m1.setData(slotTime);
        m1.setEstado(EventoEstado.AGENDADO);
        m1.setCriadoEm(LocalDateTime.now().minusHours(2));

        Utilizador criador = new Utilizador();
        criador.setId(10L);

        Marcacao m2 = new Marcacao();
        m2.setId(2L);
        m2.setData(slotTime);
        m2.setEstado(EventoEstado.AGENDADO);
        m2.setCriadoEm(LocalDateTime.now().minusHours(1));
        m2.setCriadoPor(criador);

        List<Marcacao> futureMarcacoes = new ArrayList<>(List.of(m1, m2));
        when(marcacaoRepository.findActiveFutureMarcacoes(any(LocalDateTime.class), eq(tipo))).thenReturn(futureMarcacoes);

        // Act
        ConfiguracaoSlotDTO result = calendarioService.atualizarCapacidadePorSlot(tipo, novaCapacidade);

        // Assert
        assertNotNull(result);
        assertEquals(novaCapacidade, result.getCapacidadePorSlot());

        // Como a capacidade reduziu de 3 para 1, e havia 2 marcações, a mais recente (m2, criada há 1h) deve ser cancelada,
        // enquanto a mais antiga (m1, criada há 2h) deve ser mantida.
        assertEquals(EventoEstado.AGENDADO, m1.getEstado());
        assertEquals(EventoEstado.CANCELADO, m2.getEstado());
        assertNotNull(m2.getMotivoCancelamento());
        assertTrue(m2.getMotivoCancelamento().contains("redução de capacidade"));

        verify(marcacaoRepository, times(1)).save(m2);
        verify(auditLogService, times(1)).log(eq("ATUALIZAR_ESTADO_MARCACAO"), eq("MARCACAO"), eq(2L), anyString());
        verify(notificacaoService, times(1)).notificarCancelamento(any(), any(), anyString());
    }
}
