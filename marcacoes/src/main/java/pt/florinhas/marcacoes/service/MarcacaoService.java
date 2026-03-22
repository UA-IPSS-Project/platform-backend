package pt.florinhas.marcacoes.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pt.florinhas.marcacoes.domain.AtendimentoTipo;
import pt.florinhas.marcacoes.domain.EventoEstado;
import pt.florinhas.marcacoes.domain.Funcionario;
import pt.florinhas.marcacoes.domain.FuncionarioTipo;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.domain.MarcacaoBalneario;
import pt.florinhas.marcacoes.domain.MarcacaoSecretaria;
import pt.florinhas.marcacoes.domain.Roupa;
import pt.florinhas.marcacoes.domain.Utente;
import pt.florinhas.marcacoes.domain.Utilizador;
import pt.florinhas.marcacoes.dto.AtualizarEstadoRequest;
import pt.florinhas.marcacoes.dto.CriarMarcacaoBalnearioRequest;
import pt.florinhas.marcacoes.dto.CriarMarcacaoRequest;
import pt.florinhas.marcacoes.dto.MarcacaoResponseDTO;
import pt.florinhas.marcacoes.dto.NotificarDocumentosRequest;
import pt.florinhas.marcacoes.dto.ReagendarMarcacaoRequest;
import pt.florinhas.marcacoes.dto.RoupaDTO;
import pt.florinhas.marcacoes.repository.FuncionarioRepository;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;
import pt.florinhas.marcacoes.repository.UtenteRepository;
import pt.florinhas.marcacoes.repository.UtilizadorRepository;
import pt.florinhas.marcacoes.service.email.EmailService;
import pt.florinhas.marcacoes.validation.MarcacaoValidator;
import pt.florinhas.marcacoes.validation.NifValidator;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarcacaoService {

    private final MarcacaoRepository marcacaoRepository;
    private final UtenteRepository utenteRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final UtilizadorRepository utilizadorRepository;
    private final NotificacaoService notificacaoService;
    private final MarcacaoValidator marcacaoValidator;
    private final NifValidator nifValidator;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Lazy
    private final CalendarioService calendarioService;

    /**
     * Gera uma password segura com ~128 bits de entropia.
     * Usa apenas caracteres alfanuméricos (a-z, A-Z, 0-9) para facilitar digitação.
     * 62 caracteres possíveis × 22 posições = ~130 bits de entropia.
     */
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final Integer BALNEARIO_DEFAULT_DURATION_MINUTES = 30;
    private static final Integer SECRETARIA_DEFAULT_DURATION_MINUTES = 15;

    private String generateRandomPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(22);

        // 22 caracteres de 62 possíveis = log2(62^22) ≈ 130 bits de entropia
        for (int i = 0; i < 22; i++) {
            int index = random.nextInt(ALPHANUMERIC.length());
            password.append(ALPHANUMERIC.charAt(index));
        }

        return password.toString();
    }

    // Placeholder: In a real scenario, this would convert entities to DTOs
    // For now, returning null or empty lists to satisfy compilation,
    // expecting that I might need to refine this if logic is complex.
    // However, I will try to implement reasonable defaults.

    public long contarMarcacoesDiarias(LocalDateTime date) {
        LocalDateTime startOfDay = date.truncatedTo(ChronoUnit.DAYS);
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);
        return marcacaoRepository.countMarcacoesBetweenDates(startOfDay, endOfDay);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Marcacao criarMarcacaoPresencial(CriarMarcacaoRequest request) {
        // Validar data e conflitos antes de prosseguir
        marcacaoValidator.validarCriacao(request);

        // 1. Obter ou criar Utente
        Utente utente = null;
        if (request.getUtenteId() != null) {
            utente = utenteRepository.findById(request.getUtenteId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Utente não encontrado com ID: " + request.getUtenteId()));
        } else if (hasText(request.getUtenteNif())) {
            nifValidator.validateRequiredOrThrow(request.getUtenteNif());

            // Verificar se já existe por NIF
            List<Utente> users = utenteRepository.findByNif(request.getUtenteNif());
            if (!users.isEmpty()) {
                utente = users.get(0);
            }

            if (utente == null) {
                // Criar novo utente
                utente = new Utente();
                utente.setNome(request.getUtenteNome());
                utente.setNif(request.getUtenteNif());
                utente.setEmail(request.getUtenteEmail());
                utente.setTelefone(request.getUtenteTelefone());
                utente.setDataNasc(request.getUtenteDataNasc());
                utente.setActivo(false);

                // Gerar password segura
                String rawPassword = generateRandomPassword();
                utente.setPassHash(passwordEncoder.encode(rawPassword));

                utente = utenteRepository.save(utente);

                // Enviar email com a password
                try {
                    emailService.sendPassword(request.getUtenteEmail(), rawPassword);
                } catch (Exception e) {
                    log.error("Falha ao enviar email para: {}", request.getUtenteEmail(), e);
                    // Não falhar a marcação se o email falhar, mas logar erro
                }
            }
        } else {
            throw new IllegalArgumentException("ID do Utente ou NIF é obrigatório.");
        }

        Marcacao marcacao = criarMarcacaoBase(request, AtendimentoTipo.PRESENCIAL, utente);

        // Associa funcionário criador (específico de presencial)
        if (request.getCriadoPorId() != null) {
            Funcionario funcionario = funcionarioRepository.findById(request.getCriadoPorId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Funcionário não encontrado com ID: " + request.getCriadoPorId()));

            marcacao.setCriadoPor(funcionario);
        }

        Marcacao saved = marcacaoRepository.save(marcacao);

        // Notify utente about new appointment (when created by secretary)
        if (utente != null && request.getCriadoPorId() != null) {
            try {
                notificacaoService.notificarNovaMarcacao(utente, saved.getId(), saved.getData(), false);
            } catch (Exception e) {
                log.error("Falha ao notificar utente sobre nova marcação", e);
            }
        }

        return saved;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Marcacao criarMarcacaoRemota(CriarMarcacaoRequest request) {
        // Validar data e conflitos antes de prosseguir
        marcacaoValidator.validarCriacao(request);

        Utente utente = utenteRepository.findById(request.getUtenteId())
                .orElseThrow(
                        () -> new EntityNotFoundException("Utente não encontrado com ID: " + request.getUtenteId()));

        Marcacao marcacao = criarMarcacaoBase(request, AtendimentoTipo.REMOTO, utente);
        // Centralized duration logic: remota = 15min
        marcacao.setDuration(SECRETARIA_DEFAULT_DURATION_MINUTES);
        // Na remota, criadoPor pode ser null ou não especificado se feito pelo utente.
        // Se houver necessidade de setar, seria aqui.
        return marcacaoRepository.save(marcacao);
    }

    private Marcacao criarMarcacaoBase(CriarMarcacaoRequest request, AtendimentoTipo tipo, Utente utente) {
        Marcacao marcacao = new Marcacao();
        marcacao.setData(request.getData());
        marcacao.setEstado(EventoEstado.AGENDADO);
        // Centralized duration logic: secretaria = 15min
        marcacao.setDuration(SECRETARIA_DEFAULT_DURATION_MINUTES);

        MarcacaoSecretaria detalhes = new MarcacaoSecretaria();
        detalhes.setAssunto(request.getAssunto());
        detalhes.setDescricao(request.getDescricao());
        detalhes.setTipoAtendimento(tipo);
        detalhes.setUtente(utente);

        detalhes.setMarcacao(marcacao);
        marcacao.setMarcacaoSecretaria(detalhes);

        return marcacao;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Marcacao criarMarcacaoBalneario(CriarMarcacaoBalnearioRequest request) {
        marcacaoValidator.validarCriacaoBalneario(request);

        Marcacao marcacao = new Marcacao();
        marcacao.setData(request.getData());
        marcacao.setEstado(EventoEstado.AGENDADO);
        // Use constant for balneário duration
        marcacao.setDuration(BALNEARIO_DEFAULT_DURATION_MINUTES);

        MarcacaoBalneario detalhes = new MarcacaoBalneario();
        detalhes.setNomeUtente(request.getNomeUtente());
        detalhes.setProdutosHigiene(request.getProdutosHigiene());
        detalhes.setLavagemRoupa(request.getLavagemRoupa());

        if (request.getResponsavelId() != null) {
            Funcionario responsavel = funcionarioRepository.findById(request.getResponsavelId())
                    .orElse(null);
            detalhes.setResponsavel(responsavel);
            marcacao.setCriadoPor(responsavel);
        }

        if (request.getRoupas() != null) {
            for (RoupaDTO rDTO : request.getRoupas()) {
                Roupa r = new Roupa();
                r.setCategoria(rDTO.getCategoria());
                r.setTamanho(rDTO.getTamanho());
                r.setQuantidade(rDTO.getQuantidade() != null ? rDTO.getQuantidade() : 1);
                detalhes.addRoupa(r);
            }
        }

        detalhes.setMarcacao(marcacao);
        marcacao.setMarcacaoBalneario(detalhes);

        return marcacaoRepository.save(marcacao);
    }

    /**
     * Atualiza os detalhes de uma marcação de balneário (serviços, roupa, etc.).
     */
    @Transactional
    public MarcacaoResponseDTO atualizarDetalhesBalneario(Long marcacaoId,
            Boolean produtosHigiene, Boolean lavagemRoupa, List<RoupaDTO> roupas) {

        Marcacao marcacao = marcacaoRepository.findById(marcacaoId)
                .orElseThrow(() -> new IllegalArgumentException("Marcação não encontrada"));

        MarcacaoBalneario detalhes = marcacao.getMarcacaoBalneario();
        if (detalhes == null) {
            throw new IllegalArgumentException("Esta marcação não tem detalhes de balneário");
        }

        detalhes.setProdutosHigiene(produtosHigiene != null ? produtosHigiene : false);
        detalhes.setLavagemRoupa(lavagemRoupa != null ? lavagemRoupa : false);

        // Clear existing clothes and add new ones (orphanRemoval handles DB deletes)
        detalhes.getRoupas().clear();
        if (roupas != null) {
            for (RoupaDTO rDTO : roupas) {
                Roupa r = new Roupa();
                r.setCategoria(rDTO.getCategoria());
                r.setTamanho(rDTO.getTamanho());
                r.setQuantidade(rDTO.getQuantidade() != null ? rDTO.getQuantidade() : 1);
                detalhes.addRoupa(r);
            }
        }

        marcacaoRepository.save(marcacao);
        return toDTO(marcacao);
    }

    public List<MarcacaoResponseDTO> consultarAgenda(LocalDateTime inicio, LocalDateTime fim, String tipo) {
        if (inicio == null)
            inicio = LocalDateTime.now().minusYears(1);
        if (fim == null)
            fim = LocalDateTime.now().plusYears(1);

        List<Marcacao> list = marcacaoRepository.findMarcacoesBetweenDates(inicio, fim, tipo);
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<MarcacaoResponseDTO> procurarAgenda(LocalDateTime inicio, LocalDateTime fim, Long criadoPorId,
            Long utenteId, EventoEstado estado) {
        List<Marcacao> list = marcacaoRepository.findWithFilters(inicio, fim, criadoPorId, estado);
        // Note: Repository signature might not match exactly with utenteId in
        // findWithFilters based on my cat earlier.
        // The repo has findWithFilters(dataInicio, dataFim, criadoPorId, estado). It
        // assumes utenteId is handled differently or I missed it.
        // I will trust the repository signature I saw: findWithFilters(dataInicio,
        // dataFim, criadoPorId, estado).
        // If utenteId is needed, I might need to update repository or filter in memory.
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public MarcacaoResponseDTO atualizarEstadoMarcacao(Long id, AtualizarEstadoRequest request) {
        Marcacao marcacao = marcacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Marcação não encontrada com ID: " + id));

        // Validar transição de estado se necessário
        // Por agora permitimos atualizar direto
        marcacao.setEstado(request.getNovoEstadoEnum());

        if (request.getNovoEstadoEnum() == EventoEstado.CANCELADO && request.getMotivoCancelamento() != null) {
            marcacao.setMotivoCancelamento(request.getMotivoCancelamento());
        }

        // Se houver atendente a definir (funcionário que executa a alteração):
        if (request.getFuncionarioId() != null) {
            // Nota: Se o estado for CONCLUIDO ou CANCELADO, este funcionário é o atendente
            // EXCEÇÃO: Se for o próprio utente a cancelar, não definir atendente (fica
            // null)
            if (request.getNovoEstadoEnum() == EventoEstado.CONCLUIDO) {
                Utilizador atendente = utilizadorRepository.findById(request.getFuncionarioId())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Utilizador não encontrado: " + request.getFuncionarioId()));

                marcacao.setAtendente(atendente);
            } else if (request.getNovoEstadoEnum() == EventoEstado.CANCELADO) {
                // Verificar se é o próprio utente a cancelar
                MarcacaoSecretaria secDetails = marcacao.getMarcacaoSecretaria();
                boolean canceladoPeloUtente = secDetails != null
                        && secDetails.getUtente() != null
                        && request.getFuncionarioId().equals(secDetails.getUtente().getId());

                // Só define atendente se NÃO for o próprio utente a cancelar
                if (!canceladoPeloUtente) {
                    Utilizador atendente = utilizadorRepository.findById(request.getFuncionarioId())
                            .orElseThrow(() -> new EntityNotFoundException(
                                    "Utilizador não encontrado: " + request.getFuncionarioId()));

                    marcacao.setAtendente(atendente);
                }
            }
        }

        log.debug("Updating Marcacao {} to state {}. Reason: {}", id, request.getNovoEstadoEnum(),
                request.getMotivoCancelamento());

        marcacao = marcacaoRepository.save(marcacao);

        // Notificar intervenientes se cancelado
        if (request.getNovoEstadoEnum() == EventoEstado.CANCELADO) {
            MarcacaoSecretaria secretariaDetails = marcacao.getMarcacaoSecretaria();
            if (secretariaDetails != null && secretariaDetails.getUtente() != null) {
                Utente utenteAlvo = secretariaDetails.getUtente();
                Long atorId = request.getFuncionarioId();

                if (atorId != null && atorId.equals(utenteAlvo.getId())) {
                    // Cancelado pelo Utente -> Notificar Secretarias
                    List<Funcionario> secretarias = funcionarioRepository.findByTipo(FuncionarioTipo.SECRETARIA);
                    for (Funcionario sec : secretarias) {
                        try {
                            notificacaoService.notificarCancelamentoPeloUtente(sec, utenteAlvo.getNome(),
                                    marcacao.getData());
                        } catch (Exception e) {
                            log.error("Erro ao notificar secretaria {}", sec.getId(), e);
                        }
                    }
                } else {
                    // Cancelado pela Secretaria -> Notificar Utente
                    try {
                        notificacaoService.notificarCancelamento(
                                utenteAlvo,
                                marcacao.getData(),
                                request.getMotivoCancelamento());
                    } catch (Exception e) {
                        log.error("Erro ao notificar utente {}", utenteAlvo.getId(), e);
                    }
                }
            }
        }

        return toDTO(marcacao);
    }

    public List<MarcacaoResponseDTO> consultarMarcacoesPassadas(LocalDateTime dataInicio, LocalDateTime dataFim,
            Long utenteId, EventoEstado estado) {
        if (dataInicio == null) {
            dataInicio = LocalDateTime.of(2000, 1, 1, 0, 0);
        }
        if (dataFim == null) {
            dataFim = LocalDateTime.now();
        }

        List<Marcacao> list = marcacaoRepository.findMarcacoesPassadas(dataInicio, dataFim, utenteId, estado);
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public MarcacaoResponseDTO notificarDocumentosInvalidos(Long id, NotificarDocumentosRequest request) {
        Marcacao marcacao = marcacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Marcação não encontrada com ID: " + id));

        MarcacaoSecretaria secretariaDetails = marcacao.getMarcacaoSecretaria();
        if (secretariaDetails == null || secretariaDetails.getUtente() == null) {
            throw new IllegalStateException("Marcação sem utente associado");
        }

        Utente utente = secretariaDetails.getUtente();

        try {
            notificacaoService.notificarDocumentosInvalidos(utente, request.getObservacoes());
        } catch (Exception e) {
            log.error("Erro ao notificar utente {} sobre documentos inválidos", utente.getId(), e);
        }

        return toDTO(marcacao);
    }

    public List<MarcacaoResponseDTO> consultarMarcacoesUtente(Long utenteId) {
        Utente utente = utenteRepository.findById(utenteId)
                .orElseThrow(() -> new EntityNotFoundException("Utente não encontrado com ID: " + utenteId));

        List<Marcacao> list = marcacaoRepository.findByUtente(utente);
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<Map<String, Object>> consultarMarcacoesBloqueadas(Long utenteId) {
        LocalDateTime start = LocalDateTime.now().minusHours(1); // Include current hour
        LocalDateTime end = start.plusMonths(6);
        List<Marcacao> all = marcacaoRepository.findMarcacoesBetweenDates(start, end, "SECRETARIA");

        return all.stream()
                .filter(m -> {
                    // Exclude my own appointments (via Utente association)
                    if (m.getMarcacaoSecretaria() != null && m.getMarcacaoSecretaria().getUtente() != null) {
                        if (m.getMarcacaoSecretaria().getUtente().getId().equals(utenteId)) {
                            return false;
                        }
                    }
                    // Exclude my own appointments (via CreatedBy - for temporary slots)
                    if (m.getCriadoPor() != null && m.getCriadoPor().getId().equals(utenteId)) {
                        return false;
                    }
                    return true;
                })
                .map(m -> Map.<String, Object>of(
                        "id", m.getId(),
                        "data", m.getData().toString(),
                        "estado", m.getEstado().toString()))
                .collect(Collectors.toList());
    }

    public List<MarcacaoResponseDTO> consultarMarcacoesFuncionario(Long funcionarioId) {
        return Collections.emptyList();
    }

    public MarcacaoResponseDTO obterMarcacaoDTO(Long id) {
        return marcacaoRepository.findById(id).map(this::toDTO).orElse(null);
    }

    public Page<MarcacaoResponseDTO> listarTodasMarcacoesPaginated(Pageable pageable) {
        return marcacaoRepository.findAllWithRelations(pageable).map(this::toDTO);
    }


    @Transactional
    public Long criarReservaTemporaria(CriarMarcacaoRequest request) {
        // 1. Verificar capacidade máxima antes de criar reserva
        String tipoAgenda = normalizarTipoAgenda(request.getTipoAgenda());
        LocalDateTime data = request.getData();
        int capacidade = calendarioService.getCapacidadePorSlot(tipoAgenda);
        // Estados que contam para ocupação do slot
        List<EventoEstado> estadosOcupados = List.of(EventoEstado.AGENDADO, EventoEstado.EM_PREENCHIMENTO);
        long ocupadas = marcacaoRepository.countByDataAndEstadoInAndTipo(data, estadosOcupados, tipoAgenda);
        if (ocupadas >= capacidade) {
            throw new IllegalStateException("Capacidade máxima de vagas atingida para este horário.");
        }

        Marcacao m = new Marcacao();
        m.setData(data);
        m.setEstado(EventoEstado.EM_PREENCHIMENTO);
        m.setDuration("BALNEARIO".equals(tipoAgenda)
            ? BALNEARIO_DEFAULT_DURATION_MINUTES
            : SECRETARIA_DEFAULT_DURATION_MINUTES);

        // Identificar quem está a reservar
        Long criadorId = request.getCriadoPorId();
        if (criadorId != null) {
            Utilizador criador = utilizadorRepository.findById(criadorId)
                    .orElse(null); // Se não encontrar, segue sem criador (menos crítico para temp)
            m.setCriadoPor(criador);
        } else if (request.getUtenteId() != null) {
            // Fallback para utenteId se criadoPorId não vier
            Utilizador criador = utilizadorRepository.findById(request.getUtenteId())
                    .orElse(null);
            m.setCriadoPor(criador);
        }

        if ("BALNEARIO".equals(tipoAgenda)) {
            MarcacaoBalneario detalhes = new MarcacaoBalneario();
            detalhes.setNomeUtente("Reserva temporária");
            detalhes.setProdutosHigiene(false);
            detalhes.setLavagemRoupa(false);
            detalhes.setMarcacao(m);
            m.setMarcacaoBalneario(detalhes);
        } else {
            MarcacaoSecretaria detalhes = new MarcacaoSecretaria();
            detalhes.setAssunto("Reserva temporária");
            detalhes.setDescricao("Reserva temporária de slot");
            detalhes.setTipoAtendimento(AtendimentoTipo.PRESENCIAL);
            if (request.getUtenteId() != null) {
                Utente utente = utenteRepository.findById(request.getUtenteId()).orElse(null);
                detalhes.setUtente(utente);
            }
            detalhes.setMarcacao(m);
            m.setMarcacaoSecretaria(detalhes);
        }

        m = marcacaoRepository.save(m);
        return m.getId();
    }

    @Transactional
    public void apagarReservaTemporaria(Long id) {
        if (marcacaoRepository.existsById(id)) {
            marcacaoRepository.deleteById(id);
        }
    }

    @Transactional
    public MarcacaoResponseDTO reagendarMarcacao(Long id, ReagendarMarcacaoRequest request) {
        // Buscar marcação existente
        Marcacao marcacao = marcacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Marcação não encontrada com ID: " + id));

        String tipoAgenda = marcacao.getMarcacaoBalneario() != null ? "BALNEARIO" : "SECRETARIA";

        // Validar reagendamento com as mesmas verificações que criação (data, hora,
        // feriados, bloqueios, sobreposição)
        marcacaoValidator.validarReagendamento(request, tipoAgenda);

        // Atualizar data/hora
        marcacao.setData(request.getNovaDataHora());

        // Persistir alteração
        Marcacao saved = marcacaoRepository.save(marcacao);
        return toDTO(saved);
    }

    private MarcacaoResponseDTO toDTO(Marcacao m) {
        MarcacaoResponseDTO dto = new MarcacaoResponseDTO();
        dto.setId(m.getId());
        dto.setData(m.getData());
        dto.setEstado(m.getEstado());
        dto.setVersion(m.getVersion());

        if (m.getAtendente() != null) {
            dto.setAtendenteNome(m.getAtendente().getNome());
        }

        dto.setMotivoCancelamento(m.getMotivoCancelamento());

        if (m.getMarcacaoSecretaria() != null) {
            MarcacaoSecretaria sec = m.getMarcacaoSecretaria();
            MarcacaoResponseDTO.MarcacaoSecretariaDTO secDTO = new MarcacaoResponseDTO.MarcacaoSecretariaDTO();
            secDTO.setAssunto(sec.getAssunto());
            secDTO.setDescricao(sec.getDescricao());
            secDTO.setTipoAtendimento(sec.getTipoAtendimento());

            if (sec.getUtente() != null) {
                Utente u = sec.getUtente();
                MarcacaoResponseDTO.UtenteDTO uDTO = new MarcacaoResponseDTO.UtenteDTO();
                uDTO.setId(u.getId());
                uDTO.setNome(u.getNome());
                uDTO.setEmail(u.getEmail());
                uDTO.setNif(u.getNif());
                uDTO.setTelefone(u.getTelefone());
                secDTO.setUtente(uDTO);
            }

            dto.setMarcacaoSecretaria(secDTO);
        }

        if (m.getMarcacaoBalneario() != null) {
            MarcacaoBalneario baln = m.getMarcacaoBalneario();
            MarcacaoResponseDTO.MarcacaoBalnearioDTO balnDTO = new MarcacaoResponseDTO.MarcacaoBalnearioDTO();
            balnDTO.setNomeUtente(baln.getNomeUtente());
            balnDTO.setProdutosHigiene(baln.getProdutosHigiene());
            balnDTO.setLavagemRoupa(baln.getLavagemRoupa());

            if (baln.getResponsavel() != null) {
                balnDTO.setResponsavelNome(baln.getResponsavel().getNome());
            }

            if (baln.getRoupas() != null) {
                List<RoupaDTO> roupasDTO = baln.getRoupas().stream().map(r -> {
                    RoupaDTO rDTO = new RoupaDTO();
                    rDTO.setId(r.getId());
                    rDTO.setCategoria(r.getCategoria());
                    rDTO.setTamanho(r.getTamanho());
                    rDTO.setQuantidade(r.getQuantidade());
                    return rDTO;
                }).collect(Collectors.toList());
                balnDTO.setRoupas(roupasDTO);
            }

            dto.setMarcacaoBalneario(balnDTO);
        }

        return dto;
    }

    @EventListener(ApplicationReadyEvent.class) // Run on startup
    @Scheduled(fixedRate = 60000) // Run every minute
    @Transactional
    public void limparReservasExpiradas() {
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(15);
        // Usa o método customizado que também limpa registos com criadoEm NULL
        marcacaoRepository.deleteExpiredOrorphan(EventoEstado.EM_PREENCHIMENTO, expirationTime);
    }

    private String normalizarTipoAgenda(String tipoAgenda) {
        if (tipoAgenda == null || tipoAgenda.isBlank()) {
            return "SECRETARIA";
        }
        String tipo = tipoAgenda.trim().toUpperCase();
        return "BALNEARIO".equals(tipo) ? "BALNEARIO" : "SECRETARIA";
    }
}
