package com.MC656.backend.repository;

// Bibliotecas para os testes automáticos
import static org.junit.jupiter.api.Assertions.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.MC656.backend.entity.CentroAcademico;
import com.MC656.backend.entity.TipoContexto;

@SpringBootTest
class CentroAcademicoRepositoryTest {

    @Autowired
    private CentroAcademicoRepository centroAcademicoRepository;

    // TESTE: Após a criação de um centro acadêmico no banco de dados, verifica se ele persiste em buscas futuras
    @Test
    void devePersistirCentroAcademico(){

        // Cria um Centro Acadêmico genérico e salva ele no banco de dados
        CentroAcademico ca_teste = new CentroAcademico("Centro acadêmico teste", 2);
        CentroAcademico ca_salvo = centroAcademicoRepository.save(ca_teste);

        // O banco deve gerar um ID não nulo para o centro acadêmico
        assertNotNull(ca_salvo.getId());

        // Busca o Centro Acadêmico salvo no banco de dados
        Optional<CentroAcademico> resultado = centroAcademicoRepository.findById(ca_salvo.getId());
        assertNotNull(resultado.orElse(null));

        // Salva o Centro Acadêmico encontrado, para verificar se os dados estão corretos
        CentroAcademico ca_encontrado = resultado.orElseThrow();

        // Verifica se os dados foram preservados
        assertEquals("Centro acadêmico teste", ca_encontrado.getNome());
        assertEquals(TipoContexto.CENTRO_ACADEMICO, ca_encontrado.getTipo());
        assertEquals(2, ca_encontrado.getTempoVigencia());
    }
}