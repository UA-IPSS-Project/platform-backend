package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.minio.MinioClient;
import pt.florinhas.marcacoes.domain.Documento;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.dto.DocumentoDTO;
import pt.florinhas.marcacoes.exception.ResourceNotFoundException;
import pt.florinhas.marcacoes.repository.DocumentoRepository;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;

@ExtendWith(MockitoExtension.class)
class DocumentoServiceTest {

    @Mock
    private DocumentoRepository documentoRepository;

    @Mock
    private MarcacaoRepository marcacaoRepository;

    @Mock
    private MinioClient minioClient;

    @InjectMocks
    private DocumentoService documentoService;

    @Test
    void removerDocumento_DeveRemoverDaBaseDados_QuandoDocumentoExiste() {
        Documento documento = new Documento();
        documento.setId(10L);
        documento.setCaminho("2026/02/teste.pdf");

        when(documentoRepository.findById(10L)).thenReturn(Optional.of(documento));

        documentoService.removerDocumento(10L);

        verify(documentoRepository).delete(documento);
    }

    @Test
    void removerDocumento_DeveLancarResourceNotFound_QuandoDocumentoNaoExiste() {
        when(documentoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> documentoService.removerDocumento(99L));

        verify(documentoRepository, never()).delete(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void pesquisarDocumentosPorMetadados_DeveLancarIllegalArgumentException_QuandoIntervaloInvalido() {
        LocalDateTime desde = LocalDateTime.of(2026, 2, 26, 12, 0);
        LocalDateTime ate = LocalDateTime.of(2026, 2, 25, 12, 0);

        assertThrows(
            IllegalArgumentException.class,
            () -> documentoService.pesquisarDocumentosPorMetadados(1L, null, null, null, desde, ate)
        );

        verify(documentoRepository, never()).pesquisarPorMetadados(
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void pesquisarDocumentosPorMetadados_DeveMapearResultadosParaDTO() {
        Marcacao marcacao = new Marcacao();
        marcacao.setId(33L);

        Documento documento = new Documento();
        documento.setId(11L);
        documento.setNomeOriginal("relatorio.pdf");
        documento.setTipo("application/pdf");
        documento.setTamanho(2048L);
        documento.setUploadedEm(LocalDateTime.of(2026, 2, 25, 10, 30));
        documento.setMarcacao(marcacao);

        when(documentoRepository.pesquisarPorMetadados(33L, "relatorio", null, "application/pdf", null, null))
            .thenReturn(List.of(documento));

        List<DocumentoDTO> resultado = documentoService.pesquisarDocumentosPorMetadados(
            33L,
            "relatorio",
            null,
            "application/pdf",
            null,
            null
        );

        assertEquals(1, resultado.size());
        assertEquals(11L, resultado.get(0).id());
        assertEquals("relatorio.pdf", resultado.get(0).nomeOriginal());
        assertEquals(33L, resultado.get(0).marcacaoId());
    }
}
