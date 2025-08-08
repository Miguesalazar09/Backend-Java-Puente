# Guía de Ejecución con Docker

## Prerrequisitos
- Docker instalado
- Docker Compose instalado
- Clave API de Alpha Vantage (opcional, para pruebas reales)

## Configuración Inicial

1. **Clonar y navegar al proyecto:**
```bash
cd /home/migue/Desktop/Backend-Java-Puente
```

2. **Configurar Alpha Vantage API Key (opcional):**
Editar `docker-compose.yml` y reemplazar `your_api_key_here` con tu clave real.

## Comandos de Ejecución

### Construir y ejecutar todos los servicios:
```bash
docker-compose up --build
```

### Ejecutar en segundo plano:
```bash
docker-compose up -d --build
```

### Ver logs de todos los servicios:
```bash
docker-compose logs -f
```

### Ver logs de un servicio específico:
```bash
docker-compose logs -f user-service
docker-compose logs -f favorites-service
docker-compose logs -f external-service
docker-compose logs -f api-gateway
```

### Detener todos los servicios:
```bash
docker-compose down
```

### Detener y eliminar volúmenes (reset completo):
```bash
docker-compose down -v
```

## Endpoints Disponibles

### API Gateway (Puerto 8080):
- **Usuarios:** http://localhost:8080/api/users
- **Favoritos:** http://localhost:8080/api/favorites
- **Servicios Externos:** http://localhost:8080/api/external

### Servicios Directos:
- **User Service:** http://localhost:8081
- **Favorites Service:** http://localhost:8082
- **External Service:** http://localhost:8083

### Bases de Datos:
- **PostgreSQL:** localhost:5432
- **Redis:** localhost:6379

## Usuarios de Prueba

### Usuario Admin:
- Email: admin@example.com
- Password: password
- Role: ADMIN

### Usuario Normal:
- Email: user@example.com
- Password: password
- Role: USER

## Verificación de Salud

Cada servicio expone endpoints de health check:
- http://localhost:8080/actuator/health (API Gateway)
- http://localhost:8081/actuator/health (User Service)
- http://localhost:8082/actuator/health (Favorites Service)
- http://localhost:8083/actuator/health (External Service)

## Pruebas Básicas

### 1. Registro de usuario:
```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "name": "Test User",
    "password": "password123"
  }'
```

### 2. Login:
```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

### 3. Obtener datos de Alpha Vantage:
```bash
curl -X GET "http://localhost:8080/api/external/stock/AAPL"
```

## Troubleshooting

### Si los contenedores no inician:
1. Verificar que no hay otros servicios usando los puertos
2. Revisar logs: `docker-compose logs`
3. Rebuild: `docker-compose down && docker-compose up --build`

### Si hay problemas de conexión entre servicios:
1. Verificar que todos los servicios estén en la misma red
2. Usar nombres de contenedor para comunicación interna
3. Revisar variables de entorno en docker-compose.yml

### Reset completo:
```bash
docker-compose down -v
docker system prune -f
docker-compose up --build
```
