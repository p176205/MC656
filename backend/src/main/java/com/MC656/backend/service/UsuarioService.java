package com.MC656.backend.service;

import org.springframework.stereotype.Service;

import com.MC656.backend.dto.UsuarioRequest;
import com.MC656.backend.entity.Usuario;
import com.MC656.backend.repository.UsuarioRepository;

@Service // Aqui conterá a lógica relacionada ao usuario
// Pegamos a requisição do front (usuarioRequest), transformamos para um objeto Usuario e salvamos no banco (usuarioRepository.save(usuario))
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario salvar(UsuarioRequest request) {

        Usuario usuario = new Usuario(request.getNome(), request.getEmail());

        return usuarioRepository.save(usuario);
    }
}
