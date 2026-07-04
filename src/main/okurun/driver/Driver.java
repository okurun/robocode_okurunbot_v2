package okurun.driver;

import java.util.HashMap;
import java.util.Map;

import okurun.OkuRunBot;
import okurun.driver.actions.*;

/**
 * 操舵士クラス
 */
public class Driver {
    private final Map<String, DriveAction> actions = new HashMap<>();

    public void init(OkuRunBot bot) {
        actions.put(MoveToDriveAction.class.getName(), new MoveToDriveAction());
        actions.put(AvoidWallDriveAction.class.getName(), new AvoidWallDriveAction());
    }

    public void action(OkuRunBot bot) {
        String actionName = bot.getCommander().getDriveActionName(bot);
        while (actionName != null) {
            actionName = actions.get(actionName).action(bot);
        }

        // actionが失敗した場合はとりあえず前進する
        actions.get(MoveToDriveAction.class.getName()).action(bot);
    }
}
