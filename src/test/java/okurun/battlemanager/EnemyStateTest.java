package okurun.battlemanager;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EnemyStateTest {

    @Test
    void testInitializationAndGetPosition() {
        EnemyState state = new EnemyState(1, 100, 10.5, 20.5, 45.0, 8.0, 100.0, 1.5, 0.5, 150.0);
        
        assertEquals(1, state.id);
        assertEquals(100, state.scannedTurnNum); // original typo 'scandTurnNum' is used as-is
        assertEquals(10.5, state.x);
        assertEquals(20.5, state.y);
        assertEquals(45.0, state.heading);
        assertEquals(8.0, state.velocity);
        assertEquals(100.0, state.energy);
        assertEquals(1.5, state.turnDegree);
        assertEquals(0.5, state.acceleration);
        assertEquals(150.0, state.distance);
        
        double[] pos = state.getPosition();
        assertArrayEquals(new double[]{10.5, 20.5}, pos, 0.0001);
    }
}
