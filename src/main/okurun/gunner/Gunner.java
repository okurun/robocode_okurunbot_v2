package okurun.gunner;

import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

import dev.robocode.tankroyale.botapi.BulletState;
import dev.robocode.tankroyale.botapi.events.*;
import okurun.OkuRunBot;
import okurun.commander.Commander;
import okurun.gunner.actions.*;

/**
 * 砲撃手クラス
 */
public class Gunner {
    public static enum ActionId {
        SCAN,
        TRACKING,
        EXECUTION,
        MAX_POWER;
    }

    private final Map<ActionId, GunAction> actions = new HashMap<>();
    private final AtomicInteger lastFiredTurnNum = new AtomicInteger(0);
    private final Deque<BulletHistory> bulletStack = new ConcurrentLinkedDeque<>();
    private final Map<Integer, BulletHistory> bulletHistories = new ConcurrentHashMap<>();
    private final Deque<Integer> removeBullets = new ConcurrentLinkedDeque<>();

    public void setLastFiredTurnNum(int turnNum) {
        lastFiredTurnNum.set(turnNum);
    }

    public int getLastFiredTurnNum() {
        return lastFiredTurnNum.get();
    }

    private void addBulletHistory(BulletState bulletState) {
        if (bulletHistories.containsKey(bulletState.getBulletId())) {
            return;
        }
        final BulletHistory bulletHistory = bulletStack.pollFirst();
        if (bulletHistory == null) {
            System.out.println("Warning: onBulletFired(): bulletHistory is null");
            return;
        }
        bulletHistory.bulletId = bulletState.getBulletId();
        bulletHistory.power = bulletState.getPower();
        bulletHistories.put(bulletHistory.bulletId, bulletHistory);
    }

    public Collection<BulletHistory> getBulletHistories() {
        return bulletHistories.values();
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
     * 
     * @param bulletId 弾丸ID
     * @return 弾丸履歴
     */
    public BulletHistory getBulletHistory(int bulletId) {
        return bulletHistories.get(bulletId);
    }

    /**
     * ターン毎のアクションの前にコールされるイベント
     * このイベントはメインスレッドからコールされます
     * 
     * @param bot Bot
     */
    public void onPreAction(OkuRunBot bot) {
    }

    /**
     * ターン毎のアクションイベント
     * このイベントはメインスレッドからコールされます
     * 
     * @param bot Bot
     */
    public void onAction(OkuRunBot bot) {
        final Commander commander = bot.getCommander();
        ActionId action = commander.getGunActionName(bot);
        while (action != null) {
            action = actions.get(action).action(bot);
        }
    }

    /**
     * ターン毎のアクションの後にコールされるイベント
     * このイベントはメインスレッドからコールされます
     * 
     * @param bot Bot
     */
    public void onPostAction(OkuRunBot bot) {
    }

    /**
     * ゲームが開始された時の処理
     * 
     * @param e   ゲーム開始イベント
     * @param bot Bot
     */
    public void onGameStarted(GameStartedEvent e, OkuRunBot bot) {
        actions.put(ActionId.SCAN, new ScanGunAction());
        actions.put(ActionId.TRACKING, new TrackingGunAction());
        actions.put(ActionId.EXECUTION, new ExecutionGunAction());
        actions.put(ActionId.MAX_POWER, new MaxPowerGunAction());
    }

    /**
     * ラウンド終了時の処理
     * 
     * @param e
     * @param bot
     */
    public void onRoundEnded(RoundEndedEvent e, OkuRunBot bot) {
        try {
            lastFiredTurnNum.set(0);
            bulletStack.clear();
            bulletHistories.clear();
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
            e.getBulletStates().forEach(bulletState -> {
                addBulletHistory(bulletState);
            });
            while (removeBullets.size() > 0) {
                bulletHistories.remove(removeBullets.pollFirst());
            }
            for (final BulletHistory bulletHistory : bulletHistories.values()) {
                if (bulletHistory.predictTurnNum >= bot.getTurnNumber()) {
                    continue;
                }
                boolean exists = false;
                for (final BulletState bulletState : e.getBulletStates()) {
                    if (bulletHistory.bulletId == bulletState.getBulletId()) {
                        exists = true;
                        break;
                    }
                }
                if (exists) {
                    continue;
                }
                removeBullets.addLast(bulletHistory.bulletId);
            }
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
            setLastFiredTurnNum(e.getTurnNumber());
            addBulletHistory(e.getBullet());
            final BulletHistory bulletHistory = getBulletHistory(e.getBullet().getBulletId());
            if (bulletHistory == null) {
                return;
            }
            bot.getEnemyManager().getEnemyProfile(bulletHistory.targetEnemyId).onBulletFired(e, bot);
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
            bulletHistories.remove(e.getBullet().getBulletId());
            removeBullets.remove(e.getBullet().getBulletId());
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
        try {
            bulletHistories.remove(e.getBullet().getBulletId());
            removeBullets.remove(e.getBullet().getBulletId());
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
            bulletHistories.remove(e.getBullet().getBulletId());
            removeBullets.remove(e.getBullet().getBulletId());
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }
}
