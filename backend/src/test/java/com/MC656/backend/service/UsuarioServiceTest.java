package com.MC656.backend.service;

import com.MC656.backend.dto.UsuarioCreateDTO;
import com.MC656.backend.dto.UsuarioLoginDTO;
import com.MC656.backend.dto.UsuarioResponseDTO;
import com.MC656.backend.dto.UsuarioResponseLoginDTO;
import com.MC656.backend.entity.Usuario;
import com.MC656.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    // ---------- cadastrar ----------

    @Test
    void testCadastrar_Sucesso() {
        UsuarioCreateDTO dto = new UsuarioCreateDTO("Maria", "maria@email.com", "senha123");
        Usuario usuarioSalvo = new Usuario("Maria", "maria@email.com", "senha123");
        usuarioSalvo.setId(1L);

        when(usuarioRepository.existsByEmail("maria@email.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("hash-bcrypt");
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
        assertEquals("hash-bcrypt", usuarioCapturado.getSenha());
        verify(passwordEncoder).encode("senha123");
    }

    @Test
    void testCadastrar_EmailJaExistente() {
        UsuarioCreateDTO dto = new UsuarioCreateDTO("Maria", "maria@email.com", "senha123");

        when(usuarioRepository.existsByEmail("maria@email.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> usuarioService.cadastrar(dto));

        verify(usuarioRepository).existsByEmail("maria@email.com");
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void testCadastrar_SenhaNaoEhSalvaEmTextoPuro() {
        PasswordEncoder encoderReal = new BCryptPasswordEncoder();
        UsuarioService servicoComBCrypt = new UsuarioService(usuarioRepository, encoderReal);
        UsuarioCreateDTO dto = new UsuarioCreateDTO("Maria", "maria@email.com", "senha123");

        when(usuarioRepository.existsByEmail("maria@email.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        servicoComBCrypt.cadastrar(dto);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        String senhaSalva = captor.getValue().getSenha();

        assertNotEquals("senha123", senhaSalva);
        assertTrue(encoderReal.matches("senha123", senhaSalva));
    }

    //  login 
    

    private Usuario usuarioComSenhaCodificada(long id, String senhaCodificada) {
        Usuario u = new Usuario("João", "joao@email.com", senhaCodificada);
        u.setId(id);
        return u;
    }

    @Test
    void testLogin_Sucesso_RetornaIdDoUsuario() {
        Usuario u = usuarioComSenhaCodificada(1L, "hash123");
        when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("senha123", "hash123")).thenReturn(true);

        UsuarioResponseLoginDTO resposta =
                usuarioService.login(new UsuarioLoginDTO("joao@email.com", "senha123"));

        assertEquals(1L, resposta.id());
    }

    @Test
    void testLogin_EmailInexistente() {
        when(usuarioRepository.findByEmail("x@email.com")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> usuarioService.login(new UsuarioLoginDTO("x@email.com", "senha123")));

        assertEquals("E-mail ou senha inválidos", ex.getMessage());
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void testLogin_SenhaIncorreta() {
        Usuario u = usuarioComSenhaCodificada(1L, "hash123");
        when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("errada", "hash123")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> usuarioService.login(new UsuarioLoginDTO("joao@email.com", "errada")));

        assertEquals("E-mail ou senha inválidos", ex.getMessage());
    }

    @Test
    void testLogin_MesmaMensagemParaEmailInexistenteESenhaErrada() {
        when(usuarioRepository.findByEmail("x@email.com")).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail("joao@email.com"))
                .thenReturn(Optional.of(usuarioComSenhaCodificada(1L, "hash123")));
        when(passwordEncoder.matches("errada", "hash123")).thenReturn(false);

        String msgEmail = assertThrows(IllegalArgumentException.class,
                () -> usuarioService.login(new UsuarioLoginDTO("x@email.com", "senha123"))).getMessage();
        String msgSenha = assertThrows(IllegalArgumentException.class,
                () -> usuarioService.login(new UsuarioLoginDTO("joao@email.com", "errada"))).getMessage();

        // p não revelar se o email existe ou não
        assertEquals(msgEmail, msgSenha);
    }

    @Test
    void testLogin_ComBCryptReal() {
        PasswordEncoder encoderReal = new BCryptPasswordEncoder();
        UsuarioService servicoComBCrypt = new UsuarioService(usuarioRepository, encoderReal);
        Usuario u = usuarioComSenhaCodificada(1L, encoderReal.encode("senha123"));
        when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(u));

        assertEquals(1L, servicoComBCrypt.login(new UsuarioLoginDTO("joao@email.com", "senha123")).id());
        assertThrows(IllegalArgumentException.class,
                () -> servicoComBCrypt.login(new UsuarioLoginDTO("joao@email.com", "errada")));
    }

    // getUser 

    @Test
    void testGetUser_Sucesso() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioComSenhaCodificada(1L, "hash123")));

        UsuarioResponseDTO resposta = usuarioService.getUser(1L);

        assertEquals(1L, resposta.id());
        assertEquals("João", resposta.nome());
        assertEquals("joao@email.com", resposta.email());
    }

    @Test
    void testGetUser_UsuarioNaoEncontrado() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> usuarioService.getUser(99L));

        assertEquals("Usuário não encontrado", ex.getMessage());
    }
}