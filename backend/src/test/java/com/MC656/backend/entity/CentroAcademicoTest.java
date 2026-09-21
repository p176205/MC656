package com.MC656.backend.entity;

// Bibliotecas para os testes automáticos
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.MC656.backend.entity.CentroAcademico;

class CentroAcademicoTest {

    // TESTE 1: Verifica se a criação de um novo centro academico é feita corretamente (Nome, tipo e período de vigência válidos)
    @Test
    void deveCriarCentroAcademicoComDadosValidos(){

        CentroAcademico ca_teste = new CentroAcademico("Centro acadêmico teste", 2);

        assertEquals("Centro acadêmico teste", ca_teste.getNome());
        assertEquals(TipoContexto.CENTRO_ACADEMICO, ca_teste.getTipo());
        assertEquals(2, ca_teste.getTempoVigencia());
    }

    // TESTE 2: Não permite a criação de um centro acadêmico com tempo de vigência inválido
    @Test
    void naoDevePermitirTempoVigenciaZeroOuMenos(){

        // 2.1: tempo_vigencia == 0 (inválido)
        assertThrows(IllegalArgumentException.class, () -> new CentroAcademico("Centro academico teste", 0));

        // 2.2: tempo_vigencia == -10 (inválido)
        assertThrows(IllegalArgumentException.class, () -> new CentroAcademico("Centro academico teste", -10));
    }
}