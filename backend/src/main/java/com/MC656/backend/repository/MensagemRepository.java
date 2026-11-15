package com.MC656.backend.repository;

import com.MC656.backend.entity.Mensagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensagemRepository extends JpaRepository<Mensagem, Long>{

    // Permite recuperar mensagens de um contexto específico, em ordem de envio
    List<Mensagem> findByContextoIdOrderByDataEnvioAsc(Long contextoId);
}
