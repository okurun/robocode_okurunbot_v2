package okurun.arenamap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dev.robocode.tankroyale.botapi.Constants;
import dev.robocode.tankroyale.botapi.IBot;
import dev.robocode.tankroyale.botapi.events.*;
import okurun.OkuRunBot;
import okurun.battlemanager.BattleManager;
import okurun.battlemanager.EnemyState;

/**
 * アリーナマップクラス
 */
public class ArenaMap {
    public static enum WallId {
        LEFT, TOP, RIGHT, BOTTOM
    }

    /**
     * 壁クラス
     */
    public class Wall {
        public final WallId id;
        public final int x, y;

        Wall(WallId id, int x, int y) {
            this.id = id;
            this.x = x;
            this.y = y;
        }

        /**
         * 指定した方向に面しているかを返します
         * 進行方向が壁に向いている場合はtrue、そうでない場合はfalseを返します
         * 
         * @param direction 進行方向（度数法：0〜360）
         * @return 進行方向に面している場合はtrue、そうでない場合はfalse
         */
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

            final double r = OkuRunBot.BODY_SIZE * 0.5;
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
         * 
         * @param bot ボット
         * @return 壁と平行になるために左にきるべき角度（-90 〜 90）
         */
        public double getLeftTurnAngleToParallel(IBot bot) {
            switch (id) {
                case LEFT, RIGHT -> {
                    final double diff90 = bot.normalizeRelativeAngle(90 - bot.getDirection());
                    final double diff270 = bot.normalizeRelativeAngle(270 - bot.getDirection());
                    return (Math.abs(diff90) <= Math.abs(diff270)) ? diff90 : diff270;
                }
                case TOP, BOTTOM -> {
                    final double diff0 = bot.normalizeRelativeAngle(0 - bot.getDirection());
                    final double diff180 = bot.normalizeRelativeAngle(180 - bot.getDirection());
                    return (Math.abs(diff0) <= Math.abs(diff180)) ? diff0 : diff180;
                }
                default -> throw new IllegalStateException("Unknown wall id: " + id);
            }
        }
    }

    public static enum AreaId {
        TOP_LEFT, TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT
    }

    /**
     * エリアクラス
     */
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
            return new double[] { (x1 + x2) * 0.5, (y1 + y2) * 0.5 };
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

