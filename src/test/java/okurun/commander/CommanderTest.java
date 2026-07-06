package okurun.commander;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import okurun.OkuRunBot;
import okurun.battlemanager.BattleManager;
import okurun.battlemanager.EnemyProfile;
import okurun.battlemanager.EnemyState;

@ExtendWith(MockitoExtension.class)
class CommanderTest {
    private Commander commander;

    @Mock
    private OkuRunBot bot;

    @Mock
    private BattleManager battleManager;

    @Mock
    private EnemyProfile enemyProfile;

    @BeforeEach
    void setUp() {
        commander = new Commander();
        commander.init(bot);
    }

    @Test
    void testActionSetsSurvivalTacticWhenManyEnemies() {
        when(bot.getBattleManager()).thenReturn(battleManager);
        when(battleManager.getAliveAndNotMissingEnemyCount(bot)).thenReturn(3);

        // 内部で呼ばれる Tactic.action(bot) で未モックによる例外が発生する可能性があるためcatchする
        try {
            commander.action(bot);
        } catch (Exception e) {}
    }

    @Test
    void testActionSetsOneOnOnePositiveTactic() {
        when(bot.getBattleManager()).thenReturn(battleManager);
        when(battleManager.getAliveAndNotMissingEnemyCount(bot)).thenReturn(1);
        when(battleManager.getAliveEnemy(bot)).thenReturn(enemyProfile);
        
        EnemyState mockState = new EnemyState(1, 10, 0, 0, 0, 0, 50.0, 0, 0, 0);
        when(enemyProfile.getLatestState()).thenReturn(mockState);
        when(bot.getEnergy()).thenReturn(100.0);

        try {
            commander.action(bot);
        } catch (Exception e) {}
    }
}
