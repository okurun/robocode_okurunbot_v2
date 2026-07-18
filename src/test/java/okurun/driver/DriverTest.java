package okurun.driver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.robocode.tankroyale.botapi.events.GameStartedEvent;

import static org.mockito.Mockito.*;

import okurun.OkuRunBot;
import okurun.commander.Commander;
import okurun.battlemanager.BattleManager;

@ExtendWith(MockitoExtension.class)
class DriverTest {
    private Driver driver;

    @Mock
    private OkuRunBot bot;

    @Mock
    private Commander commander;

    @Mock
    private BattleManager battleManager;

    @Mock
    private GameStartedEvent gameStartedEvent;

    @BeforeEach
    void setUp() {
        driver = new Driver();
        driver.onGameStarted(gameStartedEvent, bot);
    }

    @Test
    void testActionDelegatesToCommander() {
        when(bot.getCommander()).thenReturn(commander);
        when(commander.getDriveAction(bot)).thenReturn(null);

        try {
            driver.onAction(bot);
        } catch (Exception e) {
        }

        verify(commander).getDriveAction(bot);
    }
}
