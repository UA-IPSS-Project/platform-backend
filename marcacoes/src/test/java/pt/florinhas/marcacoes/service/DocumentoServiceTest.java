package pt.florinhas.marcacoes.service;

import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.StatObjectResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import pt.florinhas.common_data.domain.Funcionario;
import pt.florinhas.common_data.domain.FuncionarioTipo;
import pt.florinhas.common_data.domain.NotificacaoTipo;
import pt.florinhas.common_data.domain.Utente;
import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.common_data.repository.FuncionarioRepository;
import pt.florinhas.marcacoes.domain.Documento;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.domain.MarcacaoBalneario;
import pt.florinhas.marcacoes.domain.MarcacaoSecretaria;
import pt.florinhas.marcacoes.dto.DocumentoDTO;
import pt.florinhas.marcacoes.dto.DocumentoMetadataDTO;
import pt.florinhas.marcacoes.exception.ResourceNotFoundException;
import pt.florinhas.marcacoes.repository.DocumentoRepository;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentoServiceTest {

    @Mock
    private DocumentoRepository documentoRepository;

    @Mock
    private MarcacaoRepository marcacaoRepository;

    @Mock
    private MinioClient minioClient;

    @Mock
    private FuncionarioRepository funcionarioRepository;

    @Mock
    private NotificacaoService notificacaoService;

    @InjectMocks
    private DocumentoService documentoService;

    private Marcacao marcacao;
    private Utente utente;
    private Documento documento;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(documentoService, "bucketName", "marcacoes");
        ReflectionTestUtils.setField(documentoService, "maxFileSize", 10_485_760L);

        utente = new Utente();
        utente.setId(100L);
        utente.setNome("Utente Teste");
        utente.setNif("100000002");
        utente.setEmail("utente@test.com");

        MarcacaoSecretaria secretaria = new MarcacaoSecretaria();
        secretaria.setAssunto("Pedido / Documentos");
        secretaria.setUtente(utente);

        marcacao = new Marcacao();
        marcacao.setId(1L);
        marcacao.setData(LocalDateTime.of(2026, 4, 10, 10, 30));
        marcacao.setMarcacaoSecretaria(secretaria);
        marcacao.setCriadoPor(utente);

        secretaria.setMarcacao(marcacao);

        documento = new Documento();
        documento.setId(50L);
        documento.setNomeOriginal("100000002_PEDIDO_DOCUMENTOS_1_20260410.pdf");
        documento.setNomeArmazenado("M1_D1.pdf");
        documento.setCaminho("2026/04/M1_D1.pdf");
        documento.setTipo("application/pdf");
        documento.setTamanho(123L);
        documento.setUploadedEm(LocalDateTime.of(2026, 4, 10, 9, 0));
        documento.setMarcacao(marcacao);
        documento.setSequencia(1);
    }

    private MultipartFile pdfFile(String filename) {
        return new MockMultipartFile(
                "file",
                filename,
                "application/pdf",
                "conteudo".getBytes()
        );
    }

    @Test
    void uploadDocumentoShouldThrowWhenMarcacaoNotFound() {
        when(marcacaoRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> documentoService.uploadDocumento(1L, pdfFile("doc.pdf"))
        );

        assertEquals("Marcação não encontrada com ID: 1", ex.getMessage());
    }

    @Test
    void uploadDocumentoShouldThrowWhenLimitReached() {
        when(marcacaoRepository.findById(1L)).thenReturn(Optional.of(marcacao));
        when(documentoRepository.countByMarcacaoId(1L)).thenReturn(10L);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> documentoService.uploadDocumento(1L, pdfFile("doc.pdf"))
        );

        assertEquals("Limite máximo de 10 ficheiros por marcação atingido.", ex.getMessage());
    }

    @Test
    void uploadDocumentoShouldThrowWhenFileEmpty() {
        when(marcacaoRepository.findById(1L)).thenReturn(Optional.of(marcacao));
        when(documentoRepository.countByMarcacaoId(1L)).thenReturn(0L);

        MultipartFile file = new MockMultipartFile("file", "vazio.pdf", "application/pdf", new byte[0]);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> documentoService.uploadDocumento(1L, file)
        );

        assertEquals("Ficheiro vazio não é permitido", ex.getMessage());
    }

    @Test
    void uploadDocumentoShouldThrowWhenFileTooLarge() {
        when(marcacaoRepository.findById(1L)).thenReturn(Optional.of(marcacao));
        when(documentoRepository.countByMarcacaoId(1L)).thenReturn(0L);
        ReflectionTestUtils.setField(documentoService, "maxFileSize", 5L);

        MultipartFile file = new MockMultipartFile(
                "file",
                "grande.pdf",
                "application/pdf",
                "123456".getBytes()
        );

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> documentoService.uploadDocumento(1L, file)
        );

        assertTrue(ex.getMessage().contains("Ficheiro excede o tamanho máximo permitido"));
    }

    @Test
    void uploadDocumentoShouldThrowWhenMimeTypeInvalid() {
        when(marcacaoRepository.findById(1L)).thenReturn(Optional.of(marcacao));
        when(documentoRepository.countByMarcacaoId(1L)).thenReturn(0L);

        MultipartFile file = new MockMultipartFile(
                "file",
                "script.exe",
                "application/octet-stream",
                "abc".getBytes()
        );

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> documentoService.uploadDocumento(1L, file)
        );

        assertEquals("Tipo de ficheiro não permitido. Tipos aceites: PDF, JPEG, PNG, DOC, DOCX", ex.getMessage());
    }

    @Test
    void uploadDocumentoShouldThrowWhenFilenameInvalid() {
        when(marcacaoRepository.findById(1L)).thenReturn(Optional.of(marcacao));
        when(documentoRepository.countByMarcacaoId(1L)).thenReturn(0L);

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(5L);
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getOriginalFilename()).thenReturn("");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> documentoService.uploadDocumento(1L, file)
        );

        assertEquals("Nome do ficheiro inválido", ex.getMessage());
    }

    @Test
    void uploadDocumentoShouldUploadAndPersistSuccessfully() throws Exception {
        when(marcacaoRepository.findById(1L)).thenReturn(Optional.of(marcacao));
        when(documentoRepository.countByMarcacaoId(1L)).thenReturn(2L);
        when(documentoRepository.findMaxSequenciaByMarcacaoId(1L)).thenReturn(Optional.of(2));
        when(minioClient.bucketExists(any())).thenReturn(true);
        when(documentoRepository.save(any(Documento.class))).thenAnswer(inv -> {
            Documento d = inv.getArgument(0);
            d.setId(99L);
            return d;
        });

        Funcionario secretaria1 = new Funcionario();
        secretaria1.setId(10L);
        Funcionario secretaria2 = new Funcionario();
        secretaria2.setId(11L);

        when(funcionarioRepository.findByTipo(FuncionarioTipo.SECRETARIA))
                .thenReturn(List.of(secretaria1, secretaria2));

        DocumentoDTO result = documentoService.uploadDocumento(1L, pdfFile("meu_doc.pdf"));

        assertNotNull(result);
        assertEquals(99L, result.id());
        assertEquals(1L, result.marcacaoId());
        assertEquals("Utente Teste", result.utenteNome());
        assertEquals("100000002", result.utenteNif());
        assertEquals(3, result.sequencia());

        ArgumentCaptor<Documento> captor = ArgumentCaptor.forClass(Documento.class);
        verify(documentoRepository).save(captor.capture());

        Documento saved = captor.getValue();
        assertEquals("application/pdf", saved.getTipo());
        assertEquals(3, saved.getSequencia());
        assertTrue(saved.getNomeOriginal().startsWith("100000002_PEDIDO_DOCUMENTOS_3_20260410"));
        assertEquals("M1_D3.pdf", saved.getNomeArmazenado());
        assertTrue(saved.getCaminho().endsWith("/M1_D3.pdf"));

        verify(minioClient).putObject(any());
        verify(notificacaoService).criarNotificacao(10L, "Novo documento enviado", "Um novo documento foi enviado para a marcação #1.", NotificacaoTipo.FICHEIRO);
        verify(notificacaoService).criarNotificacao(11L, "Novo documento enviado", "Um novo documento foi enviado para a marcação #1.", NotificacaoTipo.FICHEIRO);
    }

    @Test
    void uploadDocumentoShouldCreateBucketWhenMissing() throws Exception {
        when(marcacaoRepository.findById(1L)).thenReturn(Optional.of(marcacao));
        when(documentoRepository.countByMarcacaoId(1L)).thenReturn(0L);
        when(documentoRepository.findMaxSequenciaByMarcacaoId(1L)).thenReturn(Optional.empty());
        when(minioClient.bucketExists(any())).thenReturn(false);
        when(documentoRepository.save(any(Documento.class))).thenAnswer(inv -> {
            Documento d = inv.getArgument(0);
            d.setId(1L);
            return d;
        });
        when(funcionarioRepository.findByTipo(FuncionarioTipo.SECRETARIA)).thenReturn(List.of());

        DocumentoDTO result = documentoService.uploadDocumento(1L, pdfFile("doc.pdf"));

        assertNotNull(result);
        verify(minioClient).makeBucket(any());
        verify(minioClient).putObject(any());
    }

    @Test
    void uploadDocumentoShouldFallbackToCriadoPorNifWhenNoMarcacaoSecretariaUtente() throws Exception {
        Marcacao outraMarcacao = new Marcacao();
        outraMarcacao.setId(7L);
        outraMarcacao.setData(LocalDateTime.of(2026, 4, 12, 15, 0));

        Utilizador criador = new Utente();
        criador.setId(222L);
        criador.setNif("245000001");
        criador.setNome("Criador");
        outraMarcacao.setCriadoPor(criador);

        when(marcacaoRepository.findById(7L)).thenReturn(Optional.of(outraMarcacao));
        when(documentoRepository.countByMarcacaoId(7L)).thenReturn(0L);
        when(documentoRepository.findMaxSequenciaByMarcacaoId(7L)).thenReturn(Optional.empty());
        when(minioClient.bucketExists(any())).thenReturn(true);
        when(documentoRepository.save(any(Documento.class))).thenAnswer(inv -> {
            Documento d = inv.getArgument(0);
            d.setId(77L);
            return d;
        });
        when(funcionarioRepository.findByTipo(FuncionarioTipo.SECRETARIA)).thenReturn(List.of());

        documentoService.uploadDocumento(7L, pdfFile("fallback.pdf"));

        ArgumentCaptor<Documento> captor = ArgumentCaptor.forClass(Documento.class);
        verify(documentoRepository).save(captor.capture());
        assertTrue(captor.getValue().getNomeOriginal().startsWith("245000001_SEM_ASSUNTO_1_20260412"));
    }

    @Test
    void uploadDocumentoShouldUseBalnearioAssuntoWhenBalnearioExists() throws Exception {
        MarcacaoBalneario bal = new MarcacaoBalneario();
        Marcacao marcacaoBal = new Marcacao();
        marcacaoBal.setId(8L);
        marcacaoBal.setData(LocalDateTime.of(2026, 4, 13, 11, 0));
        marcacaoBal.setMarcacaoBalneario(bal);

        when(marcacaoRepository.findById(8L)).thenReturn(Optional.of(marcacaoBal));
        when(documentoRepository.countByMarcacaoId(8L)).thenReturn(0L);
        when(documentoRepository.findMaxSequenciaByMarcacaoId(8L)).thenReturn(Optional.empty());
        when(minioClient.bucketExists(any())).thenReturn(true);
        when(documentoRepository.save(any(Documento.class))).thenAnswer(inv -> {
            Documento d = inv.getArgument(0);
            d.setId(88L);
            return d;
        });

        DocumentoDTO result = documentoService.uploadDocumento(8L, pdfFile("bal.pdf"));

        assertEquals(88L, result.id());

        ArgumentCaptor<Documento> captor = ArgumentCaptor.forClass(Documento.class);
        verify(documentoRepository).save(captor.capture());
        assertTrue(captor.getValue().getNomeOriginal().contains("_BALNEARIO_1_"));
    }

    @Test
    void uploadDocumentoShouldWrapMinioFailureInIOException() throws Exception {
        when(marcacaoRepository.findById(1L)).thenReturn(Optional.of(marcacao));
        when(documentoRepository.countByMarcacaoId(1L)).thenReturn(0L);
        when(documentoRepository.findMaxSequenciaByMarcacaoId(1L)).thenReturn(Optional.empty());
        when(minioClient.bucketExists(any())).thenReturn(true);
        doThrow(new RuntimeException("MinIO down")).when(minioClient).putObject(any());

        IOException ex = assertThrows(
                IOException.class,
                () -> documentoService.uploadDocumento(1L, pdfFile("erro.pdf"))
        );

        assertEquals("Erro ao guardar ficheiro no MinIO", ex.getMessage());
        assertNotNull(ex.getCause());
    }

    @Test
    void listarDocumentosDaMarcacaoShouldMapDtos() {
        when(documentoRepository.findByMarcacaoId(1L)).thenReturn(List.of(documento));

        List<DocumentoDTO> result = documentoService.listarDocumentosDaMarcacao(1L);

        assertEquals(1, result.size());
        assertEquals("100000002_PEDIDO_DOCUMENTOS_1_20260410.pdf", result.get(0).nomeOriginal());
        assertEquals("Utente Teste", result.get(0).utenteNome());
    }

    @Test
    void pesquisarDocumentosPorMetadadosShouldThrowWhenIntervalInvalid() {
        LocalDateTime desde = LocalDateTime.of(2026, 4, 12, 0, 0);
        LocalDateTime ate = LocalDateTime.of(2026, 4, 10, 0, 0);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> documentoService.pesquisarDocumentosPorMetadados(
                        1L, null, null, null, null, null, desde, ate
                )
        );

        assertEquals("marcacaoDesde não pode ser posterior a marcacaoAte", ex.getMessage());
    }

    @Test
    void pesquisarDocumentosPorMetadadosShouldFilterByAllFields() {
        Documento outro = new Documento();
        outro.setId(51L);
        outro.setNomeOriginal("OUTRO.pdf");
        outro.setNomeArmazenado("X.pdf");
        outro.setTipo("image/png");
        outro.setTamanho(55L);
        outro.setMarcacao(marcacao);
        outro.setSequencia(2);

        when(documentoRepository.findByMarcacaoIdOrderByUploadedEmDesc(1L)).thenReturn(List.of(documento, outro));

        List<DocumentoDTO> result = documentoService.pesquisarDocumentosPorMetadados(
                1L,
                "documentos_1",
                "m1_d1",
                "application/pdf",
                "utente teste",
                "100000002",
                null,
                null
        );

        assertEquals(1, result.size());
        assertEquals(50L, result.get(0).id());
    }

    @Test
    void pesquisarDocumentosPorMetadadosShouldUseMarcacaoIntervalQueryWhenMarcacaoIdPresent() {
        LocalDateTime desde = LocalDateTime.of(2026, 4, 1, 0, 0);
        LocalDateTime ate = LocalDateTime.of(2026, 4, 30, 23, 59);

        when(documentoRepository.findByMarcacaoIdAndMarcacaoDataBetween(1L, desde, ate))
                .thenReturn(List.of(documento));

        List<DocumentoDTO> result = documentoService.pesquisarDocumentosPorMetadados(
                1L, null, null, null, null, null, desde, ate
        );

        assertEquals(1, result.size());
        verify(documentoRepository).findByMarcacaoIdAndMarcacaoDataBetween(1L, desde, ate);
    }

    @Test
    void pesquisarDocumentosPorMetadadosShouldUseGlobalRangeQueryWithoutMarcacaoId() {
        LocalDateTime desde = LocalDateTime.of(2026, 4, 1, 0, 0);
        LocalDateTime ate = LocalDateTime.of(2026, 4, 30, 23, 59);

        when(documentoRepository.findByMarcacaoDataBetween(desde, ate)).thenReturn(List.of(documento));

        List<DocumentoDTO> result = documentoService.pesquisarDocumentosPorMetadados(
                null, null, null, null, null, null, desde, ate
        );

        assertEquals(1, result.size());
        verify(documentoRepository).findByMarcacaoDataBetween(desde, ate);
    }

    @Test
    void obterDocumentoShouldReturnDto() {
        when(documentoRepository.findById(50L)).thenReturn(Optional.of(documento));

        DocumentoDTO result = documentoService.obterDocumento(50L);

        assertEquals(50L, result.id());
        assertEquals(1L, result.marcacaoId());
    }

    @Test
    void obterDocumentoShouldThrowWhenMissing() {
        when(documentoRepository.findById(50L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> documentoService.obterDocumento(50L)
        );

        assertEquals("Documento não encontrado com ID: 50", ex.getMessage());
    }

    @Test
    void obterMetadadosDocumentoShouldReturnCompleteMetadata() throws Exception {
        when(documentoRepository.findById(50L)).thenReturn(Optional.of(documento));

        StatObjectResponse stat = mock(StatObjectResponse.class);
        when(stat.userMetadata()).thenReturn(Map.of("k", "v"));
        when(stat.lastModified()).thenReturn(ZonedDateTime.parse("2026-04-10T12:00:00Z"));
        when(stat.etag()).thenReturn("etag-123");

        when(minioClient.statObject(any())).thenReturn(stat);

        DocumentoMetadataDTO result = documentoService.obterMetadadosDocumento(50L);

        assertEquals(50L, result.id());
        assertEquals("M1_D1.pdf", result.nomeArmazenado());
        assertEquals("etag-123", result.etag());
        assertEquals("v", result.minioUserMetadata().get("k"));
    }

    @Test
    void obterMetadadosDocumentoShouldThrowWhenMinioFails() throws Exception {
        when(documentoRepository.findById(50L)).thenReturn(Optional.of(documento));
        when(minioClient.statObject(any())).thenThrow(new RuntimeException("boom"));

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> documentoService.obterMetadadosDocumento(50L)
        );

        assertTrue(ex.getMessage().startsWith("Erro ao obter metadados do documento: "));
    }

    @Test
    void carregarFicheiroShouldReturnResource() throws Exception {
        when(documentoRepository.findById(50L)).thenReturn(Optional.of(documento));

        GetObjectResponse response = mock(GetObjectResponse.class);
        when(minioClient.getObject(any())).thenReturn(response);

        Resource resource = documentoService.carregarFicheiro(50L);

        assertNotNull(resource);
    }

    @Test
    void carregarFicheiroShouldThrowWhenMinioFails() throws Exception {
        when(documentoRepository.findById(50L)).thenReturn(Optional.of(documento));
        when(minioClient.getObject(any())).thenThrow(new RuntimeException("down"));

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> documentoService.carregarFicheiro(50L)
        );

        assertTrue(ex.getMessage().startsWith("Erro ao carregar ficheiro: " + documento.getNomeOriginal()));
    }

    @Test
    void removerDocumentoShouldDeleteWhenFound() {
        when(documentoRepository.findById(50L)).thenReturn(Optional.of(documento));

        documentoService.removerDocumento(50L);

        verify(documentoRepository).delete(documento);
    }

    @Test
    void removerDocumentoShouldThrowWhenMissing() {
        when(documentoRepository.findById(50L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> documentoService.removerDocumento(50L)
        );

        assertEquals("Documento não encontrado com ID: 50", ex.getMessage());
    }

    @Test
    void notificarDocumentoInvalidoShouldNotifyUtenteFromMarcacaoSecretaria() {
        when(marcacaoRepository.findById(1L)).thenReturn(Optional.of(marcacao));
        when(documentoRepository.findById(50L)).thenReturn(Optional.of(documento));

        documentoService.notificarDocumentoInvalido(1L, 50L, "Assinatura em falta");

        verify(notificacaoService).criarNotificacao(
                100L,
                "Documento inválido",
                "Na marcação do dia 2026-04-10, o documento '100000002_PEDIDO_DOCUMENTOS_1_20260410.pdf' é inválido. Motivo: Assinatura em falta",
                NotificacaoTipo.DOCUMENTO_INVALIDO
        );
    }

    @Test
    void notificarDocumentoInvalidoShouldFallbackToCriadoPor() {
        Marcacao semSecretaria = new Marcacao();
        semSecretaria.setId(2L);
        semSecretaria.setData(LocalDateTime.of(2026, 4, 20, 8, 0));

        Utilizador criador = new Utente();
        criador.setId(200L);
        criador.setNome("Criador");
        criador.setNif("200000001");
        semSecretaria.setCriadoPor(criador);

        Documento doc = new Documento();
        doc.setId(60L);
        doc.setNomeOriginal("doc.pdf");

        when(marcacaoRepository.findById(2L)).thenReturn(Optional.of(semSecretaria));
        when(documentoRepository.findById(60L)).thenReturn(Optional.of(doc));

        documentoService.notificarDocumentoInvalido(2L, 60L, null);

        verify(notificacaoService).criarNotificacao(
                200L,
                "Documento inválido",
                "Na marcação do dia 2026-04-20, o documento 'doc.pdf' é inválido.",
                NotificacaoTipo.DOCUMENTO_INVALIDO
        );
    }

    @Test
    void notificarDocumentoInvalidoShouldThrowWhenNoUtenteCanBeDetermined() {
        Marcacao semUtente = new Marcacao();
        semUtente.setId(3L);

        when(marcacaoRepository.findById(3L)).thenReturn(Optional.of(semUtente));
        when(documentoRepository.findById(50L)).thenReturn(Optional.of(documento));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> documentoService.notificarDocumentoInvalido(3L, 50L, "motivo")
        );

        assertEquals("Não foi possível identificar o utente para notificação.", ex.getMessage());
    }
}