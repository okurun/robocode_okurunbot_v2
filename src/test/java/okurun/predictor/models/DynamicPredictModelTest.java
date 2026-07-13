package okurun.predictor.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

import dev.robocode.tankroyale.botapi.Constants;
import okurun.OkuRunBot;
import okurun.battlemanager.EnemyProfile;
import okurun.battlemanager.EnemyState;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayDeque;
import java.util.Deque;

@ExtendWith(MockitoExtension.class)
class DynamicPredictModelTest {

    private DynamicPredictModel model;

    @Mock
    private OkuRunBot bot;

    @Mock
    private EnemyProfile enemyProfile;

    @BeforeEach
    void setUp() {
        model = new DynamicPredictModel();
    }

    @Test
    void testNextTurnState_ReturnsNullWhenNoHistory() {
        Deque<EnemyState> history = new ArrayDeque<>();
        EnemyState currentState = new EnemyState(1, 10, 100, 100, 90, 5, 100, 0, 0, 100);
        when(enemyProfile.getStateHistory()).thenReturn(history);
        
        assertNull(model.nextTurnState(bot, currentState, enemyProfile));
    }

    @Test
    void testNextTurnState_AccelerateForward() {
        Deque<EnemyState> history = new ArrayDeque<>();
        history.add(new EnemyState(1, 9, 100, 100, 90, 4, 100, 0, 0, 100));
        when(enemyProfile.getStateHistory()).thenReturn(history);
        
        // velocity > 0, acceleration > 0
        EnemyState currentState = new EnemyState(1, 10, 100, 100, 90, 5.0, 100, 0, 1.0, 100);
        
        EnemyState nextState = model.nextTurnState(bot, currentState, enemyProfile);
        assertNotNull(nextState);
        assertEquals(currentState.scannedTurnNum + 1, nextState.scannedTurnNum);
        // velocity += 1.0 (Constants.ACCELERATION)
        assertEquals(6.0, nextState.velocity, 0.001);
    }

    @Test
    void testNextTurnState_DecelerateForward() {
        Deque<EnemyState> history = new ArrayDeque<>();
        history.add(new EnemyState(1, 9, 100, 100, 90, 6, 100, 0, 0, 100));
        when(enemyProfile.getStateHistory()).thenReturn(history);
        
        // velocity > 0, acceleration < 0
        EnemyState currentState = new EnemyState(1, 10, 100, 100, 90, 5.0, 100, 0, -1.0, 100);
        
        EnemyState nextState = model.nextTurnState(bot, currentState, enemyProfile);
        assertNotNull(nextState);
        // Constants.DECELERATION is -2.0, so velocity += -2.0
        assertEquals(3.0, nextState.velocity, 0.001);
    }

    @Test
    void testNextTurnState_AccelerateBackward() {
        Deque<EnemyState> history = new ArrayDeque<>();
        history.add(new EnemyState(1, 9, 100, 100, 90, -4, 100, 0, 0, 100));
        when(enemyProfile.getStateHistory()).thenReturn(history);
        
        // velocity < 0, acceleration < 0 (this means speed magnitude is increasing)
        EnemyState currentState = new EnemyState(1, 10, 100, 100, 90, -5.0, 100, 0, -1.0, 100);
        
        EnemyState nextState = model.nextTurnState(bot, currentState, enemyProfile);
        assertNotNull(nextState);
        // velocity -= Constants.ACCELERATION (1.0), so velocity becomes -6.0
        assertEquals(-6.0, nextState.velocity, 0.001);
    }

    @Test
    void testNextTurnState_DecelerateBackward() {
        Deque<EnemyState> history = new ArrayDeque<>();
        history.add(new EnemyState(1, 9, 100, 100, 90, -6, 100, 0, 0, 100));
        when(enemyProfile.getStateHistory()).thenReturn(history);
        
        // velocity < 0, acceleration > 0 (this means speed magnitude is decreasing)
        EnemyState currentState = new EnemyState(1, 10, 100, 100, 90, -5.0, 100, 0, 1.0, 100);
        
        EnemyState nextState = model.nextTurnState(bot, currentState, enemyProfile);
        assertNotNull(nextState);
        // velocity -= Constants.DECELERATION (-2.0), so velocity += 2.0 -> -3.0
        assertEquals(-3.0, nextState.velocity, 0.001);
    }

    @Test
    void testNextTurnState_MaxSpeedLimit() {
        Deque<EnemyState> history = new ArrayDeque<>();
        history.add(new EnemyState(1, 9, 100, 100, 90, 7.5, 100, 0, 0, 100));
        when(enemyProfile.getStateHistory()).thenReturn(history);
        
        // acceleration > 0
        EnemyState currentState = new EnemyState(1, 10, 100, 100, 90, 7.5, 100, 0, 1.0, 100);
        
        EnemyState nextState = model.nextTurnState(bot, currentState, enemyProfile);
        assertNotNull(nextState);
        // 7.5 + 1.0 = 8.5 -> clamped to 8.0 (Constants.MAX_SPEED)
        assertEquals(Constants.MAX_SPEED, nextState.velocity, 0.001);
    }

    @Test
    void testNextTurnState_MinSpeedLimit() {
        Deque<EnemyState> history = new ArrayDeque<>();
        history.add(new EnemyState(1, 9, 100, 100, 90, -7.5, 100, 0, 0, 100));
        when(enemyProfile.getStateHistory()).thenReturn(history);
        
        // velocity < 0, acceleration < 0
        EnemyState currentState = new EnemyState(1, 10, 100, 100, 90, -7.5, 100, 0, -1.0, 100);
        
        EnemyState nextState = model.nextTurnState(bot, currentState, enemyProfile);
        assertNotNull(nextState);
        // -7.5 - 1.0 = -8.5 -> clamped to -8.0 (-Constants.MAX_SPEED)
        assertEquals(-Constants.MAX_SPEED, nextState.velocity, 0.001);
    }
}
