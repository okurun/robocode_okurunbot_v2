package okurun.arenamap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dev.robocode.tankroyale.botapi.Constants;
import dev.robocode.tankroyale.botapi.IBot;
import okurun.OkuRunBot;
import okurun.battlemanager.BattleManager;
import okurun.battlemanager.EnemyState;

public class ArenaMap {
    public static enum WallId {
        LEFT, TOP, RIGHT, BOTTOM
    }

    public class Wall {
        public final WallId id;
        public final int x, y;

        Wall(WallId id, int x, int y) {
            this.id = id;
            this.x = x;
            this.y = y;
        }

        public boolean isFacing(double direction) {
            return switch (id) {
                case TOP -> (direction > 0 && direction < 180);
                case BOTTOM -> (direction > 180 && direction < 360);
                case LEFT -> (direction > 90 && direction < 270);
                case RIGHT -> (direction < 90 || direction > 270);
            };
        }

        /**
         * この壁と衝突するまでの距離を返します。
         * 進行方向が壁に向いていない場合は Double.POSITIVE_INFINITY を返します。
         * 
         * @param px        現在のX座標
         * @param py        現在のY座標
         * @param direction 移動方向（度数法：0〜360）
         * @return 壁に衝突するまでの距離。衝突しない場合は Double.POSITIVE_INFINITY
         */
        public double getDistanceToCollision(double px, double py, double direction) {
            if (!isFacing(direction)) {
                return Double.POSITIVE_INFINITY;
            }

            final double r = OkuRunBot.BODY_SIZE / 2;
            final double angleRad = Math.toRadians(direction);

            return switch (id) {
                case TOP -> (y - r - py) / Math.sin(angleRad);
                case BOTTOM -> (y + r - py) / Math.sin(angleRad);
                case LEFT -> (x + r - px) / Math.cos(angleRad);
                case RIGHT -> (x - r - px) / Math.cos(angleRad);
            };
        }

        /**
         * この壁に衝突するまでのターン数を返します。
         * 進行方向が壁に向いていない、または速度が0以下の場合は Double.POSITIVE_INFINITY を返します。
         * 速度が正の値の場合、壁の反対側（向こう側）まで移動してしまう場合も Double.POSITIVE_INFINITY を返します。
         * 
         * @param px        現在のX座標
         * @param py        現在のY座標
         * @param direction 移動方向（度数法：0〜360）
         * @param speed     移動速度
         * @return 壁に衝突するまでのターン数。衝突しない場合は Double.POSITIVE_INFINITY
         */
        public double getTurnsToCollision(double px, double py, double direction, double speed) {
            if (speed <= 0) {
                return Double.POSITIVE_INFINITY;
            }
            final double distance = getDistanceToCollision(px, py, direction);
            if (distance == Double.POSITIVE_INFINITY) {
                return Double.POSITIVE_INFINITY;
            }
            if (distance <= 0) {
                return 0;
            }
            return distance / speed;
        }

        /**
         * この壁と平行な方向に向くために、ハンドルを左にきるべき角度（度数法）を返します。
         * 返される角度は -90度から 90度の範囲です（マイナス値は右にきることを表します）。
         * 
         * @param bot ボット
         * @return 壁と平行になるために左にきるべき角度（-90 〜 90）
         */
        public double getLeftTurnAngleToParallel(IBot bot) {
            double h = bot.getDirection();
            switch (id) {
                case LEFT, RIGHT -> {
                    double diff90 = normalizeRelativeAngle(90 - h);
                    double diff270 = normalizeRelativeAngle(270 - h);
                    return (Math.abs(diff90) <= Math.abs(diff270)) ? diff90 : diff270;
                }
                case TOP, BOTTOM -> {
                    double diff0 = normalizeRelativeAngle(0 - h);
                    double diff180 = normalizeRelativeAngle(180 - h);
                    return (Math.abs(diff0) <= Math.abs(diff180)) ? diff0 : diff180;
                }
                default -> throw new IllegalStateException("Unknown wall id: " + id);
            }
        }

        private double normalizeRelativeAngle(double angle) {
            double angleDiff = angle;
            while (angleDiff <= -180)
                angleDiff += 360;
            while (angleDiff > 180)
                angleDiff -= 360;
            return angleDiff;
        }
    }

    public static enum AreaId {
        TOP_LEFT, TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT
    }

    public class Area {
        public final AreaId id;
        public final double x1, y1, x2, y2;

