package pt.florinhas.marcacoes.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@Service
@Transactional
public class MarcacaoService{

    @Autowired
    private MarcacaoRepository marcacaoRepository;
    
    @Autowired
    private MarcacaoSecretariaRepository marcacaoSecretariaRepository;
    
    @Autowired
    private UtenteRepository utenteRepository;
    
    @Autowired
    private FuncionarioRepository funcionarioRepository;
    
    @Autowired
    private UtilizadorRepository utilizadorRepository;
    
    @Autowired
    private UtilizadorService utilizadorService;
    
    //@Autowired
    //private FuncionarioRepository funcionarioRepository;
    
    //@Autowired
    //private EmailService emailService;

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
        Funcionario criadoPor = funcionarioRepository.findById(request.getFuncionarioId()).orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));
        
        validarFuncionarioSecretaria(criadoPor); // Apenas secretaria pode criar

        // Procurar ou criar utente por NIF usando o UtilizadorService
        Utente utente = utilizadorService.obterOuCriarUtente(
            request.getUtenteNif(), 
            request.getUtenteNome(), 
            request.getUtenteEmail(), 
            request.getUtenteTelefone()
        );
        
        // Verificar se os dados do request coincidem com os dados do utente existente
        if (request.getUtenteNome() != null && !request.getUtenteNome().trim().isEmpty() 
            && !utente.getNome().equalsIgnoreCase(request.getUtenteNome())) {
            throw new RuntimeException("O nome fornecido não coincide com o utente registado com o NIF " + request.getUtenteNif());
        }
        
        if (request.getUtenteEmail() != null && !request.getUtenteEmail().trim().isEmpty() 
            && !utente.getEmail().equalsIgnoreCase(request.getUtenteEmail())) {
            throw new RuntimeException("O email fornecido não coincide com o utente registado com o NIF " + request.getUtenteNif());
        }
        
        if (request.getUtenteTelefone() != null && !request.getUtenteTelefone().trim().isEmpty() 
            && !utente.getTelefone().equals(request.getUtenteTelefone())) {
            throw new RuntimeException("O telefone fornecido não coincide com o utente registado com o NIF " + request.getUtenteNif());
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
        
        System.out.println("Marcação presencial criada com sucesso: " + savedMarcacao.getId());
        
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
        
        System.out.println("Marcação remota criada com sucesso: " + savedMarcacao.getId());
        
        return savedMarcacao;
    }

    
    public List<MarcacaoResponseDTO> consultarAgenda(LocalDateTime dataInicio, LocalDateTime dataFim) {
        return marcacaoRepository.findMarcacoesBetweenDates(dataInicio, dataFim).stream()
                .map(this::converterParaDTO)
                .toList();
    }

    
    public List<MarcacaoResponseDTO> procurarAgenda(LocalDateTime dataInicio, LocalDateTime dataFim, Long criadoPorId, Long utenteId, EventoEstado estado) {
        List<Marcacao> marcacoes = marcacaoRepository.findMarcacoesBetweenDates(dataInicio, dataFim);
        
        return marcacoes.stream()
                .filter(m -> criadoPorId == null || m.getCriadoPor().getId().equals(criadoPorId))
                .filter(m -> utenteId == null || (m.getMarcacaoSecretaria() != null && m.getMarcacaoSecretaria().getUtente().getId().equals(utenteId)))
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
            throw new RuntimeException("Conflito de versão: a marcação foi modificada por outro utilizador. Por favor, recarregue e tente novamente.");
        }

        EventoEstado estadoAtual = marcacao.getEstado();
        
        // Validar transições de estado permitidas
        validarTransicaoEstado(estadoAtual, novoEstado);

        if (atualizadoPor instanceof Utente utente && utente.equals(marcacao.getCriadoPor()) && novoEstado.equals(EventoEstado.CANCELADO)) {
            // Utente que criou a marcação pode apenas marcar com cancelado
            marcacao.setEstado(novoEstado);
            Marcacao savedMarcacao = marcacaoRepository.save(marcacao);
            return converterParaDTO(savedMarcacao);
        }

        if (atualizadoPor instanceof Funcionario funcionario) {
            validarFuncionarioSecretaria(funcionario); // Apenas secretaria pode atualizar estado
            marcacao.setEstado(novoEstado);
            Marcacao savedMarcacao = marcacaoRepository.save(marcacao);
            return converterParaDTO(savedMarcacao);
        }
        else {
            throw new RuntimeException("Apenas funcionários podem atualizar o estado da marcação");
        }
    }

    
    public List<MarcacaoResponseDTO> consultarMarcacoesPassadas(LocalDateTime dataInicio, LocalDateTime dataFim, Long utenteId, EventoEstado estado) {
        // Se não foram fornecidas datas, buscar desde o início até agora
        LocalDateTime inicio = dataInicio != null ? dataInicio : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime fim = LocalDateTime.now();
        
        List<Marcacao> marcacoes = marcacaoRepository.findMarcacoesBetweenDates(inicio, fim);
        
        return marcacoes.stream()
                .filter(m -> m.getData().isBefore(LocalDateTime.now()))
                // Apenas estados finalizados: CONCLUIDO, CANCELADO, AVISO (não compareceu)
                .filter(m -> m.getEstado() == EventoEstado.CONCLUIDO || 
                            m.getEstado() == EventoEstado.CANCELADO || 
                            m.getEstado() == EventoEstado.AVISO)
                .map(this::converterParaDTO)
                .toList();
    }

    
    public Optional<Marcacao> findById(Long id) {
        if (id == null) { throw new IllegalArgumentException("ID não pode ser nulo"); }
        return marcacaoRepository.findById(id);
    }

    
    public List<Marcacao> findAll() { return marcacaoRepository.findAll(); }

    
    public void deleteById(Long id) {
        if (id == null) { throw new IllegalArgumentException("ID não pode ser nulo"); }
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
        if (data.isBefore(LocalDateTime.now())) { throw new RuntimeException("Não é possível agendar marcações para datas passadas"); }
        // TODO: Lógica de matriz de horários pode ser adicionada aqui
    }
    
    private void validarFuncionarioSecretaria(Funcionario funcionario) {
        // Verificar se o funcionário pertence à secretaria
        if (funcionario.getTipo() != FuncionarioTipo.SECRETARIA) { throw new RuntimeException("Apenas funcionários da secretaria podem realizar esta ação"); }
    }
    
    private void validarTransicaoEstado(EventoEstado estadoAtual, EventoEstado novoEstado) {
        // Estados finais não podem ser alterados
        if (estadoAtual == EventoEstado.CONCLUIDO) {
            throw new RuntimeException("Não é possível alterar o estado de uma marcação já concluída");
        }
        
        // Marcação em progresso só pode ser concluída ou marcada com aviso
        if (estadoAtual == EventoEstado.EM_PROGRESSO) {
            if (novoEstado != EventoEstado.CONCLUIDO && novoEstado != EventoEstado.AVISO) {
                throw new RuntimeException("Uma marcação em progresso só pode ser concluída ou marcada com aviso. Não é possível cancelar ou voltar ao estado agendado.");
            }
        }
        
        // Marcação cancelada não pode ser reativada
        if (estadoAtual == EventoEstado.CANCELADO) {
            throw new RuntimeException("Não é possível alterar o estado de uma marcação cancelada");
        }
        
        // Marcação com não compareceu não pode ser alterada
        if (estadoAtual == EventoEstado.NAO_COMPARECIDO) {
            throw new RuntimeException("Não é possível alterar o estado de uma marcação marcada como não comparecimento");
        }
        
        // Validar que não se pode voltar para EM_PREENCHIMENTO
        if (novoEstado == EventoEstado.EM_PREENCHIMENTO) {
            throw new RuntimeException("Não é possível voltar ao estado de preenchimento");
        }
        
        // De AGENDADO pode-se ir para: EM_PROGRESSO, CANCELADO, AVISO
        if (estadoAtual == EventoEstado.AGENDADO) {
            if (novoEstado != EventoEstado.EM_PROGRESSO && 
                novoEstado != EventoEstado.CANCELADO && 
                novoEstado != EventoEstado.AVISO) {
                throw new RuntimeException("De agendado só é possível iniciar a marcação, cancelar ou marcar como aviso");
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
            // getReferenceById é mais eficiente que findById pois cria apenas um proxy sem ir à BD imediatamente
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
        
        switch (tipoNotificacao) {
            case "NOVA_MARCACAO" -> {
                assunto = "Nova Marcação Criada";
                mensagem = "A sua marcação para %s foi agendada com sucesso.".formatted(
                        marcacao.getData());
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
            //emailService.enviarEmail(marcacao.getMarcacaoSecretaria().getUtente().getEmail(), assunto, mensagem);
            System.out.println("Email enviado para " + marcacao.getMarcacaoSecretaria().getUtente().getEmail() + " com assunto: " + assunto + " e mensagem: " + mensagem);
        }
    }
    

    
    
    public List<java.util.Map<String, Object>> consultarMarcacoesBloqueadas(Long utenteId) {
        // Verificar se utente existe
        if (!utenteRepository.existsById(utenteId)) {
            throw new RuntimeException("Utente não encontrado");
        }
        
        List<Marcacao> todasMarcacoes = findAll();
        
        // Filtrar marcações que NÃO são do utente
        return todasMarcacoes.stream()
            .filter(m -> m.getMarcacaoSecretaria() != null && 
                       m.getMarcacaoSecretaria().getUtente() != null &&
                       !m.getMarcacaoSecretaria().getUtente().getId().equals(utenteId))
            .map(m -> {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("id", m.getId());
                map.put("data", m.getData());
                return map;
            })
            .toList();
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
        
        return dto;
    }
    
    
    public List<MarcacaoResponseDTO> listarTodasMarcacoesDTO() {
        return findAll().stream().map(this::converterParaDTO).toList();
    }
    
    
    public MarcacaoResponseDTO obterMarcacaoDTO(Long id) {
        Marcacao marcacao = findById(id).orElseThrow(() -> new RuntimeException("Marcação não encontrada"));
        return converterParaDTO(marcacao);
    }
    
}