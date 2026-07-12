package okurun.predictor.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import okurun.OkuRunBot;
import okurun.battlemanager.EnemyState;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayDeque;
import java.util.Deque;

@ExtendWith(MockitoExtension.class)
class SimplePredictModelTest {

    private SimplePredictModel model;

    @Mock
    private OkuRunBot bot;

    @BeforeEach
    void setUp() {
        model = new SimplePredictModel();
    }

    @Test
    void testNextTurnState_ReturnsNullWhenNoHistory() {
        Deque<EnemyState> history = new ArrayDeque<>();
        EnemyState currentState = new EnemyState(1, 10, 100, 100, 90, 5, 100, 0, 0, 100);
        
        assertNull(model.nextTurnState(bot, currentState, history));
    }

    @Test
    void testNextTurnState_ConstantLinearMotion() {
        Deque<EnemyState> history = new ArrayDeque<>();
        history.add(new EnemyState(1, 9, 100, 100, 90, 5, 100, 0, 0, 100));
        
        // velocity = 5.0, heading = 90
        EnemyState currentState = new EnemyState(1, 10, 100, 100, 90, 5.0, 100, 10, 1.0, 100);
        
        EnemyState nextState = model.nextTurnState(bot, currentState, history);
        assertNotNull(nextState);
        assertEquals(currentState.scannedTurnNum + 1, nextState.scannedTurnNum);
        
        // Velocity and heading remain the same. Turn degree is 0. Acceleration is 0.
        assertEquals(5.0, nextState.velocity, 0.001);
        assertEquals(90.0, nextState.heading, 0.001);
        assertEquals(0.0, nextState.turnDegree, 0.001);
        assertEquals(0.0, nextState.acceleration, 0.001);
    }
}
