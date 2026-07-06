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
import okurun.gunner.actions.ScanGunAction;
import okurun.radaroperator.actions.AllScanRadarAction;

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
        assertNull(tactic.getPredictorModelName(bot));
        assertTrue(tactic.getMinSpeed(bot) > 0);
    }

    @Test
    void testAction() {
        when(bot.getBattleManager()).thenReturn(battleManager);
        when(battleManager.getZeroEnergyEnemy(bot)).thenReturn(null);
        when(battleManager.getNearestAliveEnemy(bot)).thenReturn(null);
        
        when(bot.getArenaMap()).thenReturn(arenaMap);
        ArenaMap.Area safeArea = mock(ArenaMap.Area.class);
        when(arenaMap.getSafeArea(bot)).thenReturn(safeArea);
        when(safeArea.getCenter()).thenReturn(new double[]{100.0, 100.0});
        when(bot.getPosition()).thenReturn(new double[]{200.0, 200.0});
        
        tactic.action(bot);
        
        assertEquals(AllScanRadarAction.class.getName(), tactic.getRadarActionName(bot));
        assertEquals(ScanGunAction.class.getName(), tactic.getGunActionName(bot));
    }
}
