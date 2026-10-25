package com.MC656.backend.repository;

import com.MC656.backend.entity.Participacao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ParticipacaoRepository extends JpaRepository<Participacao, Long> {

    List<Participacao> findByUsuarioId(Long usuarioId);

    List<Participacao> findByUsuarioIdAndContextoId(Long usuarioId, Long contextoId);

    List<Participacao> findByContextoId(Long contextoId);
}