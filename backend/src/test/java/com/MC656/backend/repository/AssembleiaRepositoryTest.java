package com.MC656.backend.repository;

// Bibliotecas para os testes automáticos
import static org.junit.jupiter.api.Assertions.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.MC656.backend.entity.*;

@SpringBootTest 
public class AssembleiaRepositoryTest {
    
    @Autowired 
    private AssembleiaRepository assembleiaRepository;

    // TESTE: Após a criação de uma assembleia no banco de dados, verifica se ela persiste em buscas futuras
    @Test
    void devePersistirAssembleia(){

        // Cria uma assembleia genérica e salva ela no banco de dados
        Assembleia assembleia_teste = new Assembleia("Assembleia teste", 10);
        assembleia_teste.adicionaPauta("Pauta 1");
        assembleia_teste.adicionaPauta("Pauta 2");

        Assembleia assembleia_salva = assembleiaRepository.save(assembleia_teste);

        // O banco deve gerar um ID não nulo para a assembleia
        assertNotNull(assembleia_salva.getId());

        // Busca a assembleia salva no banco de dados
        Optional<Assembleia> resultado = assembleiaRepository.findById(assembleia_salva.getId());
        assertNotNull(resultado.orElse(null));

        // Salva a assembleia encontrada, para verificar se os dados estão corretos
        Assembleia assembleia_encontrada = resultado.orElseThrow();

        // Verifica se os dados foram preservados
        assertEquals("Assembleia teste", assembleia_encontrada.getNome());
        assertEquals(TipoContexto.ASSEMBLEIA, assembleia_encontrada.getTipo());
        assertEquals(10, assembleia_encontrada.getParticipantes());
        assertEquals(2, assembleia_encontrada.getPautas().size());
        assertEquals("Pauta 1", assembleia_encontrada.getPautas().get(0));
        assertEquals("Pauta 2", assembleia_encontrada.getPautas().get(1));
    }
}


