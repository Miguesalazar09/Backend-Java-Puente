package com.example.externalservice.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class InstrumentCacheService {

    private static final Logger logger = LoggerFactory.getLogger(InstrumentCacheService.class);
    private static final String INSTRUMENTS_CACHE = "instruments";
    private static final String INSTRUMENTS_LIST_KEY = "available_instruments";

    @Cacheable(value = INSTRUMENTS_CACHE, key = "'" + INSTRUMENTS_LIST_KEY + "'")
    public List<String> getAvailableInstruments() {
        logger.info("🔍 Cache MISS - Obteniendo lista de instrumentos desde fuente estática");
        
        try {
            List<String> instruments = getStaticInstrumentsList();
            
            logger.info("✅ Lista de instrumentos obtenida y guardada en cache por 1 hora");
            logger.info("📊 Total de instrumentos: {}", instruments.size());
            
            return instruments;
            
        } catch (Exception e) {
            logger.error("❌ Error obteniendo lista de instrumentos: {}", e.getMessage());
            // Fallback a lista básica en caso de error
            return getBasicInstrumentsList();
        }
    }

    @CacheEvict(value = INSTRUMENTS_CACHE, key = "'" + INSTRUMENTS_LIST_KEY + "'")
    public void clearInstrumentsCache() {
        logger.info("🗑️  Cache de instrumentos limpiada manualmente");
    }

    @CacheEvict(value = INSTRUMENTS_CACHE, allEntries = true)
    public void clearAllCache() {
        logger.info("🗑️  Toda la cache de instrumentos limpiada");
    }

    private List<String> getStaticInstrumentsList() {
        return Arrays.asList(
            // Tech Giants
            "AAPL", "GOOGL", "GOOG", "MSFT", "META", "AMZN", "TSLA", "NVDA", "NFLX", "ORCL", 
            "CRM", "ADBE", "INTC", "AMD", "NOW", "UBER", "SHOP", "ZM", "PINS", "SNAP",
            
            // Financial Services  
            "JPM", "BAC", "WFC", "GS", "MS", "V", "MA", "PYPL", "SQ", "C", "USB", "PNC", 
            "TFC", "COF", "AXP", "BLK", "SPGI", "CME", "ICE", "SCHW",
            
            // Healthcare & Pharmaceuticals
            "JNJ", "PFE", "UNH", "ABBV", "MRK", "TMO", "ABT", "DHR", "BMY", "AMGN", "GILD", 
            "LLY", "CVS", "ANTM", "CI", "HUM", "CNC", "MOH", "EW", "ISRG",
            
            // Consumer Goods & Retail
            "WMT", "HD", "PG", "KO", "PEP", "DIS", "NKE", "MCD", "SBUX", "TGT", "LOW", 
            "COST", "BKNG", "ABNB", "EBAY", "ETSY", "W", "CHWY", "PTON", "LULU",
            
            // Energy & Utilities
            "XOM", "CVX", "COP", "EOG", "SLB", "PSX", "VLO", "MPC", "KMI", "WMB", 
            "NEE", "DUK", "SO", "AEP", "EXC", "XEL", "WEC", "ES", "AWK", "D",
            
            // Industrial & Transportation
            "CAT", "HON", "MMM", "GE", "RTX", "LMT", "BA", "UPS", "FDX", "CSX", 
            "UNP", "NSC", "DAL", "AAL", "UAL", "LUV", "JBLU", "ALK", "HA", "SAVE",
            
            // Real Estate & REITs
            "AMT", "PLD", "CCI", "EQIX", "PSA", "EXR", "WELL", "DLR", "SPG", "O", 
            "REYN", "AVB", "EQR", "UDR", "CPT", "MAA", "ESS", "AIV", "PEAK", "BXP",
            
            // Materials & Chemicals
            "LIN", "APD", "ECL", "SHW", "NEM", "FCX", "CTVA", "DOW", "LYB", "PPG", 
            "NUE", "STLD", "CLF", "AA", "X", "MT", "VALE", "RIO", "BHP", "SCCO",
            
            // Telecommunications
            "VZ", "T", "TMUS", "CHTR", "CMCSA", "ROKU", "SIRI", "LBRDK"
        );
    }

    private List<String> getBasicInstrumentsList() {
        return Arrays.asList(
            "AAPL", "GOOGL", "MSFT", "AMZN", "TSLA", "META", "NVDA", "NFLX", 
            "JPM", "V", "MA", "JNJ", "WMT", "PG", "HD", "KO", "PEP", "VZ", "T"
        );
    }
}
