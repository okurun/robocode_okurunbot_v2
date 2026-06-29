package okurun.radaroperator.actions;

import okurun.OkuRunBot;
import okurun.battlemanager.BattleManager;
import okurun.battlemanager.EnemyProfile;
import okurun.battlemanager.EnemyState;
import okurun.commander.Commander;
import okurun.predictor.Predictor;

/**
 * ターゲットを常に追うアクション
 */
public class TargetScanRadarAction implements RadarAction {

    @Override
    public String action(OkuRunBot bot) {
        final Commander commander = bot.getCommander();
        final int targetEnemyId = commander.getTargetEnemyId(bot);
        if (targetEnemyId == Commander.NO_TARGET) {
            return AllScanRadarAction.class.getName();
        }

        final BattleManager battleManager = bot.getBattleManager();
        final EnemyProfile targetEnemyProfile = battleManager.getEnemyProfile(targetEnemyId);
        if (targetEnemyProfile == null) {
            return AllScanRadarAction.class.getName();
        }

        final EnemyState latestEnemyState = targetEnemyProfile.getLatestState();
        if (latestEnemyState == null) {
            return AllScanRadarAction.class.getName();
        }

        if (bot.getTurnNumber() - latestEnemyState.scandTurnNum > 3) {
            // 見失ったら全周スキャンに戻ります
            return AllScanRadarAction.class.getName();
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
