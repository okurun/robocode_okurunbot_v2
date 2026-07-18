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
import okurun.enemymanager.EnemyProfile;
import okurun.enemymanager.EnemyState;

@ExtendWith(MockitoExtension.class)
class ZigzagPredictModelTest {
    private ZigzagPredictModel model;

    @Mock
    private OkuRunBot bot;

    @Mock
    private EnemyProfile enemyProfile;

    @BeforeEach
    void setUp() {
        model = new ZigzagPredictModel();
    }

    @Test
    void testCanPredictReturnsFalseWhenNotEnoughHistory() {
        Deque<EnemyState> history = new ArrayDeque<>();
        when(enemyProfile.getStateHistory()).thenReturn(history);
        assertFalse(model.canPredict(bot, enemyProfile));
    }

    @Test
    void testCanPredictReturnsFalseWhenZigzagging() {
        Deque<EnemyState> history = new ArrayDeque<>();
        when(enemyProfile.getStateHistory()).thenReturn(history);
        when(bot.getTurnNumber()).thenReturn(13);

        // 旋回方向（turnDegree）が変わらない履歴を用意
        history.add(new EnemyState(1, 12, 0, 0, 0, 0, 100, 5.0, 0, 0)); // Turn LEFT
        history.add(new EnemyState(1, 11, 0, 0, 0, 0, 100, 5.0, 0, 0)); // Turn LEFT
        history.add(new EnemyState(1, 10, 0, 0, 0, 0, 100, 5.0, 0, 0)); // Turn LEFT
        history.add(new EnemyState(1, 9, 0, 0, 0, 0, 100, 5.0, 0, 0)); // Turn LEFT
        history.add(new EnemyState(1, 8, 0, 0, 0, 0, 100, 5.0, 0, 0)); // Turn LEFT
        history.add(new EnemyState(1, 7, 0, 0, 0, 0, 100, 5.0, 0, 0)); // Turn LEFT

        assertFalse(model.canPredict(bot, enemyProfile));
    }

    @Test
    void testCanPredictReturnsFalseWhenZigzagging2() {
        Deque<EnemyState> history = new ArrayDeque<>();
        when(enemyProfile.getStateHistory()).thenReturn(history);
        when(bot.getTurnNumber()).thenReturn(13);

        // 旋回方向（turnDegree）が+,-と交互に変わる回数が少ない履歴を用意
        history.add(new EnemyState(1, 12, 0, 0, 0, 0, 100, 5.0, 0, 0)); // Turn LEFT
        history.add(new EnemyState(1, 11, 0, 0, 0, 0, 100, 5.0, 0, 0)); // Turn LEFT
        history.add(new EnemyState(1, 10, 0, 0, 0, 0, 100, 5.0, 0, 0)); // Turn LEFT
        history.add(new EnemyState(1, 9, 0, 0, 0, 0, 100, -5.0, 0, 0)); // Turn RIGHT
        history.add(new EnemyState(1, 8, 0, 0, 0, 0, 100, 5.0, 0, 0)); // Turn LEFT
        history.add(new EnemyState(1, 7, 0, 0, 0, 0, 100, 5.0, 0, 0)); // Turn LEFT

        assertFalse(model.canPredict(bot, enemyProfile));
    }

    @Test
    void testCanPredictReturnsTrueWhenZigzagging() {
        Deque<EnemyState> history = new ArrayDeque<>();
        when(enemyProfile.getStateHistory()).thenReturn(history);
        when(bot.getTurnNumber()).thenReturn(13);

        // 旋回方向（turnDegree）が+,-と交互に変わる履歴を用意
        history.add(new EnemyState(1, 12, 0, 0, 0, 0, 100, 5.0, 0, 0)); // Turn LEFT
        history.add(new EnemyState(1, 11, 0, 0, 0, 0, 100, -5.0, 0, 0)); // Turn RIGHT
        history.add(new EnemyState(1, 10, 0, 0, 0, 0, 100, 5.0, 0, 0)); // Turn LEFT
        history.add(new EnemyState(1, 9, 0, 0, 0, 0, 100, -5.0, 0, 0)); // Turn RIGHT
        history.add(new EnemyState(1, 8, 0, 0, 0, 0, 100, 5.0, 0, 0)); // Turn LEFT
        history.add(new EnemyState(1, 7, 0, 0, 0, 0, 100, -5.0, 0, 0)); // Turn RIGHT

        assertTrue(model.canPredict(bot, enemyProfile));
    }

    @Test
    void testCanPredictReturnsTrueWhenZigzagging2() {
        Deque<EnemyState> history = new ArrayDeque<>();
        when(enemyProfile.getStateHistory()).thenReturn(history);
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

        assertTrue(model.canPredict(bot, enemyProfile));
    }
}
