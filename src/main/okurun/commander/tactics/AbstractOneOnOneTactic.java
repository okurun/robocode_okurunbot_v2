package okurun.commander.tactics;

import dev.robocode.tankroyale.botapi.graphics.Color;
import okurun.OkuRunBot;
import okurun.battlemanager.BattleManager;
import okurun.battlemanager.EnemyProfile;
import okurun.battlemanager.EnemyState;
import okurun.commander.Commander;
import okurun.predictor.Predictor;
import okurun.predictor.Predictor.PredictModelId;
import okurun.radaroperator.RadarOperator;

/**
 * 1v1戦略の抽象クラス
 */
public abstract class AbstractOneOnOneTactic extends AbstractTactic {
    @Override
    public void action(OkuRunBot bot) {
        super.action(bot);

        // 砲台の色を戦況によって変える
        final Commander commander = bot.getCommander();
        final int targetEnemyId = commander.getTargetEnemyId(bot);
        if (targetEnemyId == Commander.NO_TARGET) {
            return;
        }
        final BattleManager battleManager = bot.getBattleManager();
        final EnemyState enemyState = battleManager.getLatestEnemyState(targetEnemyId);
        if (enemyState == null) {
            return;
        }

        final double diffEnergy = bot.getEnergy() - enemyState.energy;
        if (diffEnergy > 0) {
            // 勝っている
            bot.setTurretColor(Color.RED);
        } else if (diffEnergy < 0) {
            // 負けている
            bot.setTurretColor(Color.BLUE);
        }
    }

    @Override
    protected void setTargetEnemyId(OkuRunBot bot) {
        final BattleManager battleManager = bot.getBattleManager();
        final EnemyProfile alivalEnemy = battleManager.getAliveEnemy(bot);
        if (alivalEnemy != null && alivalEnemy.getLatestState() != null) {
            // 敵の位置を把握している
            targetEnemyId.set(alivalEnemy.getId());
            return;
        }
        targetEnemyId.set(Commander.NO_TARGET);
    }

    @Override
    protected void setRadarActionId(OkuRunBot bot) {
        if (targetEnemyId.get() == Commander.NO_TARGET) {
            radarActionId = RadarOperator.ActionId.ALL_SCAN;
            return;
        }

        final BattleManager battleManager = bot.getBattleManager();
        final EnemyState latestEnemyState = battleManager.getLatestEnemyState(targetEnemyId.get());
        if (latestEnemyState == null || latestEnemyState.scannedTurnNum < bot.getTurnNumber() - 5) {
            radarActionId = RadarOperator.ActionId.ALL_SCAN;
            return;
        }

        radarActionId = RadarOperator.ActionId.TARGET_SCAN;
    }

    @Override
    protected void setPredictModelId(OkuRunBot bot) {
        if (targetEnemyId.get() != Commander.NO_TARGET) {
            final BattleManager battleManager = bot.getBattleManager();
            final Predictor predictor = bot.getPredictor();
            final EnemyProfile enemyProfile = battleManager.getEnemyProfile(targetEnemyId.get());
            for (PredictModelId model : enemyProfile.getPredictModels()) {
                if (predictor.getPredictModel(model).canPredict(bot, enemyProfile)) {
                    predictModelId = model;
                    return;
                }
            }
        }

        predictModelId = PredictModelId.NONE;
    }
}
