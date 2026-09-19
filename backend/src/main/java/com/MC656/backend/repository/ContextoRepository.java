package com.MC656.backend.repository;

import com.MC656.backend.entity.Contexto;
import org.springframework.data.jpa.repository.JpaRepository;

// provisorio, so p a issue 6 funcionar, quando a 8 for criada, so apagar
public interface ContextoRepository extends JpaRepository<Contexto, Long> {
}