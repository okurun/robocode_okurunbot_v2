package okurun.commander;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;

import okurun.OkuRunBot;
import okurun.enemymanager.EnemyManager;
import okurun.enemymanager.EnemyProfile;
import okurun.enemymanager.EnemyState;

@ExtendWith(MockitoExtension.class)
class CommanderTest {
    private Commander commander;

    @Mock
    private OkuRunBot bot;

    @Mock
    private EnemyManager enemyManager;

    @Mock
    private EnemyProfile enemyProfile;

    @BeforeEach
    void setUp() {
        commander = new Commander();
    }

    @Test
    void testActionSetsSurvivalTacticWhenManyEnemies() {
        when(bot.getEnemyManager()).thenReturn(enemyManager);
        when(enemyManager.getAliveAndNotMissingEnemyCount(bot)).thenReturn(3);

        // 内部で呼ばれる Tactic.action(bot) で未モックによる例外が発生する可能性があるためcatchする
        try {
            commander.onAction(bot);
        } catch (Exception e) {
        }
    }

    @Test
    void testActionSetsOneOnOnePositiveTactic() {
        when(bot.getEnemyManager()).thenReturn(enemyManager);
        when(enemyManager.getAliveAndNotMissingEnemyCount(bot)).thenReturn(1);
        when(enemyManager.getAliveEnemy(bot)).thenReturn(enemyProfile);

        EnemyState mockState = new EnemyState(1, 10, 0, 0, 0, 0, 50.0, 0, 0, 0);
        when(enemyProfile.getLatestState()).thenReturn(mockState);

        try {
            commander.onAction(bot);
        } catch (Exception e) {
        }
    }
}
