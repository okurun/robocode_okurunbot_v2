package okurun.gunner.actions;

import okurun.OkuRunBot;
import okurun.battlemanager.BattleManager;
import okurun.battlemanager.EnemyProfile;
import okurun.battlemanager.EnemyState;
import okurun.commander.Commander;
import okurun.gunner.Gunner;
import okurun.predictor.Predictor;

/**
 * 敵に砲を向けるだけで射撃はしないアクション
 */
public class TrackingGunAction implements GunAction {

    @Override
    public Gunner.Action action(OkuRunBot bot) {
        final Commander commander = bot.getCommander();
        final int targetEnemyId = commander.getTargetEnemyId(bot);
        if (targetEnemyId == Commander.NO_TARGET) {
            return Gunner.Action.SCAN;
        }

        final BattleManager battleManager = bot.getBattleManager();
        final EnemyProfile targetEnemyProfile = battleManager.getEnemyProfile(targetEnemyId);

        // 敵の次の位置を予測します
        final Predictor predictor = bot.getPredictor();
        EnemyState nextEnemyState = predictor.predict(bot, targetEnemyProfile, bot.getTurnNumber() + 1);
        if (nextEnemyState == null) {
            // 予測できない場合は最新のステータスを使用します
            nextEnemyState = targetEnemyProfile.getLatestState();
            if (nextEnemyState == null) {
                return Gunner.Action.SCAN;
            }
        }

        // デバッグ用に射撃目標位置に円を描きます
        // ※ 描画にはUI画面でDebug Graphicsを有効にする必要があります
        GunAction.drawCircle(bot, nextEnemyState);

        bot.setAdjustGunForBodyTurn(true);
        final double bearingTo = bot.gunBearingTo(nextEnemyState.getPosition());
        bot.setTurnGunLeft(bearingTo);

        return null;
    }

}
