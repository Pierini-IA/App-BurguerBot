package com.dioburger.service;

import com.dioburger.model.dto.JwtResponseDTO;
import com.dioburger.model.dto.LoginDTO;
import com.dioburger.model.entity.Usuario;
import com.dioburger.repository.UsuarioRepository;
import com.dioburger.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de autenticación.
 * Maneja el proceso de login y generación de tokens JWT.
 * 
 * @author Dio Burger Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UsuarioRepository usuarioRepository;

    /**
     * Autentica un usuario y genera un token JWT.
     * 
     * Flujo:
     * 1. Valida las credenciales con Spring Security
     * 2. Busca el usuario en la base de datos
     * 3. Genera un token JWT
     * 4. Retorna el token con información del usuario
     * 
     * @param loginDTO Credenciales del usuario
     * @return JwtResponseDTO con el token y datos del usuario
     * @throws BadCredentialsException Si las credenciales son inválidas
     * @throws UsernameNotFoundException Si el usuario no existe
     */
    @Transactional(readOnly = true)
    public JwtResponseDTO login(LoginDTO loginDTO) {
        log.info("Intento de login para usuario: {}", loginDTO.getUsername());

        try {
            // 1. Autenticar con Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDTO.getUsername(),
                            loginDTO.getPassword()
                    )
            );

            // 2. Obtener UserDetails autenticado
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // 3. Buscar usuario en la BD para obtener datos adicionales
            Usuario usuario = usuarioRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException(
                            "Usuario no encontrado: " + userDetails.getUsername()
                    ));

            // 4. Generar token JWT
            String token = jwtTokenProvider.generateToken(userDetails);

            log.info("Login exitoso para usuario: {} con rol: {}", 
                    usuario.getUsername(), 
                    usuario.getRol());

            // 5. Construir respuesta
            // SUPERADMIN no tiene local asociado
            String telefonoLocal = usuario.getLocal() != null 
                    ? usuario.getLocal().getTelefono() 
                    : null;

            return JwtResponseDTO.builder()
                    .token(token)
                    .type("Bearer")
                    .username(usuario.getUsername())
                    .rol(usuario.getRol().name())
                    .telefonoLocal(telefonoLocal)
                    .build();

        } catch (BadCredentialsException e) {
            log.warn("Login fallido para usuario: {} - Credenciales inválidas", 
                    loginDTO.getUsername());
            throw new BadCredentialsException("Credenciales inválidas");
        }
    }
}
