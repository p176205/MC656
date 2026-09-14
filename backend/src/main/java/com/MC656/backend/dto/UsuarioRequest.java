package com.MC656.backend.dto;

// Data Transfer Object. Essa classe representam os dados que entram e saem da API
// O frontend manda um JSON para noso back. Precisamos de uma classe para receber esse JSON
public class UsuarioRequest {
    private String nome;
    private String email;

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }
}
