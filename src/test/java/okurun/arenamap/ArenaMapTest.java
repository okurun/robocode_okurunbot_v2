package okurun.arenamap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.robocode.tankroyale.botapi.Constants;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import okurun.OkuRunBot;
import okurun.battlemanager.BattleManager;

@ExtendWith(MockitoExtension.class)
class ArenaMapTest {
    private ArenaMap arenaMap;

    @Mock
    private OkuRunBot bot;

    @Mock
    private BattleManager battleManager;

    @BeforeEach
    void setUp() {
        when(bot.getArenaHeight()).thenReturn(600);
        when(bot.getArenaWidth()).thenReturn(800);
        arenaMap = new ArenaMap();
        arenaMap.init(bot);
    }

    @Test
    void testInitAndDimensions() {
        assertEquals(600, arenaMap.getHeight());
        assertEquals(800, arenaMap.getWidth());
    }

    @Test
    void testIsInsideArena() {
        assertTrue(arenaMap.isInsideArena(400, 300));
        assertFalse(arenaMap.isInsideArena(0, 300));
        assertFalse(arenaMap.isInsideArena(800, 300));
        assertFalse(arenaMap.isInsideArena(-10, 300));
    }

    @Test
    void testGetAreaByCoordinates() {
        // TOP_LEFT: 0 <= x < 400, 300 <= y < 600
        ArenaMap.Area area = arenaMap.getArea(100, 400);
        assertNotNull(area);
        assertEquals(ArenaMap.AreaId.TOP_LEFT, area.id);

        // BOTTOM_RIGHT: 400 <= x < 800, 0 <= y < 300
        ArenaMap.Area area2 = arenaMap.getArea(600, 100);
        assertNotNull(area2);
        assertEquals(ArenaMap.AreaId.BOTTOM_RIGHT, area2.id);
    }

    @Test
    void testGetAreaByBot() {
        when(bot.getX()).thenReturn(500.0);
        when(bot.getY()).thenReturn(100.0);
        ArenaMap.Area area = arenaMap.getArea(bot);
        assertNotNull(area);
        assertEquals(ArenaMap.AreaId.BOTTOM_RIGHT, area.id);
    }

    @Test
    void testGetPotentialCollisionWalls() {
        when(bot.getX()).thenReturn(400.0);
        when(bot.getY()).thenReturn(580.0); // 上の壁(y=600)に近い
        when(bot.getDirection()).thenReturn(45.0); // 45度は右上に進む
        
        List<ArenaMap.PotentialCollisionWall> walls = arenaMap.getPotentialCollisionWalls(bot);
        assertFalse(walls.isEmpty());
        assertEquals(ArenaMap.WallId.TOP, walls.get(0).wall.id);
    }

