# 🚀 Sistema de Gestión de Usuarios con Spring Boot

Sistema completo de autenticación y gestión de usuarios con JWT, control de acceso basado en roles (RBAC) y endpoints RESTful.

## ✨ Características Principales

- 🔐 **Autenticación JWT** - Tokens seguros para acceso
- 👥 **Sistema de Roles** - USER y ADMIN con permisos diferenciados  
- 🛡️ **Control de Acceso** - Endpoints protegidos según rol
- 👤 **Gestión de Perfil** - Los usuarios pueden ver y actualizar su propio perfil
- 🔒 **Seguridad Robusta** - Validaciones, encriptación de contraseñas con BCrypt
- 📊 **CRUD Administrativo** - Gestión completa de usuarios para administradores

## 🛠️ Tecnologías Utilizadas

- **Java 17+**
- **Spring Boot 3.5.4**
- **Spring Security 6** 
- **Spring Data JPA**
- **PostgreSQL**
- **JWT (JSON Web Tokens)**
- **BCrypt** para encriptación
- **Maven** para gestión de dependencias
- **Docker** para base de datos

## 📋 Endpoints Implementados

### 🔓 Públicos
- `POST /api/users` - Registro de usuarios
- `POST /api/auth/login` - Login con JWT

### 🔒 Autenticados (USER + ADMIN)
- `GET /api/users/profile` - Ver mi perfil
- `PUT /api/users/profile` - Actualizar mi perfil

### 🛡️ Solo ADMIN
- `GET /api/admin/users` - Lista todos los usuarios
- `POST /api/admin/users` - Crear usuarios con cualquier rol
- `GET /api/admin/users/{id}` - Ver usuario específico
- `PUT /api/admin/users/{id}` - Actualizar cualquier usuario
- `DELETE /api/admin/users/{id}` - Eliminar usuarios

## 🚀 Cómo Ejecutar

### Prerrequisitos
- Java 17+
- Docker y Docker Compose
- Maven

### 1. Clonar el repositorio
\`\`\`bash
git clone https://github.com/tuusuario/tu-repo.git
cd tu-repo
\`\`\`

### 2. Iniciar la base de datos
\`\`\`bash
docker-compose up -d
\`\`\`

### 3. Ejecutar la aplicación
\`\`\`bash
./mvnw spring-boot:run
\`\`\`

La aplicación estará disponible en `http://localhost:8080`

## 📖 Uso de la API

### Registro de Usuario
\`\`\`bash
curl -X POST http://localhost:8080/api/users \\
  -H "Content-Type: application/json" \\
  -d '{
    "name": "Juan Pérez",
    "email": "juan@example.com",
    "password": "password123"
  }'
\`\`\`

### Login
\`\`\`bash
curl -X POST http://localhost:8080/api/auth/login \\
  -H "Content-Type: application/json" \\
  -d '{
    "email": "juan@example.com",
    "password": "password123"
  }'
\`\`\`

### Ver Mi Perfil
\`\`\`bash
curl -X GET http://localhost:8080/api/users/profile \\
  -H "Authorization: Bearer {tu_jwt_token}"
\`\`\`

### Actualizar Mi Perfil
\`\`\`bash
curl -X PUT http://localhost:8080/api/users/profile \\
  -H "Authorization: Bearer {tu_jwt_token}" \\
  -H "Content-Type: application/json" \\
  -d '{
    "name": "Nuevo Nombre",
    "email": "nuevo@email.com"
  }'
\`\`\`

## 🔒 Características de Seguridad

- ✅ **Contraseñas encriptadas** con BCrypt
- ✅ **Tokens JWT** con expiración
- ✅ **Validación de roles** en cada endpoint
- ✅ **Prevención de escalación de privilegios**
- ✅ **Detección de intentos de cambio de rol**
- ✅ **Validación de datos de entrada**

## 🏗️ Arquitectura

El proyecto sigue una **arquitectura hexagonal** (Clean Architecture):

- **Entrypoint** - Controllers REST
- **Application** - Casos de uso y servicios
- **Domain** - Modelos y puertos 
- **Infrastructure** - Repositorios, seguridad, configuración

## 📝 Características Especiales

### Gestión de Perfil de Usuario
- Los usuarios pueden actualizar su propio perfil sin especificar ID
- Se detectan y registran intentos de cambio de rol
- Mensajes informativos sobre qué campos se actualizaron

### Validaciones Robustas
- Campos obligatorios validados
- Emails únicos en el sistema
- Prevención de creación de admins en registro público
- Manejo de errores con mensajes claros

## 🤝 Contribuir

1. Fork del proyecto
2. Crear rama para feature (`git checkout -b feature/AmazingFeature`)
3. Commit de cambios (`git commit -m 'Add AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abrir Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para detalles.

## 👨‍💻 Autor

Tu Nombre - [@tu_usuario](https://github.com/tu_usuario)

Enlace del Proyecto: [https://github.com/tu_usuario/tu-repo](https://github.com/tu_usuario/tu-repo)
