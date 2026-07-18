package okurun.gunner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.robocode.tankroyale.botapi.events.GameStartedEvent;

import static org.mockito.Mockito.*;

import okurun.OkuRunBot;
import okurun.commander.Commander;

@ExtendWith(MockitoExtension.class)
class GunnerTest {
    private Gunner gunner;

    @Mock
    private OkuRunBot bot;

    @Mock
    private Commander commander;

    @Mock
    private GameStartedEvent gameStartedEvent;

    @BeforeEach
    void setUp() {
        gunner = new Gunner();
        gunner.onGameStarted(gameStartedEvent, bot);
    }

    @Test
    void testAction() {
        when(bot.getCommander()).thenReturn(commander);
        when(commander.getGunActionName(bot)).thenReturn(null);

        gunner.onAction(bot);

        verify(commander).getGunActionName(bot);
    }
}
