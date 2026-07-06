package okurun.arenamap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
        arenaMap = new ArenaMap();
        arenaMap.init(600, 800);
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
}
