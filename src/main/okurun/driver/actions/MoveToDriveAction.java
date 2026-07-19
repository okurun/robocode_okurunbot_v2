package okurun.driver.actions;

import dev.robocode.tankroyale.botapi.Constants;
import dev.robocode.tankroyale.botapi.graphics.Color;
import okurun.Debugger;
import okurun.OkuRunBot;
import okurun.commander.Commander;
import okurun.driver.Driver;

/**
 * 移動目標へ向かうDriveアクション
 */
public class MoveToDriveAction implements DriveAction {

    @Override
    public Driver.ActionId action(OkuRunBot bot) {
        final Commander commander = bot.getCommander();
        final double[] pos = commander.getTargetMovePosition(bot);
        if (pos == null) {
            // 移動先が設定されていない場合は何も行わない
            return null;
        }

        final double bearingTo = bot.bearingTo(pos);
        final double distance = bot.distanceTo(pos);
        bot.setMaxSpeed(Constants.MAX_SPEED);

        if (bot.getSpeed() >= 0) {
            // 前進している場合
            if (Math.abs(bearingTo) > 100) {
                // 目標が後ろ方向にある場合は後進する
                bot.setTurnLeft(bot.normalizeRelativeAngle(bearingTo - 180));
                bot.setForward(-distance);
                bot.setTracksColor(Color.GREEN);
                // System.out.println(String.format("FB bearingTo: %.1f, distance: %.1f", bot.normalizeRelativeAngle(bearingTo - 180), -distance));
            } else {
                // 目標が前方向にある場合は前進する
                bot.setTurnLeft(bearingTo);
                bot.setForward(distance);
                bot.setTracksColor(Color.BLUE);
                // System.out.println(String.format("FF bearingTo: %.1f, distance: %.1f", bearingTo, distance));
            }
        } else {
            // 後退している場合
            if (Math.abs(bearingTo) < 80) {
                // 目標が前方向にある場合は前進する
                bot.setTurnLeft(bearingTo);
                bot.setForward(distance);
                bot.setTracksColor(Color.BLUE);
                // System.out.println(String.format("BF bearingTo: %.1f, distance: %.1f", bearingTo, distance));
            } else {
                // 目標が後ろ方向にある場合は後進する
                bot.setTurnLeft(bot.normalizeRelativeAngle(bearingTo - 180));
                bot.setForward(-distance);
                bot.setTracksColor(Color.GREEN);
                // System.out.println(String.format("BB bearingTo: %.1f, distance: %.1f", bot.normalizeRelativeAngle(bearingTo - 180), -distance));
            }
        }

        final Color color = Color.LIGHT_BLUE;
        final Debugger debugger = bot.getDebugger();
        debugger.drawFillCircle(bot, pos, 5, Color.fromRgba(color, 50));
        debugger.drawLine(bot, bot.getPosition(), pos, Color.fromRgba(color, 50));

        return null;
    }

}
