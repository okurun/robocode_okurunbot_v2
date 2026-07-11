package okurun.battlemanager;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

import dev.robocode.tankroyale.botapi.events.*;
import dev.robocode.tankroyale.botapi.graphics.Color;
import okurun.OkuRunBot;
import okurun.commander.Commander;
import okurun.gunner.Gunner;

/**
 * 戦闘管理クラス
 */
public class BattleManager {
    private final Deque<BulletHistory> bulletStack = new ConcurrentLinkedDeque<>();
    private final Map<Integer, BulletHistory> bulletHistories = new ConcurrentHashMap<>();
    private final Map<Integer, EnemyProfile> enemyProfiles = new ConcurrentHashMap<>();
    private final Map<String, Object> caches = new ConcurrentHashMap<>();
    private final AtomicInteger lastFiredTurnNum = new AtomicInteger(0);

    private int enemyCount;
    private int myId = 0;

    public void init(OkuRunBot bot) {
        myId = bot.getMyId();
        enemyCount = bot.getEnemyCount();
        lastFiredTurnNum.set(0);
        bulletStack.clear();
        bulletHistories.clear();
    }

    public void preAction(OkuRunBot bot) {
        caches.clear();
        if (enemyProfiles.size() > enemyCount) {
            System.out.println(String.format(
                    "Warning enemyProfiles.size()=%d > enemyCount=%d",
                    enemyProfiles.size(),
                    enemyCount));
            for (final Entry<Integer, EnemyProfile> entry : enemyProfiles.entrySet()) {
                System.out.println(String.format(
                        "Warning Key: %d, id: %d", entry.getKey(), entry.getValue().getId()));
            }
        }
    }

    public void action(OkuRunBot bot) {
        // デバッグ用に射撃目標位置を描きます
        // ※ 描画にはUI画面でDebug Graphicsを有効にする必要があります
        for (final BulletHistory bulletHistory : bulletHistories.values()) {
            Color color = Gunner.getBulletColor(bulletHistory.bulletState.getPower());
            if (bulletHistory.predictTurnNum > bot.getTurnNumber()) {
                color = Color.fromRgba(color, 200);
            } else if (bulletHistory.predictTurnNum < bot.getTurnNumber()) {
                color = Color.fromRgba(color, 255 - (bot.getTurnNumber() - bulletHistory.predictTurnNum) * 20);
            }
            bot.drawTarget(bulletHistory.getTargetPosition(), color);
        }
    }

    /**
     * 敵ボットの総数を返します(死んだ敵も含みます)
     * 
     * @return 敵ボットの総数
     */
    public int getEnemyCount() {
        return enemyCount;
    }

    /**
     * 生存していて見失っていない敵ボットの数を返します
     * 
     * @param bot Bot
     * @return 生存していて見失っていない敵ボットの数
     */
    public int getAliveAndNotMissingEnemyCount(OkuRunBot bot) {
        int aliveEnemyCount = 0;
        for (final EnemyProfile enemyProfile : enemyProfiles.values()) {
            if (enemyProfile.isAliveAndNotMissing(bot)) {
                aliveEnemyCount++;
            }
        }
        return Math.min(aliveEnemyCount, enemyCount);
    }

    /**
     * 指定したIDの敵ボットのプロファイルを取得します
     * もしそのIDのプロファイルが存在しなければ新しく作成します
     * 
     * @param id 敵ボットのID
     * @return 指定したIDの敵ボットのプロファイル
     */
    public EnemyProfile getEnemyProfile(int id) {
        if (id == Commander.NO_TARGET || id == myId) {
            return null;
        }
        return enemyProfiles.computeIfAbsent(id, EnemyProfile::new);
    }

    /**
     * 生きていて見失っていない敵のうち、最初に発見した敵のプロファイルを取得します
     * 
     * @param bot Bot
     * @return 生きていて見失っていない敵のうち、最初に発見した敵のプロファイル
     */
    public EnemyProfile getAliveEnemy(OkuRunBot bot) {
        for (final EnemyProfile enemyProfile : enemyProfiles.values()) {
            if (enemyProfile.isAliveAndNotMissing(bot)) {
                return enemyProfile;
            }
        }
        return null;
    }

    /**
     * 指定したIDの敵の最新の敵ボットの状態を返します
     * 敵が死んでいる場合や、まだスキャンされていない場合はnullを返します
     * 
     * @param id 敵ボットのID
     * @return 指定したIDの敵の最新の敵ボットの状態
     */
    public EnemyState getLatestEnemyState(int id) {
        if (id == Commander.NO_TARGET) {
            return null;
        }
        return getEnemyProfile(id).getLatestState();
    }

    /**
     * 生存していて見失っていない、全ての敵ボットの最新の状態をMapで返します
     * 
     * @param bot Bot
     * @return 生存していて見失っていない、全ての敵ボットの最新の状態
     */
    public Map<Integer, EnemyState> getLatestAliveAndNotMissingEnemies(OkuRunBot bot) {
        Map<Integer, EnemyState> latestEnemyStates = new HashMap<>();
        if (enemyProfiles.size() == 0) {
            return latestEnemyStates;
        }
        for (final EnemyProfile enemyProfile : enemyProfiles.values()) {
            if (!enemyProfile.isAliveAndNotMissing(bot)) {
                continue;
            }
            latestEnemyStates.put(enemyProfile.getId(), enemyProfile.getLatestState());
        }
        return latestEnemyStates;
    }

