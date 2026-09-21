package com.MC656.backend.entity;

// Bibliotecas para os testes automáticos
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestExecutionListeners;

import static org.junit.jupiter.api.Assertions.*;

public class ContextoTest {
    
    // Classe auxiliar para permitir a realização de testes independentes para o contexto (Classe abstrata)
    static class ContextoTeste extends Contexto {

        public ContextoTeste(String nome, TipoContexto tipo){
            super(nome, tipo);
        }
    }

    // TESTE 1: Verifica se a criação de um novo contexto é feita corretamente (Nome e tipo válidos)
    @Test
    void deveCriarContextoComDadosValidos(){

        Contexto contexto_teste = new ContextoTeste("Contexto Teste", TipoContexto.CONDOMINIO);

        assertEquals("Contexto Teste", contexto_teste.getNome());
        assertEquals(TipoContexto.CONDOMINIO, contexto_teste.getTipo());
    }

    // TESTE 2: Não permite a criação de um novo contexto com o nome em branco
    @Test
    void naoDevePermitirNomeEmBranco(){

        // 2.1: Nome == null (inválido)
        assertThrows(IllegalArgumentException.class, () -> new ContextoTeste(null, TipoContexto.CONDOMINIO));

        // 2.2: Nome == "" (inválido)
        assertThrows(IllegalArgumentException.class, () -> new ContextoTeste("", TipoContexto.CONDOMINIO));

        // 2.3: Nome == "   " (inválido)
        assertThrows(IllegalArgumentException.class, () -> new ContextoTeste("   ", TipoContexto.CONDOMINIO));
    }

    // TESTE 3: Não permite a criação de um novo contexto com o tipo nulo
    @Test
    void naoDevePermitirTipoNulo() {
        assertThrows(IllegalArgumentException.class, () -> new ContextoTeste("Contexto Teste", null));
    }

    // TESTE 4: Permite a modificação do nome de um contexto para outro nome válido
    @Test
    void devePermitirAlterarNome() {

        Contexto contexto_teste = new ContextoTeste("Nome Inicial", TipoContexto.CONDOMINIO);

        contexto_teste.setNome("Novo Nome");
        assertEquals("Novo Nome", contexto_teste.getNome());
    }

    // TESTE 5: Não permite a modificação do nome de um contexto para um nome em branco
    @Test
    void naoDevePermitirAlterarNomeParaBranco(){

        Contexto contexto_teste = new ContextoTeste("Nome Inicial", TipoContexto.CONDOMINIO);

        // 2.1: Nome == null (inválido)
        assertThrows(IllegalArgumentException.class, () -> contexto_teste.setNome(null));
        assertEquals("Nome Inicial", contexto_teste.getNome());

        // 2.2: Nome == "" (inválido)
        assertThrows(IllegalArgumentException.class, () -> contexto_teste.setNome(""));
        assertEquals("Nome Inicial", contexto_teste.getNome());

        // 2.3: Nome == "   " (inválido)
        assertThrows(IllegalArgumentException.class, () -> contexto_teste.setNome("    "));
        assertEquals("Nome Inicial", contexto_teste.getNome());
    }
}
