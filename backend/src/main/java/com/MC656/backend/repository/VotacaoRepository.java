package com.MC656.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.MC656.backend.entity.Votacao;

public interface VotacaoRepository extends JpaRepository<Votacao, Long> {

    List<Votacao> findByContextoId(Long contextoId);
}