    /**
     * エネルギーが0の敵ボットのプロファイルを取得します
     * 
     * @param bot Bot
     * @return エネルギーが0の敵ボットのプロファイル
     */
    public EnemyProfile getZeroEnergyEnemy(OkuRunBot bot) {
        if (enemyProfiles.size() == 0) {
            return null;
        }
        for (final EnemyProfile enemyProfile : enemyProfiles.values()) {
            if (!enemyProfile.isAliveAndNotMissing(bot)) {
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

    /**
     * 最も近い生存している敵ボットのプロファイルを取得します
     * 
     * @param bot Bot
     * @return 最も近い生存している敵ボットのプロファイル
     */
    public EnemyProfile getNearestAliveEnemy(OkuRunBot bot) {
        EnemyProfile nearestAliveEnemy = null;
        double nearestDistance = Double.POSITIVE_INFINITY;
        for (final EnemyProfile enemyProfile : enemyProfiles.values()) {
            if (!enemyProfile.isAliveAndNotMissing(bot)) {
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

    /**
     * 弾丸履歴をスタックします
     * 
     * @param bulletHistory 弾丸履歴
     */
    public void addBulletStack(BulletHistory bulletHistory) {
        bulletStack.addLast(bulletHistory);
    }

    /**
     * 弾丸履歴をスタックから取得します
     * @param bulletId 弾丸ID
     * @return 弾丸履歴
     */
    public BulletHistory getBulletHistory(int bulletId) {
        return bulletHistories.get(bulletId);
    }

    /**
     * ゲームが終了した時の処理
     * 
     * @param e ゲーム終了イベント
     * @param bot Bot
     */
    public void onGameEnded(GameEndedEvent e, OkuRunBot bot) {
        enemyProfiles.clear();
    }

    /**
     * ラウンド終了時の処理
     * 
     * @param e
     * @param bot
     */
    public void onRoundEnded(RoundEndedEvent e, OkuRunBot bot) {
        lastFiredTurnNum.set(0);
        bulletStack.clear();
        bulletHistories.clear();
        for (EnemyProfile profile : enemyProfiles.values()) {
            profile.reset();
        }
    }

    /**
     * 敵ボットが死んだ時の処理
     * 
     * @param e   敵ボット死亡イベント
     * @param bot Bot
     */
    public void onBotDeath(BotDeathEvent e, OkuRunBot bot) {
        getEnemyProfile(e.getVictimId()).died();
    }

    /**
     * 自分が敵ボットにぶつかった時の処理
     * 
     * @param e   敵ボットにぶつかったイベント
     * @param bot Bot
     */
    public void onHitBot(HitBotEvent e, OkuRunBot bot) {
        getEnemyProfile(e.getVictimId()).setLastConfirmedTurn(e.getTurnNumber());
    }

    /**
     * 自分が弾を発射した時の処理
     * 
     * @param e   弾が発射されたイベント
     * @param bot Bot
     */
    public void onBulletFired(BulletFiredEvent e, OkuRunBot bot) {
        setLastFiredTurnNum(e.getTurnNumber());
        final BulletHistory bulletHistory = bulletStack.pollFirst();
        if (bulletHistory == null) {
            System.out.println("Warning: onBulletFired(): bulletHistory is null");
            return;
        }
        bulletHistory.bulletState = e.getBullet();
        bulletHistories.put(bulletHistory.bulletState.getBulletId(), bulletHistory);
    }

    /**
     * 自分が敵の弾に当った時の処理
     * 
     * @param e   弾が当たったイベント
     * @param bot Bot
     */
    public void onHitByBullet(HitByBulletEvent e, OkuRunBot bot) {
        getEnemyProfile(e.getBullet().getOwnerId()).setLastConfirmedTurn(e.getTurnNumber());
    }

    /**
     * 弾丸が敵ボットに当たった時の処理
     * 
     * @param e   弾が当たったイベント
     * @param bot Bot
     */
    public void onBulletHit(BulletHitBotEvent e, OkuRunBot bot) {
        getEnemyProfile(e.getVictimId()).setLastConfirmedTurn(e.getTurnNumber());
        bulletHistories.remove(e.getBullet().getBulletId());
    }

    /**
     * 弾が弾に当たった時の処理
     * 
     * @param e   弾が弾に当たったイベント
     * @param bot Bot
     */
    public void onBulletHitBullet(BulletHitBulletEvent e, OkuRunBot bot) {
        final int ownerId = e.getBullet().getOwnerId();
        getEnemyProfile(ownerId).setLastConfirmedTurn(e.getTurnNumber());
        bulletHistories.remove(e.getBullet().getBulletId());
    }

    /**
     * 弾が壁に当たった時の処理
     * 
     * @param e   弾が壁に当たったイベント
     * @param bot Bot
     */
    public void onBulletHitWall(BulletHitWallEvent e, OkuRunBot bot) {
        bulletHistories.remove(e.getBullet().getBulletId());
    }

    /**
     * 敵ボットをスキャンした時の処理
     * 
     * @param e   敵ボットをスキャンしたイベント
     * @param bot Bot
     */
    public void onScannedBot(ScannedBotEvent e, OkuRunBot bot) {
        final EnemyProfile enemyProfile = getEnemyProfile(e.getScannedBotId());
        final EnemyState enemyState = enemyProfile.getLatestState();
        double turnDegree = 0;
        double acceleration = 0;
        if (enemyState != null) {
            final double diffDegree = e.getDirection() - enemyState.heading;
            if (diffDegree == 0) {
                turnDegree = 0;
            } else {
                turnDegree = diffDegree / (e.getTurnNumber() - enemyState.scannedTurnNum);
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
    }
}
