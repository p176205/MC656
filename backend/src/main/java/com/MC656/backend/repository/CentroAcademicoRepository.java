package com.MC656.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.MC656.backend.entity.CentroAcademico;

// Permite a utilização de métodos do banco de dados para a manipulação de entidades da classe "CentroAcademico"
@Repository
public interface CentroAcademicoRepository extends JpaRepository<CentroAcademico, Long> {
    
}