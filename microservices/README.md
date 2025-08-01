# 🚀 Microservices Architecture - Demo Project

## 📋 Resumen de la Migración

Este proyecto ha sido **migrado exitosamente** de una arquitectura monolítica a **microservicios** manteniendo **Clean Architecture** en cada servicio.

### 🏗️ Arquitectura de Microservicios

```
                    ┌─────────────────┐
                    │   API GATEWAY   │ :8080
                    │  (Entry Point)  │
                    └─────────┬───────┘
                              │
            ┌─────────────────┼─────────────────┐
            │                 │                 │
    ┌───────▼────────┐ ┌──────▼──────┐ ┌───────▼────────┐
    │  USER SERVICE  │ │  FAVORITES  │ │   EXTERNAL     │
    │     :8081      │ │   SERVICE   │ │   SERVICE      │
    │                │ │    :8082    │ │     :8083      │
    │ • Auth         │ │ • Favorites │ │ • Alpha        │
    │ • Users        │ │ • Validation│ │   Vantage      │
    │ • Admin        │ │             │ │ • Symbol       │
    │                │ │             │ │   Validation   │
    └───────┬────────┘ └──────┬──────┘ └───────┬────────┘
            │                 │                 │
    ┌───────▼────────┐ ┌──────▼──────┐ ┌───────▼────────┐
    │   PostgreSQL   │ │ PostgreSQL  │ │     Redis      │
    │  (Users DB)    │ │(Favorites DB)│ │   (Cache)     │
    │     :5433      │ │    :5434    │ │     :6379      │
    └────────────────┘ └─────────────┘ └────────────────┘
                              │
                    ┌─────────▼───────┐
                    │ EUREKA SERVER   │ :8761
                    │(Service Discovery)│
                    └─────────────────┘
```

## 🎯 Servicios Implementados

### 1. **API Gateway** (:8080)
- **Función**: Punto de entrada único, routing, autenticación
- **Tecnologías**: Spring Cloud Gateway, Eureka Client, JWT
- **Rutas**:
  - `/api/auth/**` → User Service
  - `/api/users/**` → User Service  
  - `/api/admin/**` → User Service
  - `/api/favorites/**` → Favorites Service
  - `/api/external/**` → External Service

### 2. **User Service** (:8081)
- **Función**: Autenticación, gestión de usuarios, administración
- **BD**: PostgreSQL (puerto 5433)
- **Clean Architecture**:
  - Domain: User, Role entities
  - Application: UserService, AuthService
  - Infrastructure: JPA repositories, Security
  - Entrypoint: AuthController, UserController, AdminController

### 3. **Favorites Service** (:8082)
- **Función**: Gestión de favoritos, validación de usuarios
- **BD**: PostgreSQL (puerto 5434)
- **Comunicación**: Feign Client → User Service, External Service
- **Clean Architecture**:
  - Domain: Favorite entity
  - Application: FavoriteService
  - Infrastructure: JPA repositories, Service clients
  - Entrypoint: FavoriteController

### 4. **External Service** (:8083)
- **Función**: Integración Alpha Vantage, validación símbolos, cache
- **Cache**: Redis (puerto 6379)
- **Clean Architecture**:
  - Domain: Symbol validation logic
  - Application: ExternalDataService
  - Infrastructure: Alpha Vantage client, Redis cache
  - Entrypoint: ExternalController

### 5. **Service Discovery** (:8761)
- **Eureka Server**: Registro y descubrimiento de servicios
- **Load Balancing**: Automático entre instancias

## 🚀 Instalación y Ejecución

### Prerrequisitos
- **Java 17+**
- **Maven 3.6+**
- **Docker & Docker Compose**
- **API Key Alpha Vantage**

### 🔧 Setup Rápido

```bash
# 1. Clonar el proyecto
cd /home/migue/Desktop/demo/microservices

# 2. Configurar API Key
export ALPHA_VANTAGE_API_KEY=tu_api_key

# 3. Build y Deploy automático
./build-and-deploy.sh
```

### 🐳 Deployment Manual

```bash
# Build individual services
cd user-service && mvn clean package -DskipTests && cd ..
cd favorites-service && mvn clean package -DskipTests && cd ..
cd external-service && mvn clean package -DskipTests && cd ..
cd api-gateway && mvn clean package -DskipTests && cd ..

# Start infrastructure
docker-compose up -d
```

### ⏰ Tiempos de Startup
- **Eureka Server**: ~30s
- **Databases**: ~15s  
- **Services**: ~45s cada uno
- **Total**: ~2-3 minutos

## 🔗 Comunicación Entre Servicios

### **Service-to-Service Communication**

```java
// Favorites Service → User Service
@FeignClient(name = "user-service")
public interface UserServiceClient {
    @GetMapping("/api/users/{id}/exists")
    boolean userExists(@PathVariable("id") String userId);
}

// Favorites Service → External Service  
@FeignClient(name = "external-service")
public interface ExternalServiceClient {
    @GetMapping("/api/external/validate/{symbol}")
    ValidationResult validateSymbol(@PathVariable("symbol") String symbol);
}
```

### **Load Balancing**
- Automático via Eureka + Feign
- Round-robin por defecto
- Circuit breaker preparado (Hystrix)

## 🔒 Seguridad Distribuida

