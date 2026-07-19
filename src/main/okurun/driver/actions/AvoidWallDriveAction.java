package okurun.driver.actions;

import java.util.List;

import dev.robocode.tankroyale.botapi.Constants;
import dev.robocode.tankroyale.botapi.graphics.Color;
import okurun.OkuRunBot;
import okurun.arenamap.ArenaMap;
import okurun.driver.Driver;

/**
 * 壁を回避するDriveAction
 */
public class AvoidWallDriveAction implements DriveAction {
    private static class DriveActionParam {
        public double leftTurnAngle = 0;
        public double maxSpeed = 0;
        public double distance = 0;
    }

    @Override
    public Driver.ActionId action(OkuRunBot bot) {
        final ArenaMap arenaMap = bot.getArenaMap();
        final List<ArenaMap.PotentialCollisionWall> pcWalls = arenaMap.getPotentialCollisionWalls(bot);
        if (pcWalls.isEmpty()) {
            return null;
        }

        // 一番衝突までのターンが少ない壁を取得
        final ArenaMap.PotentialCollisionWall pcWall = pcWalls.get(0);
        final DriveActionParam driveActionParam = (bot.getSpeed() < 0)
                ? backwardAction(bot, pcWall)
                : forwardAction(bot, pcWall);

        bot.setTurnLeft(driveActionParam.leftTurnAngle);
        bot.setMaxSpeed(driveActionParam.maxSpeed);
        bot.setForward(driveActionParam.distance);
        bot.setTracksColor(Color.RED);
        return null;
    }

    /**
     * 前進時の壁回避アクション
     * 
     * @param bot    ボット
     * @param pcWall 衝突壁
     * @return DriveActionParam
     */
    private DriveActionParam forwardAction(OkuRunBot bot, ArenaMap.PotentialCollisionWall pcWall) {
        final DriveActionParam driveActionParam = new DriveActionParam();

        // 壁に対して平行より10度だけ離れる方向に旋回
        driveActionParam.leftTurnAngle = pcWall.wall.getLeftTurnAngleToParallel(bot);
        driveActionParam.leftTurnAngle = (driveActionParam.leftTurnAngle > 0)
                ? driveActionParam.leftTurnAngle + 10
                : driveActionParam.leftTurnAngle - 10;

        // 減速しないと衝突する場合は減速する
        final double turnsToCollision = pcWall.turnsToCollision;
        driveActionParam.maxSpeed = Math.min(Constants.MAX_SPEED,
                turnsToCollision * Math.abs(Constants.DECELERATION));
        driveActionParam.distance = 100;
        if (driveActionParam.maxSpeed < Constants.MAX_SPEED && bot.getSpeed() > driveActionParam.maxSpeed) {
            driveActionParam.distance = -driveActionParam.distance;
        }

        return driveActionParam;
    }

    /**
     * 後退時の壁回避アクション
     * 
     * @param bot    ボット
     * @param pcWall 衝突壁
     * @return DriveActionParam
     */
    private DriveActionParam backwardAction(OkuRunBot bot, ArenaMap.PotentialCollisionWall pcWall) {
        final DriveActionParam driveActionParam = new DriveActionParam();

        // 壁に対して平行より10度だけ離れる方向に旋回
        driveActionParam.leftTurnAngle = pcWall.wall.getLeftTurnAngleToParallel(bot);
        driveActionParam.leftTurnAngle = (driveActionParam.leftTurnAngle > 0)
                ? driveActionParam.leftTurnAngle + 10
                : driveActionParam.leftTurnAngle - 10;

        // 減速しないと衝突する場合は減速する
        final double turnsToCollision = pcWall.turnsToCollision;
        driveActionParam.maxSpeed = Math.min(Constants.MAX_SPEED,
                turnsToCollision * Math.abs(Constants.DECELERATION));
        driveActionParam.distance = -100;
        if (driveActionParam.maxSpeed < Constants.MAX_SPEED && bot.getSpeed() < -driveActionParam.maxSpeed) {
            driveActionParam.distance = -driveActionParam.distance;
        }

        return driveActionParam;
    }
}
