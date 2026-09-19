package com.MC656.backend.repository;

import com.MC656.backend.entity.Participacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParticipacaoRepository extends JpaRepository<Participacao, Long> {

    List<Participacao> findByUsuarioId(Long usuarioId);
}