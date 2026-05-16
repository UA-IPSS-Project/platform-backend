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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import pt.florinhas.common_data.domain.Funcionario;
import pt.florinhas.common_data.domain.Utente;
import pt.florinhas.common_data.repository.FuncionarioRepository;
import pt.florinhas.common_data.repository.UtenteRepository;
import pt.florinhas.common_data.repository.UtilizadorRepository;
import pt.florinhas.common_data.security.CryptoUtils;
import pt.florinhas.common_data.validation.NifValidator;
import pt.florinhas.marcacoes.domain.EventoEstado;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.dto.MarcacaoResponseDTO;
import pt.florinhas.marcacoes.repository.ItemArmazemRepository;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;
import pt.florinhas.marcacoes.service.email.EmailService;
import pt.florinhas.marcacoes.validation.MarcacaoValidator;

import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class MarcacaoServiceConsultaTest {

    @Mock private MarcacaoRepository marcacaoRepository;
    @Mock private UtenteRepository utenteRepository;
    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private UtilizadorRepository utilizadorRepository;
    @Mock private ItemArmazemRepository itemArmazemRepository;
    @Mock private NotificacaoService notificacaoService;
    @Mock private MarcacaoValidator marcacaoValidator;
    @Mock private CryptoUtils cryptoUtils;
    @Mock private NifValidator nifValidator;
    @Mock private EmailService emailService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ArmazemService armazemService;
    @Mock private AuthorizationService authorizationService;
    @Mock private AuditLogService auditLogService;

    @Mock
    @Lazy
    private CalendarioService calendarioService;

    @InjectMocks
    private MarcacaoService service;

    private Marcacao marcacao;

    @BeforeEach
    void setup() {
        marcacao = new Marcacao();
        marcacao.setId(1L);
        marcacao.setData(LocalDateTime.now());
        marcacao.setEstado(EventoEstado.AGENDADO);
    }

    @Test
    void contarMarcacoesDiarias_DeveRetornarContagem() {

        when(marcacaoRepository.countMarcacoesBetweenDates(any(), any()))
                .thenReturn(5L);

        long result = service.contarMarcacoesDiarias(LocalDateTime.now());

        assertEquals(5L, result);
    }

    @Test
    void consultarAgenda_DeveRetornarListaDTO() {

        when(marcacaoRepository.findMarcacoesBetweenDates(any(), any(), any()))
                .thenReturn(List.of(marcacao));

        List<MarcacaoResponseDTO> result =
                service.consultarAgenda(
                        LocalDateTime.now(),
                        LocalDateTime.now().plusDays(1),
                        "SECRETARIA"
                );

        assertEquals(1, result.size());
    }

    @Test
    void procurarAgenda_DeveRetornarListaDTO() {

        when(marcacaoRepository.findWithFilters(any(), any(), any(), any(), any()))
                .thenReturn(List.of(marcacao));

        List<MarcacaoResponseDTO> result =
                service.procurarAgenda(
                        LocalDateTime.now(),
                        LocalDateTime.now().plusDays(1),
                        1L,
                        1L,
                        EventoEstado.AGENDADO
                );

        assertEquals(1, result.size());
    }

    @Test
    void obterMarcacaoDTO_DeveRetornarDTO() {

        when(marcacaoRepository.findById(1L))
                .thenReturn(Optional.of(marcacao));

        MarcacaoResponseDTO dto = service.obterMarcacaoDTO(1L);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
    }

    @Test
    void obterMarcacaoDTO_DeveRetornarNullQuandoNaoExiste() {

        when(marcacaoRepository.findById(1L))
                .thenReturn(Optional.empty());

        MarcacaoResponseDTO dto = service.obterMarcacaoDTO(1L);

        assertNull(dto);
    }
    @Test
    void listarTodasMarcacoesPaginated_DeveRetornarPagina() {

        Page<Marcacao> page = new PageImpl<>(List.of(marcacao));

        when(marcacaoRepository.findAllWithRelations(any()))
                .thenReturn(page);

        Page<MarcacaoResponseDTO> result =
                service.listarTodasMarcacoesPaginated(PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
    }

    @Test
    void consultarMarcacoesUtente_DeveRetornarLista() {

        Utente utente = new Utente();
        utente.setId(1L);

        when(utenteRepository.findById(1L))
                .thenReturn(Optional.of(utente));

        when(marcacaoRepository.findByUtente(utente))
                .thenReturn(List.of(marcacao));

        List<MarcacaoResponseDTO> result =
                service.consultarMarcacoesUtente(1L);

        assertEquals(1, result.size());
    }

    @Test
    void consultarMarcacoesFuncionario_DeveRetornarLista() {

        Funcionario funcionario = new Funcionario();
        funcionario.setId(1L);

        when(funcionarioRepository.findById(1L))
                .thenReturn(Optional.of(funcionario));

        when(marcacaoRepository.findByCriadoPor(funcionario))
                .thenReturn(List.of(marcacao));

        List<MarcacaoResponseDTO> result =
                service.consultarMarcacoesFuncionario(1L);

        assertEquals(1, result.size());
    }
}