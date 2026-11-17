package com.MC656.backend.service;

import com.MC656.backend.dto.UsuarioCreateDTO;
import com.MC656.backend.dto.UsuarioLoginDTO;
import com.MC656.backend.dto.UsuarioResponseDTO;
import com.MC656.backend.dto.UsuarioResponseLoginDTO;
import com.MC656.backend.entity.Usuario;
import com.MC656.backend.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UsuarioResponseDTO cadastrar(UsuarioCreateDTO dto) {
        if (usuarioRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("E-mail já cadastrado");
        }

        String senhaCodificada = passwordEncoder.encode(dto.senha());
        Usuario usuario = new Usuario(dto.nome(), dto.email(), senhaCodificada);
        usuario = usuarioRepository.save(usuario);

        return new UsuarioResponseDTO(usuario.getId(), usuario.getNome(), usuario.getEmail());
    }

    public UsuarioResponseLoginDTO login(UsuarioLoginDTO dto) {
        // 1. Busca o usuário pelo e-mail no banco de dados
        Usuario usuario = usuarioRepository.findByEmail(dto.email())
                .orElseThrow(() -> new IllegalArgumentException("E-mail ou senha inválidos"));

        // 2. Compara a senha pura informada no DTO com a senha codificada salva no
        // banco
        if (!passwordEncoder.matches(dto.senha(), usuario.getSenha())) {
            throw new IllegalArgumentException("E-mail ou senha inválidos");
        }

        // 3. Se passou pelas validações, retorna o DTO de resposta do login
        return new UsuarioResponseLoginDTO(usuario.getId());
    }

    public UsuarioResponseDTO getUser(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        return new UsuarioResponseDTO(usuario.getId(), usuario.getNome(), usuario.getEmail());
    }
}
