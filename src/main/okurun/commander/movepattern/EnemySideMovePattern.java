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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public double getMinSpeed(OkuRunBot bot) {
        return 4;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HandlePriority getHandlePriority(OkuRunBot bot) {
        return HandlePriority.AVOID_BULLET;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccelPriority getAccelPriority(OkuRunBot bot) {
        return AccelPriority.AVOID_BULLET;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Driver.ActionId getDependentDriveActionId() {
        return Driver.ActionId.MOVE_TO;
    }

}