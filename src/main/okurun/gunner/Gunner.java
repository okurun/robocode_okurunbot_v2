package okurun.gunner;

import java.util.HashMap;
import java.util.Map;

import okurun.OkuRunBot;
import okurun.commander.Commander;
import okurun.gunner.actions.*;

/**
 * 砲撃手クラス
 */
public class Gunner {
    private final Map<String, GunAction> actions = new HashMap<>();

    public void init(OkuRunBot bot) {
        actions.put(ScanGunAction.class.getName(), new ScanGunAction());
        actions.put(TrackingGunAction.class.getName(), new TrackingGunAction());
        actions.put(NormalGunAction.class.getName(), new NormalGunAction());
        actions.put(RapidFireGunAction.class.getName(), new RapidFireGunAction());
        actions.put(ExecutionGunAction.class.getName(), new ExecutionGunAction());
    }

    public void action(OkuRunBot bot) {
        final Commander commander = bot.getCommander();
        String gunActionName = commander.getGunActionName(bot);
        while (gunActionName != null) {
            gunActionName = actions.get(gunActionName).action(bot);
        }
    }

}
