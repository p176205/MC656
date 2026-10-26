package com.MC656.backend.repository;

import com.MC656.backend.entity.Voto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Acesso a urna anonima
public interface VotoRepository extends JpaRepository<Voto, Long> {

    List<Voto> findByVotacaoId(Long votacaoId);
}
