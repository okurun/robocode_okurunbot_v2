package okurun.battlemanager;

import org.junit.jupiter.api.Test;
import java.util.Deque;
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

    @Test
    void testDied() {
        EnemyProfile profile = new EnemyProfile(1);
        profile.died();
        assertFalse(profile.isAlive());
    }

    @Test
    void testAddStateAndHistoryManagement() {
        EnemyProfile profile = new EnemyProfile(1);
        
        // 最初の状態を追加
        EnemyState state1 = new EnemyState(1, 10, 100, 100, 0, 0, 100, 0, 0, 50);
        profile.addState(state1);
        
        assertEquals(10, profile.getLastConfirmedTurn());
        assertEquals(state1, profile.getLatestState());
        assertEquals(1, profile.getStateHistory().size());
        
        // さらに35個追加して履歴が30個に制限されることを確認する
        for (int i = 11; i <= 45; i++) {
            profile.addState(new EnemyState(1, i, 100, 100, 0, 0, 100, 0, 0, 50));
        }
        
        assertEquals(45, profile.getLastConfirmedTurn());
        assertEquals(45, profile.getLatestState().scannedTurnNum);
        
        Deque<EnemyState> history = profile.getStateHistory();
        assertEquals(30, history.size()); // Max capacity is 30
        assertEquals(45, history.getFirst().scannedTurnNum);
        assertEquals(16, history.getLast().scannedTurnNum);
    }
}
