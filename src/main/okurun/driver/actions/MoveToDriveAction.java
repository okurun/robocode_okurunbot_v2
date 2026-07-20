package okurun.driver.actions;

import dev.robocode.tankroyale.botapi.Constants;
import dev.robocode.tankroyale.botapi.graphics.Color;
import okurun.OkuRunBot;

/**
 * 移動目標へ向かうDriveアクション
 */
public class MoveToDriveAction extends AbstractDriveAction {

    @Override
    protected DriveParams createDriveParams(OkuRunBot bot, double[] pos) {
        final DriveParams driveParams = new DriveParams();
        driveParams.leftTurnAngle = bot.bearingTo(pos);
        driveParams.forwardDistance = bot.distanceTo(pos);
        driveParams.maxSpeed = Constants.MAX_SPEED;
        Color color = Color.BLUE;
        if (bot.getSpeed() >= 0) {
            // 前進している場合
            if (Math.abs(driveParams.leftTurnAngle) > 100) {
                // 目標が後ろ方向にある場合は後進する
                driveParams.leftTurnAngle = bot.normalizeRelativeAngle(driveParams.leftTurnAngle - 180);
                driveParams.forwardDistance = -driveParams.forwardDistance;
                color = Color.GREEN;
            } else {
                // 目標が前方向にある場合は前進する
            }
        } else {
            // 後退している場合
            if (Math.abs(driveParams.leftTurnAngle) < 80) {
                // 目標が前方向にある場合は前進する
            } else {
                // 目標が後ろ方向にある場合は後進する
                driveParams.leftTurnAngle = bot.normalizeRelativeAngle(driveParams.leftTurnAngle - 180);
                driveParams.forwardDistance = -driveParams.forwardDistance;
                color = Color.GREEN;
            }
        }
        bot.setTracksColor(color);
        return driveParams;
    }
        
}
