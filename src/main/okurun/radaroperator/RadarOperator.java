package okurun.radaroperator;

import java.util.HashMap;
import java.util.Map;

import dev.robocode.tankroyale.botapi.events.GameStartedEvent;
import okurun.OkuRunBot;
import okurun.radaroperator.actions.*;

/**
 * レーダー技士クラス
 */
public class RadarOperator {
    public static enum ActionId {
        ALL_SCAN,
        TARGET_SCAN,
    }

    private final Map<ActionId, RadarAction> actions = new HashMap<>();

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
        ActionId action = bot.getCommander().getRadarAction(bot);
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
        actions.put(ActionId.ALL_SCAN, new AllScanRadarAction());
        actions.put(ActionId.TARGET_SCAN, new TargetScanRadarAction());
    }
}
