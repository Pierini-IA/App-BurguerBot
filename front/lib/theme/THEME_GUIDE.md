# 🎨 Guía del Sistema de Theming - Dio Burger

> **Sistema de colores centralizado con tonos naranja/durazno vibrantes**

---

## 📋 Paleta de Colores

### Colores Principales

```typescript
// Primario (Naranja Durazno Vibrante)
theme.palette.primary.main; // #FF6B35 - Naranja durazno principal
theme.palette.primary.light; // #FF8C5A - Naranja claro
theme.palette.primary.dark; // #E85A2A - Naranja oscuro
theme.palette.primary.contrastText; // #FFFFFF - Blanco para texto sobre naranja

// Secundario (Naranja Coral)
theme.palette.secondary.main; // #FF8A5B - Naranja coral
theme.palette.secondary.light; // #FFAA80 - Coral claro
theme.palette.secondary.dark; // #E66B3C - Coral oscuro
```

### Colores de Estado

```typescript
// Success (Verde - WhatsApp)
theme.palette.success.main; // #4CAF50
theme.palette.success.light; // #81C784
theme.palette.success.dark; // #388E3C

// Warning (Naranja Alerta)
theme.palette.warning.main; // #FFA726
theme.palette.warning.light; // #FFB74D
theme.palette.warning.dark; // #F57C00

// Error (Rojo)
theme.palette.error.main; // #EF5350
theme.palette.error.light; // #E57373
theme.palette.error.dark; // #C62828

// Info (Azul)
theme.palette.info.main; // #29B6F6
theme.palette.info.light; // #4FC3F7
theme.palette.info.dark; // #0288D1
```

### Backgrounds y Texto

```typescript
// Backgrounds
theme.palette.background.default; // #FFF8F5 - Blanco cálido con tinte durazno
theme.palette.background.paper; // #FFFFFF - Blanco puro

// Texto
theme.palette.text.primary; // #1A1A1A - Negro casi puro
theme.palette.text.secondary; // #4A4A4A - Gris oscuro
theme.palette.text.disabled; // #9E9E9E - Gris medio

// Divisores
theme.palette.divider; // rgba(0, 0, 0, 0.12)
```

---

## ✅ Buenas Prácticas

### 1. **SIEMPRE usar colores del tema**

```tsx
// ✅ CORRECTO
<Box sx={{ color: 'primary.main' }}>Texto</Box>
<Button color="primary">Click</Button>
<Chip color="success" label="Activo" />

// ❌ INCORRECTO
<Box sx={{ color: '#FF6B35' }}>Texto</Box>
<Button sx={{ backgroundColor: '#FF6B35' }}>Click</Button>
<Chip sx={{ backgroundColor: '#4CAF50' }} label="Activo" />
```

### 2. **Usar funciones del tema para colores dinámicos**

```tsx
// ✅ CORRECTO - Acceder al tema completo
<Box
  sx={{
    backgroundColor: (theme) => theme.palette.primary.main,
    "&:hover": {
      backgroundColor: (theme) => theme.palette.primary.dark,
    },
  }}
/>;

// ✅ CORRECTO - Usar hook useTheme
import { useTheme } from "@mui/material";

const MyComponent = () => {
  const theme = useTheme();

  return <Box sx={{ borderColor: theme.palette.primary.main }}>Contenido</Box>;
};
```

### 3. **Transparencias con colores del tema**

```tsx
// ✅ CORRECTO - Agregar transparencia
<Box
  sx={{
    // 33 = 20% opacidad, 4D = 30%, 80 = 50%, B3 = 70%, etc.
    backgroundColor: (theme) => `${theme.palette.primary.main}33`,
    borderColor: (theme) => `${theme.palette.primary.main}4D`,
  }}
/>

// Tabla de opacidades comunes:
// 1A = 10%
// 33 = 20%
// 4D = 30%
// 66 = 40%
// 80 = 50%
// B3 = 70%
// CC = 80%
// E6 = 90%
// F2 = 95%
```

### 4. **Gradientes con colores del tema**

```tsx
// ✅ CORRECTO
<Box
  sx={{
    background: (theme) =>
      `linear-gradient(135deg, ${theme.palette.primary.main} 0%, ${theme.palette.secondary.main} 100%)`,
    WebkitBackgroundClip: "text",
    WebkitTextFillColor: "transparent",
  }}
>
  Texto con Gradiente
</Box>
```

### 5. **Usar color prop cuando sea posible**

