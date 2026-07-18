package okurun.gunner.actions;

import dev.robocode.tankroyale.botapi.Constants;
import okurun.OkuRunBot;
import okurun.commander.Commander;
import okurun.enemymanager.EnemyManager;
import okurun.enemymanager.EnemyState;
import okurun.gunner.BulletHistory;
import okurun.gunner.Gunner;

/**
 * エネルギーが０になり動けなくなった敵に止めを刺すガンアクション
 * 予測はせず、敵の現在位置に対して最小火力で発射します
 * 砲が回りきるまで発射しません
 */
public class ExecutionGunAction implements GunAction {

    @Override
    public Gunner.ActionId action(OkuRunBot bot) {
        final Commander commander = bot.getCommander();
        final int targetEnemyId = commander.getTargetEnemyId(bot);
        if (targetEnemyId == Commander.NO_TARGET) {
            return Gunner.ActionId.SCAN;
        }

        final EnemyManager enemyManager = bot.getEnemyManager();
        final EnemyState latestEnemyState = enemyManager.getLatestEnemyState(targetEnemyId);
        if (latestEnemyState == null) {
            return Gunner.ActionId.SCAN;
        }

        final double firePower = Constants.MIN_FIREPOWER;
        GunAction.drawCircle(bot, latestEnemyState);

        // 射撃目標位置に砲頭を向けます
        final double bearingTo = bot.gunBearingTo(latestEnemyState.getPosition());
        bot.setAdjustGunForBodyTurn(true);
        bot.setTurnGunLeft(bearingTo);

        if (bot.getGunHeat() > 0) {
            // 砲がクールダウン中の場合は発射しません
            return null;
        }

        // 距離によって許容範囲を変更する
        double angleTolerance = Math.max(5 - bot.distanceTo(latestEnemyState.getPosition()) / 100, 0);
        if (bot.getGunTurnRemaining() > angleTolerance) {
            // 砲頭が回頭中なら発射しません
            return null;
        }

        if (Math.abs(bearingTo) > Constants.MAX_GUN_TURN_RATE) {
            // 砲頭がまわり切らないなら発射しません
            return null;
        }

        bot.setFire(firePower);
        bot.getGunner().addBulletStack(
                new BulletHistory(commander.getPredictModelId(bot), latestEnemyState.x, latestEnemyState.y,
                        targetEnemyId,
                        latestEnemyState.scannedTurnNum, latestEnemyState.distance));
        return null;

    }

}
