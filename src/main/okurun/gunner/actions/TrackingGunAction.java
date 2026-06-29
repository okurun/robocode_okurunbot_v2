package okurun.gunner.actions;

import okurun.OkuRunBot;
import okurun.battlemanager.BattleManager;
import okurun.battlemanager.EnemyProfile;
import okurun.battlemanager.EnemyState;
import okurun.commander.Commander;
import okurun.predictor.Predictor;

/**
 * 敵に砲を向けるだけで射撃はしないアクション
 */
public class TrackingGunAction implements GunAction {

    @Override
    public String action(OkuRunBot bot) {
        final Commander commander = bot.getCommander();
        final int targetEnemyId = commander.getTargetEnemyId(bot);
        if (targetEnemyId == Commander.NO_TARGET) {
            return ScanGunAction.class.getName();
        }

        final BattleManager battleManager = bot.getBattleManager();
        final EnemyProfile targetEnemyProfile = battleManager.getEnemyProfile(targetEnemyId);
        if (targetEnemyProfile == null) {
            return ScanGunAction.class.getName();
        }

        // 敵の次の位置を予測します
        final Predictor predictor = bot.getPredictor();
        EnemyState nextEnemyState = predictor.predict(bot, targetEnemyProfile, bot.getTurnNumber() + 1);
        if (nextEnemyState == null) {
            // 予測できない場合は最新のステータスを使用します
            nextEnemyState = targetEnemyProfile.getLatestState();
            if (nextEnemyState == null) {
                return ScanGunAction.class.getName();
            }
        }

        // デバッグ用に射撃目標位置に円を描きます
        // ※ 描画にはUI画面でDebug Graphicsを有効にする必要があります
        GunAction.drawTargetPoint(bot, nextEnemyState, 0);

        bot.setAdjustGunForBodyTurn(true);
        final double bearingTo = bot.gunBearingTo(nextEnemyState.x, nextEnemyState.y);
        bot.setTurnGunLeft(bearingTo);

        return null;
    }

}