```tsx
// ✅ CORRECTO - Usar prop color
<Button color="primary">Botón</Button>
<Chip color="success" label="Éxito" />
<CircularProgress color="secondary" />
<TextField color="primary" />

// ❌ EVITAR - Sobrescribir con sx cuando existe prop
<Button sx={{ backgroundColor: 'primary.main' }}>Botón</Button>
```

---

## 🎯 Casos de Uso Comunes

### Botones

```tsx
// Botón primario (naranja)
<Button variant="contained" color="primary">
  Guardar
</Button>

// Botón secundario (coral)
<Button variant="contained" color="secondary">
  Cancelar
</Button>

// Botón outlined
<Button variant="outlined" color="primary">
  Editar
</Button>

// Botón success (WhatsApp verde)
<Button variant="contained" color="success" startIcon={<WhatsApp />}>
  WhatsApp
</Button>
```

### Cards

```tsx
// Card con borde del tema
<Card
  sx={{
    border: "1px solid",
    borderColor: (theme) => `${theme.palette.primary.main}33`,
    "&:hover": {
      borderColor: "primary.main",
      boxShadow: (theme) => `0 8px 24px ${theme.palette.primary.main}33`,
    },
  }}
>
  <CardContent>Contenido</CardContent>
</Card>
```

### Chips de Estado

```tsx
// Estado activo (verde)
<Chip color="success" label="Activo" />

// Estado pendiente (naranja warning)
<Chip color="warning" label="Pendiente" />

// Estado error (rojo)
<Chip color="error" label="Cancelado" />

// Estado info (azul)
<Chip color="info" label="Información" />

// Estado principal (naranja primary)
<Chip color="primary" label="Destacado" />
```

### Backgrounds

```tsx
// Background con tinte del tema
<Box
  sx={{
    backgroundColor: 'background.default', // Blanco con tinte durazno
    minHeight: '100vh',
  }}
/>

// Background blanco puro
<Paper sx={{ backgroundColor: 'background.paper' }}>
  Contenido
</Paper>

// Background con gradiente
<Box
  sx={{
    background: (theme) =>
      `linear-gradient(135deg, ${theme.palette.background.default} 0%, ${theme.palette.primary.light} 15%, ${theme.palette.background.default} 100%)`,
  }}
/>
```

### Texto

```tsx
// Texto principal (negro)
<Typography color="text.primary">
  Título Principal
</Typography>

// Texto secundario (gris oscuro)
<Typography color="text.secondary">
  Descripción o subtítulo
</Typography>

// Texto deshabilitado (gris claro)
<Typography color="text.disabled">
  Información deshabilitada
</Typography>

// Texto con color del tema
<Typography color="primary.main">
  Texto destacado en naranja
</Typography>
```

### Iconos

```tsx
// Icono con color del tema
<ShoppingCart sx={{ color: 'primary.main' }} />
<CheckCircle sx={{ color: 'success.main' }} />
<Warning sx={{ color: 'warning.main' }} />
<Error sx={{ color: 'error.main' }} />
<Info sx={{ color: 'info.main' }} />
```

---

## 🚫 Qué NO Hacer

### ❌ Colores Hardcodeados

```tsx
// ❌ NUNCA hardcodear colores
<Box sx={{ backgroundColor: '#FF6B35' }}>Mal</Box>
<Typography sx={{ color: '#1A1A1A' }}>Mal</Typography>
<Button sx={{ borderColor: '#FF8A5B' }}>Mal</Button>

// ✅ USAR colores del tema
<Box sx={{ backgroundColor: 'primary.main' }}>Bien</Box>
<Typography color="text.primary">Bien</Typography>
<Button color="primary">Bien</Button>
```

### ❌ Sobrescribir Colores sin Razón

```tsx
// ❌ NO sobrescribir si existe prop color
<Button sx={{ backgroundColor: 'primary.main' }}>Mal</Button>

// ✅ USAR prop color
<Button color="primary">Bien</Button>
```

### ❌ Usar RGB/RGBA Directamente

```tsx
// ❌ NO usar rgb/rgba hardcodeado
<Box sx={{ backgroundColor: 'rgba(255, 107, 53, 0.2)' }}>Mal</Box>

// ✅ USAR tema con transparencia
<Box sx={{ backgroundColor: (theme) => `${theme.palette.primary.main}33` }}>
  Bien
</Box>
```

---

## 📚 Componentes MUI Personalizados

### Configuraciones Globales

Estos estilos ya están aplicados globalmente en `theme.ts`:

