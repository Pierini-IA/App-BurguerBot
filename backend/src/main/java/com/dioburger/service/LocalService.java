package com.dioburger.service;

import com.dioburger.exception.NotFoundException;
import com.dioburger.model.entity.Local;
import com.dioburger.repository.LocalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Servicio auxiliar para operaciones comunes con locales.
 * Centraliza la lógica de búsqueda y validación de locales
 * para evitar duplicación de código (DRY).
 *
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LocalService {

    private final LocalRepository localRepository;

    /**
     * Busca un local por su teléfono único (Multi-Tenant ID).
     * Si no existe, lanza NotFoundException.
     * 
     * Este método centraliza la búsqueda de locales para evitar
     * duplicar el código de búsqueda + validación en todos los servicios.
     *
     * @param telefonoLocal Teléfono único del local
     * @return Local encontrado
     * @throws NotFoundException si el local no existe
     */
    public Local buscarPorTelefono(String telefonoLocal) {
        log.debug("Buscando local con teléfono: {}", telefonoLocal);
        
        return localRepository.findByTelefono(telefonoLocal)
                .orElseThrow(() -> new NotFoundException(
                        "Local no encontrado con teléfono: " + telefonoLocal));
    }

    /**
     * Busca un local por su teléfono, trayendo también su configuración (fetch join).
     * Usar en código que corre fuera de una request web (ej. el procesamiento en
     * segundo plano de los webhooks de Meta), donde acceder a {@code local.getConfiguracion()}
     * de forma lazy fallaría por no haber sesión de Hibernate abierta.
     *
     * @param telefonoLocal Teléfono único del local
     * @return Local con su configuración ya inicializada
     * @throws NotFoundException si el local no existe
     */
    public Local buscarPorTelefonoConConfiguracion(String telefonoLocal) {
        log.debug("Buscando local (con configuración) con teléfono: {}", telefonoLocal);

        return localRepository.findByTelefonoConConfiguracion(telefonoLocal)
                .orElseThrow(() -> new NotFoundException(
                        "Local no encontrado con teléfono: " + telefonoLocal));
    }

    /**
     * Busca un local por su teléfono y valida que exista.
     * Alias de buscarPorTelefono() para mayor claridad semántica.
     *
     * @param telefonoLocal Teléfono único del local
     * @return Local encontrado y validado
     * @throws NotFoundException si el local no existe
     */
    public Local obtenerLocal(String telefonoLocal) {
        return buscarPorTelefono(telefonoLocal);
    }

    /**
     * Verifica si un local existe por su teléfono.
     *
     * @param telefonoLocal Teléfono del local
     * @return true si existe, false si no
     */
    public boolean existe(String telefonoLocal) {
        return localRepository.findByTelefono(telefonoLocal).isPresent();
    }
}
