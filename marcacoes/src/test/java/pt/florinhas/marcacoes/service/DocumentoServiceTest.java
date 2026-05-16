package pt.florinhas.marcacoes.service;

import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.StatObjectResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import pt.florinhas.common_data.domain.Funcionario;
import pt.florinhas.common_data.domain.FuncionarioTipo;
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

    @Mock private DocumentoRepository documentoRepository;
    @Mock private MarcacaoRepository marcacaoRepository;
    @Mock private MinioClient minioClient;
    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private NotificacaoService notificacaoService;
    @Mock private SystemConfigService systemConfigService;

    private DocumentoService documentoService;

    private Marcacao marcacao;
    private Utente utente;
    private Documento documento;

    @BeforeEach
    void setup() {
        documentoService = new DocumentoService(
            documentoRepository, marcacaoRepository, minioClient, 
            funcionarioRepository, notificacaoService, systemConfigService
        );
        
        ReflectionTestUtils.setField(documentoService, "bucketName", "marcacoes");
        ReflectionTestUtils.setField(documentoService, "maxFileSize", 10_485_760L);

        utente = new Utente();
        utente.setId(100L);
        utente.setNome("Utente Teste");
        utente.setNif("100000002");

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
        return new MockMultipartFile("file", filename, "application/pdf", "conteudo".getBytes());
    }

    @Test
    @DisplayName("Deve lançar erro ao fazer upload para marcação inexistente")
    void uploadDocumentoShouldThrowWhenMarcacaoNotFound() {
        when(marcacaoRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> documentoService.uploadDocumento(1L, pdfFile("doc.pdf"), "f"));
    }

    @Test
    @DisplayName("Deve lançar erro ao exceder limite de documentos")
    void uploadDocumentoShouldThrowWhenLimitReached() {
        when(marcacaoRepository.findById(1L)).thenReturn(Optional.of(marcacao));
        when(documentoRepository.countByMarcacaoId(1L)).thenReturn(10L);
        assertThrows(IllegalArgumentException.class, () -> documentoService.uploadDocumento(1L, pdfFile("doc.pdf"), "f"));
    }

    @Test
    @DisplayName("Deve fazer upload e persistir com sucesso")
    void uploadDocumentoShouldUploadAndPersistSuccessfully() throws Exception {
        when(marcacaoRepository.findById(1L)).thenReturn(Optional.of(marcacao));
        when(documentoRepository.countByMarcacaoId(1L)).thenReturn(2L);
        when(documentoRepository.findMaxSequenciaByMarcacaoId(1L)).thenReturn(Optional.of(2));
        when(minioClient.bucketExists(any())).thenReturn(true);
        when(systemConfigService.getConfigValueAsInt(anyString(), anyInt())).thenReturn(5);
        when(documentoRepository.save(any(Documento.class))).thenAnswer(inv -> {
            Documento d = inv.getArgument(0);
            d.setId(99L);
            return d;
        });

        DocumentoDTO result = documentoService.uploadDocumento(1L, pdfFile("meu_doc.pdf"), "Finalidade Teste");

        assertNotNull(result);
        assertEquals(99L, result.id());
        verify(documentoRepository).save(any(Documento.class));
        verify(minioClient).putObject(any());
    }

    @Test
    @DisplayName("Deve listar documentos de uma marcação")
    void listarDocumentosDaMarcacaoShouldMapDtos() {
        when(documentoRepository.findByMarcacaoId(1L)).thenReturn(List.of(documento));
        List<DocumentoDTO> result = documentoService.listarDocumentosDaMarcacao(1L);
        assertEquals(1, result.size());
        assertEquals(documento.getId(), result.get(0).id());
    }

    @Test
    @DisplayName("Deve carregar ficheiro como recurso")
    void carregarFicheiroShouldReturnResource() throws Exception {
        when(documentoRepository.findById(50L)).thenReturn(Optional.of(documento));
        GetObjectResponse response = mock(GetObjectResponse.class);
        when(minioClient.getObject(any())).thenReturn(response);

        Resource resource = documentoService.carregarFicheiro(50L);
        assertNotNull(resource);
    }

    @Test
    @DisplayName("Deve remover documento com sucesso")
    void removerDocumentoShouldDeleteWhenFound() {
        when(documentoRepository.findById(50L)).thenReturn(Optional.of(documento));
        documentoService.removerDocumento(50L);
        verify(documentoRepository).delete(documento);
    }

    @Test
    @DisplayName("Deve notificar utente de documento inválido")
    void notificarDocumentoInvalidoShouldNotifyUtente() {
        when(marcacaoRepository.findById(1L)).thenReturn(Optional.of(marcacao));
        when(documentoRepository.findById(50L)).thenReturn(Optional.of(documento));

        documentoService.notificarDocumentoInvalido(1L, 50L, "Motivo");

        verify(notificacaoService).criarNotificacao(anyLong(), anyString(), anyString(), eq("DOCUMENTO_INVALIDO"));
    }
}