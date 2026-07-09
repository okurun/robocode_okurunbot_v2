package okurun.driver.actions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.robocode.tankroyale.botapi.Constants;
import okurun.OkuRunBot;
import okurun.arenamap.ArenaMap;

import java.lang.reflect.Method;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvoidWallDriveActionTest {

    private AvoidWallDriveAction action;

    @Mock
    private OkuRunBot bot;

    @Mock
    private ArenaMap.Wall wall;

    @BeforeEach
    void setUp() {
        action = new AvoidWallDriveAction();
    }

    private Object invokeActionMethod(String methodName, OkuRunBot bot, ArenaMap.PotentialCollisionWall pcWall) throws Exception {
        Method method = AvoidWallDriveAction.class.getDeclaredMethod(methodName, OkuRunBot.class, ArenaMap.PotentialCollisionWall.class);
        method.setAccessible(true);
        return method.invoke(action, bot, pcWall);
    }

    private double getDoubleField(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getDouble(obj);
    }

    private ArenaMap.PotentialCollisionWall createPcWall(double turnsToCollision) {
        ArenaMap map = new ArenaMap();
        return map.new PotentialCollisionWall(wall, turnsToCollision);
    }

    @Test
    void testForwardAction_TurnLeft_NoDeceleration() throws Exception {
        ArenaMap.PotentialCollisionWall pcWall = createPcWall(10.0);
        when(wall.getLeftTurnAngleToParallel(bot)).thenReturn(30.0);
        // bot.getSpeed() is not called because maxSpeed < Constants.MAX_SPEED is false

        Object result = invokeActionMethod("forwardAction", bot, pcWall);

        double leftTurnAngle = getDoubleField(result, "leftTurnAngle");
        double maxSpeed = getDoubleField(result, "maxSpeed");
        double distance = getDoubleField(result, "distance");

        // getLeftTurnAngleToParallel returns 30 (positive)
        // logic: leftTurnAngle + 10 = 40
        assertEquals(40.0, leftTurnAngle, 0.001);

        // turnsToCollision(10) * 2 = 20 -> min(8, 20) = 8
        assertEquals(Constants.MAX_SPEED, maxSpeed, 0.001);

        // maxSpeed is 8, distance should be 100
        assertEquals(100.0, distance, 0.001);
    }

    @Test
    void testForwardAction_TurnRight_WithDeceleration() throws Exception {
        ArenaMap.PotentialCollisionWall pcWall = createPcWall(2.0);
        when(wall.getLeftTurnAngleToParallel(bot)).thenReturn(-30.0);
        when(bot.getSpeed()).thenReturn(8.0); // Speed is greater than maxSpeed (4)

        Object result = invokeActionMethod("forwardAction", bot, pcWall);

        double leftTurnAngle = getDoubleField(result, "leftTurnAngle");
        double maxSpeed = getDoubleField(result, "maxSpeed");
        double distance = getDoubleField(result, "distance");

        // getLeftTurnAngleToParallel returns -30 (not positive)
        // logic: leftTurnAngle - 10 = -40
        assertEquals(-40.0, leftTurnAngle, 0.001);

        // turnsToCollision(2) * 2 = 4 -> min(8, 4) = 4
        assertEquals(4.0, maxSpeed, 0.001);

        // maxSpeed (4) < MAX_SPEED (8) and bot.getSpeed() (8) > maxSpeed (4)
        // distance becomes -100
        assertEquals(-100.0, distance, 0.001);
    }
    
    @Test
    void testForwardAction_WithDeceleration_SpeedAlreadyLow() throws Exception {
        ArenaMap.PotentialCollisionWall pcWall = createPcWall(2.0);
        when(wall.getLeftTurnAngleToParallel(bot)).thenReturn(0.0);
        when(bot.getSpeed()).thenReturn(2.0); // Speed is less than maxSpeed (4)

        Object result = invokeActionMethod("forwardAction", bot, pcWall);

        double leftTurnAngle = getDoubleField(result, "leftTurnAngle");
        double maxSpeed = getDoubleField(result, "maxSpeed");
        double distance = getDoubleField(result, "distance");

        // getLeftTurnAngleToParallel returns 0 (not positive)
        // logic: leftTurnAngle - 10 = -10
        assertEquals(-10.0, leftTurnAngle, 0.001);

        // turnsToCollision(2) * 2 = 4 -> min(8, 4) = 4
        assertEquals(4.0, maxSpeed, 0.001);

        // maxSpeed (4) < MAX_SPEED (8) but bot.getSpeed() (2) <= maxSpeed (4)
        // distance remains 100
        assertEquals(100.0, distance, 0.001);
    }

    @Test
    void testBackwardAction_TurnLeft_NoDeceleration() throws Exception {
        ArenaMap.PotentialCollisionWall pcWall = createPcWall(10.0);
        when(wall.getLeftTurnAngleToParallel(bot)).thenReturn(30.0);
        // speed > maxSpeed (-8) will be false if speed is -8
        // bot.getSpeed() is not called because maxSpeed < Constants.MAX_SPEED is false

        Object result = invokeActionMethod("backwardAction", bot, pcWall);

        double leftTurnAngle = getDoubleField(result, "leftTurnAngle");
        double maxSpeed = getDoubleField(result, "maxSpeed");
        double distance = getDoubleField(result, "distance");

        // getLeftTurnAngleToParallel returns 30 (positive)
        // logic: leftTurnAngle + 10 = 40
        assertEquals(40.0, leftTurnAngle, 0.001);

        // turnsToCollision(10) * 2 = 20 -> min(8, 20) = 8
        assertEquals(Constants.MAX_SPEED, maxSpeed, 0.001);

        // maxSpeed is 8, distance should be -100
        assertEquals(-100.0, distance, 0.001);
    }

    @Test
    void testBackwardAction_TurnRight_WithDeceleration() throws Exception {
        ArenaMap.PotentialCollisionWall pcWall = createPcWall(2.0);
        when(wall.getLeftTurnAngleToParallel(bot)).thenReturn(-30.0);
        // If speed is -8, then speed (-8) < -maxSpeed (-4) is TRUE, should brake
        when(bot.getSpeed()).thenReturn(-8.0); 

        Object result = invokeActionMethod("backwardAction", bot, pcWall);

        double leftTurnAngle = getDoubleField(result, "leftTurnAngle");
        double maxSpeed = getDoubleField(result, "maxSpeed");
        double distance = getDoubleField(result, "distance");

        // getLeftTurnAngleToParallel returns -30 (not positive)
        // logic: leftTurnAngle - 10 = -40
        assertEquals(-40.0, leftTurnAngle, 0.001);

        // turnsToCollision(2) * 2 = 4 -> min(8, 4) = 4
        assertEquals(4.0, maxSpeed, 0.001);

        // maxSpeed (4) < MAX_SPEED (8) is true
        // bot.getSpeed() (-8) < -maxSpeed (-4) is true
        // distance = -(-100) = 100
        assertEquals(100.0, distance, 0.001);
    }
    
    @Test
    void testBackwardAction_WithDeceleration_SpeedAlreadyLow() throws Exception {
        ArenaMap.PotentialCollisionWall pcWall = createPcWall(2.0);
        when(wall.getLeftTurnAngleToParallel(bot)).thenReturn(0.0);
        // If speed is -2, then speed (-2) < -maxSpeed (-4) is FALSE, no brake
        when(bot.getSpeed()).thenReturn(-2.0);

        Object result = invokeActionMethod("backwardAction", bot, pcWall);

        double leftTurnAngle = getDoubleField(result, "leftTurnAngle");
        double maxSpeed = getDoubleField(result, "maxSpeed");
        double distance = getDoubleField(result, "distance");

        // getLeftTurnAngleToParallel returns 0 (not positive)
        // logic: leftTurnAngle - 10 = -10
        assertEquals(-10.0, leftTurnAngle, 0.001);

        // turnsToCollision(2) * 2 = 4 -> min(8, 4) = 4
        assertEquals(4.0, maxSpeed, 0.001);

        // maxSpeed (4) < MAX_SPEED (8) is true
        // bot.getSpeed() (-2) < -maxSpeed (-4) is false
        // distance remains -100
        assertEquals(-100.0, distance, 0.001);
    }
}
