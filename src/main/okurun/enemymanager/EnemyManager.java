package okurun.enemymanager;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import dev.robocode.tankroyale.botapi.events.*;
import okurun.OkuRunBot;
import okurun.commander.Commander;
import okurun.gunner.BulletHistory;

/**
 * 敵ボット管理クラス
 */
public class EnemyManager {
    private final Map<Integer, EnemyProfile> enemyProfiles = new ConcurrentHashMap<>();
    private final Map<String, Object> caches = new ConcurrentHashMap<>();
    private final AtomicInteger enemyCount = new AtomicInteger(0);
    private final AtomicInteger myId = new AtomicInteger(0);

    /**
     * 敵ボットの総数を返します(死んだ敵も含みます)
     * 
     * @return 敵ボットの総数
     */
    public int getEnemyCount() {
        return enemyCount.get();
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
        return Math.min(aliveEnemyCount, enemyCount.get());
    }

    /**
     * 指定したIDの敵ボットのプロファイルを取得します
     * もしそのIDのプロファイルが存在しなければ新しく作成します
     * 
     * @param id 敵ボットのID
     * @return 指定したIDの敵ボットのプロファイル
     * @throws RuntimeException idがNO_TARGET、または自分のIDである場合
     */
    public EnemyProfile getEnemyProfile(int id) {
        if (id == Commander.NO_TARGET) {
            throw new RuntimeException("id is NO_TARGET");
        }
        if (id == myId.get()) {
            throw new RuntimeException("id is myId");
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
            final EnemyState latestEnemyState = enemyProfile.getLatestState();
            if (latestEnemyState == null) {
                continue;
            }
            if (latestEnemyState.energy <= 0) {
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

    /**
     * ターン毎のアクションの前にコールされるイベント
     * このイベントはメインスレッドからコールされます
     * 
     * @param bot Bot
     */
    public void onPreAction(OkuRunBot bot) {
        caches.clear();
        if (enemyProfiles.size() > enemyCount.get()) {
            System.out.println(String.format(
                    "Warning enemyProfiles.size()=%d > enemyCount=%d, bot.getEnemyCount()=%d",
                    enemyProfiles.size(),
                    enemyCount.get(),
                    bot.getEnemyCount()));
            for (final Entry<Integer, EnemyProfile> entry : enemyProfiles.entrySet()) {
                System.out.println(String.format(
                        "Warning Key: %d, id: %d", entry.getKey(), entry.getValue().getId()));
            }
        }
    }

    /**
     * ターン毎のアクションイベント
     * このイベントはメインスレッドからコールされます
     * 
     * @param bot Bot
     */
    public void onAction(OkuRunBot bot) {
    }

    /**
     * ターン毎のアクションの後にコールされるイベント
     * このイベントはメインスレッドからコールされます
     * 
     * @param bot Bot
     */
    public void onPostAction(OkuRunBot bot) {
        final int targetEnemyId = bot.getCommander().getTargetEnemyId(bot);
        if (targetEnemyId == Commander.NO_TARGET) {
            return;
        }
        final EnemyProfile enemyProfile = getEnemyProfile(targetEnemyId);
        enemyProfile.onPostAction(bot);
    }

    /**
     * ゲームが開始された時の処理
     * 
     * @param e   ゲーム開始イベント
     * @param bot Bot
     */
    public void onGameStarted(GameStartedEvent e, OkuRunBot bot) {
        try {
            enemyProfiles.clear();
            enemyCount.set(0);
            myId.set(0);
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }

    /**
     * ゲームが終了した時の処理
     * 
     * @param e   ゲーム終了イベント
     * @param bot Bot
     */
    public void onGameEnded(GameEndedEvent e, OkuRunBot bot) {
        try {
            for (final EnemyProfile enemyProfile : enemyProfiles.values()) {
                enemyProfile.onGameEnded(e, bot);
            }
            enemyProfiles.clear();
            enemyCount.set(0);
            myId.set(0);
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }

    /**
     * ラウンドが開始した時の処理
     * 
     * @param e ラウンド開始イベント
     */
    public void onRoundStarted(RoundStartedEvent e, OkuRunBot bot) {
    }

    /**
     * ラウンド終了時の処理
     * 
     * @param e
     * @param bot
     */
    public void onRoundEnded(RoundEndedEvent e, OkuRunBot bot) {
        try {
            for (final EnemyProfile profile : enemyProfiles.values()) {
                profile.onRoundEnded(e, bot);
            }
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }

    /**
     * 1ターン中の一定時間経過毎に呼ばれる
     * 
     * @param e   1ターン中の一定時間経過イベント
     * @param bot Bot
     */
    public void onTick(TickEvent e, OkuRunBot bot) {
        try {
            if (myId.get() == 0) {
                myId.set(bot.getMyId());
            }
            if (enemyCount.get() == 0) {
                enemyCount.set(bot.getEnemyCount());
            }

        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }

    /**
     * 敵ボットが死んだ時の処理
     * 
     * @param e   敵ボット死亡イベント
     * @param bot Bot
     */
    public void onBotDeath(BotDeathEvent e, OkuRunBot bot) {
        try {
            getEnemyProfile(e.getVictimId()).onBotDeath(e, bot);
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }

    /**
     * 自分が敵ボットにぶつかった時の処理
     * 
     * @param e   敵ボットにぶつかったイベント
     * @param bot Bot
     */
    public void onHitBot(HitBotEvent e, OkuRunBot bot) {
        try {
            getEnemyProfile(e.getVictimId()).onHitBot(e, bot);
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }

    /**
     * 自分が弾を発射した時の処理
     * 
     * @param e   弾が発射されたイベント
     * @param bot Bot
     */
    public void onBulletFired(BulletFiredEvent e, OkuRunBot bot) {
        try {
            final BulletHistory bulletHistory = bot.getGunner().getBulletHistory(e.getBullet().getBulletId());
            if (bulletHistory == null) {
                return;
            }
            getEnemyProfile(bulletHistory.targetEnemyId).onBulletFired(e, bot);
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }

    /**
     * 自分が敵の弾に当った時の処理
     * 
     * @param e   弾が当たったイベント
     * @param bot Bot
     */
    public void onHitByBullet(HitByBulletEvent e, OkuRunBot bot) {
        try {
            getEnemyProfile(e.getBullet().getOwnerId()).onHitByBullet(e, bot);
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }

    /**
     * 弾丸が敵ボットに当たった時の処理
     * 
     * @param e   弾が当たったイベント
     * @param bot Bot
     */
    public void onBulletHit(BulletHitBotEvent e, OkuRunBot bot) {
        try {
            getEnemyProfile(e.getVictimId()).onBulletHit(e, bot);
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }

    /**
     * 弾が弾に当たった時の処理
     * 
     * @param e   弾が弾に当たったイベント
     * @param bot Bot
     */
    public void onBulletHitBullet(BulletHitBulletEvent e, OkuRunBot bot) {
        final int ownerId = e.getHitBullet().getOwnerId();
        try {
            getEnemyProfile(ownerId).onBulletHitBullet(e, bot);
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }

    /**
     * 弾が壁に当たった時の処理
     * 
     * @param e   弾が壁に当たったイベント
     * @param bot Bot
     */
    public void onBulletHitWall(BulletHitWallEvent e, OkuRunBot bot) {
        try {
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }

    /**
     * 敵ボットをスキャンした時の処理
     * 
     * @param e   敵ボットをスキャンしたイベント
     * @param bot Bot
     */
    public void onScannedBot(ScannedBotEvent e, OkuRunBot bot) {
        try {
            getEnemyProfile(e.getScannedBotId()).onScannedBot(e, bot);
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }
}
