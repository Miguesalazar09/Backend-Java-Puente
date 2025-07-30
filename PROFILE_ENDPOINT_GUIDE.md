## 📋 ENDPOINT: PUT /api/users/profile

### 🔐 AUTENTICACIÓN REQUERIDA
- **Token JWT:** Debe incluirse en el header `Authorization: Bearer <token>`
- **Roles:** Cualquier usuario autenticado (USER o ADMIN)

### 📝 FORMATO DE DATOS

**Content-Type:** `application/json`

**Body (JSON):**
```json
{
  "name": "string (opcional)",
  "email": "string (opcional)", 
  "password": "string (opcional)"
}
```

### 📊 CAMPOS DISPONIBLES

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `name` | String | ❌ Opcional | Nombre completo del usuario |
| `email` | String | ❌ Opcional | Email del usuario |
| `password` | String | ❌ Opcional | Nueva contraseña |

### ✅ CARACTERÍSTICAS IMPORTANTES

1. **Campos Opcionales:** Puedes enviar solo los campos que quieres actualizar
2. **Identificación Automática:** No necesitas pasar ID, usa el usuario logueado
3. **Validaciones:**
   - Campos no pueden estar vacíos (si se envían)
   - Email no puede estar en uso por otro usuario
   - El rol se mantiene (no se puede cambiar)

### 🔧 EJEMPLOS DE USO

#### Ejemplo 1: Actualizar solo el nombre
```bash
curl -X PUT http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Nuevo Nombre"
  }'
```

#### Ejemplo 2: Actualizar nombre y email
```bash
curl -X PUT http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Juan Pérez",
    "email": "juan.perez@nuevo.com"
  }'
```

#### Ejemplo 3: Cambiar contraseña
```bash
curl -X PUT http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "password": "nuevaContraseña123"
  }'
```

#### Ejemplo 4: Actualizar todo
```bash
curl -X PUT http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "name": "María González",
    "email": "maria.gonzalez@email.com",
    "password": "superSecure456"
  }'
```

### 📨 RESPUESTA

**Éxito (200 OK):**
```json
{
  "success": true,
  "message": "Perfil actualizado exitosamente",
  "user": {
    "id": "uuid-del-usuario",
    "name": "Nombre Actualizado",
    "email": "email@actualizado.com",
    "pass": "$2a$10$hashedPassword...",
    "role": "USER"
  }
}
```

**Error (400 Bad Request):**
```json
{
  "success": false,
  "message": "Error: Email already in use by another user",
  "user": null
}
```

### ⚠️ ERRORES COMUNES

- **401 Unauthorized:** Token JWT no válido o ausente
- **400 Bad Request:** Datos inválidos (campos vacíos, email duplicado)
- **404 Not Found:** Usuario no encontrado

### 🔒 SEGURIDAD

- ✅ Solo puede actualizar su propio perfil
- ✅ No puede cambiar su rol
- ✅ Email debe ser único en el sistema
- ✅ Password se hashea automáticamente
