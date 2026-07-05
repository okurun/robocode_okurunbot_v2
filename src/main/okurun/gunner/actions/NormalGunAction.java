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
 * 敵を狙って発射する通常アクション
 */
public class NormalGunAction implements GunAction {

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
        final EnemyState currentEnemyState = predictor.predict(bot, targetEnemyProfile, bot.getTurnNumber());
        if (currentEnemyState == null) {
            return ScanGunAction.class.getName();
        }

        // 弾丸のパワーを計算します
        double bulletPower = getBulletPower(bot, currentEnemyState);

        // 射撃目標位置を計算します
        // 弾丸のパワーが0以下なら、最低のパワーで計算します
        EnemyState fireTarget = GunAction.getFireTarget(bot, targetEnemyProfile,
                (bulletPower <= 0) ? Constants.MIN_FIREPOWER : bulletPower);
        if (fireTarget == null) {
            return TrackingGunAction.class.getName();
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

        if (bot.getGunTurnRemaining() > 0) {
            // 砲頭が回頭中なら発射しません
            return null;
        }

        if (Math.abs(bearingTo) > bot.getMaxGunTurnRate()) {
            // 砲頭がまわり切らないなら発射しません
            return null;
        }

        // 威力を上げても当たると判断出来る場合は威力を上げる
        final double distance = bot.distanceTo(fireTarget.x, fireTarget.y);
        if (distance <= bot.calcBulletSpeed(3)) {
            bulletPower = 3;
        } else if (distance <= bot.calcBulletSpeed(2)) {
            bulletPower = 2;
        } else if (distance <= bot.calcBulletSpeed(1)) {
            bulletPower = 1;
        }

        if (bulletPower <= 0) {
            // 弾丸のパワーが0以下なら発射しません
            return null;
        }

        bot.setFire(bulletPower);

        // デバッグ用に弾丸の情報をスタックに保存します
        battleManager.bulletStack.addLast(
                new BulletHistory(commander.getPredictorModelName(bot), fireTarget.x, fireTarget.y, targetEnemyId,
                        fireTarget.scannedTurnNum));
        return null;
    }

    /**
     * 撃つ弾のパワーを計算します
     * 
     * @param bot
     * @param currentEnemyState 攻撃対象の現在の状態
     * @return 撃つ弾のパワー
     */
    private static double getBulletPower(OkuRunBot bot, EnemyState currentEnemyState) {
        final Commander commander = bot.getCommander();
        double bulletPower = commander.getBaseBulletPower(bot);

        // 敵との距離が近い時はパワーを上げ、遠い時はパワーを下げる
        final double distance = bot.distanceTo(currentEnemyState.x, currentEnemyState.y);
        if (distance > 0 && distance < 150) {
            bulletPower += (150 - distance) * 0.01;
        } else if (distance > 160) {
            bulletPower -= (distance - 160) * 0.01;
        }

        // 敵との相対速度が自分に接近する動きならパワーを上げる
        final double approachVelocity = Commander.getApproachVelocity(bot, currentEnemyState);
        if (approachVelocity > 0) {
            bulletPower -= Math.abs(approachVelocity / Constants.MAX_SPEED) * 1.5;
        } else if (approachVelocity < 0) {
            bulletPower += Math.abs(approachVelocity / Constants.MAX_SPEED) * 1.5;
        }

        // 距離が近く敵が自分に対して縦方向に向いている時はパワーを上げる
        if (distance <= 150) {
            final double enemyLateralAngle = Math.abs(commander.getEnemyLateralAngle(bot, currentEnemyState));
            if (enemyLateralAngle <= 20 || enemyLateralAngle >= 160) {
                // 縦方向に向いている
                bulletPower += 0.3;
            } else if (enemyLateralAngle >= 70 && enemyLateralAngle <= 110) {
                // 横方向に向いている
                bulletPower -= 0.3;
            }
        }

        return bulletPower;
    }
}
