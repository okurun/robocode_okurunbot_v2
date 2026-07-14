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
        ALL_SCAN, TARGET_SCAN
    }

    private Map<ActionId, RadarAction> actions = new HashMap<>();

    public void preAction(OkuRunBot bot) {
    }

    public void action(OkuRunBot bot) {
        ActionId action = bot.getCommander().getRadarAction(bot);
        while (action != null) {
            action = actions.get(action).action(bot);
        }
    }

    public void postAction(OkuRunBot bot) {
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
