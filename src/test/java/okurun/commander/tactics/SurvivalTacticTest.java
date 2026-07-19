package okurun.commander.tactics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;

import okurun.OkuRunBot;
import okurun.arenamap.ArenaMap;
import okurun.commander.Commander;
import okurun.enemymanager.EnemyManager;
import okurun.predictor.Predictor;

@ExtendWith(MockitoExtension.class)
class SurvivalTacticTest {

    private SurvivalTactic tactic;

    @Mock
    private OkuRunBot bot;

    @Mock
    private ArenaMap arenaMap;

    @Mock
    private EnemyManager enemyManager;

    @BeforeEach
    void setUp() {
        tactic = new SurvivalTactic();
    }

    @Test
    void testGetters() {
        assertEquals(Commander.NO_TARGET, tactic.getTargetEnemyId(bot));
        assertEquals(Predictor.PredictModelId.SIMPLE, tactic.getPredictModelId(bot));
    }
}
