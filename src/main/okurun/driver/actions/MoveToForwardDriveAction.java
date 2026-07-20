package okurun.driver.actions;

import dev.robocode.tankroyale.botapi.Constants;
import okurun.OkuRunBot;

/**
 * 前進して移動目標へ向かうDriveアクション
 */
public class MoveToForwardDriveAction extends AbstractDriveAction {

    @Override
    protected DriveParams createDriveParams(OkuRunBot bot, double[] pos) {
        final DriveParams driveParams = new DriveParams();
        driveParams.leftTurnAngle = bot.bearingTo(pos);
        driveParams.forwardDistance = bot.distanceTo(pos);
        driveParams.maxSpeed = Constants.MAX_SPEED;
        return driveParams;
    }

}
