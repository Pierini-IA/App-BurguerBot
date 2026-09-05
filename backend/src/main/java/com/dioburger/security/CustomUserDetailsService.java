package com.dioburger.security;

import com.dioburger.model.entity.Usuario;
import com.dioburger.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;

/**
 * Servicio personalizado para cargar detalles de usuario desde la base de datos.
 * Implementa UserDetailsService de Spring Security.
 * 
 * @author Dio Burger Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Carga un usuario por su username.
     * Este método es llamado por Spring Security durante la autenticación.
     * 
     * @param username Username del usuario
     * @return UserDetails con la información del usuario
     * @throws UsernameNotFoundException Si el usuario no existe
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Cargando usuario: {}", username);

        // Buscar usuario en la base de datos
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Usuario no encontrado: {}", username);
                    return new UsernameNotFoundException(
                            "Usuario no encontrado con username: " + username
                    );
                });

        log.debug("Usuario encontrado: {} con rol: {}", username, usuario.getRol());

        // Convertir el usuario de la BD a UserDetails de Spring Security
        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword()) // Ya debe estar hasheada con BCrypt
                .authorities(getAuthorities(usuario))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

    /**
     * Obtiene las autoridades (roles) del usuario.
     * 
     * @param usuario Usuario de la base de datos
     * @return Colección de autoridades
     */
    private Collection<? extends GrantedAuthority> getAuthorities(Usuario usuario) {
        // Convertir el enum Rol a GrantedAuthority
        String rol = usuario.getRol().name(); // ROLE_ADMIN o ROLE_COCINA
        
        log.debug("Asignando rol {} al usuario {}", rol, usuario.getUsername());
        
        return Collections.singletonList(new SimpleGrantedAuthority(rol));
    }
}
