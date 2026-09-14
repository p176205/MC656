package com.MC656.backend.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.MC656.backend.dto.UsuarioRequest;
import com.MC656.backend.entity.Usuario;
import com.MC656.backend.service.UsuarioService;

// Aqui é o endpoint (rota) que o front vai usar para mandar e pegar dados
@RestController // Diz que essa classe possui endpoints HTTP da minha API.
@RequestMapping ("/usuarios") // endereco base
@CrossOrigin (origins = "http://localhost:3000")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping 
    public Usuario cadastrar(@RequestBody UsuarioRequest request) {// Pegamos o JSON vindo da requisicao do front

        return usuarioService.salvar(request);
    }
}