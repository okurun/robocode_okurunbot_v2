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

public class MaxPowerGunAction implements GunAction {

    private static final double FIREPOWER_SEARCH_STEP = 0.4;

    @Override
    public Gunner.Action action(OkuRunBot bot) {
        final Commander commander = bot.getCommander();
        final int targetEnemyId = commander.getTargetEnemyId(bot);
        if (targetEnemyId == Commander.NO_TARGET) {
            return Gunner.Action.SCAN;
        }

        final BattleManager battleManager = bot.getBattleManager();
        final EnemyProfile targetEnemyProfile = battleManager.getEnemyProfile(targetEnemyId);
        if (targetEnemyProfile == null) {
            return Gunner.Action.SCAN;
        }
        final Predictor predictor = bot.getPredictor();
        final EnemyState currentEnemyState = predictor.predict(bot, targetEnemyProfile, bot.getTurnNumber());
        if (currentEnemyState == null) {
            return Gunner.Action.TRACKING;
        }

        double firePower;
        EnemyState fireTarget = null;
        for (firePower = Constants.MAX_FIREPOWER; firePower > 0; firePower -= FIREPOWER_SEARCH_STEP) {
            fireTarget = GunAction.getFireTarget(bot, targetEnemyProfile, firePower);
            if (fireTarget != null) {
                break;
            }
        }
        if (fireTarget == null) {
            return Gunner.Action.TRACKING;
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
        GunAction.drawTargetPoint(bot, fireTarget, firePower);

        // 射撃目標位置に砲頭を向けます
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
