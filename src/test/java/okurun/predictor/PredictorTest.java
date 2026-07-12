package okurun.predictor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.robocode.tankroyale.botapi.events.GameStartedEvent;
import okurun.OkuRunBot;
import okurun.predictor.Predictor.Model;
import okurun.predictor.models.*;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;

@ExtendWith(MockitoExtension.class)
class PredictorTest {
    private Predictor predictor;

    @Mock
    private OkuRunBot bot;

    @Mock
    private GameStartedEvent gameStartedEvent;

    @BeforeEach
    void setUp() {
        predictor = new Predictor();
        predictor.onGameStarted(gameStartedEvent, bot);
    }

    @Test
    void testGetPredictModel() {
        PredictModel model;

        model = predictor.getPredictModel(Model.SIMPLE);
        assertNotNull(model);
        assertTrue(model instanceof SimplePredictModel);

        model = predictor.getPredictModel(Model.ZIGZAG);
        assertNotNull(model);
        assertTrue(model instanceof ZigzagPredictModel);
    }

    @Test
    void testCalcPositionLinear() {
        // heading=0 (右向き), velocity=1, diffTurnNum=5
        double[] pos = Predictor.calcPosition(0.0, 0.0, 0.0, 1.0, 5);
        // Math.cos(0)=1, Math.sin(0)=0
        // x = 0 + 1 * 1 * 5 = 5
        // y = 0 + 1 * 0 * 5 = 0
        assertArrayEquals(new double[]{5.0, 0.0}, pos, 0.0001);

        // heading=90 (上向き)
        double[] pos2 = Predictor.calcPosition(0.0, 0.0, 90.0, 1.0, 5);
        // Math.cos(90)=0, Math.sin(90)=1
        // x = 0 + 1 * 0 * 5 = 0
        // y = 0 + 1 * 1 * 5 = 50
        assertArrayEquals(new double[]{0.0, 5.0}, pos2, 0.0001);
    }
    
    @Test
    void testCalcPositionWithTurn() {
        // 初期状態から90度旋回しながら移動
        // 1ターン目: 角度は 0 + 90 = 90度 (上向き) になり、そのまま移動
        double[] pos = Predictor.calcPosition(0.0, 0.0, 0.0, 1.0, 90.0, 1);
        assertArrayEquals(new double[]{0.0, 1.0}, pos, 0.0001);
        
        // 4ターンかけて1周
        // 1ターン目: (0, 1), heading=90
        // 2ターン目: (-1, 1), heading=180
        // 3ターン目: (-1, 0), heading=270
        // 4ターン目: (0, 0), heading=360
        double[] pos4 = Predictor.calcPosition(0.0, 0.0, 0.0, 1.0, 90.0, 4);
        assertArrayEquals(new double[]{0.0, 0.0}, pos4, 0.0001);
    }
}