        Area(AreaId id, double x1, double y1, double x2, double y2) {
            this.id = id;
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        /**
         * 指定座標がエリア内に含まれるかを返します
         * 
         * @param x
         * @param y
         * @return
         */
        public boolean isInside(double x, double y) {
            return x >= x1 && x < x2 && y >= y1 && y < y2;
        }

        /**
         * 中心座標を返します
         * 
         * @return
         */
        public double[] getCenter() {
            return new double[] { (x1 + x2) / 2, (y1 + y2) / 2 };
        }

        /**
         * 反対側のエリアを返します
         * 
         * @return
         */
        public Area getOppositeArea() {
            return switch (id) {
                case TOP_LEFT -> areas.get(AreaId.BOTTOM_RIGHT);
                case TOP_RIGHT -> areas.get(AreaId.BOTTOM_LEFT);
                case BOTTOM_RIGHT -> areas.get(AreaId.TOP_LEFT);
                case BOTTOM_LEFT -> areas.get(AreaId.TOP_RIGHT);
            };
        }

        /**
         * 隣接するエリアを返す
         * 
         * @return 隣接するエリアのリスト
         */
        public List<Area> getNeighboringAreas() {
            return switch (id) {
                case TOP_LEFT -> List.of(areas.get(AreaId.TOP_RIGHT), areas.get(AreaId.BOTTOM_LEFT));
                case TOP_RIGHT -> List.of(areas.get(AreaId.TOP_LEFT), areas.get(AreaId.BOTTOM_RIGHT));
                case BOTTOM_RIGHT -> List.of(areas.get(AreaId.BOTTOM_LEFT), areas.get(AreaId.TOP_RIGHT));
                case BOTTOM_LEFT -> List.of(areas.get(AreaId.BOTTOM_RIGHT), areas.get(AreaId.TOP_LEFT));
            };
        }
    }

    private int height;
    private int width;
    private Map<WallId, Wall> walls;
    private Map<AreaId, Area> areas;
    private final Map<String, Object> caches = new ConcurrentHashMap<>();

    public void init(int height, int width) {
        this.height = height;
        this.width = width;
        this.walls = Map.of(
                WallId.LEFT, new Wall(WallId.LEFT, 0, -1),
                WallId.TOP, new Wall(WallId.TOP, -1, height),
                WallId.RIGHT, new Wall(WallId.RIGHT, width, -1),
                WallId.BOTTOM, new Wall(WallId.BOTTOM, -1, 0));
        final int halfWidth = width / 2;
        final int halfHeight = height / 2;
        this.areas = Map.of(
                AreaId.TOP_LEFT, new Area(AreaId.TOP_LEFT, 0, halfHeight, halfWidth, height),
                AreaId.TOP_RIGHT, new Area(AreaId.TOP_RIGHT, halfWidth, halfHeight, width, height),
                AreaId.BOTTOM_RIGHT, new Area(AreaId.BOTTOM_RIGHT, halfWidth, 0, width, halfHeight),
                AreaId.BOTTOM_LEFT, new Area(AreaId.BOTTOM_LEFT, 0, 0, halfWidth, halfHeight));
    }

    public void action(OkuRunBot bot) {
        caches.clear();
    }

