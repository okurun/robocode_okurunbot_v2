package okurun.driver.actions;

import java.util.Random;

import dev.robocode.tankroyale.botapi.Constants;
import okurun.OkuRunBot;
import okurun.bulletmanager.BulletManager;
import okurun.bulletmanager.EnemyBulletWave;
import okurun.commander.Commander;
import okurun.commander.Commander.AccelPriority;
import okurun.commander.Commander.HandlePriority;
import okurun.driver.Driver;

abstract class AbstractDriveAction implements DriveAction {
    private static final int AVOID_BULLET_TURNS = 8;

    /** ランダムクラス */
    protected Random random = new Random();
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

    /**
     * HandlePriorityに応じて旋回角度を更新する
     * 
     * @param bot         ボット
     * @param driveParams DriveParams
     * @return DriveParams
     */
    private DriveParams transTurnAngle(OkuRunBot bot, DriveParams driveParams) {
        final DriveParams newDriveParams = driveParams.clone();
        final Commander commander = bot.getCommander();
        if (commander.getHandlePriority(bot) == HandlePriority.AVOID_BULLET) {
            final BulletManager bulletManager = bot.getBulletManager();
            final EnemyBulletWave wave = bulletManager.getNearestEnemyBulletWave(bot);
            if (wave != null && wave.getCountDown() <= AVOID_BULLET_TURNS) {
                double bearingTo = bot.bearingTo(wave.getPosition());
                if ((bearingTo > -45 && bearingTo < 45) || (bearingTo > 135 && bearingTo < -135)) {
                    newDriveParams.leftTurnAngle = (bearingTo < 0) ? bearingTo + 90 : bearingTo - 90;
                }
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
                final BulletManager bulletManager = bot.getBulletManager();
                final EnemyBulletWave wave = bulletManager.getNearestEnemyBulletWave(bot);
                if (wave != null && wave.getCountDown() <= AVOID_BULLET_TURNS) {
                    double bearingTo = bot.bearingTo(wave.getPosition());
                    if ((bearingTo <= -45 && bearingTo >= -135) || (bearingTo >= 45 && bearingTo <= 135)) {
                        if (wave.moveForward) {
                            // 減速もしくは逆進する
                            newDriveParams.forwardDistance = -50;
                        } else {
                            // 減速もしくは逆進する
                            newDriveParams.forwardDistance = 50;
                        }
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
