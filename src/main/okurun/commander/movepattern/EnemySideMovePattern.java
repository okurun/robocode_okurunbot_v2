package okurun.commander.movepattern;

import okurun.OkuRunBot;
import okurun.commander.Commander;
import okurun.commander.Commander.AccelPriority;
import okurun.commander.Commander.HandlePriority;
import okurun.commander.tactics.Tactic;
import okurun.driver.Driver;
import okurun.enemymanager.EnemyManager;
import okurun.enemymanager.EnemyProfile;
import okurun.enemymanager.EnemyState;
import okurun.predictor.Predictor;

public class EnemySideMovePattern extends AbstractMovePattern {

    @Override
    public double[] getMovePosition(OkuRunBot bot) {
        final Commander commander = bot.getCommander();
        final int targetEnemyId = commander.getTargetEnemyId(bot);
        if (targetEnemyId == Commander.NO_TARGET) {
            return null;
        }

        final EnemyManager enemyManager = bot.getEnemyManager();
        final EnemyProfile targetEnemyProfile = enemyManager.getEnemyProfile(targetEnemyId);
        final EnemyState latestEnemyState = targetEnemyProfile.getLatestState();
        if (latestEnemyState == null) {
            return null;
        }

        final Predictor predictor = bot.getPredictor();
        EnemyState predictedEnemyState = predictor.predict(bot, targetEnemyProfile, bot.getTurnNumber());
        if (predictedEnemyState == null) {
            predictedEnemyState = latestEnemyState;
        }

        // 敵位置の少し横を目指します
        // 距離は自分と敵のエネルギー差を考慮して調整します
        final double distance = Math.max(0, 200 - ((bot.getEnergy() - latestEnemyState.energy) * 5));
        final boolean clockwise = true;
        double[] targetMovePosition = Tactic.calculatePointCUsingTrig(
                bot.getPosition(), predictedEnemyState.getPosition(), distance, clockwise);
        if (!bot.getArenaMap().isInsideArena(targetMovePosition)) {
            // 目標位置がアリーナの外なら逆サイドから回り込みます
            targetMovePosition = Tactic.calculatePointCUsingTrig(
                    bot.getPosition(), predictedEnemyState.getPosition(), distance, !clockwise);
        }
        return targetMovePosition;
    }

    @Override
    public double getMinSpeed(OkuRunBot bot) {
        return 4;
    }

    @Override
    public HandlePriority getHandlePriority(OkuRunBot bot) {
        final Commander commander = bot.getCommander();
        final EnemyManager enemyManager = bot.getEnemyManager();
        final EnemyState enemyState = enemyManager.getLatestEnemyState(commander.getTargetEnemyId(bot));
        if (enemyState == null) {
            return HandlePriority.TARGET;
        }

        final double enemyLateralAngle = Math.abs(commander.getEnemyLateralAngle(bot, enemyState));
        if (enemyLateralAngle <= 30 || enemyLateralAngle >= 120) {
            // 敵が自分からみて縦方向にいる場合はジグザク走行する
            return HandlePriority.AVOID_BULLET;
        }

        return HandlePriority.TARGET;
    }

    @Override
    public AccelPriority getAccelPriority(OkuRunBot bot) {
        final Commander commander = bot.getCommander();
        final EnemyManager enemyManager = bot.getEnemyManager();
        final EnemyState enemyState = enemyManager.getLatestEnemyState(commander.getTargetEnemyId(bot));
        if (enemyState == null) {
            return AccelPriority.MAX_SPEED;
        }

        final double enemyLateralAngle = Math.abs(commander.getEnemyLateralAngle(bot, enemyState));
        if (enemyLateralAngle >= 60 && enemyLateralAngle <= 120) {
            // 敵が自分からみて横方向にいる場合はランダムにブレーキをかける
            return AccelPriority.AVOID_BULLET;
        }

        return AccelPriority.MAX_SPEED;
    }

    /**
     * このムーブパターンが依存するドライブアクションIDを取得します
     * 
     * @return このムーブパターンが依存するドライブアクションID
     */
    @Override
    public Driver.ActionId getDependentDriveActionId() {
        return Driver.ActionId.MOVE_TO_FORWARD;
    }

}