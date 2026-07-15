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
 * できるだけ最大火力で射撃します
 * 砲が回りきらない場合は威力を小さくして再予測してみます
 * 砲が回りきるまで発射しません
 * 最大火力はCommander.getBaseFirePower()を使用します
 */
public class MaxPowerGunAction implements GunAction {

    private static final double FIREPOWER_SEARCH_STEP = 0.4;

    @Override
    public Gunner.ActionId action(OkuRunBot bot) {
        final Commander commander = bot.getCommander();
        final int targetEnemyId = commander.getTargetEnemyId(bot);
        if (targetEnemyId == Commander.NO_TARGET) {
            return Gunner.ActionId.SCAN;
        }

        final BattleManager battleManager = bot.getBattleManager();
        final EnemyProfile targetEnemyProfile = battleManager.getEnemyProfile(targetEnemyId);

        double firePower = Math.min(Math.min(commander.getBaseFirePower(bot), Constants.MAX_FIREPOWER),
                bot.getEnergy() - 0.1);
        EnemyState fireTarget = null;
        double bearingTo = 0;
        for (; firePower > 0; firePower -= FIREPOWER_SEARCH_STEP) {
            fireTarget = GunAction.getFireTarget(bot, targetEnemyProfile, firePower);
            if (fireTarget != null) {
                bearingTo = bot.gunBearingTo(fireTarget.getPosition());
                if (Math.abs(bearingTo) <= bot.getMaxGunTurnRate()) {
                    break;
                }
                // 砲が回りきらないなら威力を弱めて、まわり切る予測位置になるか試す
            }
        }
        if (fireTarget == null) {
            return Gunner.ActionId.TRACKING;
        }

        // デバッグ用に射撃目標位置に円を描きます
        // ※ 描画にはUI画面でDebug Graphicsを有効にする必要があります
        GunAction.drawCircle(bot, fireTarget);

        // 射撃目標位置に砲頭を向けます
        bot.setAdjustGunForBodyTurn(true);
        bot.setTurnGunLeft(bearingTo);

        if (bot.getGunHeat() > 0) {
            // 砲がクールダウン中の場合は発射しません
            return null;
        }

        if (commander.getWaitForGunTurn(bot)) {
            double maxTurnAngle = Math.max(5 - bot.distanceTo(fireTarget.getPosition()) / 100, 0);
            if (bot.getGunTurnRemaining() > maxTurnAngle) {
                // 砲頭が回頭中なら発射しません
                return null;
            }

            if (Math.abs(bearingTo) > bot.getMaxGunTurnRate()) {
                // 砲頭がまわり切らないなら発射しません
                return null;
            }
        }

        if (firePower <= 0) {
            // 弾丸のパワーが0以下なら発射しません
            return null;
        }

        bot.setFire(firePower);

        // デバッグ用に弾丸の情報をスタックに保存します
        GunAction.stackBulletHistory(bot,
                new BulletHistory(commander.getPredictModelId(bot), fireTarget.x, fireTarget.y, targetEnemyId,
                        fireTarget.scannedTurnNum, fireTarget.distance));
        return null;
    }

}
