package okurun.gunner.actions;

import dev.robocode.tankroyale.botapi.Constants;
import okurun.OkuRunBot;
import okurun.battlemanager.BattleManager;
import okurun.battlemanager.BulletHistory;
import okurun.battlemanager.EnemyProfile;
import okurun.battlemanager.EnemyState;
import okurun.commander.Commander;
import okurun.gunner.Gunner;
import okurun.predictor.Predictor;

/**
 * 敵を狙って発射する通常アクション
 * 砲が回りきるまで発射しません
 * 火力はCommander.getBaseFirePower()を使用します
 * 火力が0以下の時は砲を向けるだけで射撃しません（予測は火力0.1で予測）
 */
public class NormalGunAction implements GunAction {

    @Override
    public Gunner.Action action(OkuRunBot bot) {
        final Commander commander = bot.getCommander();
        final int targetEnemyId = commander.getTargetEnemyId(bot);
        if (targetEnemyId == Commander.NO_TARGET) {
            return Gunner.Action.SCAN;
        }

        final BattleManager battleManager = bot.getBattleManager();
        final EnemyProfile targetEnemyProfile = battleManager.getEnemyProfile(targetEnemyId);
        final Predictor predictor = bot.getPredictor();
        final EnemyState currentEnemyState = predictor.predict(bot, targetEnemyProfile, bot.getTurnNumber());
        if (currentEnemyState == null) {
            return Gunner.Action.TRACKING;
        }

        // 弾丸のパワーを計算します
        double firePower = Math.min(Math.min(commander.getBaseFirePower(bot), Constants.MAX_FIREPOWER), bot.getEnergy() - 0.1);

        // 射撃目標位置を計算します
        // 弾丸のパワーが0以下なら、最低のパワーで計算します
        EnemyState fireTarget = GunAction.getFireTarget(bot, targetEnemyProfile,
                (firePower <= 0) ? Constants.MIN_FIREPOWER : firePower);
        if (fireTarget == null) {
            return Gunner.Action.TRACKING;
        }

        // デバッグ用に射撃目標位置に円を描きます
        // ※ 描画にはUI画面でDebug Graphicsを有効にする必要があります
        GunAction.drawCircle(bot, fireTarget);

        // 射撃目標位置に砲頭を向けます
        final double bearingTo = bot.gunBearingTo(fireTarget.getPosition());
        bot.setAdjustGunForBodyTurn(true);
        bot.setTurnGunLeft(bearingTo);

        if (bot.getGunHeat() > 0) {
            // 砲がクールダウン中の場合は発射しません
            return null;
        }

        if (bot.getGunTurnRemaining() > 0) {
            // 砲頭が回頭中なら発射しません
            return null;
        }

        if (Math.abs(bearingTo) > bot.getMaxGunTurnRate()) {
            // 砲頭がまわり切らないなら発射しません
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
