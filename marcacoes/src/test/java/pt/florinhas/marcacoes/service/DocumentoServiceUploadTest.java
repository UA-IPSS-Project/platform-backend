package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;

import pt.florinhas.common_data.domain.Funcionario;
import pt.florinhas.common_data.domain.FuncionarioTipo;
import pt.florinhas.common_data.domain.Utente;
import pt.florinhas.common_data.repository.FuncionarioRepository;
import pt.florinhas.marcacoes.domain.Documento;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.domain.MarcacaoBalneario;
import pt.florinhas.marcacoes.domain.MarcacaoSecretaria;
import pt.florinhas.marcacoes.dto.DocumentoDTO;
import pt.florinhas.marcacoes.repository.DocumentoRepository;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;

@ExtendWith(MockitoExtension.class)
class DocumentoServiceUploadTest {

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

    @Mock
    private SystemConfigService systemConfigService;

    @InjectMocks
    private DocumentoService documentoService;

    @BeforeEach
    void setup() {

        ReflectionTestUtils.setField(
                documentoService,
                "bucketName",
                "test-bucket"
        );

        ReflectionTestUtils.setField(
                documentoService,
                "maxFileSize",
                10485760L
        );
    }

    @Test
    void uploadDocumento_DeveFazerUploadComSucesso()
            throws Exception {

        Utente utente = criarUtente();

        Marcacao marcacao =
                criarMarcacaoSecretaria(utente);

        MockMultipartFile file =
                criarPdf();

        when(marcacaoRepository.findById(1L))
                .thenReturn(Optional.of(marcacao));

        when(documentoRepository.countByMarcacaoId(1L))
                .thenReturn(0L);

        when(documentoRepository.findMaxSequenciaByMarcacaoId(1L))
                .thenReturn(Optional.of(0));

        when(systemConfigService.getConfigValueAsInt(any(), anyInt()))
                .thenReturn(5);

        when(minioClient.bucketExists(any(BucketExistsArgs.class)))
                .thenReturn(true);

        when(documentoRepository.save(any()))
                .thenAnswer(invocation -> {

                    Documento d = invocation.getArgument(0);
                    d.setId(1L);

                    return d;
                });

        DocumentoDTO dto =
                documentoService.uploadDocumento(
                        1L,
                        file,
                        "Documento utente"
                );

        assertNotNull(dto);

        verify(minioClient)
                .putObject(any(PutObjectArgs.class));

        verify(documentoRepository)
                .save(any(Documento.class));
    }

    @Test
    void uploadDocumento_DeveCriarBucketQuandoNaoExiste()
            throws Exception {

        Marcacao marcacao =
                criarMarcacaoSecretaria(criarUtente());

        MockMultipartFile file =
                criarPdf();

        when(marcacaoRepository.findById(1L))
                .thenReturn(Optional.of(marcacao));

        when(documentoRepository.countByMarcacaoId(1L))
                .thenReturn(0L);

        when(documentoRepository.findMaxSequenciaByMarcacaoId(1L))
                .thenReturn(Optional.of(0));

        when(systemConfigService.getConfigValueAsInt(any(), anyInt()))
                .thenReturn(5);

        when(minioClient.bucketExists(any(BucketExistsArgs.class)))
                .thenReturn(false);

        when(documentoRepository.save(any()))
                .thenAnswer(invocation -> {

                    Documento d = invocation.getArgument(0);
                    d.setId(1L);

                    return d;
                });

        documentoService.uploadDocumento(
                1L,
                file,
                null
        );

        verify(minioClient)
                .makeBucket(any(MakeBucketArgs.class));
    }

    @Test
    void uploadDocumento_DeveGerarSequenciaIncremental()
            throws Exception {

        Marcacao marcacao =
                criarMarcacaoSecretaria(criarUtente());

        MockMultipartFile file =
                criarPdf();

        when(marcacaoRepository.findById(1L))
                .thenReturn(Optional.of(marcacao));

        when(documentoRepository.countByMarcacaoId(1L))
                .thenReturn(0L);

        when(documentoRepository.findMaxSequenciaByMarcacaoId(1L))
                .thenReturn(Optional.of(7));

        when(systemConfigService.getConfigValueAsInt(any(), anyInt()))
                .thenReturn(5);

        when(minioClient.bucketExists(any(BucketExistsArgs.class)))
                .thenReturn(true);

        when(documentoRepository.save(any()))
                .thenAnswer(invocation -> {

                    Documento d = invocation.getArgument(0);
                    d.setId(1L);

                    return d;
                });

        DocumentoDTO dto =
                documentoService.uploadDocumento(
                        1L,
                        file,
                        null
                );

        assertNotNull(dto);

        ArgumentCaptor<Documento> captor =
                ArgumentCaptor.forClass(Documento.class);

        verify(documentoRepository)
                .save(captor.capture());

        Documento salvo = captor.getValue();

        assertEquals(8, salvo.getSequencia());
    }

