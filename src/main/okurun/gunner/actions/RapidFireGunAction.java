package okurun.gunner.actions;

import dev.robocode.tankroyale.botapi.Constants;
import okurun.OkuRunBot;
import okurun.battlemanager.BattleManager;
import okurun.battlemanager.BulletHistory;
import okurun.battlemanager.EnemyProfile;
import okurun.battlemanager.EnemyState;
import okurun.commander.Commander;
import okurun.predictor.Predictor;

/**
 * 連射します
 */
public class RapidFireGunAction implements GunAction {

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
        final Predictor predictor = bot.getPredictor();
        EnemyState currentEnemyState = predictor.predict(bot, targetEnemyProfile, bot.getTurnNumber());
        if (currentEnemyState == null) {
            // 予測できない場合は最新のステータスを使用します
            currentEnemyState = targetEnemyProfile.getLatestState();
            if (currentEnemyState == null) {
                return ScanGunAction.class.getName();
            }
        }

        double bulletPower = Constants.MIN_FIREPOWER;

        // 射撃目標位置を計算します
        EnemyState fireTarget = GunAction.getFireTarget(bot, targetEnemyProfile, bulletPower);
        if (fireTarget == null) {
            // 予測できない場合は最新のステータスを使用します
            fireTarget = targetEnemyProfile.getLatestState();
            if (fireTarget == null) {
                return ScanGunAction.class.getName();
            }
        }

        // デバッグ用に射撃目標位置に円を描きます
        // ※ 描画にはUI画面でDebug Graphicsを有効にする必要があります
        GunAction.drawTargetPoint(bot, fireTarget, bulletPower);

        // 射撃目標位置に砲頭を向けます
        final double bearingTo = bot.gunBearingTo(fireTarget.x, fireTarget.y);
        bot.setAdjustGunForBodyTurn(true);
        bot.setTurnGunLeft(bearingTo);

        if (bot.getGunHeat() > bot.getGunCoolingRate()) {
            // 砲がクールダウン中の場合は発射しません
            return null;
        }

        bot.setFire(bulletPower);

        // デバッグ用に弾丸の情報をスタックに保存します
        battleManager.bulletStack.addLast(
                new BulletHistory(commander.getPredictorModelName(bot), fireTarget.x, fireTarget.y, targetEnemyId,
                        fireTarget.scannedTurnNum));
        return null;
    }
}
