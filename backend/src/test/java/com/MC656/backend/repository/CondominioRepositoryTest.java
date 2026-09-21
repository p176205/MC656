package com.MC656.backend.repository;

// Bibliotecas para os testes automáticos
import static org.junit.jupiter.api.Assertions.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.MC656.backend.entity.Condominio;
import com.MC656.backend.entity.TipoContexto;

@SpringBootTest
class CondominioRepositoryTest {

    @Autowired
    private CondominioRepository condominioRepository;

    // TESTE: Após a criação de um condomínio no banco de dados, verifica se ele persiste em buscas futuras
    @Test
    void devePersistirCondominio(){

        // Cria um condomínio genérico e salva ele no bancon de dados
        Condominio condominio_teste = new Condominio("Condomínio Teste",10);
        Condominio condominio_salvo = condominioRepository.save(condominio_teste);

        // O banco deve gerar um ID não nulo para o condomínio
        assertNotNull(condominio_salvo.getId());

        // Busca o condomínio salvo no banco de dados
        Optional<Condominio> resultado = condominioRepository.findById(condominio_salvo.getId());
        assertNotNull(resultado.orElse(null));

        // Salva o condomínio encontrado, para verificar se os dados estão corretos
        Condominio condominio_encontrado = resultado.orElseThrow();

        // Verifica se os dados foram preservados
        assertEquals("Condomínio Teste", condominio_encontrado.getNome());
        assertEquals(TipoContexto.CONDOMINIO, condominio_encontrado.getTipo());
        assertEquals(10, condominio_encontrado.getTotalMoradores());
    }
}