    public Wall getWall(WallId id) {
        return walls.get(id);
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    /**
     * 指定座標が壁の内側にあるかを返します
     * 
     * @param x x座標
     * @param y y座標
     * @return 壁の内側にある場合はtrue、そうでない場合はfalse
     */
    public boolean isInsideArena(double x, double y) {
        return x > 0 && x < width && y > 0 && y < height;
    }

    /**
     * 指定座標が壁の内側にあるかを返します
     * 
     * @param position [x座標, y座標]
     * @return 壁の内側にある場合はtrue、そうでない場合はfalse
     */
    public boolean isInsideArena(double[] position) {
        return isInsideArena(position[0], position[1]);
    }

    /**
     * 指定された進行方向に対して、衝突する可能性のある壁のリストを返します。
     * 
     * @param direction 進行方向（度数法：0〜360）
     * @return 衝突する可能性のある壁のリスト
     */
    @SuppressWarnings("unchecked")
    public List<Wall> getWallsFacingTo(double direction) {
        if (caches.containsKey("wallsFacingTo" + direction)) {
            return (List<Wall>) caches.get("wallsFacingTo" + direction);
        }
        List<Wall> facing = new ArrayList<>();
        for (Wall wall : this.walls.values()) {
            if (wall.isFacing(direction)) {
                facing.add(wall);
            }
        }
        caches.put("wallsFacingTo" + direction, facing);
        return facing;
    }

    public class PotentialCollisionWall {
        private Wall wall;
        private double turnsToCollision;

        public PotentialCollisionWall(Wall wall, double turnsToCollision) {
            this.wall = wall;
            this.turnsToCollision = turnsToCollision;
        }

        public Wall getWall() {
            return wall;
        }

        public double getTurnsToCollision() {
            return turnsToCollision;
        }
    }

    @SuppressWarnings("unchecked")
    public List<PotentialCollisionWall> getPotentialCollisionWalls(OkuRunBot bot) {
        if (caches.containsKey("potentialCollisionWalls")) {
            return (List<PotentialCollisionWall>) caches.get("potentialCollisionWalls");
        }
        final List<PotentialCollisionWall> collisionWalls = getPotentialCollisionWalls(bot.getX(), bot.getY(), bot.getDirection(),
                Constants.MAX_SPEED);
        caches.put("potentialCollisionWalls", collisionWalls);
        return collisionWalls;
    }

    private List<PotentialCollisionWall> getPotentialCollisionWalls(double x, double y, double d, double s) {
        if (s == 0) {
            return List.of();
        }

        final double direction = (s < 0) ? (d + 180) % 360 : d;
        final double speed = (s < 0) ? -s : s;

        final double deceleration = Math.abs(dev.robocode.tankroyale.botapi.Constants.DECELERATION);
        final double limitTurns = Math.ceil(speed / deceleration) + 1;

        // 移動方向に面している壁
        final List<Wall> facingWalls = getWallsFacingTo(direction);
        final List<PotentialCollisionWall> collisionWalls = new ArrayList<>();

        for (Wall wall : facingWalls) {
            double turns = wall.getTurnsToCollision(x, y, direction, speed);
            if (turns <= limitTurns) {
                collisionWalls.add(new PotentialCollisionWall(wall, turns));
            }
        }

        // 衝突までのターン数でソートする（近い順）
        collisionWalls.sort((w1, w2) -> Double.compare(
                w1.getTurnsToCollision(),
                w2.getTurnsToCollision()));

        return collisionWalls;
    }

    /**
     * 指定座標が含まれるエリアを返します
     * 座標が壁上に乗っている場合はnullを返します
     * 
     * @param x
     * @param y
     * @return
     */
    public Area getArea(double x, double y) {
        for (final Area area : areas.values()) {
            if (area.isInside(x, y)) {
                return area;
            }
        }
        return null;
    }

    /**
     * 各エリアにいる敵の数を返します
     * 
     * @param bot
     * @return
     */
    @SuppressWarnings("unchecked")
    private Map<AreaId, Integer> getAreasEnemyCount(OkuRunBot bot) {
        if (caches.containsKey("areasEnemyCount")) {
            return (Map<AreaId, Integer>) caches.get("areasEnemyCount");
        }

        final BattleManager battleManager = bot.getBattleManager();
        final Map<Integer, EnemyState> enemyStates = battleManager.getLatestAlivalEnemyStates();
        final Map<AreaId, Integer> enemyCount = new HashMap<>();
        for (final Area area : areas.values()) {
            enemyCount.put(area.id, 0);
        }
        for (final EnemyState enemyState : enemyStates.values()) {
            if (enemyState == null) {
                continue;
            }
            for (final Area area : areas.values()) {
                if (area.isInside(enemyState.x, enemyState.y)) {
                    enemyCount.put(area.id, enemyCount.get(area.id) + 1);
                    continue;
                }
            }
        }
        caches.put("areasEnemyCount", enemyCount);
        return enemyCount;
    }

    /**
     * 敵の少ない安全なエリア一覧を返します
     * 
     * @param bot
     * @return
     */
    @SuppressWarnings("unchecked")
    private List<Area> getSafeAreas(OkuRunBot bot) {
        if (caches.containsKey("safeAreas")) {
            return (List<Area>) caches.get("safeAreas");
        }

        final Map<AreaId, Integer> areaEnemyCount = getAreasEnemyCount(bot);
        int minEnemyCount = Integer.MAX_VALUE;
        List<Area> safeAreas = new ArrayList<>();
        for (final AreaId areaId : areaEnemyCount.keySet()) {
            int enemyCount = areaEnemyCount.get(areaId);
            if (enemyCount < minEnemyCount) {
                minEnemyCount = enemyCount;
            }
        }
        final Area currentArea = getArea(bot.getX(), bot.getY());
        final List<Area> neighboringAreas = currentArea.getNeighboringAreas();
        boolean containNeighboringArea = false; // 隣のエリアが含まれているか
        for (final AreaId areaId : areaEnemyCount.keySet()) {
            if (areaEnemyCount.get(areaId) == minEnemyCount) {
                final Area area = areas.get(areaId);
                if (area.equals(currentArea)) {
                    caches.put("safeAreas", List.of(area));
                    return (List<Area>) caches.get("safeAreas");
                }
                if (neighboringAreas.contains(area)) {
                    containNeighboringArea = true;
                }
                safeAreas.add(area);
            }
        }
        if (containNeighboringArea && safeAreas.contains(currentArea.getOppositeArea())) {
            // 隣エリアが含まれている場合は、反対側エリアを除外する（隣エリアを優先するため）
            safeAreas.remove(currentArea.getOppositeArea());
        }
        caches.put("safeAreas", safeAreas);
        return safeAreas;
    }

    /**
     * 安全エリアの中で進行方向に近い方を返します
     * 
     * @param bot
     * @return
     */
    public Area getSafeArea(OkuRunBot bot) {
        if (caches.containsKey("safeArea")) {
            return (Area) caches.get("safeArea");
        }

        final List<Area> safeAreas = getSafeAreas(bot);
        double minDegree = Double.MAX_VALUE;
        Area minArea = null;
        for (final Area area : safeAreas) {
            final double[] center = area.getCenter();
            final double degree = bot.bearingTo(center[0], center[1]);
            if (degree < minDegree) {
                minDegree = degree;
                minArea = area;
            }
        }
        caches.put("safeArea", minArea);
        return minArea;
    }
}
