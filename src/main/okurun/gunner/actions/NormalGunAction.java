package okurun.gunner.actions;

import dev.robocode.tankroyale.botapi.Constants;
import okurun.OkuRunBot;
import okurun.battlemanager.BattleManager;
import okurun.battlemanager.BulletStatus;
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
        double bulletPower = Math.ceil(getBulletPower(bot, currentEnemyState));

        // 射撃目標位置を計算します
        // 弾丸のパワーが0以下なら、最低1のパワーで計算します
        EnemyState fireTarget = GunAction.getFireTarget(bot, targetEnemyProfile, bulletPower <= 0 ? 1 : bulletPower);
        if (fireTarget == null) {
            return TrackingGunAction.class.getName();
        }

        // デバッグ用に射撃目標位置に円を描きます
        // ※ 描画にはUI画面でDebug Graphicsを有効にする必要があります
        GunAction.drawTargetPoint(bot, fireTarget, (int) bulletPower);

        // 射撃目標位置に砲頭を向けます
        final double bearingTo = bot.gunBearingTo(fireTarget.x, fireTarget.y);
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

        if (Math.abs(bearingTo) > Constants.MAX_GUN_TURN_RATE * 0.5) {
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
            if (bulletPower >= -1
                    && Math.abs(bot.gunBearingTo(currentEnemyState.x, currentEnemyState.y) - bearingTo) < 5) {
                // 現在位置と予測位置が自分から見てほぼ一直線なら発射します
                bot.setFire(1);
                battleManager.bulletStack.addLast(
                        new BulletStatus(commander.getPredictorModelName(bot), fireTarget.x, fireTarget.y, targetEnemyId,
                                fireTarget.scandTurnNum));
                return null;
            }
            // 弾丸のパワーが0以下なら発射しません
            return null;
        }

        bot.setFire(bulletPower);

        // デバッグ用に弾丸の情報をスタックに保存します
        battleManager.bulletStack.addLast(
                new BulletStatus(commander.getPredictorModelName(bot), fireTarget.x, fireTarget.y, targetEnemyId,
                        fireTarget.scandTurnNum));
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
        double bulletPower = bot.getCommander().getBaseBulletPower(bot);

        // 敵との距離が近い時はパワーを上げる
        final double distance = bot.distanceTo(currentEnemyState.x, currentEnemyState.y);
        if (distance <= 20) {
            bulletPower += 1.5;
        } else if (distance > 20 && distance <= 50) {
            bulletPower += 1;
        } else if (distance > 50 && distance <= 100) {
            bulletPower += 0.5;
        } else if (distance > 200 && distance <= 300) {
            bulletPower -= 0.5;
        } else if (distance > 300 && distance <= 400) {
            bulletPower -= 1;
        } else if (distance > 400 && distance <= 500) {
            bulletPower -= 1.5;
        } else if (distance > 500 && distance <= 600) {
            bulletPower -= 2;
        } else if (distance > 600 && distance <= 700) {
            bulletPower -= 2.5;
        } else if (distance > 700) {
            bulletPower -= 3;
        }

        // 敵との相対速度が自分に接近する動きならパワーを上げる
        final double approachVelocity = Commander.getApproachVelocity(bot, currentEnemyState);
        if (approachVelocity >= Constants.MAX_SPEED) {
            bulletPower += 1.5;
        } else if (approachVelocity >= Constants.MAX_SPEED * 0.5) {
            bulletPower += 1;
        } else if (approachVelocity >= Constants.MAX_SPEED * 0.25) {
            bulletPower += 0.5;
        } else if (approachVelocity <= Constants.MAX_SPEED * -0.25) {
            bulletPower -= 0.5;
        } else if (approachVelocity <= Constants.MAX_SPEED * -0.5) {
            bulletPower -= 1;
        } else if (approachVelocity <= Constants.MAX_SPEED * -1) {
            bulletPower -= 1.5;
        }

        // 距離が近く敵が自分に対して縦方向に向いている時はパワーを上げる
        if (distance <= 150) {
            final double enemyLateralAngle = Commander.getEnemyLateralAngle(bot, currentEnemyState);
            if ((enemyLateralAngle >= 160 && enemyLateralAngle <= 200)
                    || (enemyLateralAngle <= -160 && enemyLateralAngle >= -200)) {
                bulletPower += 0.5;
            } else if ((enemyLateralAngle >= 70 && enemyLateralAngle <= 110)
                    || (enemyLateralAngle <= -70 && enemyLateralAngle >= -110)) {
                bulletPower -= 0.5;
            }
        }

        return bulletPower;
    }
}
