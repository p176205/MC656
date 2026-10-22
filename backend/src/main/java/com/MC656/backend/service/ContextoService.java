package com.MC656.backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.MC656.backend.dto.AssembleiaRequestDTO;
import com.MC656.backend.dto.CentroAcademicoRequestDTO;
import com.MC656.backend.dto.CondominioRequestDTO;
import com.MC656.backend.dto.UsuarioCreateDTO;
import com.MC656.backend.dto.UsuarioResponseDTO;
import com.MC656.backend.entity.Assembleia;
import com.MC656.backend.entity.CentroAcademico;
import com.MC656.backend.entity.Condominio;
import com.MC656.backend.entity.Contexto;
import com.MC656.backend.entity.Papel;
import com.MC656.backend.entity.Participacao;
import com.MC656.backend.entity.TipoContexto;
import com.MC656.backend.entity.Usuario;
import com.MC656.backend.repository.ContextoRepository;
import com.MC656.backend.repository.ParticipacaoRepository;
import com.MC656.backend.repository.UsuarioRepository;

@Service
public class ContextoService {

    private final ContextoRepository contextoRepository;
    private final ParticipacaoRepository participacaoRepository;
    private final UsuarioRepository usuarioRepository;

    public ContextoService(ContextoRepository contextoRepository, ParticipacaoRepository participacaoRepository,
            UsuarioRepository usuarioRepository) {

        this.contextoRepository = contextoRepository;
        this.participacaoRepository = participacaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Assembleia criarAssembleia(
            AssembleiaRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(3L)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        // Criar a entidade concreta
        Assembleia assembleia = new Assembleia(dto.nome(), dto.participantes());

        Assembleia contextoSalvo = contextoRepository.save(assembleia);

        // Registrar o criador como administrador
        Participacao participacao = new Participacao(usuario, assembleia, Papel.ADMINISTRADOR);

        participacaoRepository.save(participacao);

        return contextoSalvo;
    }

    public CentroAcademico criarCentroAcademico(
            CentroAcademicoRequestDTO dto) {

        Usuario usuario = usuarioRepository.findById(3L)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        // Criar a entidade concreta
        CentroAcademico centroAcademico = new CentroAcademico(dto.nome(), dto.tempo_vigencia());

        CentroAcademico contextoSalvo = contextoRepository.save(centroAcademico);

        // Registrar o criador como administrador
        Participacao participacao = new Participacao(usuario, centroAcademico, Papel.ADMINISTRADOR);

        participacaoRepository.save(participacao);

        return contextoSalvo;
    }

    public Condominio criarCondominio(
            CondominioRequestDTO dto) {

        Usuario usuario = usuarioRepository.findById(3L)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        // Criar a entidade concreta
        Condominio condominio = new Condominio(dto.nome(), dto.total_moradores());

        Condominio contextoSalvo = contextoRepository.save(condominio);

        // Registrar o criador administrador
        Participacao participacao = new Participacao(usuario, condominio, Papel.ADMINISTRADOR);

        participacaoRepository.save(participacao);

        return contextoSalvo;
    }
}
