package com.MC656.backend.entity;

// Bibliotecas para os teste automáticos
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class CondominioTest {

    // TESTE 1: Verifica se a criação de um novo condominio é feita corretamente (Nome, tipo e número de moradores válidos)
    @Test
    void deveCriarCondominioComDadosValido(){

        Condominio condominio_teste = new Condominio("Condomínio Teste", 10);

        assertEquals("Condomínio Teste", condominio_teste.getNome());
        assertEquals(TipoContexto.CONDOMINIO, condominio_teste.getTipo());
        assertEquals(10, condominio_teste.getTotalMoradores());
    }

    // TESTE 2: Não permite a criação de um condomínio com uma quantidade inválida de moradores
    @Test
    void naoDevePermitirZeroOuMenosMoradores(){

        // 2.1: total_moradores == 0 (inválido)
        assertThrows(IllegalArgumentException.class, () -> new Condominio("Condomínio Teste", 0));

        // 2.2: total_moradores == -10 (inválido)
        assertThrows(IllegalArgumentException.class, () -> new Condominio("Condominio Teste", -10));
    }

    // TESTE 3: Permite a modificação do número de moradores de um condominio para outro número válido
    @Test
    void devePermitirAlterarTotalMoradores(){

        Condominio condominio_teste = new Condominio("Condomínio Teste", 10);

        condominio_teste.setTotalMoradores(20);
        assertEquals(20, condominio_teste.getTotalMoradores());
    }

    // TESTE 4: Não permite alterar a quantidade de moradores para um valor inválido
    @Test
    void naoDevePermitirAlterarParaZeroOuMenosMoradores(){

        Condominio condominio_teste = new Condominio("Condomínio Teste", 10);

        // 4.1: total_moradores == 0 (inválido)
        assertThrows(IllegalArgumentException.class, () -> condominio_teste.setTotalMoradores(0));
        assertEquals(10, condominio_teste.getTotalMoradores());

        // 4.2: total_moradores == -10 (inválido)
        assertThrows(IllegalArgumentException.class, () -> condominio_teste.setTotalMoradores(-10));
        assertEquals(10, condominio_teste.getTotalMoradores());
    }
}