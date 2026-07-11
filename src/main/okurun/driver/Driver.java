package okurun.driver;

import java.util.HashMap;
import java.util.Map;

import okurun.OkuRunBot;
import okurun.driver.actions.*;

/**
 * 操舵士クラス
 */
public class Driver {
    public static enum Action {
        MOVE_TO, AVOID_WALL
    }

    private final Map<Action, DriveAction> actions = new HashMap<>();

    public void init(OkuRunBot bot) {
        actions.put(Action.MOVE_TO, new MoveToDriveAction());
        actions.put(Action.AVOID_WALL, new AvoidWallDriveAction());
    }

    public void preAction(OkuRunBot bot) {
    }

    public void action(OkuRunBot bot) {
        Action action = bot.getCommander().getDriveAction(bot);
        while (action != null) {
            action = actions.get(action).action(bot);
        }

        // actionが失敗した場合はとりあえず前進する
        actions.get(Action.MOVE_TO).action(bot);
    }
}
