package okurun.commander.tactics;

import dev.robocode.tankroyale.botapi.graphics.Color;
import okurun.OkuRunBot;
import okurun.commander.Commander;
import okurun.enemymanager.EnemyManager;
import okurun.enemymanager.EnemyProfile;
import okurun.enemymanager.EnemyState;
import okurun.predictor.Predictor;
import okurun.predictor.Predictor.PredictModelId;
import okurun.radaroperator.RadarOperator;

/**
 * 1v1戦略の抽象クラス
 */
public abstract class AbstractOneOnOneTactic extends AbstractTactic {

    @Override
    protected void updateTargetEnemyId(OkuRunBot bot) {
        final EnemyManager enemyManager = bot.getEnemyManager();
        final EnemyProfile alivalEnemy = enemyManager.getAliveEnemy(bot);
        if (alivalEnemy != null && alivalEnemy.getLatestState() != null) {
            // 敵の位置を把握している
            targetEnemyId.set(alivalEnemy.getId());
            return;
        }
        targetEnemyId.set(Commander.NO_TARGET);
    }

    @Override
    protected void updateRadarActionId(OkuRunBot bot) {
        if (targetEnemyId.get() == Commander.NO_TARGET) {
            radarActionId = RadarOperator.ActionId.ALL_SCAN;
            return;
        }

        final EnemyManager enemyManager = bot.getEnemyManager();
        final EnemyState latestEnemyState = enemyManager.getLatestEnemyState(targetEnemyId.get());
        if (latestEnemyState == null || latestEnemyState.scannedTurnNum < bot.getTurnNumber() - 5) {
            radarActionId = RadarOperator.ActionId.ALL_SCAN;
            return;
        }

        radarActionId = RadarOperator.ActionId.TARGET_SCAN;
    }

    @Override
    protected void updatePredictModelId(OkuRunBot bot) {
        if (targetEnemyId.get() != Commander.NO_TARGET) {
            final EnemyManager enemyManager = bot.getEnemyManager();
            final Predictor predictor = bot.getPredictor();
            final EnemyProfile enemyProfile = enemyManager.getEnemyProfile(targetEnemyId.get());
            for (PredictModelId model : enemyProfile.getPredictModels()) {
                if (predictor.getPredictModel(model).canPredict(bot, enemyProfile)) {
                    predictModelId = model;
                    return;
                }
            }
        }

        predictModelId = PredictModelId.NONE;
    }

    /**
     * ターン毎のアクションイベント
     * このイベントはメインスレッドからコールされます
     * 
     * @param bot Bot
     */
    @Override
    public void onAction(OkuRunBot bot) {
        super.onAction(bot);

        // 砲台の色を戦況によって変える
        final Commander commander = bot.getCommander();
        final int targetEnemyId = commander.getTargetEnemyId(bot);
        if (targetEnemyId == Commander.NO_TARGET) {
            return;
        }
        final EnemyManager enemyManager = bot.getEnemyManager();
        final EnemyState enemyState = enemyManager.getLatestEnemyState(targetEnemyId);
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

}
