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
        assertNull(tactic.getPredictModelId(bot));
    }

    @Test
    void testAction() {
        when(bot.getBattleManager()).thenReturn(battleManager);
        when(battleManager.getZeroEnergyEnemy(bot)).thenReturn(null);
        when(battleManager.getNearestAliveEnemy(bot)).thenReturn(null);

        when(bot.getArenaMap()).thenReturn(arenaMap);
        tactic.action(bot);

        assertEquals(RadarOperator.ActionId.ALL_SCAN, tactic.getRadarActionId(bot));
        assertEquals(Gunner.ActionId.SCAN, tactic.getGunActionId(bot));
    }
}
