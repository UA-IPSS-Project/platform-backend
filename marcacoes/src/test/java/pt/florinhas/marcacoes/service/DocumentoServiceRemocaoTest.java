package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pt.florinhas.common_data.domain.Utente;
import pt.florinhas.common_data.exception.ResourceNotFoundException;
import pt.florinhas.marcacoes.domain.Documento;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.domain.MarcacaoSecretaria;
import pt.florinhas.marcacoes.repository.DocumentoRepository;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;

@ExtendWith(MockitoExtension.class)
class DocumentoServiceRemocaoTest {

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

    @Test
    void removerDocumento_DeveRemoverDocumento() {

        Documento documento =
                criarDocumento();

        when(documentoRepository.findById(1L))
                .thenReturn(Optional.of(documento));

        documentoService.removerDocumento(1L);

        verify(documentoRepository)
                .delete(documento);
    }

    @Test
    void removerDocumento_DeveLancarErro() {

        when(documentoRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> documentoService.removerDocumento(1L)
        );
    }

    @Test
    void notificarDocumentoInvalido_DeveCriarNotificacao() {

        Documento documento =
                criarDocumento();

        Marcacao marcacao =
                documento.getMarcacao();

        when(marcacaoRepository.findById(1L))
                .thenReturn(Optional.of(marcacao));

        when(documentoRepository.findById(2L))
                .thenReturn(Optional.of(documento));

        documentoService.notificarDocumentoInvalido(
                1L,
                2L,
                "Documento ilegível"
        );

        verify(notificacaoService)
                .criarNotificacao(
                        eq(1L),
                        eq("Documento inválido"),
                        contains("Documento ilegível"),
                        eq("DOCUMENTO_INVALIDO")
                );
    }

    @Test
    void notificarDocumentoInvalido_DeveLancarErroSemUtente() {

        Documento documento =
                new Documento();

        documento.setId(1L);

        Marcacao marcacao =
                new Marcacao();

        marcacao.setId(1L);

        documento.setMarcacao(marcacao);

        when(marcacaoRepository.findById(1L))
                .thenReturn(Optional.of(marcacao));

        when(documentoRepository.findById(2L))
                .thenReturn(Optional.of(documento));

        assertThrows(
                IllegalArgumentException.class,
                () -> documentoService.notificarDocumentoInvalido(
                        1L,
                        2L,
                        "erro"
                )
        );
    }

    private Documento criarDocumento() {

        Utente utente =
                new Utente();

        utente.setId(1L);
        utente.setNome("Nuno");

        MarcacaoSecretaria secretaria =
                new MarcacaoSecretaria();

        secretaria.setUtente(utente);

        Marcacao marcacao =
                new Marcacao();

        marcacao.setId(1L);
        marcacao.setData(LocalDateTime.now());
        marcacao.setMarcacaoSecretaria(secretaria);

        secretaria.setMarcacao(marcacao);

        Documento documento =
                new Documento();

        documento.setId(2L);
        documento.setNomeOriginal("teste.pdf");
        documento.setMarcacao(marcacao);

        return documento;
    }
}