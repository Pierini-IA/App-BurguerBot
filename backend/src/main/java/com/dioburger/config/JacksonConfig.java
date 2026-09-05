package com.dioburger.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra el módulo de Jackson para Hibernate.
 *
 * <p>Sin esto, serializar una entidad JPA con asociaciones {@code LAZY} sin
 * inicializar explota (StackOverflow por ciclos, o error de
 * {@code ByteBuddyInterceptor}). Con el módulo, las asociaciones lazy no
 * cargadas se serializan como {@code null} en vez de forzar la carga.</p>
 *
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Module hibernate6Module() {
        Hibernate6Module module = new Hibernate6Module();
        // open-in-view está activo: forzamos la carga de asociaciones lazy al
        // serializar para que el frontend reciba categoría, receta, ítems, etc.
        // Los ciclos se cortan con @JsonIgnore / @JsonIgnoreProperties en las entidades.
        module.enable(Hibernate6Module.Feature.FORCE_LAZY_LOADING);
        return module;
    }
}