    @Test
    void uploadDocumento_DeveUsarFallbackCriadoPor()
            throws Exception {

        Utente criador = criarUtente();

        Marcacao marcacao = new Marcacao();
        marcacao.setId(1L);
        marcacao.setCriadoPor(criador);

        MockMultipartFile file =
                criarPdf();

        when(marcacaoRepository.findById(1L))
                .thenReturn(Optional.of(marcacao));

        when(documentoRepository.countByMarcacaoId(1L))
                .thenReturn(0L);

        when(documentoRepository.findMaxSequenciaByMarcacaoId(1L))
                .thenReturn(Optional.of(0));

        when(systemConfigService.getConfigValueAsInt(any(), anyInt()))
                .thenReturn(5);

        when(minioClient.bucketExists(any(BucketExistsArgs.class)))
                .thenReturn(true);

        when(documentoRepository.save(any()))
                .thenAnswer(invocation -> {

                    Documento d = invocation.getArgument(0);
                    d.setId(1L);

                    return d;
                });

        DocumentoDTO dto =
                documentoService.uploadDocumento(
                        1L,
                        file,
                        null
                );

        assertNotNull(dto);
    }

    @Test
    void uploadDocumento_DeveFuncionarParaBalneario()
            throws Exception {

        MarcacaoBalneario balneario =
                new MarcacaoBalneario();

        Marcacao marcacao =
                new Marcacao();

        marcacao.setId(1L);
        marcacao.setMarcacaoBalneario(balneario);

        MockMultipartFile file =
                criarPdf();

        when(marcacaoRepository.findById(1L))
                .thenReturn(Optional.of(marcacao));

        when(documentoRepository.countByMarcacaoId(1L))
                .thenReturn(0L);

        when(documentoRepository.findMaxSequenciaByMarcacaoId(1L))
                .thenReturn(Optional.of(0));

        when(systemConfigService.getConfigValueAsInt(any(), anyInt()))
                .thenReturn(5);

        when(minioClient.bucketExists(any(BucketExistsArgs.class)))
                .thenReturn(true);

        when(documentoRepository.save(any()))
                .thenAnswer(invocation -> {

                    Documento d = invocation.getArgument(0);
                    d.setId(1L);

                    return d;
                });

        DocumentoDTO dto =
                documentoService.uploadDocumento(
                        1L,
                        file,
                        null
                );

        assertNotNull(dto);
    }

    @Test
    void uploadDocumento_DeveTruncarFinalidadeGrande()
            throws Exception {

        String finalidade =
                "A".repeat(500);

        Marcacao marcacao =
                criarMarcacaoSecretaria(criarUtente());

        MockMultipartFile file =
                criarPdf();

        when(marcacaoRepository.findById(1L))
                .thenReturn(Optional.of(marcacao));

        when(documentoRepository.countByMarcacaoId(1L))
                .thenReturn(0L);

        when(documentoRepository.findMaxSequenciaByMarcacaoId(1L))
                .thenReturn(Optional.of(0));

        when(systemConfigService.getConfigValueAsInt(any(), anyInt()))
                .thenReturn(5);

        when(minioClient.bucketExists(any(BucketExistsArgs.class)))
                .thenReturn(true);

        when(documentoRepository.save(any()))
                .thenAnswer(invocation -> {

                    Documento d = invocation.getArgument(0);
                    d.setId(1L);

                    return d;
                });

        documentoService.uploadDocumento(
                1L,
                file,
                finalidade
        );

        ArgumentCaptor<Documento> captor =
                ArgumentCaptor.forClass(Documento.class);

        verify(documentoRepository)
                .save(captor.capture());

        Documento salvo = captor.getValue();

        assertTrue(
                salvo.getFinalidade().length() <= 255
        );
    }

