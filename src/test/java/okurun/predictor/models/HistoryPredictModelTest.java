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
        assertFalse(ZigzagPredictModel.canUse(bot, history));
    }

    @Test
    void testCanUseReturnsFalseWhenZigzagging() {
        Deque<EnemyState> history = new ArrayDeque<>();
        when(bot.getTurnNumber()).thenReturn(13);

        // 旋回方向（turnDegree）が変わらない履歴を用意
        history.add(new EnemyState(1, 12, 0, 0, 0, 0, 100, 5.0, 0, 0)); // Turn LEFT
        history.add(new EnemyState(1, 11, 0, 0, 0, 0, 100, 5.0, 0, 0)); // Turn LEFT
        history.add(new EnemyState(1, 10, 0, 0, 0, 0, 100, 5.0, 0, 0)); // Turn LEFT
        history.add(new EnemyState(1, 9, 0, 0, 0, 0, 100, 5.0, 0, 0)); // Turn LEFT
        history.add(new EnemyState(1, 8, 0, 0, 0, 0, 100, 5.0, 0, 0)); // Turn LEFT
        history.add(new EnemyState(1, 7, 0, 0, 0, 0, 100, 5.0, 0, 0)); // Turn LEFT

        assertFalse(ZigzagPredictModel.canUse(bot, history));
    }

    @Test
    void testCanUseReturnsFalseWhenZigzagging2() {
        Deque<EnemyState> history = new ArrayDeque<>();
        when(bot.getTurnNumber()).thenReturn(13);

        // 旋回方向（turnDegree）が+,-と交互に変わる回数が少ない履歴を用意
        history.add(new EnemyState(1, 12, 0, 0, 0, 0, 100, 5.0, 0, 0)); // Turn LEFT
        history.add(new EnemyState(1, 11, 0, 0, 0, 0, 100, 5.0, 0, 0)); // Turn LEFT
        history.add(new EnemyState(1, 10, 0, 0, 0, 0, 100, 5.0, 0, 0)); // Turn LEFT
        history.add(new EnemyState(1, 9, 0, 0, 0, 0, 100, -5.0, 0, 0)); // Turn RIGHT
        history.add(new EnemyState(1, 8, 0, 0, 0, 0, 100, 5.0, 0, 0)); // Turn LEFT
        history.add(new EnemyState(1, 7, 0, 0, 0, 0, 100, 5.0, 0, 0)); // Turn LEFT

        assertFalse(ZigzagPredictModel.canUse(bot, history));
    }

    @Test
    void testCanUseReturnsTrueWhenZigzagging() {
        Deque<EnemyState> history = new ArrayDeque<>();
        when(bot.getTurnNumber()).thenReturn(13);

        // 旋回方向（turnDegree）が+,-と交互に変わる履歴を用意
        history.add(new EnemyState(1, 12, 0, 0, 0, 0, 100, 5.0, 0, 0)); // Turn LEFT
        history.add(new EnemyState(1, 11, 0, 0, 0, 0, 100, -5.0, 0, 0)); // Turn RIGHT
        history.add(new EnemyState(1, 10, 0, 0, 0, 0, 100, 5.0, 0, 0)); // Turn LEFT
        history.add(new EnemyState(1, 9, 0, 0, 0, 0, 100, -5.0, 0, 0)); // Turn RIGHT
        history.add(new EnemyState(1, 8, 0, 0, 0, 0, 100, 5.0, 0, 0)); // Turn LEFT
        history.add(new EnemyState(1, 7, 0, 0, 0, 0, 100, -5.0, 0, 0)); // Turn RIGHT

        assertTrue(ZigzagPredictModel.canUse(bot, history));
    }

    @Test
    void testCanUseReturnsTrueWhenZigzagging2() {
        Deque<EnemyState> history = new ArrayDeque<>();
        when(bot.getTurnNumber()).thenReturn(15);

        // 旋回方向（turnDegree）が+,-と交互にゆっくり変わる履歴を用意
        history.add(new EnemyState(1, 14, 0, 0, 0, 0, 100, 5.0, 0, 0)); // Turn LEFT
        history.add(new EnemyState(1, 13, 0, 0, 0, 0, 100, 0, 0, 0)); // Turn STRAIGHT
        history.add(new EnemyState(1, 12, 0, 0, 0, 0, 100, -5.0, 0, 0)); // Turn RIGHT
        history.add(new EnemyState(1, 11, 0, 0, 0, 0, 100, 0, 0, 0)); // Turn STRAIGHT
        history.add(new EnemyState(1, 10, 0, 0, 0, 0, 100, 5.0, 0, 0)); // Turn LEFT
        history.add(new EnemyState(1, 9, 0, 0, 0, 0, 100, 0, 0, 0)); // Turn STRAIGHT
        history.add(new EnemyState(1, 8, 0, 0, 0, 0, 100, -5.0, 0, 0)); // Turn RIGHT
        history.add(new EnemyState(1, 7, 0, 0, 0, 0, 100, 0, 0, 0)); // Turn STRAIGHT
        history.add(new EnemyState(1, 6, 0, 0, 0, 0, 100, 5.0, 0, 0)); // Turn LEFT
        history.add(new EnemyState(1, 5, 0, 0, 0, 0, 100, 0, 0, 0)); // Turn STRAIGHT
        history.add(new EnemyState(1, 4, 0, 0, 0, 0, 100, -5.0, 0, 0)); // Turn RIGHT
        history.add(new EnemyState(1, 3, 0, 0, 0, 0, 100, 0, 0, 0)); // Turn STRAIGHT

        assertTrue(ZigzagPredictModel.canUse(bot, history));
    }
}
