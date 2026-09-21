package com.MC656.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.MC656.backend.entity.Assembleia;

// Permite a utilização de métodos do banco de dados para a manipulação de entidades da classe "Assembleia"
@Repository
public interface AssembleiaRepository extends JpaRepository<Assembleia, Long> {
    
}