    @Test
    void uploadDocumento_DeveNotificarSecretarias()
            throws Exception {

        Utente utente = criarUtente();

        Marcacao marcacao =
                criarMarcacaoSecretaria(utente);

        marcacao.setCriadoPor(utente);

        Funcionario secretaria =
                new Funcionario();

        secretaria.setId(99L);

        MockMultipartFile file =
                criarPdf();

        when(marcacaoRepository.findById(1L))
                .thenReturn(Optional.of(marcacao));

        when(documentoRepository.countByMarcacaoId(1L))
                .thenReturn(0L);

        when(documentoRepository.findMaxSequenciaByMarcacaoId(1L))
                .thenReturn(Optional.of(0));

        when(systemConfigService.getConfigValueAsInt(any(), anyInt()))
                .thenReturn(5);

        when(minioClient.bucketExists(any(BucketExistsArgs.class)))
                .thenReturn(true);

        when(documentoRepository.save(any()))
                .thenAnswer(invocation -> {

                    Documento d = invocation.getArgument(0);
                    d.setId(1L);

                    return d;
                });

        when(funcionarioRepository.findByTipo(FuncionarioTipo.SECRETARIA))
                .thenReturn(List.of(secretaria));

        documentoService.uploadDocumento(
                1L,
                file,
                null
        );

        verify(notificacaoService)
                .criarNotificacao(
                        eq(99L),
                        eq("Novo documento enviado"),
                        contains("marcação"),
                        eq("FICHEIRO")
                );
    }

    @Test
    void uploadDocumento_NaoDeveNotificarQuandoCriadorNaoEUtente()
            throws Exception {

        Funcionario funcionario =
                new Funcionario();

        funcionario.setId(50L);

        Marcacao marcacao =
                criarMarcacaoSecretaria(criarUtente());

        marcacao.setCriadoPor(funcionario);

        MockMultipartFile file =
                criarPdf();

        when(marcacaoRepository.findById(1L))
                .thenReturn(Optional.of(marcacao));

        when(documentoRepository.countByMarcacaoId(1L))
                .thenReturn(0L);

        when(documentoRepository.findMaxSequenciaByMarcacaoId(1L))
                .thenReturn(Optional.of(0));

        when(systemConfigService.getConfigValueAsInt(any(), anyInt()))
                .thenReturn(5);

        when(minioClient.bucketExists(any(BucketExistsArgs.class)))
                .thenReturn(true);

        when(documentoRepository.save(any()))
                .thenAnswer(invocation -> {

                    Documento d = invocation.getArgument(0);
                    d.setId(1L);

                    return d;
                });

        documentoService.uploadDocumento(
                1L,
                file,
                null
        );

        verify(notificacaoService, never())
                .criarNotificacao(
                        anyLong(),
                        anyString(),
                        anyString(),
                        anyString()
                );
    }

    @Test
    void uploadDocumento_DeveGuardarDataExpiracao()
            throws Exception {

        Marcacao marcacao =
                criarMarcacaoSecretaria(criarUtente());

        MockMultipartFile file =
                criarPdf();

        when(marcacaoRepository.findById(1L))
                .thenReturn(Optional.of(marcacao));

        when(documentoRepository.countByMarcacaoId(1L))
                .thenReturn(0L);

        when(documentoRepository.findMaxSequenciaByMarcacaoId(1L))
                .thenReturn(Optional.of(0));

        when(systemConfigService.getConfigValueAsInt(any(), anyInt()))
                .thenReturn(10);

        when(minioClient.bucketExists(any(BucketExistsArgs.class)))
                .thenReturn(true);

        when(documentoRepository.save(any()))
                .thenAnswer(invocation -> {

                    Documento d = invocation.getArgument(0);
                    d.setId(1L);

                    return d;
                });

        documentoService.uploadDocumento(
                1L,
                file,
                null
        );

        ArgumentCaptor<Documento> captor =
                ArgumentCaptor.forClass(Documento.class);

        verify(documentoRepository)
                .save(captor.capture());

        Documento salvo = captor.getValue();

        assertNotNull(salvo.getDataExpiracao());
    }

    private MockMultipartFile criarPdf() {

        return new MockMultipartFile(
                "file",
                "teste.pdf",
                "application/pdf",
                "conteudo".getBytes()
        );
    }

    private Utente criarUtente() {

        Utente utente = new Utente();

        utente.setId(1L);
        utente.setNome("Nuno");
        utente.setNif("123456789");

        return utente;
    }

    private Marcacao criarMarcacaoSecretaria(
            Utente utente
    ) {

        MarcacaoSecretaria secretaria =
                new MarcacaoSecretaria();

        secretaria.setAssunto("Assunto Teste");
        secretaria.setUtente(utente);

        Marcacao marcacao =
                new Marcacao();

        marcacao.setId(1L);
        marcacao.setData(LocalDateTime.now());
        marcacao.setMarcacaoSecretaria(secretaria);

        secretaria.setMarcacao(marcacao);

        return marcacao;
    }
}