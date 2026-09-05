# 🧪 Guía de Testing

**Versión**: 2.2.0  
**Tests**: 66 tests | 100% passing

---

## 📊 Resumen de Tests

```
╔════════════════════════════════════════╗
║   TESTS EJECUTADOS:       66 / 66     ║
║   TESTS EXITOSOS:         66 ✅       ║
║   TESTS FALLIDOS:          0          ║
║   TASA DE ÉXITO:         100%         ║
║   TIEMPO:                ~25s         ║
╚════════════════════════════════════════╝
```

---

## 📑 Suites de Tests

### 1. PlanServiceTest (13 tests) ✅
**Propósito:** Validar lógica de planes de suscripción

| Test | Descripción |
|------|-------------|
| `basico_tieneAcceso_panelWeb` | BÁSICO tiene acceso a PANEL_WEB |
| `basico_noTieneAcceso_botWhatsapp` | BÁSICO NO tiene acceso a BOT_WHATSAPP |
| `estandar_tieneAcceso_botWhatsapp` | ESTÁNDAR tiene acceso a BOT_WHATSAPP |
| `premium_tieneAcceso_todasLasFeatures` | PREMIUM tiene acceso a todo |
| ... | (9 tests más) |

### 2. LocalServiceTest (10 tests) ✅
**Propósito:** Validar método DRY `buscarPorTelefono()`

| Test | Descripción |
|------|-------------|
| `buscarPorTelefono_localExiste_retornaLocal` | Happy path |
| `buscarPorTelefono_localNoExiste_lanzaNotFoundException` | Error handling |
| `buscarPorTelefono_telefonoConMas_funciona` | Formato +549... |
| ... | (7 tests más) |

### 3. PlanValidationAspectTest (7 tests) ✅
**Propósito:** Validar aspecto AOP para @RequiresFeature

| Test | Descripción |
|------|-------------|
| `accesoPermitido_ejecutaMetodo` | Ejecuta método si tiene acceso |
| `accesoDenegado_lanzaAccesoDenegadoException` | Lanza excepción si no tiene acceso |
| ... | (5 tests más) |

### 4. UsuarioCreateDTOTest (20 tests) ✅
**Propósito:** Validación de Bean Validation API

| Test | Descripción |
|------|-------------|
| `username_notBlank_required` | Username obligatorio |
| `username_size_min4` | Username mínimo 4 caracteres |
| `password_size_min8` | Password mínimo 8 caracteres |
| `rol_notNull_required` | Rol obligatorio |
| `telefonoLocal_pattern_valid` | Formato +549... |
| ... | (15 tests más) |

### 5. PedidoServiceTest (16 tests) ✅
**Propósito:** Validar lógica de negocio de pedidos

| Test | Descripción |
|------|-------------|
| `crearPedido_happyPath_success` | Flujo completo exitoso |
| `crearPedido_requestIdDuplicado_retornaPedidoExistente` | Idempotencia |
| `crearPedido_modalidadDelivery_solicitaRepartidor` | DELIVERY solicita repartidor |
| `crearPedido_stockInsuficiente_lanzaException` | Validación de stock |
| ... | (12 tests más) |

---

## 🚀 Comandos de Ejecución

### Ejecutar todos los tests
```bash
mvn clean test
```

### Ejecutar tests específicos
```bash
# Por clase
mvn test -Dtest=PlanServiceTest
mvn test -Dtest=PedidoServiceTest

# Por patrón
mvn test -Dtest="*ServiceTest"
```

### Generar reporte de cobertura
```bash
mvn clean test jacoco:report

# Abrir reporte
start target/site/jacoco/index.html  # Windows
open target/site/jacoco/index.html   # Mac
```

---

## 📈 Cobertura de Código

**Framework:** JaCoCo 0.8.11

**Módulos con cobertura:**
- ✅ `com.dioburger.service` - Servicios de negocio
- ✅ `com.dioburger.aspect` - Aspectos AOP
- ✅ `com.dioburger.model.dto` - DTOs con validación
- ✅ `com.dioburger.mapper` - MapStruct mappers

---

## 🛠️ Tecnologías de Testing

| Tecnología | Uso |
|------------|-----|
| **JUnit 5** | Framework de testing |
| **Mockito** | Mocking de dependencias |
| **Hibernate Validator** | Validación de DTOs |
| **Spring Boot Test** | Integración con Spring |
| **JaCoCo** | Cobertura de código |

---

## 📝 Patrones de Testing

### Arrange-Act-Assert (AAA)
```java
@Test
void buscarPorTelefono_localExiste_retornaLocal() {
    // Arrange
    Local local = new Local();
    when(repository.findByTelefono(telefono)).thenReturn(Optional.of(local));
    
    // Act
    Local result = service.buscarPorTelefono(telefono);
    
    // Assert
    assertNotNull(result);
    assertEquals(local, result);
}
```

### Verificación de Interacciones
```java
@Test
void crearPedido_notificaWebSocket() {
    // Act
    service.crearPedido(dto);
    
    // Assert
    verify(webSocketService, times(1)).emitirPedido(any());
}
```

---

## 🎯 Próximos Tests

### Pendientes
- [ ] ReservaServiceTest
- [ ] StockServiceTest
- [ ] WebSocketServiceTest
- [ ] Controllers (integration tests)

### Mejoras
- [ ] Aumentar cobertura en controllers
- [ ] Agregar tests de integración con DB
- [ ] Tests E2E con Testcontainers

---

## 📊 Historial de Tests

| Versión | Tests | Passing | Cobertura |
|---------|-------|---------|-----------|
| v2.2.0 | 66 | 100% | ~60% |
| v2.1.0 | 30 | 100% | ~45% |
| v2.0.0 | 20 | 100% | ~35% |

---

*Última actualización: Octubre 2025*
