package com.example.demo.infrastructure.scheduler;

import com.example.demo.application.usecase.ExternalDataUseCase;
import com.example.demo.infrastructure.external.dto.InstrumentListDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Servicio programado para actualizar datos de instrumentos financieros
 * Se ejecuta cada 5 minutos durante horarios de mercado
 */
@Component
@ConditionalOnProperty(
    value = "external.api.scheduler.enabled", 
    havingValue = "true", 
    matchIfMissing = false
)
public class ExternalDataScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(ExternalDataScheduler.class);
    
    private final ExternalDataUseCase externalDataUseCase;
    
    public ExternalDataScheduler(ExternalDataUseCase externalDataUseCase) {
        this.externalDataUseCase = externalDataUseCase;
    }
    
    /**
     * Job programado que se ejecuta cada 5 minutos
     * fixedRate = 300000 (5 minutos en ms)
     */
    @Scheduled(fixedRate = 300000)
    @Async
    public void updateFinancialData() {
        logger.info("Iniciando actualizacion programada de datos financieros");
        
        try {
            // Obtener lista de instrumentos actualizada
            InstrumentListDTO instruments = externalDataUseCase.getAllInstruments();
            
            if (instruments != null && instruments.bestMatches() != null) {
                int instrumentCount = instruments.bestMatches().size();
                logger.info("Datos actualizados exitosamente. {} instrumentos procesados", instrumentCount);
                
                // Aqui podrias agregar logica adicional como:
                // - Guardar en cache
                // - Notificar a usuarios sobre cambios importantes
                // - Actualizar metricas
                // - Enviar alertas si hay problemas
                
            } else {
                logger.warn("No se obtuvieron datos de instrumentos en la actualizacion programada");
            }
            
        } catch (Exception e) {
            logger.error("Error durante la actualizacion programada de datos financieros", e);
            
            // Aqui podrias agregar:
            // - Metricas de errores
            // - Alertas para el equipo de desarrollo
            // - Reintentos con backoff exponencial
        }
    }
    
    /**
     * Job que se ejecuta solo durante horarios de mercado (lunes a viernes, 9:30-16:00 EST)
     * Cron: cada 5 minutos, de 9 a 16 horas, lunes a viernes
     */
    @Scheduled(cron = "0 */5 9-16 * * MON-FRI", zone = "America/New_York")
    @Async
    public void updateDuringMarketHours() {
        logger.info("Actualizacion durante horarios de mercado");
        
        try {
            // Logica especifica para horarios de mercado
            // Podrias hacer llamadas mas frecuentes o con diferentes parametros
            InstrumentListDTO instruments = externalDataUseCase.getAllInstruments();
            
            if (instruments != null) {
                logger.info("Actualizacion de horario de mercado completada");
            }
            
        } catch (Exception e) {
            logger.error("Error en actualizacion de horario de mercado", e);
        }
    }
    
    /**
     * Job que se ejecuta una vez al dia para limpieza y mantenimiento
     * Cron: todos los dias a las 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Async
    public void dailyMaintenance() {
        logger.info("Ejecutando mantenimiento diario");
        
        try {
            // Aqui podrias agregar:
            // - Limpieza de cache expirado
            // - Compactacion de logs
            // - Generacion de reportes diarios
            // - Backup de configuraciones
            
            logger.info("Mantenimiento diario completado");
            
        } catch (Exception e) {
            logger.error("Error durante mantenimiento diario", e);
        }
    }
}
