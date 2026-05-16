package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pt.florinhas.common_data.exception.ResourceNotFoundException;
import pt.florinhas.marcacoes.domain.Documento;
import pt.florinhas.marcacoes.dto.DocumentoDTO;
import pt.florinhas.marcacoes.repository.DocumentoRepository;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;

@ExtendWith(MockitoExtension.class)
class DocumentoServiceConsultaTest {

    @Mock
    private DocumentoRepository documentoRepository;

    @Mock
    private MarcacaoRepository marcacaoRepository;

    @Mock
    private io.minio.MinioClient minioClient;

    @Mock
    private pt.florinhas.common_data.repository.FuncionarioRepository funcionarioRepository;

    @Mock
    private NotificacaoService notificacaoService;

    @Mock
    private SystemConfigService systemConfigService;

    @InjectMocks
    private DocumentoService documentoService;

    private Documento documento;

    @BeforeEach
    void setup() {

        documento = new Documento();

        documento.setId(1L);
        documento.setNomeOriginal("teste.pdf");
        documento.setNomeArmazenado("M1_D1.pdf");
        documento.setTipo("application/pdf");
        documento.setUploadedEm(LocalDateTime.now());

        pt.florinhas.marcacoes.domain.Marcacao marcacao =
                new pt.florinhas.marcacoes.domain.Marcacao();

        marcacao.setId(1L);

        documento.setMarcacao(marcacao);
    }

    @Test
    void listarDocumentosDaMarcacao_DeveRetornarLista() {

        when(documentoRepository.findByMarcacaoId(1L))
                .thenReturn(List.of(documento));

        List<DocumentoDTO> resultado =
                documentoService.listarDocumentosDaMarcacao(1L);

        assertEquals(1, resultado.size());

        verify(documentoRepository)
                .findByMarcacaoId(1L);
    }

    @Test
    void listarDocumentosDaMarcacao_DeveRetornarListaVazia() {

        when(documentoRepository.findByMarcacaoId(1L))
                .thenReturn(List.of());

        List<DocumentoDTO> resultado =
                documentoService.listarDocumentosDaMarcacao(1L);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void obterDocumento_DeveRetornarDocumento() {

        when(documentoRepository.findById(1L))
                .thenReturn(Optional.of(documento));

        DocumentoDTO dto =
                documentoService.obterDocumento(1L);

        assertNotNull(dto);
    }

    @Test
    void obterDocumento_DeveLancarExcecao() {

        when(documentoRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> documentoService.obterDocumento(1L)
        );
    }

    @Test
    void pesquisarDocumentosPorMetadados_DeveFiltrarPorNomeOriginal() {

        when(documentoRepository.findAllByOrderByUploadedEmDesc())
                .thenReturn(List.of(documento));

        List<DocumentoDTO> resultado =
                documentoService.pesquisarDocumentosPorMetadados(
                        null,
                        "teste",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );

        assertEquals(1, resultado.size());
    }

    @Test
    void pesquisarDocumentosPorMetadados_DeveFiltrarPorTipo() {

        when(documentoRepository.findAllByOrderByUploadedEmDesc())
                .thenReturn(List.of(documento));

        List<DocumentoDTO> resultado =
                documentoService.pesquisarDocumentosPorMetadados(
                        null,
                        null,
                        null,
                        "application/pdf",
                        null,
                        null,
                        null,
                        null
                );

        assertEquals(1, resultado.size());
    }

    @Test
    void pesquisarDocumentosPorMetadados_DeveRetornarVazioQuandoNaoCoincide() {

        when(documentoRepository.findAllByOrderByUploadedEmDesc())
                .thenReturn(List.of(documento));

        List<DocumentoDTO> resultado =
                documentoService.pesquisarDocumentosPorMetadados(
                        null,
                        "naoexiste",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );

        assertTrue(resultado.isEmpty());
    }

    @Test
    void pesquisarDocumentosPorMetadados_DeveLancarErroQuandoDatasInvalidas() {

        LocalDateTime inicio =
                LocalDateTime.now();

        LocalDateTime fim =
                inicio.minusDays(1);

        assertThrows(
                IllegalArgumentException.class,
                () -> documentoService.pesquisarDocumentosPorMetadados(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        inicio,
                        fim
                )
        );
    }
}