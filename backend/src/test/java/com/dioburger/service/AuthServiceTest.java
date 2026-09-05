package com.dioburger.service;

import com.dioburger.model.dto.JwtResponseDTO;
import com.dioburger.model.dto.LoginDTO;
import com.dioburger.model.entity.Local;
import com.dioburger.model.entity.Usuario;
import com.dioburger.model.enums.Rol;
import com.dioburger.repository.UsuarioRepository;
import com.dioburger.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para AuthService.
 * Valida el proceso de autenticación y generación de tokens JWT.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de AuthService")
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private LoginDTO loginDTO;
    private Usuario usuarioAdmin;
    private Usuario usuarioSuperadmin;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        // Login DTO
        loginDTO = new LoginDTO();
        loginDTO.setUsername("admin@dioburger.com");
        loginDTO.setPassword("password123");

        // Local para usuario Admin
        Local local = Local.builder()
                .id(1L)
                .nombre("Sucursal Centro")
                .telefono("549349366512")
                .build();

        // Usuario Admin con local
        usuarioAdmin = Usuario.builder()
                .id(1L)
                .username("admin@dioburger.com")
                .password("$2a$10$encodedPassword")
                .rol(Rol.ROLE_ADMIN)
                .local(local)
                .build();

        // Usuario Superadmin sin local
        usuarioSuperadmin = Usuario.builder()
                .id(2L)
                .username("superadmin@dioburger.com")
                .password("$2a$10$encodedPassword")
                .rol(Rol.ROLE_SUPERADMIN)
                .local(null)
                .build();

        // UserDetails para Spring Security
        userDetails = User.builder()
                .username("admin@dioburger.com")
                .password("$2a$10$encodedPassword")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .build();
    }

    @Test
    @DisplayName("login - Credenciales válidas de usuario ADMIN con local")
    void testLogin_CredencialesValidasAdmin_RetornaTokenConTelefonoLocal() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(usuarioRepository.findByUsername("admin@dioburger.com"))
                .thenReturn(Optional.of(usuarioAdmin));
        when(jwtTokenProvider.generateToken(userDetails))
                .thenReturn("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token");

        // Act
        JwtResponseDTO response = authService.login(loginDTO);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token");
        assertThat(response.getType()).isEqualTo("Bearer");
        assertThat(response.getUsername()).isEqualTo("admin@dioburger.com");
        assertThat(response.getRol()).isEqualTo("ROLE_ADMIN");
        assertThat(response.getTelefonoLocal()).isEqualTo("549349366512");

        // Verify interactions
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(usuarioRepository).findByUsername("admin@dioburger.com");
        verify(jwtTokenProvider).generateToken(userDetails);
    }

    @Test
    @DisplayName("login - Credenciales válidas de SUPERADMIN sin local")
    void testLogin_CredencialesValidasSuperadmin_RetornaTelefonoLocalNull() {
        // Arrange
        UserDetails superadminDetails = User.builder()
                .username("superadmin@dioburger.com")
                .password("$2a$10$encodedPassword")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPERADMIN")))
                .build();

        LoginDTO superadminLogin = new LoginDTO();
        superadminLogin.setUsername("superadmin@dioburger.com");
        superadminLogin.setPassword("superpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(superadminDetails);
        when(usuarioRepository.findByUsername("superadmin@dioburger.com"))
                .thenReturn(Optional.of(usuarioSuperadmin));
        when(jwtTokenProvider.generateToken(superadminDetails))
                .thenReturn("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.superadmin.token");

        // Act
        JwtResponseDTO response = authService.login(superadminLogin);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.superadmin.token");
        assertThat(response.getRol()).isEqualTo("ROLE_SUPERADMIN");
        assertThat(response.getTelefonoLocal()).isNull(); // SUPERADMIN no tiene local
    }

    @Test
    @DisplayName("login - Credenciales inválidas lanza BadCredentialsException")
    void testLogin_CredencialesInvalidas_LanzaBadCredentialsException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginDTO))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Credenciales inválidas");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(usuarioRepository, never()).findByUsername(anyString());
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    @DisplayName("login - Usuario no existe en BD lanza UsernameNotFoundException")
    void testLogin_UsuarioNoExisteEnBD_LanzaUsernameNotFoundException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(usuarioRepository.findByUsername("admin@dioburger.com"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginDTO))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(usuarioRepository).findByUsername("admin@dioburger.com");
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    @DisplayName("login - Password vacío lanza BadCredentialsException")
    void testLogin_PasswordVacio_LanzaBadCredentialsException() {
        // Arrange
        LoginDTO loginInvalido = new LoginDTO();
        loginInvalido.setUsername("admin@dioburger.com");
        loginInvalido.setPassword("");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginInvalido))
                .isInstanceOf(BadCredentialsException.class);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("login - Usuario de COCINA puede autenticarse")
    void testLogin_UsuarioCocina_RetornaTokenExitosamente() {
        // Arrange
        Usuario usuarioCocina = Usuario.builder()
                .id(3L)
                .username("cocina@dioburger.com")
                .password("$2a$10$encodedPassword")
                .rol(Rol.ROLE_COCINA)
                .local(usuarioAdmin.getLocal())
                .build();

        UserDetails cocinaDetails = User.builder()
                .username("cocina@dioburger.com")
                .password("$2a$10$encodedPassword")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_COCINA")))
                .build();

        LoginDTO cocinaLogin = new LoginDTO();
        cocinaLogin.setUsername("cocina@dioburger.com");
        cocinaLogin.setPassword("cocinapass");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(cocinaDetails);
        when(usuarioRepository.findByUsername("cocina@dioburger.com"))
                .thenReturn(Optional.of(usuarioCocina));
        when(jwtTokenProvider.generateToken(cocinaDetails))
                .thenReturn("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.cocina.token");

        // Act
        JwtResponseDTO response = authService.login(cocinaLogin);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getRol()).isEqualTo("ROLE_COCINA");
        assertThat(response.getTelefonoLocal()).isEqualTo("549349366512");
    }

    @Test
    @DisplayName("login - Verifica que se crea UsernamePasswordAuthenticationToken correctamente")
    void testLogin_CreaAuthenticationTokenConCredencialesCorrectas() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(usuarioRepository.findByUsername(anyString()))
                .thenReturn(Optional.of(usuarioAdmin));
        when(jwtTokenProvider.generateToken(any()))
                .thenReturn("token");

        // Act
        authService.login(loginDTO);

        // Assert
        verify(authenticationManager).authenticate(
                argThat(token -> 
                        token instanceof UsernamePasswordAuthenticationToken &&
                        token.getPrincipal().equals("admin@dioburger.com") &&
                        token.getCredentials().equals("password123")
                )
        );
    }

    @Test
    @DisplayName("login - Token JWT generado tiene formato Bearer")
    void testLogin_TokenTieneTipoBearer() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(usuarioRepository.findByUsername(anyString()))
                .thenReturn(Optional.of(usuarioAdmin));
        when(jwtTokenProvider.generateToken(any()))
                .thenReturn("jwt.token.here");

        // Act
        JwtResponseDTO response = authService.login(loginDTO);

        // Assert
        assertThat(response.getType()).isEqualTo("Bearer");
    }

    @Test
    @DisplayName("login - Username es case-sensitive")
    void testLogin_UsernameCaseSensitive() {
        // Arrange
        LoginDTO loginMayusculas = new LoginDTO();
        loginMayusculas.setUsername("ADMIN@DIOBURGER.COM");
        loginMayusculas.setPassword("password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginMayusculas))
                .isInstanceOf(BadCredentialsException.class);
    }
}
