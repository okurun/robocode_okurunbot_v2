package okurun.predictor;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PredictionAccuracyTest {

    @Test
    void testInitialState() {
        PredictionAccuracy accuracy = new PredictionAccuracy();
        assertEquals("fireCount: 0, hitRate: 0.0%, missRate: 0.0%", accuracy.getAccuracyString());
    }

    @Test
    void testIncrementsAndRates() {
        PredictionAccuracy accuracy = new PredictionAccuracy();
        
        accuracy.incrementFireCount();
        accuracy.incrementFireCount();
        accuracy.incrementHitCount();
        
        // 2発撃って1発ヒット（50%）
        assertEquals("fireCount: 2, hitRate: 50.0%, missRate: 0.0%", accuracy.getAccuracyString());
        
        accuracy.incrementFireCount();
        accuracy.incrementFireCount();
        accuracy.incrementMissCount();
        
        // 4発撃って1発ヒット（25%）、1発ミス（25%）
        assertEquals("fireCount: 4, hitRate: 25.0%, missRate: 25.0%", accuracy.getAccuracyString());
    }
    
    @Test
    void testRounding() {
        PredictionAccuracy accuracy = new PredictionAccuracy();
        for (int i = 0; i < 3; i++) {
            accuracy.incrementFireCount();
        }
        accuracy.incrementHitCount();
        
        // 3発撃って1発ヒット（33.333...%） -> 33.3%になるべき
        assertEquals("fireCount: 3, hitRate: 33.3%, missRate: 0.0%", accuracy.getAccuracyString());
    }
}
