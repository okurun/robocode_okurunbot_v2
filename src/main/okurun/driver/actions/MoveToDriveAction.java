package okurun.driver.actions;

import dev.robocode.tankroyale.botapi.Constants;
import dev.robocode.tankroyale.botapi.graphics.Color;
import okurun.OkuRunBot;
import okurun.commander.Commander;
import okurun.commander.Commander.AccelePriority;
import okurun.commander.Commander.HandlePriority;

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
        if (commander.getHandlePriority(bot) == HandlePriority.AVOID) {
            final double turnRate = Math.abs(bot.getTurnRate()) * 0.5;
            bearingTo += (bearingTo > 0) ? turnRate : -turnRate;
        }
        bot.setTurnLeft(bearingTo);

        double distance = bot.distanceTo(pos[0], pos[1]);
        double speed = Constants.MAX_SPEED;
        if (commander.getAccelePriority(bot) == AccelePriority.HANDLE) {
            final double diffTurnRate = Math.abs(bearingTo) - Math.abs(bot.getTurnRate());
            if (diffTurnRate > 0) {
                speed = Math.max(commander.getMinSpeed(bot), bot.getSpeed() - 1);
            }
        }
        bot.setForward(distance);
        bot.setMaxSpeed(speed);

        final Color color = Color.fromRgba(Color.LIGHT_BLUE, 50);
        bot.drawCircle(pos[0], pos[1], 5, color);
        bot.drawLine(bot.getX(), bot.getY(), pos[0], pos[1], color);
        return null;
    }

}
