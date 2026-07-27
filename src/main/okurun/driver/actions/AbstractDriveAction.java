package okurun.driver.actions;

import java.util.Random;

import dev.robocode.tankroyale.botapi.Constants;
import okurun.OkuRunBot;
import okurun.commander.Commander;
import okurun.commander.Commander.AccelPriority;
import okurun.commander.Commander.HandlePriority;
import okurun.driver.Driver;

abstract class AbstractDriveAction implements DriveAction {
    /** ランダムクラス */
    protected Random random = new Random();
    /** 回避時のパラメータ */
    protected AvoidParams avoidParams = new AvoidParams();
    /** 移動時のパラメータ */
    protected DriveParams driveParams = null;

    /**
     * DriveParamsを作成する
     * 
     * @param bot ボット
     * @param pos 移動先
     * @return DriveParams
     */
    protected abstract DriveParams createDriveParams(OkuRunBot bot, double[] pos);

    /**
     * {@inheritDoc}
     */
    @Override
    public Driver.ActionId onAction(OkuRunBot bot) {
        final Commander commander = bot.getCommander();
        final double[] pos = commander.getTargetMovePosition(bot);
        if (pos == null) {
            // 移動先が設定されていない場合は何も行わない
            return null;
        }

        driveParams = createDriveParams(bot, pos);

        // 予測を外すため移動情報を更新する
        avoidParams.randNum = random.nextInt(20);
        driveParams = transTurnAngle(bot, driveParams);
        driveParams = transAccelInfo(bot, driveParams);

        bot.setTurnLeft(driveParams.leftTurnAngle);
        bot.setForward(driveParams.forwardDistance);
        bot.setMaxSpeed(driveParams.maxSpeed);

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DriveParams getDriveParams() {
        return driveParams;
    }

    /** 回避時のパラメータ */
    private static class AvoidParams {
        /** 回避方向 */
        public Turn turn = Turn.LEFT;
        /** ランダム値 */
        public int randNum = 1;
    }

    /** 回避方向 */
    private static enum Turn {
        LEFT,
        RIGHT;

        /**
         * 逆方向を取得
         * 
         * @return 逆方向
         */
        public Turn opposite() {
            return this == LEFT ? RIGHT : LEFT;
        }
    }

    /**
     * HandlePriorityに応じて旋回角度を更新する
     * 
     * @param bot         ボット
     * @param driveParams DriveParams
     * @return DriveParams
     */
    private DriveParams transTurnAngle(OkuRunBot bot, DriveParams driveParams) {
        final DriveParams newDriveParams = driveParams.clone();
        final double maxTurnRate = bot.getMaxTurnRate();
        if (Math.abs(newDriveParams.leftTurnAngle) < maxTurnRate) {
            // 旋回に余裕がある（直進に近い状態）
            if (bot.getCommander().getHandlePriority(bot) == HandlePriority.AVOID_BULLET) {
                // 回避行動を行う
                if (avoidParams.randNum == 0) {
                    // 左右を入れ替える
                    avoidParams.turn = avoidParams.turn.opposite();
                }
                switch (avoidParams.turn) {
                    case Turn.LEFT:
                        newDriveParams.leftTurnAngle += maxTurnRate;
                        break;
                    case Turn.RIGHT:
                        newDriveParams.leftTurnAngle -= maxTurnRate;
                        break;
                }
                newDriveParams.leftTurnAngle = Math.min(Math.max(newDriveParams.leftTurnAngle, -maxTurnRate),
                        maxTurnRate);
            }
        }
        return newDriveParams;
    }

    /**
     * AccelPriorityに応じた加速情報を更新する
     * 
     * @param bot         ロボット
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
