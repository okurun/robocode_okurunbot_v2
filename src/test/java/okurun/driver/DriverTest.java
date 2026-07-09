package okurun.driver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

    @BeforeEach
    void setUp() {
        driver = new Driver();
        driver.init(bot);
    }

    @Test
    void testActionDelegatesToCommander() {
        when(bot.getCommander()).thenReturn(commander);
        when(commander.getDriveAction(bot)).thenReturn(null);

        try {
            driver.action(bot);
        } catch (Exception e) {
        }

        verify(commander).getDriveAction(bot);
    }
}
