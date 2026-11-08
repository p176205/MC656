package com.MC656.backend.repository;

import com.MC656.backend.entity.RegistroVotante;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistroVotanteRepository extends JpaRepository<RegistroVotante, Long> {

    boolean existsByVotacaoIdAndUsuarioId(Long votacaoId, Long usuarioId);
}
