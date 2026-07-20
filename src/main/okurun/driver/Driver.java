package okurun.driver;

import java.util.HashMap;
import java.util.Map;

import dev.robocode.tankroyale.botapi.events.GameStartedEvent;
import okurun.OkuRunBot;
import okurun.driver.actions.*;

/**
 * 操舵士クラス
 */
public class Driver {
    public static enum ActionId {
        MOVE_TO_FORWARD,
        MOVE_TO,
        AVOID_WALL,
    }

    private final Map<ActionId, DriveAction> actions = new HashMap<>();

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
        ActionId action = bot.getCommander().getDriveAction(bot);
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
        actions.put(ActionId.MOVE_TO_FORWARD, new MoveToForwardDriveAction());
        actions.put(ActionId.MOVE_TO, new MoveToDriveAction());
        actions.put(ActionId.AVOID_WALL, new AvoidWallDriveAction());
    }
}
