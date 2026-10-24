package com.MC656.backend.repository;

import com.MC656.backend.entity.Votacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VotacaoRepository extends JpaRepository<Votacao, Long> {

    List<Votacao> findByContextoId(Long contextoId);
}
