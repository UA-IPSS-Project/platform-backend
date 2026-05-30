package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.StatObjectResponse;
import pt.florinhas.common_data.domain.Utente;
import pt.florinhas.common_data.exception.ResourceNotFoundException;
import pt.florinhas.common_data.repository.FuncionarioRepository;
import pt.florinhas.marcacoes.domain.Documento;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.domain.MarcacaoSecretaria;
import pt.florinhas.marcacoes.dto.DocumentoDTO;
import pt.florinhas.marcacoes.dto.DocumentoMetadataDTO;
import pt.florinhas.marcacoes.repository.DocumentoRepository;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;

class DocumentoServiceTest {

    private DocumentoRepository documentoRepository;
    private MarcacaoRepository marcacaoRepository;
    private MinioClient minioClient;
    private FuncionarioRepository funcionarioRepository;
    private NotificacaoService notificacaoService;
    private SystemConfigService systemConfigService;

    private DocumentoService service;

    @BeforeEach
    void setUp() throws Exception {

        documentoRepository = mock(DocumentoRepository.class);
        marcacaoRepository = mock(MarcacaoRepository.class);
        minioClient = mock(MinioClient.class);
        funcionarioRepository = mock(FuncionarioRepository.class);
        notificacaoService = mock(NotificacaoService.class);
        systemConfigService = mock(SystemConfigService.class);

        service = new DocumentoService(
                documentoRepository,
                marcacaoRepository,
                minioClient,
                funcionarioRepository,
                notificacaoService,
                systemConfigService);

        setField("bucketName", "marcacoes");
        setField("maxFileSize", 10485760L);
    }

    @Test
    void uploadDocumento_DeveFazerUpload() throws Exception {

        Utente utente = new Utente();
        utente.setNif("123456789");

        MarcacaoSecretaria secretaria = new MarcacaoSecretaria();
        secretaria.setUtente(utente);
        secretaria.setAssunto("Consulta");

        Marcacao marcacao = new Marcacao();
        marcacao.setId(1L);
        marcacao.setData(LocalDateTime.now());
        marcacao.setMarcacaoSecretaria(secretaria);

        MultipartFile file =
                new MockMultipartFile(
                        "file",
                        "teste.pdf",
                        "application/pdf",
                        "abc".getBytes());

        Documento documento = new Documento();

        documento.setId(1L);
        documento.setMarcacao(marcacao);

        when(marcacaoRepository.findById(1L))
                .thenReturn(Optional.of(marcacao));

        when(documentoRepository.countByMarcacaoId(1L))
                .thenReturn(0L);

        when(documentoRepository.findMaxSequenciaByMarcacaoId(1L))
                .thenReturn(Optional.of(0));

        when(minioClient.bucketExists(any()))
                .thenReturn(true);

        when(systemConfigService.getConfigValueAsInt(
                "documento.retencao.anos",
                5))
                .thenReturn(5);

        when(documentoRepository.save(any()))
                .thenReturn(documento);

        DocumentoDTO result =
                service.uploadDocumento(
                        1L,
                        file,
                        "RGPD");

        assertNotNull(result);

        verify(documentoRepository).save(any());
    }

    @Test
    void uploadDocumento_DeveLancarErroMarcacaoNaoExiste() {

        MultipartFile file =
                new MockMultipartFile(
                        "file",
                        "teste.pdf",
                        "application/pdf",
                        "abc".getBytes());

        when(marcacaoRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.uploadDocumento(
                        1L,
                        file,
                        "Teste"));
    }

