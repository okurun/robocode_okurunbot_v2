package okurun.driver.actions;

import dev.robocode.tankroyale.botapi.Constants;
import dev.robocode.tankroyale.botapi.graphics.Color;
import okurun.OkuRunBot;
import okurun.commander.Commander;

public class MoveToDriveAction implements DriveAction {

    @Override
    public String action(OkuRunBot bot) {
        final Commander commander = bot.getCommander();
        final double[] pos = commander.getTargetMovePosition(bot);
        if (pos == null) {
            // 移動先が設定されていない場合は何も行わない
            return null;
        }
        double bearingTo = bot.bearingTo(pos[0], pos[1]);
        if (commander.isZigzagAllowed(bot)) {
            bearingTo += (bearingTo > 0) ? 20 : -20;
        }
        double distance = bot.distanceTo(pos[0], pos[1]);
        bot.setTurnLeft(bearingTo);
        bot.setForward(distance);
        bot.setMaxSpeed(Constants.MAX_SPEED);

        final Color color = Color.fromRgba(Color.LIGHT_BLUE, 50);
        bot.drawCircle(pos[0], pos[1], 5, color);
        bot.drawLine(bot.getX(), bot.getY(), pos[0], pos[1], color);
        return null;
    }

}
