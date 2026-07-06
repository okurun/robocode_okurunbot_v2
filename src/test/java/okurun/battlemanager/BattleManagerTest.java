package okurun.battlemanager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import dev.robocode.tankroyale.botapi.events.BotDeathEvent;
import okurun.OkuRunBot;

@ExtendWith(MockitoExtension.class)
class BattleManagerTest {
    private BattleManager battleManager;

    @Mock
    private OkuRunBot bot;

    @Mock
    private BotDeathEvent botDeathEvent;

    @BeforeEach
    void setUp() {
        battleManager = new BattleManager();
        when(bot.getEnemyCount()).thenReturn(2);
        when(bot.getMyId()).thenReturn(1);
        battleManager.init(bot);
    }

    @Test
    void testInit() {
        // myId=1, enemyCount=2 -> enemyId is 2, 3
        assertEquals(2, battleManager.getEnemyCount());
        assertNotNull(battleManager.getEnemyProfile(2));
        assertNotNull(battleManager.getEnemyProfile(3));
        assertNull(battleManager.getEnemyProfile(1)); // myId is skipped
    }

    @Test
    void testOnBotDeath() {
        EnemyProfile profile = battleManager.getEnemyProfile(2);
        assertTrue(profile.isAlive());

        when(botDeathEvent.getVictimId()).thenReturn(2);
        battleManager.onBotDeath(botDeathEvent, bot);

        assertFalse(profile.isAlive());
    }
}
