package okurun.commander.tactics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import okurun.OkuRunBot;
import okurun.arenamap.ArenaMap;
import okurun.battlemanager.BattleManager;
import okurun.commander.Commander;
import okurun.commander.Commander.AccelePriority;
import okurun.commander.Commander.HandlePriority;
import okurun.gunner.Gunner;
import okurun.radaroperator.RadarOperator;

@ExtendWith(MockitoExtension.class)
class SurvivalTacticTest {

    private SurvivalTactic tactic;

    @Mock
    private OkuRunBot bot;

    @Mock
    private ArenaMap arenaMap;

    @Mock
    private BattleManager battleManager;

    @BeforeEach
    void setUp() {
        tactic = new SurvivalTactic();
    }

    @Test
    void testGetters() {
        assertEquals(Commander.NO_TARGET, tactic.getTargetEnemyId(bot));
        assertEquals(HandlePriority.AVOID_BULLET, tactic.getHandlePriority(bot));
        assertEquals(AccelePriority.MAX_SPEED, tactic.getAccelePriority(bot));
        assertNull(tactic.getPredictModel(bot));
        assertTrue(tactic.getMinSpeed(bot) > 0);
    }

    @Test
    void testAction() {
        when(bot.getBattleManager()).thenReturn(battleManager);
        when(battleManager.getZeroEnergyEnemy(bot)).thenReturn(null);
        when(battleManager.getNearestAliveEnemy(bot)).thenReturn(null);

        when(bot.getArenaMap()).thenReturn(arenaMap);
        tactic.action(bot);

        assertEquals(RadarOperator.ActionId.ALL_SCAN, tactic.getRadarAction(bot));
        assertEquals(Gunner.ActionId.SCAN, tactic.getGunActionName(bot));
    }
}
