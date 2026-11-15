package okurun.gunner;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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

    public void setLastFiredTurnNum(int turnNum) {
        lastFiredTurnNum.set(turnNum);
    }

    public int getLastFiredTurnNum() {
        return lastFiredTurnNum.get();
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
            action = actions.get(action).onAction(bot);
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
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }
}
