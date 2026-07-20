package okurun.driver.actions;

import dev.robocode.tankroyale.botapi.Constants;
import dev.robocode.tankroyale.botapi.graphics.Color;
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

        final DriveParams driveParams = createDriveParams(bot, pos);

        // System.out.println(String.format(
        //     " TurnLefe: %.1f, Forward: %.1f",
        //     driveParams.leftTurnAngle,
        //     driveParams.forwardDistance));

        bot.setTurnLeft(driveParams.leftTurnAngle);
        bot.setForward(driveParams.forwardDistance);
        bot.setMaxSpeed(driveParams.maxSpeed);

        return null;
    }

    private DriveParams createDriveParams(OkuRunBot bot, double[] pos) {
        final DriveParams driveParams = new DriveParams();
        driveParams.leftTurnAngle = bot.bearingTo(pos);
        driveParams.forwardDistance = bot.distanceTo(pos);
        driveParams.maxSpeed = Constants.MAX_SPEED;
        Color color = Color.BLUE;
        // System.out.print(String.format(
        //         "(%d) speed: %.1f, bearing: %.1f, distance: %.1f => ",
        //         bot.getTurnNumber(),
        //         bot.getSpeed(),
        //         driveParams.leftTurnAngle,
        //         driveParams.forwardDistance));
        if (bot.getSpeed() >= 0) {
            // 前進している場合
            if (Math.abs(driveParams.leftTurnAngle) > 100) {
                // 目標が後ろ方向にある場合は後進する
                driveParams.leftTurnAngle = bot.normalizeRelativeAngle(driveParams.leftTurnAngle - 180);
                driveParams.forwardDistance = -driveParams.forwardDistance;
                color = Color.GREEN;
                // System.out.print("FB");
            } else {
                // 目標が前方向にある場合は前進する
                // System.out.print("FF");
            }
        } else {
            // 後退している場合
            if (Math.abs(driveParams.leftTurnAngle) < 80) {
                // 目標が前方向にある場合は前進する
                // System.out.print("BF");
            } else {
                // 目標が後ろ方向にある場合は後進する
                driveParams.leftTurnAngle = bot.normalizeRelativeAngle(driveParams.leftTurnAngle - 180);
                driveParams.forwardDistance = -driveParams.forwardDistance;
                color = Color.GREEN;
                // System.out.print("BB");
            }
        }
        bot.setTracksColor(color);
        return driveParams;
    }
        
}
