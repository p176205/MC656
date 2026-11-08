package com.MC656.backend.dto;

import com.MC656.backend.entity.OpcaoVoto;
import jakarta.validation.constraints.NotNull;

/* Enquanto nao houver autenticacao, o id do eleitor vem no corpo da requisicao.
   Quando houver login, ele deve sair daqui e vir do usuario autenticado. */
public record VotoRequestDTO(
        @NotNull(message = "O usuário é obrigatório")
        Long usuarioId,

        @NotNull(message = "A opção de voto é obrigatória")
        OpcaoVoto opcao
) {}
