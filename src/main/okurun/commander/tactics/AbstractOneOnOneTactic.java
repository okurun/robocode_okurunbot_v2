package okurun.commander.tactics;

import okurun.OkuRunBot;
import okurun.battlemanager.BattleManager;
import okurun.battlemanager.EnemyProfile;
import okurun.battlemanager.EnemyState;
import okurun.commander.Commander;
import okurun.commander.Commander.AccelePriority;
import okurun.commander.Commander.HandlePriority;
import okurun.predictor.models.*;

public abstract class AbstractOneOnOneTactic extends AbstractTactic {
    @Override
    protected void setPredictorModelName(OkuRunBot bot) {
        if (targetEnemyId != Commander.NO_TARGET) {
            final BattleManager battleManager = bot.getBattleManager();
            final EnemyProfile targetEnemyProfile = battleManager.getEnemyProfile(targetEnemyId);
            if (targetEnemyProfile != null) {
                if (HistoryPredictModel.canUse(bot, targetEnemyProfile.getStateHistory())) {
                    predictorModelName = HistoryPredictModel.class.getName();
                    return;
                }
            }
        }
        predictorModelName = SimplePredictModel.class.getName();
    }

    @Override
    public HandlePriority getHandlePriority(OkuRunBot bot) {
        final BattleManager battleManager = bot.getBattleManager();
        final EnemyProfile targetEnemyProfile = battleManager.getEnemyProfile(targetEnemyId);
        if (targetEnemyProfile == null) {
            return HandlePriority.TARGET;
        }
        final EnemyState enemyState = targetEnemyProfile.getLatestState();
        if (enemyState == null) {
            return HandlePriority.TARGET;
        }

        final Commander commander = bot.getCommander();
        final double enemyLateralAngle = Math.abs(commander.getEnemyLateralAngle(bot, enemyState));
        if (enemyLateralAngle <= 30 || enemyLateralAngle >= 120) {
            // 敵が自分からみて縦方向にいる場合はジグザク走行する
            return HandlePriority.AVOID_BULLET;
        }

        return HandlePriority.TARGET;
    }

    @Override
    public AccelePriority getAccelePriority(OkuRunBot bot) {
        final BattleManager battleManager = bot.getBattleManager();
        final EnemyProfile targetEnemyProfile = battleManager.getEnemyProfile(targetEnemyId);
        if (targetEnemyProfile == null) {
            return AccelePriority.MAX_SPEED;
        }
        final EnemyState enemyState = targetEnemyProfile.getLatestState();
        if (enemyState == null) {
            return AccelePriority.MAX_SPEED;
        }

        final Commander commander = bot.getCommander();
        final double enemyLateralAngle = Math.abs(commander.getEnemyLateralAngle(bot, enemyState));
        if (enemyLateralAngle >= 60 && enemyLateralAngle <= 120) {
            // 敵が自分からみて横方向にいる場合はランダムにブレーキをかける
            return AccelePriority.AVOID_BULLET;
        }

        return AccelePriority.MAX_SPEED;
    }
}