    @Test
    void testGetLeftTurnAngleToParallelForward() {
        when(bot.getSpeed()).thenReturn((double) Constants.MAX_SPEED);
        when(bot.normalizeRelativeAngle(anyDouble())).thenAnswer(invocation -> {
            double angle = invocation.getArgument(0);
            while (angle <= -180) angle += 360;
            while (angle > 180) angle -= 360;
            return angle;
        });

        // LEFT wall (parallel to 90 or 270)
        ArenaMap.Wall leftWall = arenaMap.getWall(ArenaMap.WallId.LEFT);
        
        when(bot.getDirection()).thenReturn(30.0);
        assertEquals(60.0, leftWall.getLeftTurnAngleToParallel(bot), 0.001);

        when(bot.getDirection()).thenReturn(150.0);
        assertEquals(-60.0, leftWall.getLeftTurnAngleToParallel(bot), 0.001);

        when(bot.getDirection()).thenReturn(330.0);
        assertEquals(-60.0, leftWall.getLeftTurnAngleToParallel(bot), 0.001);

        when(bot.getDirection()).thenReturn(210.0);
        assertEquals(60.0, leftWall.getLeftTurnAngleToParallel(bot), 0.001);

        // RIGHT wall (parallel to 90 or 270)
        ArenaMap.Wall rightWall = arenaMap.getWall(ArenaMap.WallId.RIGHT);
        
        when(bot.getDirection()).thenReturn(30.0);
        assertEquals(60.0, rightWall.getLeftTurnAngleToParallel(bot), 0.001);

        when(bot.getDirection()).thenReturn(150.0);
        assertEquals(-60.0, rightWall.getLeftTurnAngleToParallel(bot), 0.001);

        when(bot.getDirection()).thenReturn(330.0);
        assertEquals(-60.0, rightWall.getLeftTurnAngleToParallel(bot), 0.001);

        when(bot.getDirection()).thenReturn(210.0);
        assertEquals(60.0, rightWall.getLeftTurnAngleToParallel(bot), 0.001);

        // TOP wall (parallel to 0 or 180)
        ArenaMap.Wall topWall = arenaMap.getWall(ArenaMap.WallId.TOP);

        when(bot.getDirection()).thenReturn(30.0);
        assertEquals(-30.0, topWall.getLeftTurnAngleToParallel(bot), 0.001);

        when(bot.getDirection()).thenReturn(150.0);
        assertEquals(30.0, topWall.getLeftTurnAngleToParallel(bot), 0.001);

        when(bot.getDirection()).thenReturn(210.0);
        assertEquals(-30.0, topWall.getLeftTurnAngleToParallel(bot), 0.001);
        
        when(bot.getDirection()).thenReturn(330.0);
        assertEquals(30.0, topWall.getLeftTurnAngleToParallel(bot), 0.001);

        // BOTTOM wall (parallel to 0 or 180)
        ArenaMap.Wall bottomWall = arenaMap.getWall(ArenaMap.WallId.BOTTOM);

        when(bot.getDirection()).thenReturn(30.0);
        assertEquals(-30.0, bottomWall.getLeftTurnAngleToParallel(bot), 0.001);

        when(bot.getDirection()).thenReturn(150.0);
        assertEquals(30.0, bottomWall.getLeftTurnAngleToParallel(bot), 0.001);

        when(bot.getDirection()).thenReturn(210.0);
        assertEquals(-30.0, bottomWall.getLeftTurnAngleToParallel(bot), 0.001);
        
        when(bot.getDirection()).thenReturn(330.0);
        assertEquals(30.0, bottomWall.getLeftTurnAngleToParallel(bot), 0.001);
    }

    @Test
    void testGetLeftTurnAngleToParallelBackward() {
        when(bot.getSpeed()).thenReturn((double) -Constants.MAX_SPEED);
        when(bot.normalizeRelativeAngle(anyDouble())).thenAnswer(invocation -> {
            double angle = invocation.getArgument(0);
            while (angle <= -180) angle += 360;
            while (angle > 180) angle -= 360;
            return angle;
        });

        // LEFT, RIGHT wall (parallel to 90 or 270)
        ArenaMap.Wall leftWall = arenaMap.getWall(ArenaMap.WallId.LEFT);
        
        when(bot.getDirection()).thenReturn(30.0);
        assertEquals(60.0, leftWall.getLeftTurnAngleToParallel(bot), 0.001);

        when(bot.getDirection()).thenReturn(150.0);
        assertEquals(-60.0, leftWall.getLeftTurnAngleToParallel(bot), 0.001);

        when(bot.getDirection()).thenReturn(330.0);
        assertEquals(-60.0, leftWall.getLeftTurnAngleToParallel(bot), 0.001);

        when(bot.getDirection()).thenReturn(210.0);
        assertEquals(60.0, leftWall.getLeftTurnAngleToParallel(bot), 0.001);

        // TOP, BOTTOM wall (parallel to 0 or 180)
        ArenaMap.Wall topWall = arenaMap.getWall(ArenaMap.WallId.TOP);

        when(bot.getDirection()).thenReturn(30.0);
        assertEquals(-30.0, topWall.getLeftTurnAngleToParallel(bot), 0.001);

        when(bot.getDirection()).thenReturn(150.0);
        assertEquals(30.0, topWall.getLeftTurnAngleToParallel(bot), 0.001);

        when(bot.getDirection()).thenReturn(210.0);
        assertEquals(-30.0, topWall.getLeftTurnAngleToParallel(bot), 0.001);
        
        when(bot.getDirection()).thenReturn(330.0);
        assertEquals(30.0, topWall.getLeftTurnAngleToParallel(bot), 0.001);
    }
}
