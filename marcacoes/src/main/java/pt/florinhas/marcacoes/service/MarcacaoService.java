package pt.florinhas.marcacoes.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import pt.florinhas.marcacoes.domain.AtendimentoTipo;
import pt.florinhas.marcacoes.domain.EventoEstado;
import pt.florinhas.marcacoes.domain.Funcionario;
import pt.florinhas.marcacoes.domain.FuncionarioTipo;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.domain.MarcacaoSecretaria;
import pt.florinhas.marcacoes.domain.Utente;
import pt.florinhas.marcacoes.domain.Utilizador;
import pt.florinhas.marcacoes.dto.AtualizarEstadoRequest;
import pt.florinhas.marcacoes.dto.CriarMarcacaoRequest;
import pt.florinhas.marcacoes.dto.MarcacaoResponseDTO;
import pt.florinhas.marcacoes.dto.NotificarDocumentosRequest;
import pt.florinhas.marcacoes.repository.FuncionarioRepository;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;
import pt.florinhas.marcacoes.repository.MarcacaoSecretariaRepository;
import pt.florinhas.marcacoes.repository.UtenteRepository;
import pt.florinhas.marcacoes.repository.UtilizadorRepository;
import pt.florinhas.marcacoes.service.email.EmailService;
import pt.florinhas.marcacoes.service.NotificacaoService;

/**
 * Serviço central de gestão de marcações.
 *
 * Este serviço concentra a maior parte da lógica de negócio do sistema:
 * - Criação de marcações (presenciais e remotas)
 * - Gestão de estados e respetivas transições
 * - Controlo de concorrência (versões)
 * - Reservas temporárias de slots
 * - Consultas de agenda, histórico e bloqueios
 * - Conversão de entidades para DTOs
 *
 * A anotação @Transactional garante consistência em operações complexas
 * que envolvem múltiplas entidades (Marcacao + MarcacaoSecretaria).
 */
@Service
@Transactional
@Slf4j
public class MarcacaoService {

    private final MarcacaoRepository marcacaoRepository;
    private final MarcacaoSecretariaRepository marcacaoSecretariaRepository;
    private final UtenteRepository utenteRepository; // Keeping this as it's used in obterOuCriarUtente indirectly
    private final FuncionarioRepository funcionarioRepository; // Keeping this as it's used
    private final UtilizadorRepository utilizadorRepository; // Keeping this as it's used
    private final UtilizadorService utilizadorService;
    private final EmailService emailService; // Assuming this is now active
    private final NotificacaoService notificacaoService;
    private final CalendarioService calendarioService;

    // Construtor com injeção de dependências
    public MarcacaoService(
            MarcacaoRepository marcacaoRepository,
            MarcacaoSecretariaRepository marcacaoSecretariaRepository,
            UtenteRepository utenteRepository,
            FuncionarioRepository funcionarioRepository,
            UtilizadorRepository utilizadorRepository,
            UtilizadorService utilizadorService,
            EmailService emailService,
            NotificacaoService notificacaoService,
            CalendarioService calendarioService) {
        this.marcacaoRepository = marcacaoRepository;
        this.marcacaoSecretariaRepository = marcacaoSecretariaRepository;
        this.utenteRepository = utenteRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.utilizadorRepository = utilizadorRepository;
        this.utilizadorService = utilizadorService;
        this.emailService = emailService;
        this.notificacaoService = notificacaoService;
        this.calendarioService = calendarioService;
    }

    public long contarMarcacoesDiarias(LocalDateTime data) {
        LocalDateTime inicioDia = data.toLocalDate().atStartOfDay();
        LocalDateTime fimDia = inicioDia.plusDays(1).minusSeconds(1);

        // Contar apenas marcações com estados ativos: AGENDADO, EM_PROGRESSO, AVISO
        return marcacaoRepository.findMarcacoesBetweenDates(inicioDia, fimDia).stream()
                .filter(m -> m.getEstado() == EventoEstado.AGENDADO ||
                        m.getEstado() == EventoEstado.EM_PROGRESSO ||
                        m.getEstado() == EventoEstado.AVISO)
                .count();
    }

