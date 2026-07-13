package okurun.battlemanager;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EnemyProfileTest {

    @Test
    void testInitialization() {
        EnemyProfile profile = new EnemyProfile(2);
        assertEquals(2, profile.getId());
        assertTrue(profile.isAlive());
        assertEquals(0, profile.getLastConfirmedTurn());
        assertNull(profile.getLatestState());
        assertTrue(profile.getStateHistory().isEmpty());
    }
}
