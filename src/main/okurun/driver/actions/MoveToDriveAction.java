package okurun.driver.actions;

import java.util.Random;

import dev.robocode.tankroyale.botapi.Constants;
import dev.robocode.tankroyale.botapi.graphics.Color;
import okurun.OkuRunBot;
import okurun.battlemanager.BattleManager;
import okurun.battlemanager.EnemyState;
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
        if (commander.getHandlePriority(bot) == HandlePriority.AVOID_BULLET) {
            if (Math.abs(bearingTo) < Constants.MAX_TURN_RATE) {
                bearingTo = (bearingTo > 0) ? Constants.MAX_TURN_RATE : -Constants.MAX_TURN_RATE;
            }
        }
        bot.setTurnLeft(bearingTo);

        double distance = bot.distanceTo(pos[0], pos[1]);
        final double speed;
        switch (commander.getAccelePriority(bot)) {
            case AccelePriority.HANDLE:
                final double diffTurnRate = Math.abs(bearingTo) - Math.abs(bot.getTurnRate());
                if (diffTurnRate > 0) {
                    speed = Math.max(commander.getMinSpeed(bot), bot.getSpeed() - 1);
                } else {
                    speed = Constants.MAX_SPEED;
                }
                break;
            case AccelePriority.AVOID_BULLET:
                speed = Constants.MAX_SPEED;
                if (Math.abs(bearingTo) <= Constants.MAX_TURN_RATE && bot.getSpeed() > commander.getMinSpeed(bot)) {
                    final BattleManager battleManager = bot.getBattleManager();
                    final EnemyState enemyState = battleManager.getLatestEnemyState(commander.getTargetEnemyId(bot));
                    int randNum = 10;
                    if (enemyState != null) {
                        final double enemyDistance = bot.distanceTo(enemyState.x, enemyState.y);
                        if (enemyDistance > 150) {
                            randNum = (int) Math.ceil(enemyDistance / bot.calcBulletSpeed(1));
                        }
                    }
                    final Random random = new Random();
                    if (random.nextInt(randNum) == 0) {
                        distance = -1;
                    }
                }
                break;
            default:
                speed = Constants.MAX_SPEED;
                break;
        }
        bot.setForward(distance);
        bot.setMaxSpeed(speed);

        final Color color = Color.fromRgba(Color.LIGHT_BLUE, 50);
        bot.drawCircle(pos[0], pos[1], 5, color);
        bot.drawLine(bot.getX(), bot.getY(), pos[0], pos[1], color);
        return null;
    }

}