        /**
         * ボットから見た隣接するエリアのうち、進行方向に最も近いエリアを返します
         * 
         * @param bot Bot
         * @return 隣接するエリアのうち、進行方向に最も近いエリア
         */
        public Area getNeighboringArea(OkuRunBot bot) {
            final List<Area> neighboringAreas = getNeighboringAreas();
            if (Math.abs(bot.bearingTo(neighboringAreas.get(0).getCenter())) > Math.abs(bot
                    .bearingTo(neighboringAreas.get(1).getCenter()))) {
                // 隣接エリアのうち、進行方向に最も近いエリアを返す
                return neighboringAreas.get(1);
            }
            return neighboringAreas.get(0);
        }
    }

    private int height;
    private int width;
    private Map<WallId, Wall> walls;
    private Map<AreaId, Area> areas;
    private final Map<String, Object> caches = new ConcurrentHashMap<>();

    public void preAction(OkuRunBot bot) {
        caches.clear();
    }

    public void action(OkuRunBot bot) {
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
     * アリーナの中心座標を返す
     * 
     * @return アリーナの中心座標 [x, y]
     */
    public double[] getCenter() {
        return new double[] { width / 2.0, height / 2.0 };
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
     * 進行方向に面している壁を返します
     * 
     * @param direction 進行方向（度数法：0〜360）
     * @return 進行方向に面している壁のリスト
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

    /**
     * 衝突する可能性がある壁を表すクラス
     */
    public class PotentialCollisionWall {
        public final Wall wall;
        public final double turnsToCollision;

        public PotentialCollisionWall(Wall wall, double turnsToCollision) {
            this.wall = wall;
            this.turnsToCollision = turnsToCollision;
        }
    }

    /**
     * Botが衝突する可能性のある壁のリストを返します
     * 
     * @param bot Bot
     * @return 衝突する可能性のある壁のリスト
     */
    @SuppressWarnings("unchecked")
    public List<PotentialCollisionWall> getPotentialCollisionWalls(OkuRunBot bot) {
        if (caches.containsKey("potentialCollisionWalls")) {
            return (List<PotentialCollisionWall>) caches.get("potentialCollisionWalls");
        }
        final List<PotentialCollisionWall> collisionWalls = getPotentialCollisionWalls(
                bot.getX(), bot.getY(), bot.getDirection(), Constants.MAX_SPEED);
        caches.put("potentialCollisionWalls", collisionWalls);
        return collisionWalls;
    }

    /**
     * 座標、進行方向、速度から衝突する可能性のある壁を返します
     * 
     * @param x x座標
     * @param y y座標
     * @param d 進行方向
     * @param s 速度
     * @return 衝突する可能性のある壁のリスト
     */
    private List<PotentialCollisionWall> getPotentialCollisionWalls(double x, double y, double d, double s) {
        if (s == 0) {
            return List.of();
        }

        // 後退している場合は進行方向を反転させる
        final double direction = (s < 0) ? (d + 180) % 360 : d;
        // 後退している場合は速度を正にする
        final double speed = (s < 0) ? -s : s;

        final double deceleration = Math.abs(dev.robocode.tankroyale.botapi.Constants.DECELERATION);
        // 減速が完了するまでのターン数を計算（プラス1は安全マージン）
        final double limitTurns = Math.ceil(speed / deceleration) + 1;

        // 移動方向に面している壁
        final List<Wall> facingWalls = getWallsFacingTo(direction);

        final List<PotentialCollisionWall> collisionWalls = new ArrayList<>();
        for (Wall wall : facingWalls) {
            // 衝突までのターン数を計算
            double turns = wall.getTurnsToCollision(x, y, direction, speed);
            // 減速が完了する前に壁に衝突する可能性がある場合
            if (turns <= limitTurns) {
                collisionWalls.add(new PotentialCollisionWall(wall, turns));
            }
        }

        // 衝突までのターン数でソートする（近い順）
        collisionWalls.sort((w1, w2) -> Double.compare(
                w1.turnsToCollision,
                w2.turnsToCollision));

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
     * Botが現在いるエリアを返します
     * 
     * @param bot Bot
     * @return Botがいるエリア
     */
    public Area getArea(OkuRunBot bot) {
        return getArea(bot.getX(), bot.getY());
    }

    /**
     * 各エリアにいる敵の数を返します
     * 
     * @param bot Bot
     * @return 各エリアの敵の数
     */
    @SuppressWarnings("unchecked")
    private Map<AreaId, Integer> getAreasEnemyCount(OkuRunBot bot) {
        if (caches.containsKey("areasEnemyCount")) {
            return (Map<AreaId, Integer>) caches.get("areasEnemyCount");
        }

        final BattleManager battleManager = bot.getBattleManager();
        // 生存している敵の最後の状態を取得
        final Map<Integer, EnemyState> enemyStates = battleManager.getLatestAliveAndNotMissingEnemies(bot);

        final Map<AreaId, Integer> enemyCount = new HashMap<>();
        // 各エリアの敵の数を初期化
        for (final Area area : areas.values()) {
            enemyCount.put(area.id, 0);
        }
        if (enemyStates.size() == 0) {
            caches.put("areasEnemyCount", enemyCount);
            return enemyCount;
        }
        // 各敵がどのエリアにいるかを確認してカウントする
        for (final EnemyState enemyState : enemyStates.values()) {
            if (enemyState == null) {
                continue;
            }
            final Area area = getArea(enemyState.x, enemyState.y);
            if (area == null) {
                continue;
            }
            enemyCount.put(area.id, enemyCount.get(area.id) + 1);
        }
        caches.put("areasEnemyCount", enemyCount);
        return enemyCount;
    }

    /**
     * 敵の少ない安全なエリア一覧を返します
     * 
     * @param bot Bot
     * @return 敵の少ない安全なエリア一覧
     */
    @SuppressWarnings("unchecked")
    private List<Area> getSafeAreas(OkuRunBot bot) {
        if (caches.containsKey("safeAreas")) {
            return (List<Area>) caches.get("safeAreas");
        }

        // 各エリアの敵の数を取得
        final Map<AreaId, Integer> areaEnemyCount = getAreasEnemyCount(bot);
        // 一番敵の数が少ないエリアの敵の数
        int minEnemyCount = areaEnemyCount.values().stream().min(Integer::compare).orElse(0);
        // 現在のエリアを取得
        final Area currentArea = getArea(bot);
        // 現在のエリアの隣のエリアのリストを取得
        final List<Area> neighboringAreas = currentArea.getNeighboringAreas();
        // 隣のエリアに敵の数が一番少ないエリアが含まれているか
        boolean containNeighboringArea = false;
        // 一番敵の数が少ないエリアのリストを取得
        List<Area> safeAreas = new ArrayList<>();
        for (final AreaId areaId : areaEnemyCount.keySet()) {
            if (areaEnemyCount.get(areaId) == minEnemyCount) {
                final Area area = areas.get(areaId);
                if (area.equals(currentArea)) {
                    // 現在のエリアが一番敵の数が少ない場合は、現在のエリアを返す
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
            // 隣エリアが含まれている場合は、反対側エリアを除外する（反対側エリアへ向かうと激戦区であるアリーナ中心を通らざるを得ないため）
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

        // 安全エリアのリストを取得
        final List<Area> safeAreas = getSafeAreas(bot);
        if (safeAreas.size() == 1) {
            caches.put("safeArea", safeAreas.get(0));
            return safeAreas.get(0);
        }

        double minDegree = Double.MAX_VALUE;
        Area minArea = null;
        for (final Area area : safeAreas) {
            final double[] center = area.getCenter();
            final double degree = Math.abs(bot.bearingTo(center[0], center[1]));
            if (degree < minDegree) {
                minDegree = degree;
                minArea = area;
            }
        }
        caches.put("safeArea", minArea);
        return minArea;
    }

    /**
     * ゲームが開始された時の処理
     * 
     * @param e ゲーム開始イベント
     * @param bot Bot
     */
    public void onGameStarted(GameStartedEvent e, OkuRunBot bot) {
        this.height = bot.getArenaHeight();
        this.width = bot.getArenaWidth();
        walls = Map.of(
                WallId.LEFT, new Wall(WallId.LEFT, 0, -1),
                WallId.TOP, new Wall(WallId.TOP, -1, height),
                WallId.RIGHT, new Wall(WallId.RIGHT, width, -1),
                WallId.BOTTOM, new Wall(WallId.BOTTOM, -1, 0));
        final double halfWidth = width * 0.5;
        final double halfHeight = height * 0.5;
        areas = Map.of(
                AreaId.TOP_LEFT, new Area(AreaId.TOP_LEFT, 0, halfHeight, halfWidth, height),
                AreaId.TOP_RIGHT, new Area(AreaId.TOP_RIGHT, halfWidth, halfHeight, width, height),
                AreaId.BOTTOM_RIGHT, new Area(AreaId.BOTTOM_RIGHT, halfWidth, 0, width, halfHeight),
                AreaId.BOTTOM_LEFT, new Area(AreaId.BOTTOM_LEFT, 0, 0, halfWidth, halfHeight));    }
}
