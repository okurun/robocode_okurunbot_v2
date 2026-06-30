package okurun.battlemanager;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

import dev.robocode.tankroyale.botapi.events.BotDeathEvent;
import dev.robocode.tankroyale.botapi.events.RoundEndedEvent;
import dev.robocode.tankroyale.botapi.events.ScannedBotEvent;
import dev.robocode.tankroyale.botapi.graphics.Color;
import okurun.OkuRunBot;

public class BattleManager {
    public final Deque<BulletStatus> bulletStack = new ConcurrentLinkedDeque<>();
    public final Map<Integer, BulletStatus> bullets = new ConcurrentHashMap<>();

    private final Map<Integer, EnemyProfile> enemyProfiles = new ConcurrentHashMap<>();
    private final Map<String, Object> caches = new ConcurrentHashMap<>();
    private final AtomicInteger lastFiredTurnNum = new AtomicInteger(0);

    private int enemyCount;
    private int myId;

    public void init(OkuRunBot bot) {
        enemyCount = bot.getEnemyCount();
        myId = bot.getMyId();
        init();
    }

    private void init() {
        lastFiredTurnNum.set(0);
        enemyProfiles.clear();
        bulletStack.clear();
        bullets.clear();
        for (int i = 1; i <= enemyCount + 1; i++) {
            if (i == myId)
                continue;
            enemyProfiles.put(i, new EnemyProfile(i));
        }
    }

    public void action(OkuRunBot bot) {
        caches.clear();

        // デバッグ用に射撃目標位置に円を描きます
        // ※ 描画にはUI画面でDebug Graphicsを有効にする必要があります
        for (final BulletStatus bulletStatus : bullets.values()) {
            Color color;
            switch ((int) bulletStatus.bulletState.getPower()) {
                case 1:
                    color = Color.YELLOW;
                    break;
                case 2:
                    color = Color.ORANGE;
                    break;
                case 3:
                    color = Color.RED;
                    break;
                default:
                    color = Color.WHITE;
                    break;
            }
            if (bulletStatus.predictTurnNum > bot.getTurnNumber()) {
                color = Color.fromRgba(color, 200);
            } else if (bulletStatus.predictTurnNum < bot.getTurnNumber()) {
                color = Color.fromRgba(color, 255 - (bot.getTurnNumber() - bulletStatus.predictTurnNum) * 20);
            }
            bot.drawTarget(bulletStatus.targetX, bulletStatus.targetY, color);
        }
    }

    public int getEnemyCount() {
        return enemyCount;
    }

    public int getAliveEnemyCount() {
        int aliveEnemyCount = 0;
        for (final EnemyProfile enemyProfile : enemyProfiles.values()) {
            if (enemyProfile.isAlive()) {
                aliveEnemyCount++;
            }
        }
        return aliveEnemyCount;
    }

    /**
     * 全ての生存している敵をスキャンしたかどうかを返します
     * 
     * @return 全ての生存している敵をスキャンしたかどうか
     */
    public boolean hasScannedAllAliveEnemies() {
        if (caches.containsKey("hasScannedAllAliveEnemies")) {
            return (boolean) caches.get("hasScannedAllAliveEnemies");
        }
        for (final EnemyProfile enemyProfile : enemyProfiles.values()) {
            if (enemyProfile.isAlive() && enemyProfile.getLatestState() == null) {
                caches.put("hasScannedAllAliveEnemies", Boolean.FALSE);
                return false;
            }
        }
        caches.put("hasScannedAllAliveEnemies", Boolean.TRUE);
        return true;
    }

    public EnemyProfile getEnemyProfile(int id) {
        return enemyProfiles.get(id);
    }

    /**
     * 生きている敵のうち、最初に発見した敵のプロファイルを取得します
     * 
     * @return 生きている敵のうち、最初に発見した敵のプロファイル
     */
    public EnemyProfile getAlivalEnemy() {
        for (final EnemyProfile enemyProfile : enemyProfiles.values()) {
            if (enemyProfile.isAlive()) {
                return enemyProfile;
            }
        }
        return null;
    }

    public EnemyState getLatestEnemyState(int id) {
        return enemyProfiles.get(id).getLatestState();
    }

    public Map<Integer, EnemyState> getLatestAlivalEnemyStates() {
        Map<Integer, EnemyState> latestEnemyStates = new HashMap<>();
        for (final EnemyProfile enemyProfile : enemyProfiles.values()) {
            if (!enemyProfile.isAlive()) {
                continue;
            }
            latestEnemyStates.put(enemyProfile.getId(), enemyProfile.getLatestState());
        }
        return latestEnemyStates;
    }

    public EnemyProfile getZeroEnergyEnemy() {
        for (final EnemyProfile enemyProfile : enemyProfiles.values()) {
            if (!enemyProfile.isAlive()) {
                continue;
            }
            final EnemyState latesEnemyState = enemyProfile.getLatestState();
            if (latesEnemyState == null) {
                continue;
            }
            if (latesEnemyState.energy <= 0) {
                return enemyProfile;
            }
        }
        return null;
    }

    public EnemyProfile getNearestAliveEnemy(OkuRunBot bot) {
        EnemyProfile nearestAliveEnemy = null;
        double nearestDistance = Double.POSITIVE_INFINITY;
        for (final EnemyProfile enemyProfile : enemyProfiles.values()) {
            if (!enemyProfile.isAlive()) {
                continue;
            }
            final EnemyState latestEnemyState = enemyProfile.getLatestState();
            if (latestEnemyState == null) {
                continue;
            }
            double distance = bot.distanceTo(latestEnemyState.x, latestEnemyState.y);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestAliveEnemy = enemyProfile;
            }
        }
        return nearestAliveEnemy;
    }

    public void setLastFiredTurnNum(int turnNum) {
        lastFiredTurnNum.set(turnNum);
    }

    public int getLastFiredTurnNum() {
        return lastFiredTurnNum.get();
    }

    public void onRoundEnded(RoundEndedEvent e, OkuRunBot bot) {
        init();
    }

    public void onBotDeath(BotDeathEvent e, OkuRunBot bot) {
        final EnemyProfile enemyProfile = enemyProfiles.get(e.getVictimId());
        if (enemyProfile == null) {
            return;
        }
        enemyProfile.died();
    }

    public void onScannedBot(ScannedBotEvent e, OkuRunBot bot) {
        final EnemyProfile enemyProfile = enemyProfiles.get(e.getScannedBotId());
        final EnemyState enemyState = enemyProfile.getLatestState();
        double turnDegree = 0;
        double acceleration = 0;
        if (enemyState != null) {
            final double diffDegree = e.getDirection() - enemyState.heading;
            if (diffDegree == 0) {
                turnDegree = 0;
            } else {
                turnDegree = diffDegree / (e.getTurnNumber() - enemyState.scandTurnNum);
            }
            acceleration = e.getSpeed() - enemyState.velocity;
        }

        enemyProfile.addState(new EnemyState(
                e.getScannedBotId(),
                e.getTurnNumber(),
                e.getX(),
                e.getY(),
                e.getDirection(),
                e.getSpeed(),
                e.getEnergy(),
                turnDegree,
                acceleration,
                bot.distanceTo(e.getX(), e.getY())));
        if (e.getEnergy() <= 0) {
            enemyProfile.died();
        }
    }

}
