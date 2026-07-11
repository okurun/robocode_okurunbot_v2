package okurun.radaroperator;

import java.util.HashMap;
import java.util.Map;

import okurun.OkuRunBot;
import okurun.radaroperator.actions.*;

/**
 * レーダー技士クラス
 */
public class RadarOperator {
    public static enum Action {
        ALL_SCAN, TARGET_SCAN
    }

    private Map<Action, RadarAction> actions = new HashMap<>();

    public void init(OkuRunBot bot) {
        actions.put(Action.ALL_SCAN, new AllScanRadarAction());
        actions.put(Action.TARGET_SCAN, new TargetScanRadarAction());
    }

    public void preAction(OkuRunBot bot) {
    }

    public void action(OkuRunBot bot) {
        Action action = bot.getCommander().getRadarAction(bot);
        while (action != null) {
            action = actions.get(action).action(bot);
        }
    }
}
