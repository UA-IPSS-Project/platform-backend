package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.dto.CriarMarcacaoRequest;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;

@ExtendWith(MockitoExtension.class)
class MarcacaoServiceReservaTemporariaTest {

        @Mock
        private MarcacaoRepository marcacaoRepository;
        @Mock
        private CalendarioService calendarioService;

        @InjectMocks
        private MarcacaoService service;

        @Test
        void criarReservaTemporaria_DeveCriarReserva() {

                CriarMarcacaoRequest request = new CriarMarcacaoRequest();
                request.setTipoAgenda("SECRETARIA");
                request.setData(LocalDateTime.now().plusDays(1));

                when(calendarioService.getCapacidadePorSlot(any()))
                                .thenReturn(5);

                when(marcacaoRepository.countByDataAndEstadoInAndTipo(any(), any(), any()))
                                .thenReturn(0L);

                Marcacao saved = new Marcacao();
                saved.setId(1L);

                when(marcacaoRepository.save(any()))
                                .thenReturn(saved);

                Long id = service.criarReservaTemporaria(request);

                assertEquals(1L, id);
        }

        @Test
        void criarReservaTemporaria_DeveLancarExcecaoQuandoSlotCheio() {

                CriarMarcacaoRequest request = new CriarMarcacaoRequest();
                request.setTipoAgenda("SECRETARIA");
                request.setData(LocalDateTime.now());

                when(calendarioService.getCapacidadePorSlot(any()))
                                .thenReturn(1);

                when(marcacaoRepository.countByDataAndEstadoInAndTipo(any(), any(), any()))
                                .thenReturn(1L);

                assertThrows(IllegalStateException.class,
                                () -> service.criarReservaTemporaria(request));
        }

        @Test
        void apagarReservaTemporaria_DeveApagar() {

                when(marcacaoRepository.existsById(1L))
                                .thenReturn(true);

                service.apagarReservaTemporaria(1L);

                verify(marcacaoRepository).deleteById(1L);
        }
}