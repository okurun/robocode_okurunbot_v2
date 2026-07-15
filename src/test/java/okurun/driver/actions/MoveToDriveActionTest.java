package okurun.driver.actions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import okurun.Debugger;
import okurun.OkuRunBot;
import okurun.battlemanager.BattleManager;
import okurun.commander.Commander;
import okurun.commander.Commander.AccelePriority;
import okurun.commander.Commander.HandlePriority;
import okurun.driver.Driver;

@ExtendWith(MockitoExtension.class)
class MoveToDriveActionTest {
    private MoveToDriveAction action;

    @Mock
    private OkuRunBot bot;

    @Mock
    private Commander commander;

    @Mock
    private BattleManager battleManager;

    @Mock
    private Debugger debugger;

    @BeforeEach
    void setUp() {
        action = new MoveToDriveAction();
    }

    @Test
    void testActionReturnsNullWhenNoTargetPosition() {
        when(bot.getCommander()).thenReturn(commander);
        when(commander.getTargetMovePosition(bot)).thenReturn(null);

        Driver.ActionId result = action.action(bot);
        assertNull(result);
    }

    @Test
    void testActionSetsMoveCommands() {
        lenient().when(bot.getCommander()).thenReturn(commander);
        lenient().when(commander.getTargetMovePosition(bot)).thenReturn(new double[] { 200.0, 300.0 });
        lenient().when(commander.getHandlePriority(bot)).thenReturn(HandlePriority.TARGET);
        lenient().when(commander.getAccelePriority(bot)).thenReturn(AccelePriority.MAX_SPEED);
        lenient().when(commander.getTargetEnemyId(bot)).thenReturn(Commander.NO_TARGET);

        lenient().when(bot.bearingTo(200.0, 300.0)).thenReturn(45.0);
        lenient().when(bot.distanceTo(200.0, 300.0)).thenReturn(100.0);
        lenient().when(bot.getPosition()).thenReturn(new double[] { 100.0, 100.0 });

        lenient().when(bot.getBattleManager()).thenReturn(battleManager);
        lenient().when(battleManager.getLatestEnemyState(anyInt())).thenReturn(null);

        lenient().when(bot.getDebugger()).thenReturn(debugger);

        Driver.ActionId result = action.action(bot);

        assertNull(result);
        verify(bot).setTurnLeft(45.0);
        verify(bot).setForward(100.0);
        verify(bot).setMaxSpeed(anyDouble());
    }

    @Test
    void testGetBearingTo_NoAvoidance() throws Exception {
        java.lang.reflect.Method method = MoveToDriveAction.class.getDeclaredMethod("getBearingTo", OkuRunBot.class,
                double[].class);
        method.setAccessible(true);

        when(bot.bearingTo(200.0, 300.0)).thenReturn(45.0);
        when(bot.getMaxTurnRate()).thenReturn(10.0);

        double result = (double) method.invoke(action, bot, new double[] { 200.0, 300.0 });
        assertEquals(45.0, result);
    }

    @Test
    void testGetBearingTo_AvoidanceLeftToRight() throws Exception {
        java.lang.reflect.Method method = MoveToDriveAction.class.getDeclaredMethod("getBearingTo", OkuRunBot.class,
                double[].class);
        method.setAccessible(true);

        java.lang.reflect.Field randNumField = MoveToDriveAction.class.getDeclaredField("randNum");
        randNumField.setAccessible(true);
        randNumField.set(action, 0);

        when(bot.bearingTo(200.0, 300.0)).thenReturn(5.0);
        when(bot.getMaxTurnRate()).thenReturn(10.0);

        lenient().when(bot.getCommander()).thenReturn(commander);
        when(commander.getHandlePriority(bot)).thenReturn(HandlePriority.AVOID_BULLET);

        // avoidTurn is initially LEFT, randNum == 0 flips it to RIGHT
        // RIGHT logic: bearingTo - getMaxTurnRate() * 0.5 = 5.0 - 5.0 = 0.0
        double result = (double) method.invoke(action, bot, new double[] { 200.0, 300.0 });
        assertEquals(0.0, result);
    }

