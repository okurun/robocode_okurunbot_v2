package okurun.radaroperator;

import java.util.HashMap;
import java.util.Map;

import okurun.OkuRunBot;
import okurun.radaroperator.actions.*;

/**
 * レーダー技士クラス
 */
public class RadarOperator {
    private Map<String, RadarAction> actions = new HashMap<>();

    public void init(OkuRunBot bot) {
        actions.put(AllScanRadarAction.class.getName(), new AllScanRadarAction());
        actions.put(TargetScanRadarAction.class.getName(), new TargetScanRadarAction());
    }

    public void action(OkuRunBot bot) {
        String radarActionName = bot.getCommander().getRadarActionName(bot);
        while (radarActionName != null) {
            radarActionName = actions.get(radarActionName).action(bot);
        }
    }
}
