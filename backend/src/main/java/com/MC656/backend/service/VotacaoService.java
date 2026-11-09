package com.MC656.backend.service;

import com.MC656.backend.dto.*;
import com.MC656.backend.entity.*;
import com.MC656.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/* Serviço responsavel por orquestrar o ciclo de vida completo de uma Votacao
   (tanto DELIBERACAO quanto ELEICAO): abertura, gerenciamento de chapas,
   registro de votos e apuracao de resultados.

   Convenção de excecoes (traduzida para HTTP no VotacaoController):
   - IllegalArgumentException -> dados invalidos ou permissao negada (400 Bad Request)
   - NoSuchElementException   -> recurso nao encontrado (404 Not Found)
   - IllegalStateException    -> operacao incompativel com o estado da votacao (409 Conflict) */
@Service
public class VotacaoService {

    private final VotacaoRepository votacaoRepository;
    private final VotoRepository votoRepository;
    private final RegistroVotanteRepository registroVotanteRepository;
    private final ContextoRepository contextoRepository;
    private final ContextoService contextoService;
    private final QuorumService quorumService;
    private final ChapaRepository chapaRepository;
    private final List<ApuracaoStrategy> estrategiasApuracao;

    public VotacaoService(VotacaoRepository votacaoRepository,
                          VotoRepository votoRepository,
                          RegistroVotanteRepository registroVotanteRepository,
                          ContextoRepository contextoRepository,
                          ContextoService contextoService,
                          QuorumService quorumService,
                          ChapaRepository chapaRepository,
                          List<ApuracaoStrategy> estrategiasApuracao) {
        this.votacaoRepository = votacaoRepository;
        this.votoRepository = votoRepository;
        this.registroVotanteRepository = registroVotanteRepository;
        this.contextoRepository = contextoRepository;
        this.contextoService = contextoService;
        this.quorumService = quorumService;
        this.chapaRepository = chapaRepository;
        this.estrategiasApuracao = estrategiasApuracao;
    }

    // Abertura de votacao (DELIBERACAO OU ELEICAO)
    /* Abre uma nova votacao em um contexto existente.
       Apenas o admin do contexto possui permissao para criar votacoes */
    @Transactional
    public VotacaoResponseDTO abrir(VotacaoCreateDTO dto) {
        Contexto contexto = buscarContexto(dto.contextoId());
        validarPermissaoAdministrador(dto.adminId(), contexto);

        TipoVotacao tipo = dto.tipo() == null ? TipoVotacao.DELIBERACAO : dto.tipo();

        return switch (tipo) {
            case DELIBERACAO -> abrirDeliberacao(dto, contexto);
            case ELEICAO -> abrirEleicao(dto, contexto);
        };
    }

    private VotacaoResponseDTO abrirDeliberacao(VotacaoCreateDTO dto, Contexto contexto) {
        // Cria uma votacao do tipo DELIBERACAO (pauta SIM/NAO/ABSTENCAO, sem chapas)
        if (dto.chapas() != null && !dto.chapas().isEmpty()) {
            throw new IllegalArgumentException("Votações do tipo DELIBERACAO não podem possuir chapas.");
        }
        Votacao votacao = instanciarESalvarVotacao(dto, contexto, TipoVotacao.DELIBERACAO);
        return toDTO(votacao, List.of());
    }

    private VotacaoResponseDTO abrirEleicao(VotacaoCreateDTO dto, Contexto contexto) {
        // Cria uma votacao do tipo ELEICAO juntamente com suas chapas e membros integrantes
        validarChapasDaEleicao(dto.chapas());

        Votacao votacao = instanciarESalvarVotacao(dto, contexto, TipoVotacao.ELEICAO);
        List<Chapa> chapasSalvas = criarESalvarChapas(votacao, dto.chapas());

        return toDTO(votacao, chapasSalvas);
    }

