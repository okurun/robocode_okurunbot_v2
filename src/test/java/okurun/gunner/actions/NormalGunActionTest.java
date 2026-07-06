package okurun.gunner.actions;

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
import okurun.commander.Commander;
import okurun.predictor.Predictor;

@ExtendWith(MockitoExtension.class)
class NormalGunActionTest {
    private NormalGunAction action;

    @Mock
    private OkuRunBot bot;

    @Mock
    private Commander commander;

    @Mock
    private BattleManager battleManager;

    @BeforeEach
    void setUp() {
        action = new NormalGunAction();
    }

    @Test
    void testActionReturnsScanGunActionWhenNoTarget() {
        when(bot.getCommander()).thenReturn(commander);
        when(commander.getTargetEnemyId(bot)).thenReturn(Commander.NO_TARGET);

        String result = action.action(bot);
        assertEquals(ScanGunAction.class.getName(), result);
    }

    @Test
    void testActionReturnsScanGunActionWhenTargetProfileNull() {
        when(bot.getCommander()).thenReturn(commander);
        when(commander.getTargetEnemyId(bot)).thenReturn(1);
        when(bot.getBattleManager()).thenReturn(battleManager);
        when(battleManager.getEnemyProfile(1)).thenReturn(null);

        String result = action.action(bot);
        assertEquals(ScanGunAction.class.getName(), result);
    }
}
