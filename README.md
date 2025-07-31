# Demo Spring Boot - API de Gestión de Usuarios y Favoritos Financieros

## 📋 Descripción

Esta aplicación Spring Boot implementa una API REST robusta para la gestión de usuarios y favoritos de instrumentos financieros, con integración a la API de Alpha Vantage para validación de símbolos bursátiles en tiempo real.

### 🎯 Características Principales

- **Arquitectura Clean Architecture**: Separación clara de responsabilidades en capas (Domain, Application, Infrastructure, Entrypoint)
- **Seguridad JWT**: Autenticación y autorización basada en tokens JWT
- **Validación Robusta**: Validaciones exhaustivas de datos de entrada y reglas de negocio
- **Integración Externa**: Consulta real a Alpha Vantage API para validación de símbolos financieros
- **Gestión de Roles**: Sistema de roles (USER, ADMIN) con permisos diferenciados
- **Base de Datos**: PostgreSQL con JPA/Hibernate

## 🚀 Instalación y Configuración

### Prerrequisitos

- **Java 17** o superior
- **Maven 3.6** o superior
- **PostgreSQL 12** o superior
- **Clave API de Alpha Vantage** (gratuita en [alphavantage.co](https://www.alphavantage.co/support/#api-key))

### 1. Configuración de Base de Datos

```bash
# Crear base de datos PostgreSQL
sudo -u postgres psql
CREATE DATABASE demo;
CREATE USER postgres WITH ENCRYPTED PASSWORD 'postgres123';
GRANT ALL PRIVILEGES ON DATABASE demo TO postgres;
\q
```

### 2. Configuración de Variables de Entorno

```bash
# Configurar API Key de Alpha Vantage
export ALPHA_VANTAGE_API_KEY=tu_api_key_aqui

# Opcional: Habilitar scheduler automático
export ENABLE_SCHEDULER=false
```

### 3. Instalación y Ejecución

```bash
# Clonar el repositorio
git clone <url-del-repositorio>
cd demo

# Compilar el proyecto
./mvnw clean compile

# Ejecutar la aplicación
./mvnw spring-boot:run
```

La aplicación estará disponible en: `http://localhost:8080`

### 4. Configuración Docker (Opcional)

```bash
# Construir imagen
docker build -t demo-app .

# Ejecutar con Docker Compose
docker-compose up -d
```

## 🏗️ Arquitectura del Sistema

```
┌─────────────────────────────────────────────────────────────┐
│                        ENTRYPOINT LAYER                     │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│  │   Auth      │ │    Admin    │ │  External   │           │
│  │ Controller  │ │ Controller  │ │ Controller  │  ...      │
│  └─────────────┘ └─────────────┘ └─────────────┘           │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────┴───────────────────────────────────────┐
│                     APPLICATION LAYER                       │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│  │  Favorite   │ │    User     │ │  External   │           │
│  │   Service   │ │   Service   │ │  Data Use   │  ...      │
│  │             │ │             │ │    Case     │           │
│  └─────────────┘ └─────────────┘ └─────────────┘           │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────┴───────────────────────────────────────┐
│                       DOMAIN LAYER                          │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│  │    User     │ │  Favorite   │ │    Ports    │           │
│  │   Entity    │ │   Entity    │ │(Interfaces) │  ...      │
│  └─────────────┘ └─────────────┘ └─────────────┘           │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────┴───────────────────────────────────────┐
│                   INFRASTRUCTURE LAYER                      │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│  │     JPA     │ │   Alpha     │ │   Security  │           │
│  │Repositories │ │  Vantage    │ │   Config    │  ...      │
│  │             │ │   Service   │ │             │           │
│  └─────────────┘ └─────────────┘ └─────────────┘           │
└─────────────────────────────────────────────────────────────┘
```

### Flujo de Datos Principal

1. **Entrypoint**: Recibe requests HTTP, valida formato y autorización
2. **Application**: Ejecuta lógica de negocio y coordina servicios
3. **Domain**: Define entidades y reglas de negocio centrales
4. **Infrastructure**: Maneja persistencia y servicios externos

## 📚 API Endpoints

### 🔐 Autenticación

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| POST | `/api/users` | Registro de usuario | Público |
| POST | `/api/auth/login` | Inicio de sesión | Público |

### 👤 Gestión de Usuarios

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/api/users/profile` | Obtener perfil | USER, ADMIN |
| PUT | `/api/users/profile` | Actualizar perfil | USER, ADMIN |

### 🛡️ Administración

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| POST | `/api/admin/users` | Crear usuario | ADMIN |
| GET | `/api/admin/users/{id}` | Obtener usuario | ADMIN |
| PUT | `/api/admin/users/{id}` | Actualizar usuario | ADMIN |
| DELETE | `/api/admin/users/{id}` | Eliminar usuario | ADMIN |

### ⭐ Favoritos

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| POST | `/api/favorites/{symbol}` | Agregar favorito | USER, ADMIN |
| GET | `/api/favorites` | Listar favoritos | USER, ADMIN |
| DELETE | `/api/favorites/{symbol}` | Eliminar favorito | USER, ADMIN |

### 📊 Datos Externos

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/api/external/instruments/{symbol}` | Datos de símbolo | USER, ADMIN |

## 🔒 Validaciones Implementadas

### 🎯 Validaciones de Negocio

#### 1. **Registro de Usuarios**
- ✅ Email único en el sistema
- ✅ Formato de email válido
- ✅ Campos obligatorios (username, email, password)
- ✅ Longitud mínima de contraseña

#### 2. **Gestión Administrativa**
- ✅ Validación de formato UUID en endpoints admin
- ✅ Verificación de existencia de usuario antes de operaciones
- ✅ Validación de unicidad de email en creación/actualización

#### 3. **Símbolos Financieros**
- ✅ Formato de símbolo válido (1-10 caracteres alfanuméricos)
- ✅ **Validación real con Alpha Vantage API**
- ✅ Verificación de existencia del símbolo antes de agregar a favoritos
- ✅ Manejo de errores de API externa con fallback


## 🛠️ Decisiones Técnicas

### 1. **Arquitectura Clean**
**Justificación**: Permite testabilidad, mantenibilidad y evolución independiente de cada capa.

**Beneficios**:
- Separación clara de responsabilidades
- Facilita testing unitario y de integración
- Independencia de frameworks externos
- Escalabilidad a largo plazo

### 2. **Validación en Tiempo Real con Alpha Vantage**
**Justificación**: Garantiza que solo símbolos válidos y existentes sean agregados al sistema.

**Implementación**:
```java
public ValidationResult validateSymbolExists(String symbol) {
    try {
        GlobalQuoteDTO quote = getGlobalQuote(symbol);
        return quote != null && quote.getSymbol() != null ? 
            ValidationResult.valid() : 
            ValidationResult.invalid("Símbolo no encontrado en Alpha Vantage");
    } catch (Exception e) {
        return ValidationResult.invalid("Error al validar símbolo: " + e.getMessage());
    }
}
```

### 3. **Manejo de Errores HTTP Semánticos**
**Justificación**: Mejora la experiencia del desarrollador y facilita debugging.

**Códigos implementados**:
- `400 Bad Request`: Datos de entrada inválidos
- `401 Unauthorized`: Falta de autenticación
- `403 Forbidden`: Permisos insuficientes
- `404 Not Found`: Recurso no encontrado
- `409 Conflict`: Conflicto (ej: email duplicado)
- `422 Unprocessable Entity`: Validación de negocio fallida
- `500 Internal Server Error`: Errores del servidor

### 4. **DTOs y Record Classes**
**Justificación**: Inmutabilidad, claridad en contratos de API y mejor performance.

```java
public record GlobalQuoteDTO(
    @JsonProperty("01. symbol") String symbol,
    @JsonProperty("02. open") String open,
    @JsonProperty("05. price") String price,
    // ... otros campos
) {}
```
