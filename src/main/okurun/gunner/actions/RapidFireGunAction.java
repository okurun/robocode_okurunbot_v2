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
 * 砲が回りきらない場合は威力を小さくして再予測してみます
 * 砲が回りきらなくても発射します
 * 火力はCommander.getBaseFirePower()を使用します
 * 火力が0以下の時は砲を向けるだけで射撃しません（予測は火力0.1で予測）
 */
public class RapidFireGunAction implements GunAction {

    private static final double FIREPOWER_SEARCH_STEP = 0.4;

    @Override
    public Gunner.Action action(OkuRunBot bot) {
        final Commander commander = bot.getCommander();
        final int targetEnemyId = commander.getTargetEnemyId(bot);
        if (targetEnemyId == Commander.NO_TARGET) {
            return Gunner.Action.SCAN;
        }

        double firePower = Math.min(Math.min(commander.getBaseFirePower(bot), Constants.MAX_FIREPOWER), bot.getEnergy() - 0.1);

        // 射撃目標位置を計算します
        final BattleManager battleManager = bot.getBattleManager();
        final EnemyProfile targetEnemyProfile = battleManager.getEnemyProfile(targetEnemyId);
        EnemyState fireTarget = GunAction.getFireTarget(bot, targetEnemyProfile, Math.max(firePower, Constants.MIN_FIREPOWER));
        if (fireTarget == null) {
            // 予測できない場合は最新のステータスを使用します
            fireTarget = targetEnemyProfile.getLatestState();
            if (fireTarget == null) {
                return Gunner.Action.SCAN;
            }
        }

        double bearingTo = bot.gunBearingTo(fireTarget.getPosition());
        while (Math.abs(bearingTo) > bot.getMaxGunTurnRate()) {
            // 砲がまわり切らないなら早い弾丸（威力を下げる）に変更
            if (firePower - FIREPOWER_SEARCH_STEP <= 0) {
                // 0にはならないようにする
                break;
            }
            final EnemyState prevTarget = fireTarget;
            firePower -= FIREPOWER_SEARCH_STEP;
            fireTarget = GunAction.getFireTarget(bot, targetEnemyProfile, firePower);
            if (fireTarget == null) {
                fireTarget = prevTarget;
                break;
            }
            bearingTo = bot.gunBearingTo(fireTarget.getPosition());
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

        if (firePower <= 0) {
            // 弾丸のパワーが0以下なら発射しません
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