    @Test
    void testGetAccele_DefaultPriority() throws Exception {
        java.lang.reflect.Method method = MoveToDriveAction.class.getDeclaredMethod("getAccele", OkuRunBot.class,
                double[].class, double.class);
        method.setAccessible(true);

        when(bot.distanceTo(100.0, 200.0)).thenReturn(150.0);
        lenient().when(bot.getCommander()).thenReturn(commander);
        when(commander.getAccelePriority(bot)).thenReturn(AccelePriority.MAX_SPEED);

        Object result = method.invoke(action, bot, new double[] { 100.0, 200.0 }, 45.0);

        java.lang.reflect.Field distanceField = result.getClass().getDeclaredField("distance");
        distanceField.setAccessible(true);
        java.lang.reflect.Field speedField = result.getClass().getDeclaredField("speed");
        speedField.setAccessible(true);

        assertEquals(150.0, (double) distanceField.get(result));
        assertEquals(dev.robocode.tankroyale.botapi.Constants.MAX_SPEED, (double) speedField.get(result));
    }

    @Test
    void testGetAccele_HandlePriority_DiffTurnRateGreaterThan90() throws Exception {
        java.lang.reflect.Method method = MoveToDriveAction.class.getDeclaredMethod("getAccele", OkuRunBot.class,
                double[].class, double.class);
        method.setAccessible(true);

        when(bot.distanceTo(100.0, 200.0)).thenReturn(150.0);
        lenient().when(bot.getCommander()).thenReturn(commander);
        when(commander.getAccelePriority(bot)).thenReturn(AccelePriority.HANDLE);
        when(commander.getMinSpeed(bot)).thenReturn(2.0);

        when(bot.getMaxTurnRate()).thenReturn(10.0);
        when(bot.getSpeed()).thenReturn(6.0);

        Object result = method.invoke(action, bot, new double[] { 100.0, 200.0 }, 110.0);

        java.lang.reflect.Field distanceField = result.getClass().getDeclaredField("distance");
        distanceField.setAccessible(true);
        java.lang.reflect.Field speedField = result.getClass().getDeclaredField("speed");
        speedField.setAccessible(true);

        assertEquals(-1.0, (double) distanceField.get(result));
        assertEquals(4.0, (double) speedField.get(result));
    }

    @Test
    void testGetAccele_HandlePriority_DiffTurnRateBetween0And90() throws Exception {
        java.lang.reflect.Method method = MoveToDriveAction.class.getDeclaredMethod("getAccele", OkuRunBot.class,
                double[].class, double.class);
        method.setAccessible(true);

        when(bot.distanceTo(100.0, 200.0)).thenReturn(150.0);
        lenient().when(bot.getCommander()).thenReturn(commander);
        when(commander.getAccelePriority(bot)).thenReturn(AccelePriority.HANDLE);
        when(commander.getMinSpeed(bot)).thenReturn(2.0);

        when(bot.getMaxTurnRate()).thenReturn(10.0);
        when(bot.getSpeed()).thenReturn(6.0);

        Object result = method.invoke(action, bot, new double[] { 100.0, 200.0 }, 50.0);

        java.lang.reflect.Field distanceField = result.getClass().getDeclaredField("distance");
        distanceField.setAccessible(true);
        java.lang.reflect.Field speedField = result.getClass().getDeclaredField("speed");
        speedField.setAccessible(true);

        assertEquals(150.0, (double) distanceField.get(result));
        assertEquals(5.0, (double) speedField.get(result));
    }

    @Test
    void testGetAccele_AvoidBulletPriority_Brake() throws Exception {
        java.lang.reflect.Method method = MoveToDriveAction.class.getDeclaredMethod("getAccele", OkuRunBot.class,
                double[].class, double.class);
        method.setAccessible(true);

        java.lang.reflect.Field randNumField = MoveToDriveAction.class.getDeclaredField("randNum");
        randNumField.setAccessible(true);
        randNumField.set(action, 0);

        when(bot.distanceTo(100.0, 200.0)).thenReturn(150.0);
        lenient().when(bot.getCommander()).thenReturn(commander);
        when(commander.getAccelePriority(bot)).thenReturn(AccelePriority.AVOID_BULLET);
        when(commander.getMinSpeed(bot)).thenReturn(2.0);
        when(bot.getSpeed()).thenReturn(6.0);

        Object result = method.invoke(action, bot, new double[] { 100.0, 200.0 }, 45.0);

        java.lang.reflect.Field distanceField = result.getClass().getDeclaredField("distance");
        distanceField.setAccessible(true);
        java.lang.reflect.Field speedField = result.getClass().getDeclaredField("speed");
        speedField.setAccessible(true);

        assertEquals(-1.0, (double) distanceField.get(result));
        assertEquals(dev.robocode.tankroyale.botapi.Constants.MAX_SPEED, (double) speedField.get(result));
    }
}
