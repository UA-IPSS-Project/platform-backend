package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.domain.MarcacaoBalneario;
import pt.florinhas.marcacoes.dto.MarcacaoResponseDTO;
import pt.florinhas.marcacoes.dto.RoupaDTO;
import pt.florinhas.marcacoes.repository.ItemArmazemRepository;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;
import pt.florinhas.marcacoes.validation.MarcacaoValidator;

@ExtendWith(MockitoExtension.class)
class MarcacaoServiceBalnearioTest {

    @Mock private MarcacaoRepository marcacaoRepository;
    @Mock private ItemArmazemRepository itemArmazemRepository;
    @Mock private MarcacaoValidator marcacaoValidator;
    @Mock private AuditLogService auditLogService;

    @InjectMocks
    private MarcacaoService service;

    @Test
    void atualizarDetalhesBalneario_DeveAtualizar() {

        Marcacao marcacao = new Marcacao();

        MarcacaoBalneario bal = new MarcacaoBalneario();
        bal.setRoupas(new ArrayList<>());

        marcacao.setMarcacaoBalneario(bal);

        when(marcacaoRepository.findById(1L))
                .thenReturn(Optional.of(marcacao));

        RoupaDTO roupa = new RoupaDTO();
        roupa.setCategoria("Casaco");

        MarcacaoResponseDTO dto =
                service.atualizarDetalhesBalneario(
                        1L,
                        true,
                        false,
                        List.of(roupa)
                );

        assertNotNull(dto);

        verify(auditLogService).log(
                eq("ATUALIZAR_DETALHES_BALNEARIO"),
                eq("MARCACAO"),
                eq(1L),
                anyString()
        );
    }

    @Test
    void atualizarDetalhesBalneario_DeveLancarExcecaoQuandoNaoExiste() {

        when(marcacaoRepository.findById(1L))
                .thenReturn(Optional.empty());

        List<RoupaDTO> roupas = List.of();
        assertThrows(IllegalArgumentException.class,
                () -> service.atualizarDetalhesBalneario(
                        1L,
                        true,
                        false,
                        roupas
                ));
    }
}