    public Marcacao criarMarcacaoPresencial(CriarMarcacaoRequest request) {
        // Validar campos obrigatórios
        if (request.getUtenteNif() == null || request.getUtenteNif().trim().isEmpty()) {
            throw new RuntimeException("NIF do utente é obrigatório");
        }

        validarDisponibilidade(request.getData());

        // Obter funcionário que está a criar
        Funcionario criadoPor = funcionarioRepository.findById(request.getFuncionarioId())
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));

        validarFuncionarioSecretaria(criadoPor); // Apenas secretaria pode criar

        // Procurar ou criar utente por NIF usando o UtilizadorService
        Utente utente = utilizadorService.obterOuCriarUtente(
                request.getUtenteNif(),
                request.getUtenteNome(),
                request.getUtenteEmail(),
                request.getUtenteTelefone());

        // Verificar se os dados do request coincidem com os dados do utente existente
        if (request.getUtenteNome() != null && !request.getUtenteNome().trim().isEmpty()
                && !utente.getNome().equalsIgnoreCase(request.getUtenteNome())) {
            throw new RuntimeException(
                    "O nome fornecido não coincide com o utente registado com o NIF " + request.getUtenteNif());
        }

        if (request.getUtenteEmail() != null && !request.getUtenteEmail().trim().isEmpty()
                && !utente.getEmail().equalsIgnoreCase(request.getUtenteEmail())) {
            throw new RuntimeException(
                    "O email fornecido não coincide com o utente registado com o NIF " + request.getUtenteNif());
        }

        if (request.getUtenteTelefone() != null && !request.getUtenteTelefone().trim().isEmpty()
                && !utente.getTelefone().equals(request.getUtenteTelefone())) {
            throw new RuntimeException(
                    "O telefone fornecido não coincide com o utente registado com o NIF " + request.getUtenteNif());
        }

        // Criar Marcacao
        Marcacao marcacao = new Marcacao();
        marcacao.setData(request.getData());
        marcacao.setEstado(EventoEstado.AGENDADO);
        marcacao.setCriadoPor(criadoPor);

        Marcacao savedMarcacao = marcacaoRepository.save(marcacao);

        // Criar MarcacaoSecretaria com OneToOne
        MarcacaoSecretaria marcacaoSecretaria = new MarcacaoSecretaria();
        marcacaoSecretaria.setMarcacao(savedMarcacao);
        marcacaoSecretaria.setAssunto(request.getAssunto());
        marcacaoSecretaria.setTipoAtendimento(AtendimentoTipo.PRESENCIAL);
        marcacaoSecretaria.setUtente(utente);

        // Se tiver descrição, adicionar
        if (request.getDescricao() != null && !request.getDescricao().trim().isEmpty()) {
            marcacaoSecretaria.setDescricao(request.getDescricao());
        }

        MarcacaoSecretaria savedMarcacaoSecretaria = marcacaoSecretariaRepository.save(marcacaoSecretaria);

        // Estabelecer relação bidirecional
        savedMarcacao.setMarcacaoSecretaria(savedMarcacaoSecretaria);

        notificarUtenteMarcacao(savedMarcacao, "NOVA_MARCACAO");

        // Notificar via sistema
        try {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                    .ofPattern("dd/MM/yyyy 'às' HH:mm");
            String dataFormatada = savedMarcacao.getData().format(formatter);

            log.info("A tentar criar notificação para utente ID: {}", utente.getId());
            notificacaoService.criarNotificacao(
                    utente.getId(),
                    "Nova Marcação Agendada",
                    "A sua marcação para " + dataFormatada + " foi agendada com sucesso.",
                    pt.florinhas.marcacoes.domain.NotificacaoTipo.LEMBRETE);
            log.info("Notificação criada com sucesso para utente ID: {}", utente.getId());
        } catch (Exception e) {
            log.error("Erro ao criar notificação de sistema", e);
        }

        log.info("Marcação presencial criada com sucesso: {}", savedMarcacao.getId());

        return savedMarcacao;
    }

    public Marcacao criarMarcacaoRemota(CriarMarcacaoRequest request) {
        // Buscar e validar utilizador
        Utilizador utilizador = utilizadorRepository.findById(request.getUtenteId())
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado com ID: " + request.getUtenteId()));

        if (!(utilizador instanceof Utente)) {
            throw new RuntimeException("O utilizador com ID " + request.getUtenteId() +
                    " é um " + utilizador.getClass().getSimpleName() + ", não um Utente. " +
                    "Apenas utentes podem criar marcações remotas.");
        }

        Utente utente = (Utente) utilizador;

        validarDisponibilidade(request.getData());

        // Criar Marcacao
        Marcacao marcacao = new Marcacao();
        marcacao.setData(request.getData());
        marcacao.setEstado(EventoEstado.AGENDADO);
        marcacao.setCriadoPor(utente); // Criado pelo próprio utente

        Marcacao savedMarcacao = marcacaoRepository.save(marcacao);

        // Criar MarcacaoSecretaria com OneToOne
        MarcacaoSecretaria marcacaoSecretaria = new MarcacaoSecretaria();
        marcacaoSecretaria.setMarcacao(savedMarcacao);
        marcacaoSecretaria.setAssunto(request.getAssunto());
        marcacaoSecretaria.setTipoAtendimento(AtendimentoTipo.REMOTO);
        marcacaoSecretaria.setUtente(utente);

        // Se tiver descrição, adicionar
        if (request.getDescricao() != null && !request.getDescricao().trim().isEmpty()) {
            marcacaoSecretaria.setDescricao(request.getDescricao());
        }

        MarcacaoSecretaria savedMarcacaoSecretaria = marcacaoSecretariaRepository.save(marcacaoSecretaria);

        // Estabelecer relacionamento bidirecional
        savedMarcacao.setMarcacaoSecretaria(savedMarcacaoSecretaria);

        notificarUtenteMarcacao(savedMarcacao, "NOVA_MARCACAO");

        // Notificar via sistema
        try {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                    .ofPattern("dd/MM/yyyy 'às' HH:mm");
            String dataFormatada = savedMarcacao.getData().format(formatter);

            notificacaoService.criarNotificacao(
                    utente.getId(),
                    "Nova Marcação Agendada",
                    "A sua marcação para " + dataFormatada + " foi agendada com sucesso.",
                    pt.florinhas.marcacoes.domain.NotificacaoTipo.LEMBRETE);
        } catch (Exception e) {
            log.error("Erro ao criar notificação de sistema", e);
        }

        log.info("Marcação criada com sucesso: {}", savedMarcacao.getId());

        return savedMarcacao;
    }

    public List<MarcacaoResponseDTO> consultarAgenda(LocalDateTime dataInicio, LocalDateTime dataFim) {
        return marcacaoRepository.findMarcacoesBetweenDates(dataInicio, dataFim).stream()
                .map(this::converterParaDTO)
                .toList();
    }

    public List<MarcacaoResponseDTO> procurarAgenda(LocalDateTime dataInicio, LocalDateTime dataFim, Long criadoPorId,
            Long utenteId, EventoEstado estado) {
        List<Marcacao> marcacoes = marcacaoRepository.findMarcacoesBetweenDates(dataInicio, dataFim);

        return marcacoes.stream()
                .filter(m -> criadoPorId == null || m.getCriadoPor().getId().equals(criadoPorId))
                .filter(m -> utenteId == null || (m.getMarcacaoSecretaria() != null
                        && m.getMarcacaoSecretaria().getUtente().getId().equals(utenteId)))
                .filter(m -> estado == null || m.getEstado().equals(estado))
                .map(this::converterParaDTO)
                .toList();
    }

    public MarcacaoResponseDTO atualizarEstadoMarcacao(Long marcacaoId, AtualizarEstadoRequest request) {
        if (marcacaoId == null || request == null) {
            throw new IllegalArgumentException("Argumento não pode ser nulo");
        }

        Utilizador atualizadoPor = utilizadorRepository.findById(request.getFuncionarioId())
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado"));

        EventoEstado novoEstado = request.getNovoEstadoEnum();

        Marcacao marcacao = marcacaoRepository.findById(marcacaoId)
                .orElseThrow(() -> new RuntimeException("Marcação não encontrada"));

        // Validar versão para evitar conflitos de concorrência
        if (request.getVersion() != null && !request.getVersion().equals(marcacao.getVersion())) {
            throw new RuntimeException(
                    "Conflito de versão: a marcação foi modificada por outro utilizador. Por favor, recarregue e tente novamente.");
        }

        EventoEstado estadoAtual = marcacao.getEstado();

        // Validar transições de estado permitidas
        validarTransicaoEstado(estadoAtual, novoEstado);

        // Permitir que utente cancele a sua própria marcação
        if (atualizadoPor instanceof Utente utente && novoEstado.equals(EventoEstado.CANCELADO)) {
            // Verificar se o utente é o dono da marcação (criou ou é o utente associado)
            boolean isOwner = utente.equals(marcacao.getCriadoPor()) ||
                    (marcacao.getMarcacaoSecretaria() != null &&
                            marcacao.getMarcacaoSecretaria().getUtente() != null &&
                            utente.equals(marcacao.getMarcacaoSecretaria().getUtente()));

            if (isOwner) {
                marcacao.setEstado(novoEstado);
                Marcacao savedMarcacao = marcacaoRepository.save(marcacao);

                // Notificar Secretaria (Cancelado pelo Utente)
                // Notificar Secretaria (Cancelado pelo Utente)
                try {
                    // Notificar quem criou a marcação (se for funcionário)
                    if (marcacao.getCriadoPor() instanceof Funcionario funcionario) {
                        notificacaoService.criarNotificacao(
                                funcionario.getId(),
                                "Marcação Cancelada pelo Utente",
                                "O utente " + utente.getNome() + " cancelou a marcação de " + marcacao.getData(),
                                pt.florinhas.marcacoes.domain.NotificacaoTipo.CANCELAMENTO);
                        log.info("Notificação de cancelamento enviada para funcionário criador: {}",
                                funcionario.getId());
                    } else {
                        // Fallback: se foi criada pelo próprio utente (remota) ou não tem criador
                        // definido
                        // Procurar uma secretária ativa para notificar
                        java.util.List<Funcionario> secretarias = funcionarioRepository
                                .findByTipo(FuncionarioTipo.SECRETARIA);
                        java.util.Optional<Funcionario> admin = secretarias.stream().filter(Funcionario::isActivo)
                                .findFirst();

                        if (admin.isPresent()) {
                            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                                    .ofPattern("dd/MM/yyyy 'às' HH:mm");
                            String dataFormatada = marcacao.getData().format(formatter);

                            notificacaoService.criarNotificacao(
                                    admin.get().getId(),
                                    "Marcação Cancelada pelo Utente",
                                    "O utente " + utente.getNome() + " cancelou a marcação de " + dataFormatada,
                                    pt.florinhas.marcacoes.domain.NotificacaoTipo.CANCELAMENTO);
                            log.info("Notificação de cancelamento enviada para secretaria (fallback): {}",
                                    admin.get().getId());
                        } else {
                            log.warn(
                                    "Marcação {} cancelada pelo utente, mas não foi encontrada secretária ativa para notificar.",
                                    marcacao.getId());
                        }
                    }
                } catch (Exception e) {
                    log.error("Erro ao criar notificação para secretaria", e);
                }

                return converterParaDTO(savedMarcacao);
            } else {
                throw new RuntimeException("Apenas o utente dono da marcação pode cancelá-la");
            }
        }

        if (atualizadoPor instanceof Funcionario funcionario) {
            validarFuncionarioSecretaria(funcionario); // Apenas secretaria pode atualizar estado

            // Se for um estado final gerado por funcionário, registar quem atendeu
            if (novoEstado == EventoEstado.CONCLUIDO ||
                    novoEstado == EventoEstado.NAO_COMPARECIDO ||
                    novoEstado == EventoEstado.CANCELADO) {
                marcacao.setAtendente(funcionario);
            }

            marcacao.setEstado(novoEstado);
            Marcacao savedMarcacao = marcacaoRepository.save(marcacao);

            // Se foi cancelado pela secretaria, notificar utente
            if (novoEstado == EventoEstado.CANCELADO && marcacao.getMarcacaoSecretaria() != null
                    && marcacao.getMarcacaoSecretaria().getUtente() != null) {
                try {
                    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                            .ofPattern("dd/MM/yyyy 'às' HH:mm");
                    String dataFormatada = marcacao.getData().format(formatter);

                    notificacaoService.criarNotificacao(
                            marcacao.getMarcacaoSecretaria().getUtente().getId(),
                            "Marcação Cancelada",
                            "A sua marcação de " + dataFormatada
                                    + " foi cancelada pelos serviços administrativos.",
                            pt.florinhas.marcacoes.domain.NotificacaoTipo.CANCELAMENTO);
                } catch (Exception e) {
                    log.error("Erro ao notificar utente do cancelamento", e);
                }
            }

            return converterParaDTO(savedMarcacao);
        } else {
            throw new RuntimeException("Apenas funcionários podem atualizar o estado da marcação");
        }
    }

    public List<MarcacaoResponseDTO> consultarMarcacoesPassadas(LocalDateTime dataInicio, LocalDateTime dataFim,
            Long utenteId, EventoEstado estado) {
        // Se não foram fornecidas datas, buscar desde o início até agora
        LocalDateTime inicio = dataInicio != null ? dataInicio : LocalDateTime.of(2000, 1, 1, 0, 0);
        // Alterado para buscar até ao futuro, pois o histórico agora baseia-se em
        // ESTADO e não apenas em data passada.
        // Isso permite ver marcações futuras que foram canceladas ou concluídas
        // antecipadamente.
        LocalDateTime fim = LocalDateTime.now().plusYears(100);

        List<Marcacao> marcacoes = marcacaoRepository.findMarcacoesBetweenDates(inicio, fim);

        return marcacoes.stream()
                // Remover filtro de data para incluir canceladas/concluídas futuras no
                // histórico
                // .filter(m -> m.getData().isBefore(LocalDateTime.now()))
                // Excluir estados ativos ou incompletos: AGENDADO, EM_PROGRESSO,
                // EM_PREENCHIMENTO
                // Isso inclui automaticamente: CONCLUIDO, CANCELADO, AVISO (não compareceu/docs
                // invalidos), NAO_COMPARECIDO
                .filter(m -> m.getEstado() != EventoEstado.AGENDADO &&
                        m.getEstado() != EventoEstado.EM_PROGRESSO &&
                        m.getEstado() != EventoEstado.EM_PREENCHIMENTO)
                .map(this::converterParaDTO)
                .toList();
    }

    public Optional<Marcacao> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return marcacaoRepository.findById(id);
    }

    public List<Marcacao> findAll() {
        return marcacaoRepository.findAll();
    }

    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        marcacaoRepository.deleteById(id);
    }

    public MarcacaoResponseDTO notificarDocumentosInvalidos(Long marcacaoId, NotificarDocumentosRequest request) {
        if (marcacaoId == null || request == null) {
            throw new IllegalArgumentException("Argumento não pode ser nulo");
        }

        Funcionario notificadoPor = funcionarioRepository.findById(request.getFuncionarioId())
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));

        Marcacao marcacao = marcacaoRepository.findById(marcacaoId)
                .orElseThrow(() -> new RuntimeException("Marcação não encontrada"));

        // Apenas secretaria pode notificar documentos inválidos
        validarFuncionarioSecretaria(notificadoPor);

        notificarUtenteMarcacao(marcacao, "DOCUMENTOS_INVALIDOS");

        return converterParaDTO(marcacao);
    }

    public List<MarcacaoResponseDTO> consultarMarcacoesUtente(Long utenteId) {
        Utente utente = utenteRepository.findById(utenteId)
                .orElseThrow(() -> new RuntimeException("Utente não encontrado"));
        return marcacaoRepository.findByUtente(utente).stream()
                .map(this::converterParaDTO)
                .toList();
    }

    public List<MarcacaoResponseDTO> consultarMarcacoesFuncionario(Long funcionarioId) {
        Funcionario funcionario = funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));
        return marcacaoRepository.findByCriadoPor(funcionario).stream()
                .map(this::converterParaDTO)
                .toList();
    }

    // Métodos privados auxiliares
    private void validarDisponibilidade(LocalDateTime data) {
        if (data.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Não é possível agendar marcações para datas passadas");
        }
        if (calendarioService.isSlotBloqueado(data.toLocalDate(), data.toLocalTime())) {
            throw new RuntimeException(
                    "O horário selecionado não está disponível (Feriado, Fim de Semana ou Bloqueado).");
        }
    }

    private void validarFuncionarioSecretaria(Funcionario funcionario) {
        // Verificar se o funcionário pertence à secretaria
        if (funcionario.getTipo() != FuncionarioTipo.SECRETARIA) {
            throw new RuntimeException("Apenas funcionários da secretaria podem realizar esta ação");
        }
    }

    private void validarTransicaoEstado(EventoEstado estadoAtual, EventoEstado novoEstado) {
        // Estados finais não podem ser alterados
        if (estadoAtual == EventoEstado.CONCLUIDO) {
            throw new RuntimeException("Não é possível alterar o estado de uma marcação já concluída");
        }

        // Marcação em progresso só pode ser concluída ou marcada com aviso ou não
        // compareceu
        if (estadoAtual == EventoEstado.EM_PROGRESSO) {
            if (novoEstado != EventoEstado.CONCLUIDO &&
                    novoEstado != EventoEstado.AVISO &&
                    novoEstado != EventoEstado.NAO_COMPARECIDO) {
                throw new RuntimeException(
                        "Uma marcação em progresso só pode ser concluída, marcada com aviso ou não comparecimento.");
            }
        }

        // Marcação cancelada não pode ser reativada
        if (estadoAtual == EventoEstado.CANCELADO) {
            throw new RuntimeException("Não é possível alterar o estado de uma marcação cancelada");
        }

        // Marcação com não compareceu não pode ser alterada
        if (estadoAtual == EventoEstado.NAO_COMPARECIDO) {
            throw new RuntimeException(
                    "Não é possível alterar o estado de uma marcação marcada como não comparecimento");
        }

        // Validar que não se pode voltar para EM_PREENCHIMENTO
        if (novoEstado == EventoEstado.EM_PREENCHIMENTO) {
            throw new RuntimeException("Não é possível voltar ao estado de preenchimento");
        }

        // De AGENDADO pode-se ir para: EM_PROGRESSO, CANCELADO, AVISO, NAO_COMPARECIDO
        if (estadoAtual == EventoEstado.AGENDADO) {
            if (novoEstado != EventoEstado.EM_PROGRESSO &&
                    novoEstado != EventoEstado.CANCELADO &&
                    novoEstado != EventoEstado.AVISO &&
                    novoEstado != EventoEstado.NAO_COMPARECIDO) {
                throw new RuntimeException(
                        "De agendado só é possível iniciar a marcação, cancelar, marcar como aviso ou não comparecimento");
            }
        }
    }

    public Long criarReservaTemporaria(CriarMarcacaoRequest request) {
        // 1. Verificar disponibilidade
        if (existeSobreposicao(request.getData())) {
            throw new RuntimeException("Este horário já está a ser preenchido ou ocupado por outra pessoa.");
        }

        Marcacao temp = new Marcacao();

        // 2. Definir dados básicos
        temp.setData(request.getData());
        temp.setEstado(EventoEstado.EM_PREENCHIMENTO);

        // 3. Definir o Timestamp de criação (CRÍTICO para o Cron Job)
        temp.setCriadoEm(LocalDateTime.now());

        // 4. Associar quem está a criar (se o ID vier no request)
        if (request.getCriadoPorId() != null) {
            // getReferenceById é mais eficiente que findById pois cria apenas um proxy sem
            // ir à BD imediatamente
            Utilizador criador = utilizadorRepository.getReferenceById(request.getCriadoPorId());
            temp.setCriadoPor(criador);
        }

        marcacaoRepository.save(temp);
        return temp.getId();
    }

    public void apagarReservaTemporaria(Long id) {
        marcacaoRepository.findById(id).ifPresent(m -> {
            // Só apagamos se ainda estiver em preenchimento
            if (m.getEstado() == EventoEstado.EM_PREENCHIMENTO) {
                marcacaoRepository.delete(m);
            }
        });
    }

    private boolean existeSobreposicao(LocalDateTime dataHora) {
        return marcacaoRepository.existsByDataAndEstadoNot(dataHora, EventoEstado.CANCELADO);
    }

    private void notificarUtenteMarcacao(Marcacao marcacao, String tipoNotificacao) {
        String mensagem = "";
        String assunto = "";

        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                .ofPattern("dd/MM/yyyy 'às' HH:mm");
        String dataFormatada = marcacao.getData().format(formatter);

        switch (tipoNotificacao) {
            case "NOVA_MARCACAO" -> {
                assunto = "Nova Marcação Criada";
                mensagem = "A sua marcação para %s foi agendada com sucesso.".formatted(dataFormatada);
            }
            case "CANCELAMENTO" -> {
                assunto = "Marcação Cancelada";
                mensagem = "A sua marcação foi cancelada.";
            }
            case "DOCUMENTOS_INVALIDOS" -> {
                assunto = "Documentos Inválidos";
                mensagem = "Os documentos apresentados são inválidos. Por favor, contacte a secretaria.";
            }
        }

        if (marcacao.getMarcacaoSecretaria().getUtente().getEmail() != null) {
            // emailService.enviarEmail(marcacao.getMarcacaoSecretaria().getUtente().getEmail(),
            // assunto, mensagem);
            log.info("Email simulado para {} com assunto: '{}' e mensagem: '{}'",
                    marcacao.getMarcacaoSecretaria().getUtente().getEmail(), assunto, mensagem);
        }
    }

    public List<java.util.Map<String, Object>> consultarMarcacoesBloqueadas(Long utenteId) {
        // Verificar se utente existe
        if (!utenteRepository.existsById(utenteId)) {
            throw new RuntimeException("Utente não encontrado");
        }

        List<Marcacao> todasMarcacoes = findAll();

        // Filtrar marcações que NÃO são do utente e NÃO estão canceladas
        return todasMarcacoes.stream()
                .filter(m -> {
                    // Se estiver cancelada, nunca bloqueia
                    if (m.getEstado() == EventoEstado.CANCELADO)
                        return false;

                    // Se for do próprio utente (via MarcacaoSecretaria), não é "bloqueada" (é
                    // "sua")
                    if (m.getMarcacaoSecretaria() != null &&
                            m.getMarcacaoSecretaria().getUtente() != null &&
                            m.getMarcacaoSecretaria().getUtente().getId().equals(utenteId)) {
                        return false;
                    }

                    // Se foi criada pelo próprio utente (caso de reserva temporária feita por ele
                    // mesmo)
                    if (m.getCriadoPor() != null && m.getCriadoPor().getId().equals(utenteId)) {
                        return false;
                    }

                    // Todos os outros casos são bloqueios (outros utentes, ou EM_PREENCHIMENTO de
                    // outros)
                    return true;
                })
                .map(m -> {
                    java.util.Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", m.getId());
                    map.put("data", m.getData());
                    map.put("estado", m.getEstado().name());
                    return map;
                })
                .toList();
    }

    public MarcacaoResponseDTO reagendarMarcacao(Long id, pt.florinhas.marcacoes.dto.ReagendarMarcacaoRequest request) {
        Marcacao marcacao = findById(id)
                .orElseThrow(() -> new RuntimeException("Marcação não encontrada"));

        // Validar se nova data é válida (não é no passado)
        if (request.getNovaDataHora().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Não é possível reagendar para o passado");
        }

        // Atualizar data
        marcacao.setData(request.getNovaDataHora());

        // Se estava cancelada ou aviso, talvez voltar a agendado?
        if (marcacao.getEstado() == EventoEstado.CANCELADO || marcacao.getEstado() == EventoEstado.NAO_COMPARECIDO
                || marcacao.getEstado() == EventoEstado.AVISO) {
            marcacao.setEstado(EventoEstado.AGENDADO);
        }

        Marcacao saved = marcacaoRepository.save(marcacao);
        return converterParaDTO(saved);
    }

    public MarcacaoResponseDTO converterParaDTO(Marcacao marcacao) {
        MarcacaoResponseDTO dto = new MarcacaoResponseDTO();
        dto.setId(marcacao.getId());
        dto.setVersion(marcacao.getVersion());
        dto.setData(marcacao.getData());
        dto.setEstado(marcacao.getEstado());

        if (marcacao.getMarcacaoSecretaria() != null) {
            MarcacaoResponseDTO.MarcacaoSecretariaDTO secDTO = new MarcacaoResponseDTO.MarcacaoSecretariaDTO();
            secDTO.setAssunto(marcacao.getMarcacaoSecretaria().getAssunto());
            secDTO.setDescricao(marcacao.getMarcacaoSecretaria().getDescricao());
            secDTO.setTipoAtendimento(marcacao.getMarcacaoSecretaria().getTipoAtendimento());

            if (marcacao.getMarcacaoSecretaria().getUtente() != null) {
                Utente utente = marcacao.getMarcacaoSecretaria().getUtente();
                MarcacaoResponseDTO.UtenteDTO utenteDTO = new MarcacaoResponseDTO.UtenteDTO();
                utenteDTO.setId(utente.getId());
                utenteDTO.setNome(utente.getNome());
                utenteDTO.setEmail(utente.getEmail());
                utenteDTO.setNif(utente.getNif());
                utenteDTO.setTelefone(utente.getTelefone());
                secDTO.setUtente(utenteDTO);
            }

            dto.setMarcacaoSecretaria(secDTO);
        }

        if (marcacao.getAtendente() != null) {
            dto.setAtendenteNome(marcacao.getAtendente().getNome());
        }

        return dto;
    }

    public List<MarcacaoResponseDTO> listarTodasMarcacoesDTO() {
        return findAll().stream().map(this::converterParaDTO).toList();
    }

    public MarcacaoResponseDTO obterMarcacaoDTO(Long id) {
        Marcacao marcacao = findById(id).orElseThrow(() -> new RuntimeException("Marcação não encontrada"));
        return converterParaDTO(marcacao);
    }

    /**
     * Tarefa agendada para limpar reservas temporárias expiradas.
     * Executa a cada 60 segundos (fixedRate = 60000 ms).
     * Remove marcações em estado EM_PREENCHIMENTO criadas há mais de 15 minutos.
     */
    @Scheduled(fixedRate = 60000)
    public void limparReservasExpiradas() {
        LocalDateTime limite = LocalDateTime.now().minusMinutes(15);
        // Opcional: Logging para debug
        // System.out.println("A verificar reservas expiradas anteriores a " + limite);

        marcacaoRepository.deleteByEstadoAndCriadoEmBefore(EventoEstado.EM_PREENCHIMENTO, limite);
    }
}