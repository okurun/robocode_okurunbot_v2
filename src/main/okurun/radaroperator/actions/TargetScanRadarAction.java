package okurun.radaroperator.actions;

import okurun.OkuRunBot;
import okurun.commander.Commander;
import okurun.enemymanager.EnemyManager;
import okurun.enemymanager.EnemyProfile;
import okurun.enemymanager.EnemyState;
import okurun.predictor.Predictor;
import okurun.radaroperator.RadarOperator;

/**
 * ターゲットを常に追うアクション
 */
public class TargetScanRadarAction implements RadarAction {

    @Override
    public RadarOperator.ActionId action(OkuRunBot bot) {
        final Commander commander = bot.getCommander();
        final int targetEnemyId = commander.getTargetEnemyId(bot);
        if (targetEnemyId == Commander.NO_TARGET) {
            return RadarOperator.ActionId.ALL_SCAN;
        }

        final EnemyManager enemyManager = bot.getEnemyManager();
        final EnemyProfile targetEnemyProfile = enemyManager.getEnemyProfile(targetEnemyId);
        final EnemyState latestEnemyState = targetEnemyProfile.getLatestState();
        if (latestEnemyState == null) {
            return RadarOperator.ActionId.ALL_SCAN;
        }

        if (bot.getTurnNumber() - latestEnemyState.scannedTurnNum > 3) {
            // 見失ったら全周スキャンに戻ります
            return RadarOperator.ActionId.ALL_SCAN;
        }

        final Predictor predictor = bot.getPredictor();
        EnemyState predictedState = predictor.predict(bot, targetEnemyProfile, bot.getTurnNumber());
        if (predictedState == null) {
            // 予測できなければ最新の位置をスキャンします
            predictedState = latestEnemyState;
        }

        double bearingTo = bot.radarBearingTo(predictedState.x, predictedState.y);
        // 見失いにくくするために想定位置より大きくレーダーを動かします
        if (bearingTo > 0) {
            bearingTo += 20;
        } else {
            bearingTo -= 20;
        }

        bot.setAdjustRadarForGunTurn(true);
        bot.setTurnRadarLeft(bearingTo);

        return null;
    }

}
