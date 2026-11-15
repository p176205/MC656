package com.MC656.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.MC656.backend.entity.Usuario;

// Faz a comunicação com  o banco
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // extends JpaRepository<Usuario, Long> "siz ao Spring" que queremos manipular
    // objetos Usuario cujo id é do tipo Long
    // Note que estamos herdando de uma classe. Essa classe já possui os seguintes
    // metodos:
    // - save() // inserir no banco
    // - findAll()
    // - delete()
    // - deleteById()

    boolean existsByEmail(String email);

    // Busca o usuário completo pelo e-mail
    // Usamos Optional porque o e-mail pode não existir no banco
    Optional<Usuario> findByEmail(String email);

    // Busca o usuário completo pelo id
    // Usamos Optional porque o id pode não existir no banco
    Optional<Usuario> findById(Long id);
}