    private Votacao instanciarESalvarVotacao(VotacaoCreateDTO dto, Contexto contexto, TipoVotacao tipo) {
        // Instancia a entidade Votacao e a persiste no repository
        Votacao votacao = new Votacao(
                dto.titulo(),
                dto.descricao(),
                contexto,
                tipo,
                dto.regraAprovacao(),
                dto.percentualMinimo(),
                dto.quorumMinimo(),
                dto.dataInicio(),
                dto.dataFim(),
                dto.pauta(),
                dto.participantesAptos(),
                dto.pesoVoto(),
                dto.unidadeApartamento(),
                dto.categoriaSegmento(),
                dto.elegibilidade()
        );
        return votacaoRepository.save(votacao);
    }

    private void validarPermissaoAdministrador(Long adminId, Contexto contexto) {
        // Garante que o usuario que solicitou a abertura eh admin do contexto
        if (adminId == null) {
            throw new IllegalArgumentException(
                    "O administrador responsável pela criação da votação deve ser informado.");
        }
        if (!contextoService.isAdministradorDoContexto(adminId, contexto.getId())) {
            throw new IllegalArgumentException(
                    "Apenas o administrador do contexto pode criar uma votação.");
        }
    }

    private void validarChapasDaEleicao(List<ChapaCreateDTO> chapas) {
        // Valida a lista de chapas informada na criacao de uma eleicao
        if (chapas == null || chapas.isEmpty()) {
            throw new IllegalArgumentException("A eleição deve possuir ao menos uma chapa cadastrada.");
        }

        Set<String> nomesChapas = new HashSet<>();
        Set<Long> membrosNaEleicao = new HashSet<>();

        for (ChapaCreateDTO chapaDto : chapas) {
            validarChapaIndividual(chapaDto, nomesChapas, membrosNaEleicao);
        }
    }

    private void validarChapaIndividual(ChapaCreateDTO chapaDto, Set<String> nomesChapas, Set<Long> membrosNaEleicao) {
        // Valida unicidade do nome da chapa e garante que um usuario nao participe de duas chapas
        if (chapaDto.nome() == null || chapaDto.nome().isBlank()) {
            throw new IllegalArgumentException("O nome da chapa não pode ser vazio.");
        }

        String nomeNormalizado = chapaDto.nome().trim().toLowerCase(Locale.ROOT);
        if (!nomesChapas.add(nomeNormalizado)) {
            throw new IllegalArgumentException(
                    "Não são permitidas chapas com nomes duplicados na mesma eleição: " + chapaDto.nome());
        }

        if (chapaDto.membrosIds() == null || chapaDto.membrosIds().isEmpty()) {
            throw new IllegalArgumentException(
                    "A chapa '" + chapaDto.nome() + "' deve possuir ao menos um membro integrante.");
        }

        for (Long membroId : chapaDto.membrosIds()) {
            if (membroId == null) {
                throw new IllegalArgumentException("ID de membro da chapa não pode ser nulo.");
            }
            if (!membrosNaEleicao.add(membroId)) {
                throw new IllegalArgumentException(
                        "O usuário " + membroId + " foi adicionado mais de uma vez em chapas desta eleição.");
            }
        }
    }

    // Busca os usuários membros no contexto e persiste cada chapa vinculada à eleição
    private List<Chapa> criarESalvarChapas(Votacao votacao, List<ChapaCreateDTO> chapasDto) {
        List<Chapa> chapasSalvas = new ArrayList<>();
        for (ChapaCreateDTO chapaDto : chapasDto) {
            List<Usuario> membros = chapaDto.membrosIds().stream()
                    .map(membroId -> contextoService.buscarMembroDoContexto(
                            membroId, votacao.getContexto().getId()))
                    .toList();
            Chapa chapa = new Chapa(votacao, chapaDto.nome(), chapaDto.proposta(), membros);
            chapasSalvas.add(chapaRepository.save(chapa));
        }
        return chapasSalvas;
    }

