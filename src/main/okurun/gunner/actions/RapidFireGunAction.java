package okurun.gunner.actions;

import dev.robocode.tankroyale.botapi.Constants;
import okurun.OkuRunBot;
import okurun.battlemanager.BattleManager;
import okurun.battlemanager.BulletHistory;
import okurun.battlemanager.EnemyProfile;
import okurun.battlemanager.EnemyState;
import okurun.commander.Commander;
import okurun.gunner.Gunner;

/**
 * 連射します
 */
public class RapidFireGunAction implements GunAction {

    @Override
    public Gunner.Action action(OkuRunBot bot) {
        final Commander commander = bot.getCommander();
        final int targetEnemyId = commander.getTargetEnemyId(bot);
        if (targetEnemyId == Commander.NO_TARGET) {
            return Gunner.Action.SCAN;
        }

        final double firePower = Math.min(Math.max(commander.getBaseFirePower(bot), Constants.MIN_FIREPOWER),
                Constants.MAX_FIREPOWER);

        // 射撃目標位置を計算します
        final BattleManager battleManager = bot.getBattleManager();
        final EnemyProfile targetEnemyProfile = battleManager.getEnemyProfile(targetEnemyId);
        EnemyState fireTarget = GunAction.getFireTarget(bot, targetEnemyProfile, firePower);
        if (fireTarget == null) {
            // 予測できない場合は最新のステータスを使用します
            fireTarget = targetEnemyProfile.getLatestState();
            if (fireTarget == null) {
                return Gunner.Action.SCAN;
            }
        }

        // デバッグ用に射撃目標位置に円を描きます
        // ※ 描画にはUI画面でDebug Graphicsを有効にする必要があります
        GunAction.drawTargetPoint(bot, fireTarget, firePower);

        // 射撃目標位置に砲頭を向けます
        final double bearingTo = bot.gunBearingTo(fireTarget.getPosition());
        bot.setAdjustGunForBodyTurn(true);
        bot.setTurnGunLeft(bearingTo);

        if (bot.getGunHeat() > 0) {
            // 砲がクールダウン中の場合は発射しません
            return null;
        }

        bot.setFire(firePower);

        // デバッグ用に弾丸の情報をスタックに保存します
        GunAction.stackBulletHistory(bot,
                new BulletHistory(commander.getPredictModel(bot), fireTarget.x, fireTarget.y, targetEnemyId,
                        fireTarget.scannedTurnNum, fireTarget.distance));
        return null;
    }
}
