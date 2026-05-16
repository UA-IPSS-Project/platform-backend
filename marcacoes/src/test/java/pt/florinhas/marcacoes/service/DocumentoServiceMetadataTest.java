package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;

import pt.florinhas.common_data.exception.ResourceNotFoundException;
import pt.florinhas.marcacoes.domain.Documento;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.dto.DocumentoMetadataDTO;
import pt.florinhas.marcacoes.repository.DocumentoRepository;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;

@ExtendWith(MockitoExtension.class)
class DocumentoServiceMetadataTest {

    @Mock
    private DocumentoRepository documentoRepository;

    @Mock
    private MarcacaoRepository marcacaoRepository;

    @Mock
    private MinioClient minioClient;

    @Mock
    private pt.florinhas.common_data.repository.FuncionarioRepository funcionarioRepository;

    @Mock
    private NotificacaoService notificacaoService;

    @Mock
    private SystemConfigService systemConfigService;

    @InjectMocks
    private DocumentoService documentoService;

    @BeforeEach
    void setup() {

        ReflectionTestUtils.setField(
                documentoService,
                "bucketName",
                "marcacoes"
        );
    }

    @Test
    void obterMetadadosDocumento_DeveRetornarMetadata()
            throws Exception {

        Documento documento =
                criarDocumento();

        StatObjectResponse stat =
                mock(StatObjectResponse.class);

        when(documentoRepository.findById(1L))
                .thenReturn(Optional.of(documento));

        when(stat.etag())
                .thenReturn("etag123");

        when(stat.userMetadata())
                .thenReturn(Map.of("key", "value"));

        when(minioClient.statObject(any(StatObjectArgs.class)))
                .thenReturn(stat);

        DocumentoMetadataDTO dto =
                documentoService.obterMetadadosDocumento(1L);

        assertNotNull(dto);
    }

    @Test
    void obterMetadadosDocumento_DeveLancarErro()
            throws Exception {

        Documento documento =
                criarDocumento();

        when(documentoRepository.findById(1L))
                .thenReturn(Optional.of(documento));

        when(minioClient.statObject(any(StatObjectArgs.class)))
                .thenThrow(new RuntimeException("erro"));

        assertThrows(
                ResourceNotFoundException.class,
                () -> documentoService.obterMetadadosDocumento(1L)
        );
    }

    @Test
    void carregarFicheiro_DeveRetornarResource()
            throws Exception {

        Documento documento =
                criarDocumento();

        GetObjectResponse response =
                mock(GetObjectResponse.class);

        when(documentoRepository.findById(1L))
                .thenReturn(Optional.of(documento));

        when(minioClient.getObject(any(GetObjectArgs.class)))
                .thenReturn(response);

        Resource resource =
                documentoService.carregarFicheiro(1L);

        assertNotNull(resource);
    }

    @Test
    void carregarFicheiro_DeveLancarErro()
            throws Exception {

        Documento documento =
                criarDocumento();

        when(documentoRepository.findById(1L))
                .thenReturn(Optional.of(documento));

        when(minioClient.getObject(any(GetObjectArgs.class)))
                .thenThrow(new RuntimeException("erro"));

        assertThrows(
                ResourceNotFoundException.class,
                () -> documentoService.carregarFicheiro(1L)
        );
    }

    private Documento criarDocumento() {

        Documento documento =
                new Documento();

        documento.setId(1L);
        documento.setNomeOriginal("teste.pdf");
        documento.setNomeArmazenado("M1_D1.pdf");
        documento.setCaminho("2026/05/M1_D1.pdf");

        Marcacao marcacao =
                new Marcacao();

        marcacao.setId(1L);

        documento.setMarcacao(marcacao);

        return documento;
    }
}