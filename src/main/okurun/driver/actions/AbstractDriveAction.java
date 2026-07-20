package okurun.driver.actions;

import java.util.Random;

import dev.robocode.tankroyale.botapi.Constants;
import okurun.OkuRunBot;
import okurun.commander.Commander;
import okurun.commander.Commander.AccelPriority;
import okurun.commander.Commander.HandlePriority;
import okurun.driver.Driver;

abstract class AbstractDriveAction implements DriveAction {
    class AvoidParams {
        public Turn turn = Turn.LEFT;
        public int randNum = 1;
    }

    enum Turn {
        LEFT,
        RIGHT;

        public Turn opposite() {
            return this == LEFT ? RIGHT : LEFT;
        }
    }

    protected Random random = new Random();
    protected AvoidParams avoidParams = new AvoidParams();

    @Override
    public Driver.ActionId action(OkuRunBot bot) {
        final Commander commander = bot.getCommander();
        final double[] pos = commander.getTargetMovePosition(bot);
        if (pos == null) {
            // 移動先が設定されていない場合は何も行わない
            return null;
        }

        DriveParams driveParams = createDriveParams(bot, pos);

        // 予測を外すため移動情報を更新する
        avoidParams.randNum = random.nextInt(10);
        driveParams = transTurnAngle(bot, driveParams);
        driveParams = transAccelInfo(bot, driveParams);

        bot.setTurnLeft(driveParams.leftTurnAngle);
        bot.setForward(driveParams.forwardDistance);
        bot.setMaxSpeed(driveParams.maxSpeed);

        return null;
    }

    protected abstract DriveParams createDriveParams(OkuRunBot bot, double[] pos);

    private DriveParams transTurnAngle(OkuRunBot bot, DriveParams driveParams) {
        final DriveParams newDriveParams = driveParams.clone();
        if (Math.abs(newDriveParams.leftTurnAngle) < bot.getMaxTurnRate()) {
            // 旋回に余裕がある（直進に近い状態）
            if (bot.getCommander().getHandlePriority(bot) == HandlePriority.AVOID_BULLET) {
                // 回避行動を行う
                if (avoidParams.randNum == 0) {
                    // 左右を入れ替える
                    avoidParams.turn = avoidParams.turn.opposite();
                }
                switch (avoidParams.turn) {
                    case Turn.LEFT:
                        newDriveParams.leftTurnAngle += bot.getMaxTurnRate() * 0.5;
                        break;
                    case Turn.RIGHT:
                        newDriveParams.leftTurnAngle -= bot.getMaxTurnRate() * 0.5;
                        break;
                }
            }
        }
        return newDriveParams;
    }

    /**
     * AccelPriorityに応じた加速情報を更新する
     * 
     * @param bot       ロボット
     * @param driveParams DriveParams
     * @return DriveParams
     */
    private DriveParams transAccelInfo(OkuRunBot bot, DriveParams driveParams) {
        final DriveParams newDriveParams = driveParams.clone();
        final Commander commander = bot.getCommander();
        switch (commander.getAccelPriority(bot)) {
            case AccelPriority.HANDLE:
                // 旋回を優先するため、旋回角度がMAX_TURN_RATEより大きい場合は減速する
                final double diffTurnRate = Math.abs(newDriveParams.leftTurnAngle) - Math.abs(bot.getMaxTurnRate());
                if (diffTurnRate > 90) {
                    newDriveParams.maxSpeed = Math.max(commander.getMinSpeed(bot), bot.getSpeed() - 2);
                    newDriveParams.forwardDistance = -1;
                } else if (diffTurnRate > 0) {
                    newDriveParams.maxSpeed = Math.max(commander.getMinSpeed(bot), bot.getSpeed() - 1);
                } else {
                    newDriveParams.maxSpeed = Constants.MAX_SPEED;
                }
                break;
            case AccelPriority.AVOID_BULLET:
                // 予測を外すためにランダムでブレーキをかける
                newDriveParams.maxSpeed = Constants.MAX_SPEED;
                if (bot.getSpeed() > commander.getMinSpeed(bot)) {
                    if (avoidParams.randNum == 0) {
                        // ブレーキ
                        newDriveParams.forwardDistance = -1;
                    }
                }
                break;
            default:
                newDriveParams.maxSpeed = Constants.MAX_SPEED;
                break;
        }
        return newDriveParams;
    }

}
