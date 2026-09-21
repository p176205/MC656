package com.MC656.backend.entity;

// Bibliotecas para os testes automáticos
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class AssembleiaTest {
    
    // TESTE 1: Verifica se a criação de uma nova assembleia é feita corretamente (Nome, tipo, número de participantes e lista de pautas válidos)
    @Test
    void deveCriarAssembleiaComDadosValidos(){

        Assembleia assembleia_teste = new Assembleia("Assembleia teste", 10);

        assertEquals("Assembleia teste", assembleia_teste.getNome());
        assertEquals(TipoContexto.ASSEMBLEIA, assembleia_teste.getTipo());
        assertEquals(10, assembleia_teste.getParticipantes());
        assertEquals(0, assembleia_teste.getPautas().size());
    }

    // TESTE 2: Não permite a criação de uma assembleia com uma quantidade inválida de participantes
    @Test
    void naoDevePermitirZeroOuMenosParticipantes(){

        // 2.1: partipantes == 0 (inválido)
        assertThrows(IllegalArgumentException.class, () -> new Assembleia("Assembleia teste", 0));

        // 2.2: participantes == -10 (inválido)
        assertThrows(IllegalArgumentException.class, () -> new Assembleia("Assembleia teste", -10));
    }

    // TESTE 3: Permite a modificação do número de participantes de uma assembleia para outro número válido
    @Test
    void devePermitirAlterarParticipantes(){

        Assembleia assembleia_teste = new Assembleia("Assembleia teste", 10);

        assembleia_teste.setParticipantes(20);
        assertEquals(20, assembleia_teste.getParticipantes());
    }

    // TESTE 4: Não permite alterar a quantidade de participantes para um valor inválido
    @Test
    void naoDevePermitirAlterarParaZeroOuMenosParticipantes(){

        Assembleia assembleia_teste = new Assembleia("Assembleia teste", 10);

        // 4.1: participantes == 0 (inválido)
        assertThrows(IllegalArgumentException.class, () -> assembleia_teste.setParticipantes(0));
        assertEquals(10, assembleia_teste.getParticipantes());

        // 4.2: participantes == -10 (inválido)
        assertThrows(IllegalArgumentException.class, () -> assembleia_teste.setParticipantes(-10));
        assertEquals(10, assembleia_teste.getParticipantes());
    }

    // TESTE 5: Permite adicionar novas pautas à assembleia
    @Test
    void devePermitirAdicionarPauta(){

        Assembleia assembleia_teste = new Assembleia("Assembleia teste", 10);

        assembleia_teste.adicionaPauta("Nova pauta");
        assertEquals(1, assembleia_teste.getPautas().size());
        assertEquals("Nova pauta", assembleia_teste.getPautas().get(0));
    }

}