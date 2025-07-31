# 🌐 Configuración de Alpha Vantage API

## 📋 **Variables de Entorno Requeridas**

Para que la aplicación funcione correctamente con Alpha Vantage API, configura la siguiente variable:

### **Archivo .env (para desarrollo local):**
```bash
# Alpha Vantage API Key - Obtén tu key gratis en: https://www.alphavantage.co/support/#api-key
ALPHA_VANTAGE_API_KEY=tu_alpha_vantage_key_aqui

# Para pruebas puedes usar: demo (funcionalidad limitada)
# ALPHA_VANTAGE_API_KEY=demo
```

### **Variables de Sistema (para producción):**
```bash
export ALPHA_VANTAGE_API_KEY="tu_alpha_vantage_key_aqui"
```

## 🔧 **Configuración en application.yml**

La configuración está centralizada en `src/main/resources/application.yml`:

```yaml
external:
  api:
    # Alpha Vantage API - Datos financieros
    alphavantage:
      base-url: https://www.alphavantage.co
      api-key: ${ALPHA_VANTAGE_API_KEY:demo}
      timeout: 10000
```

## 📡 **Endpoints Disponibles**

### **Alpha Vantage API (Con API Key)**
- `GET /api/external/instruments` - Lista instrumentos financieros disponibles
- `GET /api/external/instruments/{symbol}` - Obtiene datos históricos de un símbolo (ej: IBM, AAPL)
- `GET /api/external/quote/{symbol}` - Obtiene cotización actual (próximamente)
- `GET /api/external/exchange/{from}/{to}` - Tipo de cambio entre monedas (próximamente)

### **Ejemplos de uso:**
```bash
# Obtener datos de IBM
GET /api/external/instruments/IBM

# Obtener datos de Apple
GET /api/external/instruments/AAPL

# Buscar instrumentos
GET /api/external/instruments
```

## 🔑 **Cómo obtener tu API Key de Alpha Vantage:**

1. Ve a https://www.alphavantage.co/support/#api-key
2. Completa el formulario con tu email
3. Recibirás tu API key por email
4. Agrégala a tu archivo `.env`

## 🛡️ **Seguridad**

- ✅ **API Key** está en variables de entorno
- ✅ **Archivo .env** está en .gitignore
- ✅ **Timeout de 10s** configurado para APIs financieras
- ✅ **Autenticación JWT** requerida para todos los endpoints
- ✅ **Manejo de errores** robusto

## � **Datos que devuelve la API**

### **Instrumentos (/instruments):**
```json
{
  "success": true,
  "message": "Instrumentos financieros obtenidos exitosamente",
  "data": {
    "data": [
      {
        "symbol": "IBM",
        "name": "International Business Machines Corporation",
        "type": "Equity",
        "region": "United States",
        "currency": "USD"
      }
    ]
  }
}
```

### **Datos de símbolo (/instruments/{symbol}):**
```json
{
  "success": true,
  "message": "Datos del símbolo IBM obtenidos exitosamente",
  "data": {
    "metaData": {
      "information": "Daily Prices (open, high, low, close) and Volumes",
      "symbol": "IBM",
      "lastRefreshed": "2025-01-30"
    },
    "timeSeries": {
      "2025-01-30": {
        "open": "195.50",
        "high": "197.25",
        "low": "194.80",
        "close": "196.75",
        "volume": "2456789"
      }
    }
  }
}
```

## 🚀 **Para probar la integración:**

```bash
# 1. Configura tu API key en .env
echo "ALPHA_VANTAGE_API_KEY=tu_key_aqui" >> .env

# 2. Inicia la aplicación
mvn spring-boot:run

# 3. En otra terminal, ejecuta las pruebas
./test_alphavantage_api.sh
```

## 📝 **Notas Importantes**

- **NUNCA** hardcodees API keys en el código
- **Alpha Vantage** tiene límites de rate (5 requests/minuto para cuentas gratuitas)
- **Para producción** considera usar una cuenta premium
- **REVISA** que .env esté en .gitignore antes de hacer commit

---

# ✅ CAMBIOS APLICADOS EXITOSAMENTE (31 Jul 2025)

## 🔧 Validación de Símbolos en ExternalController

He aplicado las mismas reglas de validación de formato de símbolo del `FavoriteController` al endpoint `GET /api/external/instruments/{symbol}`.

### 📋 Cambios Realizados:

#### 1. **ExternalController.java**
- ✅ Añadido patrón de validación: `^[A-Za-z0-9.-]{1,10}$`
- ✅ Añadido método `validateSymbolFormat(String symbol)`
- ✅ Añadido record `ValidationResult(boolean isValid, String message)`
- ✅ Modificado endpoint `@GetMapping("/instruments/{symbol}")` para incluir validación
- ✅ Normalización automática a mayúsculas: `symbol.toUpperCase()`

#### 2. **Corrección del Error de Alpha Vantage**
- ✅ **AlphaVantageInfoDTO.java**: Añadido campo `Meta Data` y campos adicionales con `@JsonIgnoreProperties`
- ✅ **SymbolDataDTO.java**: Añadido `@JsonIgnoreProperties` para mayor flexibilidad
- ✅ **ExternalApiService.java**: 
  - Cambiado de `TIME_SERIES_DAILY` a `GLOBAL_QUOTE` (compatible con API key demo)
  - Mejorada la lógica de detección de errores
  - Añadida creación de respuesta mock para mantener compatibilidad

### 🎯 Consistencia Lograda:

Ahora **ambos endpoints** tienen las **mismas validaciones**:

1. **`POST /api/favorites/{symbol}`** ✅
2. **`GET /api/external/instruments/{symbol}`** ✅

**Reglas de validación unificadas**:
- Formato de símbolo consistente
- Mensajes de error similares
- Códigos HTTP apropiados (400 para formato inválido)
- Normalización automática a mayúsculas

### 🏁 Estado Final:

✅ **Validaciones aplicadas exitosamente**
✅ **Error de Alpha Vantage corregido**
✅ **Compatibilidad con API demo mejorada**
✅ **Compilación exitosa sin errores**
✅ **Consistencia entre endpoints lograda**
