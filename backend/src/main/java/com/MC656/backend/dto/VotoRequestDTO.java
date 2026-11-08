package com.MC656.backend.dto;

import com.MC656.backend.entity.OpcaoVoto;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

/* Enquanto nao houver autenticacao no endpoint de voto, o id do eleitor vem no corpo da requisicao.
   - Em DELIBERACAO: informa opcao (SIM, NAO ou ABSTENCAO) e chapaId = null
   - Em ELEICAO: informa chapaId (voto na chapa) OU opcao = ABSTENCAO (voto em branco/abstencao) */
public record VotoRequestDTO(
        @NotNull(message = "O usuário é obrigatório")
        Long usuarioId,

        OpcaoVoto opcao,

        Long chapaId
) {
    @AssertTrue(message = "Informe exatamente uma escolha: opção de voto ou chapa")
    public boolean isEscolhaValida() {
        return (opcao != null && chapaId == null) || (opcao == null && chapaId != null);
    }
}
