package okurun.gunner;

import java.util.HashMap;
import java.util.Map;

import dev.robocode.tankroyale.botapi.events.GameStartedEvent;
import dev.robocode.tankroyale.botapi.graphics.Color;
import okurun.OkuRunBot;
import okurun.commander.Commander;
import okurun.gunner.actions.*;

/**
 * 砲撃手クラス
 */
public class Gunner {
    public static enum Action {
        SCAN,
        TRACKING,
        NORMAL,
        RAPID_FIRE,
        EXECUTION,
        MAX_POWER;
    }

    private final Map<Action, GunAction> actions = new HashMap<>();

    public void preAction(OkuRunBot bot) {
    }

    public void action(OkuRunBot bot) {
        final Commander commander = bot.getCommander();
        Action action = commander.getGunActionName(bot);
        while (action != null) {
            action = actions.get(action).action(bot);
        }
    }

    public static Color getBulletColor(double power) {
        // 弾丸のパワーに応じて色分け
        if (power >= 3) {
            return Color.RED;
        } else if (power >= 2) {
            return Color.ORANGE;
        } else if (power >= 1) {
            return Color.YELLOW;
        }
        return Color.WHITE;
    }

    /**
     * ゲームが開始された時の処理
     * 
     * @param e ゲーム開始イベント
     * @param bot Bot
     */
    public void onGameStarted(GameStartedEvent e, OkuRunBot bot) {
        actions.put(Action.SCAN, new ScanGunAction());
        actions.put(Action.TRACKING, new TrackingGunAction());
        actions.put(Action.NORMAL, new NormalGunAction());
        actions.put(Action.RAPID_FIRE, new RapidFireGunAction());
        actions.put(Action.EXECUTION, new ExecutionGunAction());
        actions.put(Action.MAX_POWER, new MaxPowerGunAction());
    }}