### **JWT en API Gateway**
- Validación centralizada de tokens
- Headers propagados a servicios
- Autorización por rutas

### **Service-to-Service Auth**
```yaml
# API Gateway routes con auth
spring:
  cloud:
    gateway:
      routes:
        - id: user-service-admin
          uri: lb://user-service
          predicates:
            - Path=/api/admin/**
          filters:
            - name: JwtAuthenticationFilter
```

## 📊 Monitoreo y Observabilidad

### **Health Checks**
```bash
# Gateway
curl http://localhost:8080/actuator/health

# Individual services
curl http://localhost:8081/actuator/health  # User Service
curl http://localhost:8082/actuator/health  # Favorites Service
curl http://localhost:8083/actuator/health  # External Service
```

### **Service Discovery**
- Dashboard: http://localhost:8761
- Registro automático de servicios
- Health monitoring integrado

## 🧪 Testing de Microservicios

### **Test de Integración**
```bash
# Test auth flow
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'

# Test distributed flow
TOKEN="eyJhbGciOiJIUzI1NiJ9..."
curl -X POST http://localhost:8080/api/favorites/AAPL \
  -H "Authorization: Bearer $TOKEN"
```

### **Service Independence**
```bash
# Stop one service and test others
docker stop favorites-service
# User service should still work
curl http://localhost:8080/api/users/profile
```

## 🎯 Beneficios Obtenidos

### ✅ **Escalabilidad Independiente**
```bash
# Scale only favorites service
docker-compose up -d --scale favorites-service=3
```

### ✅ **Tecnología Específica**
- User Service: PostgreSQL + Security
- Favorites Service: PostgreSQL + Feign
- External Service: Redis + HTTP Client
- API Gateway: Reactive + JWT

### ✅ **Deployment Independiente**
```bash
# Deploy only updated service
cd user-service
mvn clean package
docker-compose up -d --force-recreate user-service
```

### ✅ **Fault Isolation**
- Fallo en External Service no afecta User Management
- Circuit breakers previenen cascading failures
- Graceful degradation implementado

## 🔄 Comparación: Monolito vs Microservicios

| Aspecto | Monolito Original | Microservicios |
|---------|------------------|----------------|
| **Deployment** | 1 JAR | 4 servicios independientes |
| **Databases** | 1 PostgreSQL | 2 PostgreSQL + Redis |
| **Escalabilidad** | Todo junto | Por servicio |
| **Tecnologías** | Stack único | Stack optimizado por servicio |
| **Complejidad** | Baja | Media-Alta |
| **Latencia** | Interna | Red (2-10ms adicional) |
| **Debugging** | Simple | Distributed tracing necesario |
| **Testing** | Unit + Integration | + Contract testing |

## 📈 Métricas de Migración

### **Clean Architecture Preservada**
- ✅ Domain layer: Sin dependencias externas
- ✅ Application layer: Lógica de negocio intacta  
- ✅ Infrastructure: Adaptada a comunicación distribuida
- ✅ Entrypoint: Controllers reorganizados por dominio

### **Funcionalidad Mantenida**
- ✅ Autenticación JWT
- ✅ Validación Alpha Vantage
- ✅ Gestión usuarios y roles
- ✅ Sistema de favoritos
- ✅ Validaciones robustas

### **Nuevas Capacidades**
- ✅ Service discovery
- ✅ Load balancing
- ✅ Independent scaling
- ✅ Technology diversity
- ✅ Fault isolation

## 🎓 Lecciones Aprendidas

### **Clean Architecture → Microservicios**
1. **Domain preservation**: Entidades y reglas de negocio sin cambios
2. **Port adaptation**: Interfaces se convierten en HTTP clients
3. **Use case distribution**: Casos de uso por bounded context
4. **Infrastructure splitting**: Persistencia y external services separados

### **Challenges Enfrentados**
1. **Distributed transactions**: Saga pattern para consistency
2. **Service communication**: Feign + Circuit breakers
3. **Configuration management**: Externalized config
4. **Testing complexity**: Contract testing + integration tests

## 🚦 Próximos Pasos

### **Corto Plazo**
- [ ] Monitoring dashboard (Grafana + Prometheus)
- [ ] Centralized logging (ELK Stack)
- [ ] API documentation (OpenAPI per service)

### **Mediano Plazo**  
- [ ] Event-driven architecture (Kafka)
- [ ] CQRS for read/write separation
- [ ] Advanced circuit breakers (Hystrix)

### **Largo Plazo**
- [ ] Service mesh (Istio)
- [ ] Kubernetes deployment
- [ ] Automated testing pipeline
- [ ] Multi-region deployment

---

## 🎉 **Migración Completada Exitosamente**

**El proyecto ha sido migrado de monolito a microservicios manteniendo:**
- ✅ Clean Architecture en cada servicio
- ✅ Funcionalidad completa
- ✅ Validaciones robustas  
- ✅ Integración Alpha Vantage
- ✅ Seguridad JWT

**Nuevas capacidades agregadas:**
- 🚀 Escalabilidad independiente
- 🛡️ Fault isolation
- 🔧 Technology diversity
- 📊 Service discovery
- ⚡ Load balancing

La migración demuestra cómo **Clean Architecture facilita la evolución** de monolito a microservicios preservando la lógica de negocio y mejorando la escalabilidad del sistema.