    // Consulta e gerenciamento de chapas
    @Transactional
    public ChapaResponseDTO atualizarChapa(Long votacaoId, Long chapaId, ChapaUpdateDTO dto) {
        /* Permite que um membro da chapa atualize o nome, propostas e informacoes
           da sua chapa enquanto a eleicao estiver aberta. */
        Votacao votacao = buscarVotacao(votacaoId);
        validarVotacaoAbertaDoTipoEleicao(votacao);

        Chapa chapa = buscarChapaDaEleicao(chapaId, votacaoId);
        validarPermissaoDeMembroDaChapa(chapa, dto.usuarioId());
        validarNomeUnicoDeChapaNaEleicao(votacaoId, chapaId, dto.nome());

        chapa.atualizarInformacoes(dto.nome(), dto.proposta());
        return toChapaDTO(chapaRepository.save(chapa));
    }

    @Transactional(readOnly = true)
    public List<ChapaResponseDTO> listarChapas(Long votacaoId) {
        Votacao votacao = buscarVotacao(votacaoId);
        if (votacao.getTipo() != TipoVotacao.ELEICAO) {
            throw new IllegalArgumentException("Votações do tipo DELIBERACAO não possuem chapas.");
        }
        return buscarChapasPorVotacao(votacaoId).stream()
                .map(this::toChapaDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public VotacaoResponseDTO buscar(Long votacaoId) {
        return toDTO(buscarVotacao(votacaoId));
    }

    @Transactional(readOnly = true)
    public List<VotacaoResponseDTO> listarPorContexto(Long contextoId) {
        contextoRepository.findById(contextoId)
                .orElseThrow(() -> new NoSuchElementException("Contexto não encontrado."));
        return votacaoRepository.findByContextoId(contextoId).stream()
                .map(this::toDTO)
                .toList();
    }

    private void validarVotacaoAbertaDoTipoEleicao(Votacao votacao) {
        // Verifica se a votacao eh do tipo ELEICAO e se esta ABERTA
        // para a atualizacao de chapas
        if (votacao.getTipo() != TipoVotacao.ELEICAO) {
            throw new IllegalArgumentException("Esta votação não é do tipo ELEICAO.");
        }
        if (votacao.getStatus() != StatusVotacao.ABERTA) {
            throw new IllegalStateException("Não é possível editar a chapa após o encerramento da eleição.");
        }
    }

    private void validarPermissaoDeMembroDaChapa(Chapa chapa, Long usuarioId) {
        // Verifica se usuario com usuarioId eh membro da chapa
        // para atualizacao de infos da chapa
        if (!chapa.contemMembro(usuarioId)) {
            throw new IllegalArgumentException(
                    "Apenas os membros integrantes da chapa podem alterar seu nome, propostas e informações.");
        }
    }

    private void validarNomeUnicoDeChapaNaEleicao(Long votacaoId, Long chapaId, String novoNomeInformado) {
        // Verifica se o nome da chapa pode ser alterado para o solicitado
        String novoNome = novoNomeInformado == null ? "" : novoNomeInformado.trim();
        boolean nomeEmUsoPorOutraChapa = buscarChapasPorVotacao(votacaoId).stream()
                .anyMatch(c -> !c.getId().equals(chapaId) && c.getNome().equalsIgnoreCase(novoNome));
        if (nomeEmUsoPorOutraChapa) {
            throw new IllegalArgumentException("Já existe outra chapa com este nome nesta eleição.");
        }
    }

    /* Registra o voto em duas partes, na mesma transacao:
       1) RegistroVotante: marca que o usuario votou (impede voto duplo)
       2) Voto: guarda so opcao + peso, sem identificar o usuario (sigilo) */
    @Transactional
    public void votar(Long votacaoId, VotoRequestDTO dto) {
        Votacao votacao = buscarVotacao(votacaoId);
        validarJanelaDeVotacaoAberta(votacao);

        Participacao participacao = buscarParticipacaoDoEleitor(dto.usuarioId(), votacao);
        Voto votoAnonimo = construirVotoPorModalidade(votacao, dto, participacao.getPeso());

        registrarPresencaDoEleitor(votacao, participacao.getUsuario());
        votoRepository.save(votoAnonimo);
    }

    private void validarJanelaDeVotacaoAberta(Votacao votacao) {
        if (!votacao.aceitaVotos(LocalDateTime.now())) {
            throw new IllegalStateException("A votação não está aberta para votos.");
        }
    }

    private Participacao buscarParticipacaoDoEleitor(Long usuarioId, Votacao votacao) {
        return contextoService
                .buscarParticipacaoVotante(usuarioId, votacao.getContexto().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "O usuário não participa do contexto desta votação."));
    }

    private void registrarPresencaDoEleitor(Votacao votacao, Usuario eleitor) {
        // Registra que eleitor votou, garantindo que cada eleitor vote uma unica vez, inclusive sob requisicoes concorrentes
        if (registroVotanteRepository.existsByVotacaoIdAndUsuarioId(votacao.getId(), eleitor.getId())) {
            throw new IllegalStateException("O usuário já votou nesta votação.");
        }

        try {
            registroVotanteRepository.save(new RegistroVotante(votacao, eleitor));
        } catch (DataIntegrityViolationException e) {
            // Duas requisicoes simultaneas do mesmo usuario: a constraint unica barra a segunda
            throw new IllegalStateException("O usuário já votou nesta votação.");
        }
    }

    // Usa os Factory Methods de Voto para construir o voto valido conforme o tipo da votacao
    private Voto construirVotoPorModalidade(Votacao votacao, VotoRequestDTO dto, Double peso) {
        return switch (votacao.getTipo()) {
            case DELIBERACAO -> construirVotoDeliberacao(votacao, dto, peso);
            case ELEICAO -> construirVotoEleicao(votacao, dto, peso);
        };
    }

    private Voto construirVotoDeliberacao(Votacao votacao, VotoRequestDTO dto, Double peso) {
        if (dto.opcao() == null || dto.chapaId() != null) {
            throw new IllegalArgumentException(
                    "Em deliberações, informe uma opção de voto (SIM, NAO ou ABSTENCAO).");
        }
        return Voto.emOpcao(votacao, dto.opcao(), peso);
    }

    private Voto construirVotoEleicao(Votacao votacao, VotoRequestDTO dto, Double peso) {
        if (dto.chapaId() != null && dto.opcao() == null) {
            Chapa chapa = buscarChapaDaEleicao(dto.chapaId(), votacao.getId());
            return Voto.emChapa(votacao, chapa, peso);
        }
        if (dto.opcao() == OpcaoVoto.ABSTENCAO && dto.chapaId() == null) {
            return Voto.emBranco(votacao, peso);
        }
        throw new IllegalArgumentException(
                "Em eleições, o voto deve ser atribuído a uma chapa ou registrado como ABSTENCAO.");
    }

    // Encerramento e Apuracao
    @Transactional
    public VotacaoResponseDTO encerrar(Long votacaoId) {
        Votacao votacao = buscarVotacao(votacaoId);
        votacao.encerrar();
        return toDTO(votacaoRepository.save(votacao));
    }

    /* Apura o resultado final da votacao (somente apos o encerramento).
       Delega a verificacao de quorum ao QuorumService e o calculo do veredito
       a ApuracaoStrategy correspondente ao tipo da votacao. */
    @Transactional
    public ApuracaoResponseDTO apurar(Long votacaoId) {
        Votacao votacao = buscarVotacao(votacaoId);
        encerrarAutomaticamenteSePrazoExpirado(votacao);
        exigirVotacaoEncerrada(votacao);

        List<Voto> votos = votoRepository.findByVotacaoId(votacaoId);
        boolean quorumAtingido = verificarQuorum(votacao, votos);
        List<Chapa> chapas = buscarChapasPorVotacao(votacaoId);

        ApuracaoStrategy estrategia = selecionarEstrategiaDeApuracao(votacao.getTipo());
        return estrategia.apurar(votacao, votos, chapas, quorumAtingido);
    }

    private void encerrarAutomaticamenteSePrazoExpirado(Votacao votacao) {
        // Se a data de fim já passou e o status ainda consta como ABERTA, encerra automaticamente
        if (votacao.prazoExpirado(LocalDateTime.now())) {
            votacao.encerrar();
            votacaoRepository.save(votacao);
        }
    }

    private void exigirVotacaoEncerrada(Votacao votacao) {
        if (votacao.getStatus() != StatusVotacao.ENCERRADA) {
            throw new IllegalStateException("O resultado só fica disponível após o encerramento da votação.");
        }
    }

    private boolean verificarQuorum(Votacao votacao, List<Voto> votos) {
        Map<Long, Double> eleitorado = contextoService.pesosDoEleitorado(votacao.getContexto().getId());
        return quorumService.quorumAtingido(
                votacao.getContexto().getTipo(),
                votacao.getQuorumMinimo(),
                votos,
                eleitorado
        );
    }

    private ApuracaoStrategy selecionarEstrategiaDeApuracao(TipoVotacao tipo) {
        // Seleciona a implementacao de ApuracaoStrategy compativel com o TipoVotacao
        return estrategiasApuracao.stream()
                .filter(e -> e.suporta(tipo))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Nenhuma estratégia de apuração definida para o tipo " + tipo));
    }
    
    // Metodos auxiliares de busca e conversao de DTO
    private Contexto buscarContexto(Long contextoId) {
        // Busca contexto por id do contexto
        return contextoRepository.findById(contextoId)
                .orElseThrow(() -> new NoSuchElementException("Contexto não encontrado."));
    }

    private Votacao buscarVotacao(Long votacaoId) {
        // Busca votacao por id da votacao
        return votacaoRepository.findById(votacaoId)
                .orElseThrow(() -> new NoSuchElementException("Votação não encontrada."));
    }

    private Chapa buscarChapaDaEleicao(Long chapaId, Long votacaoId) {
        // Busca chapa por id da chapa e da votacao
        return chapaRepository.findByIdAndVotacaoId(chapaId, votacaoId)
                .orElseThrow(() -> new NoSuchElementException("Chapa não encontrada nesta eleição."));
    }

    private List<Chapa> buscarChapasPorVotacao(Long votacaoId) {
        // Busca chapas a partir do id da votacao
        if (chapaRepository == null || votacaoId == null) {
            return List.of();
        }
        return chapaRepository.findByVotacaoId(votacaoId);
    }

    private VotacaoResponseDTO toDTO(Votacao v) {
        // Response apos encerramento da votacao 
        List<Chapa> chapas = v.getTipo() == TipoVotacao.ELEICAO
                ? buscarChapasPorVotacao(v.getId())
                : List.of();
        return toDTO(v, chapas);
    }

    private VotacaoResponseDTO toDTO(Votacao v, List<Chapa> chapas) {
        // Response apos abertura da votacao
        List<ChapaResponseDTO> chapasDTO = chapas.stream()
                .map(this::toChapaDTO)
                .toList();
        return new VotacaoResponseDTO(
                v.getId(),
                v.getTitulo(),
                v.getDescricao(),
                v.getContexto().getId(),
                v.getContexto().getNome(),
                v.getTipo(),
                v.getRegraAprovacao(),
                v.getPercentualMinimo(),
                v.getQuorumMinimo(),
                v.getStatus(),
                v.getDataInicio(),
                v.getDataFim(),
                v.getPauta(),
                v.getParticipantesAptos(),
                v.getPesoVoto(),
                v.getUnidadeApartamento(),
                v.getCategoriaSegmento(),
                v.getElegibilidade(),
                chapasDTO
        );
    }

    private ChapaResponseDTO toChapaDTO(Chapa chapa) {
        List<MembroChapaDTO> membrosDTO = chapa.getMembros().stream()
                .map(u -> new MembroChapaDTO(u.getId(), u.getNome(), u.getEmail()))
                .toList();
        return new ChapaResponseDTO(
                chapa.getId(),
                chapa.getVotacao().getId(),
                chapa.getNome(),
                chapa.getProposta(),
                membrosDTO
        );
    }
}
