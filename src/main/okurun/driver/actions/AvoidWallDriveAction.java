package okurun.driver.actions;

import java.util.List;

import dev.robocode.tankroyale.botapi.Constants;
import okurun.OkuRunBot;
import okurun.arenamap.ArenaMap;

public class AvoidWallDriveAction implements DriveAction {

    @Override
    public String action(OkuRunBot bot) {
        final ArenaMap arenaMap = bot.getArenaMap();
        final List<ArenaMap.PotentialCollisionWall> pcWalls = arenaMap.getPotentialCollisionWalls(bot);
        if (pcWalls.isEmpty()) {
            return null;
        }

        final ArenaMap.PotentialCollisionWall pcWall = pcWalls.get(0);

        // 壁に対して平行より10度だけ離れる方向に旋回
        final double leftTurnAngle = pcWall.wall.getLeftTurnAngleToParallel(bot);
        bot.setTurnLeft((leftTurnAngle > 0) ? leftTurnAngle + 10 : leftTurnAngle - 10);

        // 減速しないと衝突する場合は減速する
        final double turnsToCollision = pcWall.turnsToCollision;
        double maxSpeed = Math.min(Constants.MAX_SPEED, turnsToCollision * Math.abs(Constants.DECELERATION));
        bot.setMaxSpeed(maxSpeed);
        double distance = 100;
        if (maxSpeed < Constants.MAX_SPEED && bot.getSpeed() > maxSpeed) {
            distance = -10;
        }
        bot.setForward(distance);
        return null;
    }
}
