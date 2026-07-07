package okurun.predictor.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayDeque;
import java.util.Deque;

import okurun.OkuRunBot;
import okurun.battlemanager.EnemyState;

@ExtendWith(MockitoExtension.class)
class HistoryPredictModelTest {
    // private HistoryPredictModel model;

    @Mock
    private OkuRunBot bot;

    @BeforeEach
    void setUp() {
        // model = new HistoryPredictModel();
    }

    @Test
    void testCanUseReturnsFalseWhenNotEnoughHistory() {
        Deque<EnemyState> history = new ArrayDeque<>();
        assertFalse(HistoryPredictModel.canUse(bot, history));
    }

    @Test
    void testCanUseReturnsTrueWhenZigzagging() {
        Deque<EnemyState> history = new ArrayDeque<>();
        when(bot.getTurnNumber()).thenReturn(10);
        
        // 旋回方向（turnDegree）が+,-と交互に変わる履歴を用意
        history.add(new EnemyState(1, 10, 0, 0, 0, 0, 100, 5.0, 0, 0)); // Turn LEFT
        history.add(new EnemyState(1, 9, 0, 0, 0, 0, 100, -5.0, 0, 0)); // Turn RIGHT
        history.add(new EnemyState(1, 8, 0, 0, 0, 0, 100, 5.0, 0, 0));  // Turn LEFT
        history.add(new EnemyState(1, 7, 0, 0, 0, 0, 100, -5.0, 0, 0)); // Turn RIGHT
        
        assertTrue(HistoryPredictModel.canUse(bot, history));
    }
}
