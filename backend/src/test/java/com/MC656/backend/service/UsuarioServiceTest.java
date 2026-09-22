package com.MC656.backend.service;

import com.MC656.backend.dto.UsuarioCreateDTO;
import com.MC656.backend.dto.UsuarioResponseDTO;
import com.MC656.backend.entity.Usuario;
import com.MC656.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void testCadastrar_Sucesso() {
        UsuarioCreateDTO dto = new UsuarioCreateDTO("Maria", "maria@email.com", "senha123");
        Usuario usuarioSalvo = new Usuario("Maria", "maria@email.com", "senha123");
        usuarioSalvo.setId(1L);

        when(usuarioRepository.existsByEmail("maria@email.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioSalvo);

        UsuarioResponseDTO response = usuarioService.cadastrar(dto);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Maria", response.nome());
        assertEquals("maria@email.com", response.email());

        // Confirma que a checagem de duplicidade foi feita com o email certo
        verify(usuarioRepository).existsByEmail("maria@email.com");

        // Confirma O QUE foi salvo, nao so que save() foi chamado
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());

        Usuario usuarioCapturado = captor.getValue();
        assertEquals("Maria", usuarioCapturado.getNome());
        assertEquals("maria@email.com", usuarioCapturado.getEmail());
    }

    @Test
    void testCadastrar_EmailJaExistente() {
        UsuarioCreateDTO dto = new UsuarioCreateDTO("Maria", "maria@email.com", "senha123");

        when(usuarioRepository.existsByEmail("maria@email.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> usuarioService.cadastrar(dto));

        verify(usuarioRepository).existsByEmail("maria@email.com");
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
}