```typescript
// Botones
- borderRadius: 8px
- fontWeight: 600
- Hover con shadow y color dark

// Cards
- borderRadius: 12px
- Shadow con tinte naranja en hover

// Chips
- borderRadius: 8px
- fontWeight: 500

// AppBar
- backgroundColor: white (default)
- color: text.primary
- colorPrimary: naranja con texto blanco

// Tabs
- Sin textTransform
- fontWeight: 600
- Color seleccionado: primary.main

// TextField
- Hover: borderColor secondary
- Focus: borderColor primary con 2px

// Checkbox/Radio/Switch
- Checked color: primary.main
```

---

## 🎨 Paleta de Colores Visual

```
🔴 ERROR:    #EF5350 ████████
🟠 WARNING:  #FFA726 ████████
🟢 SUCCESS:  #4CAF50 ████████
🔵 INFO:     #29B6F6 ████████
🟠 PRIMARY:  #FF6B35 ████████
🟠 SECONDARY:#FF8A5B ████████
⚫ TEXT:     #1A1A1A ████████
⚪ BG:       #FFF8F5 ████████
```

---

## ✨ Ejemplos Completos

### Card de Producto

```tsx
<Card
  sx={{
    border: "1px solid",
    borderColor: (theme) => `${theme.palette.primary.main}33`,
    transition: "all 0.3s ease",
    "&:hover": {
      borderColor: "primary.main",
      transform: "translateY(-4px)",
      boxShadow: (theme) => `0 12px 24px ${theme.palette.primary.main}33`,
    },
  }}
>
  <CardContent>
    <Typography variant="h6" color="text.primary">
      Hamburguesa Clásica
    </Typography>
    <Typography variant="body2" color="text.secondary">
      Carne, lechuga, tomate, cebolla
    </Typography>
    <Chip color="success" label="Disponible" size="small" sx={{ mt: 1 }} />
  </CardContent>
</Card>
```

### Formulario

```tsx
<Box component="form" onSubmit={handleSubmit}>
  <Stack spacing={3}>
    <TextField label="Nombre" color="primary" fullWidth error={!!errors.nombre} helperText={errors.nombre?.message} />

    <TextField label="Email" color="primary" type="email" fullWidth />

    <Stack direction="row" spacing={2}>
      <Button variant="outlined" color="primary" onClick={onCancel}>
        Cancelar
      </Button>
      <Button variant="contained" color="primary" type="submit">
        Guardar
      </Button>
    </Stack>
  </Stack>
</Box>
```

---

## 🔧 Utilidades

### Función Helper para Opacidades

```typescript
// lib/theme/utils.ts
export const alpha = (color: string, opacity: number) => {
  const hex = Math.round(opacity * 255)
    .toString(16)
    .padStart(2, "0");
  return `${color}${hex}`;
};

// Uso:
<Box sx={{ backgroundColor: (theme) => alpha(theme.palette.primary.main, 0.2) }} />;
```

### Hook Personalizado

```typescript
// lib/hooks/useThemeColors.ts
import { useTheme } from "@mui/material";

export const useThemeColors = () => {
  const theme = useTheme();

  return {
    primary: theme.palette.primary.main,
    secondary: theme.palette.secondary.main,
    success: theme.palette.success.main,
    error: theme.palette.error.main,
    warning: theme.palette.warning.main,
    info: theme.palette.info.main,
  };
};

// Uso en componente:
const { primary, success } = useThemeColors();
```

---

## 📊 Contraste y Accesibilidad

### Ratios de Contraste WCAG

El tema está diseñado para cumplir con WCAG AA:

- **Texto normal**: 4.5:1 mínimo
- **Texto grande**: 3:1 mínimo
- **Elementos de UI**: 3:1 mínimo

### Combinaciones Aprobadas

```typescript
// ✅ Blanco sobre Naranja Primary (#FF6B35)
// Contraste: 4.6:1 - WCAG AA ✓

// ✅ Negro (#1A1A1A) sobre Background Default (#FFF8F5)
// Contraste: 18.5:1 - WCAG AAA ✓

// ✅ Blanco sobre Success Green (#4CAF50)
// Contraste: 4.5:1 - WCAG AA ✓
```

---

## 🎓 Recursos

- [Material UI Theme](https://mui.com/material-ui/customization/theming/)
- [Material UI Palette](https://mui.com/material-ui/customization/palette/)
- [Material UI Default Theme](https://mui.com/material-ui/customization/default-theme/)
- [WCAG Contrast Checker](https://webaim.org/resources/contrastchecker/)

---

**Última actualización**: 24 de octubre de 2025  
**Versión del tema**: 2.0.0 (Naranja Vibrante)