    @Test
    void uploadDocumento_DeveLancarErroLimiteFicheiros() {

        MultipartFile file =
                new MockMultipartFile(
                        "file",
                        "teste.pdf",
                        "application/pdf",
                        "abc".getBytes());

        when(marcacaoRepository.findById(1L))
                .thenReturn(Optional.of(new Marcacao()));

        when(documentoRepository.countByMarcacaoId(1L))
                .thenReturn(10L);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.uploadDocumento(
                        1L,
                        file,
                        "Teste"));
    }

    @Test
    void listarDocumentosDaMarcacao_DeveRetornarLista() {

        Marcacao marcacao = new Marcacao();
        marcacao.setId(10L);

        Documento documento = new Documento();
        documento.setId(1L);
        documento.setMarcacao(marcacao);

        when(documentoRepository.findByMarcacaoId(1L))
                .thenReturn(List.of(documento));

        List<DocumentoDTO> result =
                service.listarDocumentosDaMarcacao(1L);

        assertEquals(1, result.size());
    }

    @Test
    void obterDocumento_DeveRetornarDocumento() {

        Marcacao marcacao = new Marcacao();
        marcacao.setId(10L);

        Documento documento = new Documento();
        documento.setId(1L);
        documento.setMarcacao(marcacao);

        when(documentoRepository.findById(1L))
                .thenReturn(Optional.of(documento));

        DocumentoDTO result =
                service.obterDocumento(1L);

        assertEquals(1L, result.id());
    }

    @Test
    void obterMetadadosDocumento_DeveRetornarMetadata() throws Exception {

        Documento documento = new Documento();

        documento.setId(1L);
        documento.setCaminho("2025/01/teste.pdf");

        Marcacao marcacao = new Marcacao();
        marcacao.setId(10L);

        documento.setMarcacao(marcacao);

        StatObjectResponse stat =
                mock(StatObjectResponse.class);

        when(documentoRepository.findById(1L))
                .thenReturn(Optional.of(documento));

        when(minioClient.statObject(any()))
                .thenReturn(stat);

        DocumentoMetadataDTO result =
                service.obterMetadadosDocumento(1L);

        assertEquals(1L, result.id());
    }

    @Test
    void carregarFicheiro_DeveRetornarResource() throws Exception {

        Documento documento = new Documento();

        documento.setCaminho("teste.pdf");
        documento.setNomeOriginal("teste.pdf");

        Marcacao marcacao = new Marcacao();
        marcacao.setId(10L);

        documento.setMarcacao(marcacao);

        GetObjectResponse response =
                mock(GetObjectResponse.class);

        when(documentoRepository.findById(1L))
                .thenReturn(Optional.of(documento));

        when(minioClient.getObject(any()))
                .thenReturn(response);

        Resource result =
                service.carregarFicheiro(1L);

        assertNotNull(result);
    }

    @Test
    void removerDocumento_DeveEliminar() {

        Documento documento = new Documento();

        Marcacao marcacao = new Marcacao();
        marcacao.setId(10L);

        documento.setMarcacao(marcacao);

        when(documentoRepository.findById(1L))
                .thenReturn(Optional.of(documento));

        service.removerDocumento(1L);

        verify(documentoRepository).delete(documento);
    }

    @Test
    void notificarDocumentoInvalido_DeveCriarNotificacao() {

        Utente utente = new Utente();
        utente.setId(1L);

        MarcacaoSecretaria secretaria =
                new MarcacaoSecretaria();

        secretaria.setUtente(utente);

        Marcacao marcacao = new Marcacao();
        marcacao.setMarcacaoSecretaria(secretaria);
        marcacao.setData(LocalDateTime.now());

        Documento documento = new Documento();
        documento.setNomeOriginal("teste.pdf");
        documento.setMarcacao(marcacao);

        when(marcacaoRepository.findById(1L))
                .thenReturn(Optional.of(marcacao));

        when(documentoRepository.findById(2L))
                .thenReturn(Optional.of(documento));

        service.notificarDocumentoInvalido(
                1L,
                2L,
                "Inválido");

        verify(notificacaoService).criarNotificacao(
                any(),
                any(),
                any(),
                any());
    }

    @Test
    void pesquisarDocumentosPorMetadados_DeveRetornarLista() {

        Marcacao marcacao = new Marcacao();
        marcacao.setId(10L);

        Documento documento = new Documento();
        documento.setNomeOriginal("teste.pdf");
        documento.setMarcacao(marcacao);

        when(documentoRepository.findAllByOrderByUploadedEmDesc())
                .thenReturn(List.of(documento));

        List<DocumentoDTO> result =
                service.pesquisarDocumentosPorMetadados(
                        null,
                        "teste",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);

        assertEquals(1, result.size());
    }

    @Test
    void pesquisarDocumentosPorMetadados_DeveLancarErroDatasInvalidas() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.pesquisarDocumentosPorMetadados(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        LocalDateTime.now(),
                        LocalDateTime.now().minusDays(1)));
    }

    private void setField(String fieldName, Object value) throws Exception {

        Field field =
                DocumentoService.class.getDeclaredField(fieldName);

        field.setAccessible(true);

        field.set(service, value);
    }
}