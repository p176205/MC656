package com.MC656.backend.repository;

import com.MC656.backend.entity.Chapa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChapaRepository extends JpaRepository<Chapa, Long> {

    List<Chapa> findByVotacaoId(Long votacaoId);

    Optional<Chapa> findByIdAndVotacaoId(Long id, Long votacaoId);

    boolean existsByVotacaoIdAndNomeIgnoreCase(Long votacaoId, String nome);
}

