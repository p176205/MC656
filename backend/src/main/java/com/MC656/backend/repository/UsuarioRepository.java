package com.MC656.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.MC656.backend.entity.Usuario;

// Faz a comunicação com  o banco
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // extends JpaRepository<Usuario, Long> "siz ao Spring" que queremos manipular objetos Usuario cujo id é do tipo Long
    // Note que estamos herdando de uma classe. Essa classe já possui os seguintes metodos: 
    // - save() // inserir no banco
    // - findAll()
    // - findById()
    // - delete()
    // - deleteById()
    
    boolean existsByEmail(String email);
}