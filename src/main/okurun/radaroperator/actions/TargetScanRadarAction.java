package okurun.radaroperator.actions;

import okurun.OkuRunBot;
import okurun.battlemanager.BattleManager;
import okurun.battlemanager.EnemyProfile;
import okurun.battlemanager.EnemyState;
import okurun.commander.Commander;
import okurun.predictor.Predictor;
import okurun.radaroperator.RadarOperator;

/**
 * ターゲットを常に追うアクション
 */
public class TargetScanRadarAction implements RadarAction {

    @Override
    public RadarOperator.Action action(OkuRunBot bot) {
        final Commander commander = bot.getCommander();
        final int targetEnemyId = commander.getTargetEnemyId(bot);
        if (targetEnemyId == Commander.NO_TARGET) {
            return RadarOperator.Action.ALL_SCAN;
        }

        final BattleManager battleManager = bot.getBattleManager();
        final EnemyProfile targetEnemyProfile = battleManager.getEnemyProfile(targetEnemyId);
        if (targetEnemyProfile == null) {
            return RadarOperator.Action.ALL_SCAN;
        }

        final EnemyState latestEnemyState = targetEnemyProfile.getLatestState();
        if (latestEnemyState == null) {
            return RadarOperator.Action.ALL_SCAN;
        }

        if (bot.getTurnNumber() - latestEnemyState.scannedTurnNum > 3) {
            // 見失ったら全周スキャンに戻ります
            return RadarOperator.Action.ALL_SCAN;
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
