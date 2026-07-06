package okurun.gunner.actions;

import dev.robocode.tankroyale.botapi.Constants;
import okurun.OkuRunBot;
import okurun.battlemanager.BattleManager;
import okurun.battlemanager.BulletHistory;
import okurun.battlemanager.EnemyProfile;
import okurun.battlemanager.EnemyState;
import okurun.commander.Commander;

/**
 * エネルギーが０になった敵に止めを刺すガンアクション
 */
public class ExecutionGunAction implements GunAction {

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

        final EnemyState latestEnemyState = targetEnemyProfile.getLatestState();
        if (latestEnemyState == null) {
            return ScanGunAction.class.getName();
        }

        final double firePower = Constants.MIN_FIREPOWER;
        GunAction.drawTargetPoint(bot, latestEnemyState, firePower);

        // 射撃目標位置に砲頭を向けます
        final double bearingTo = bot.gunBearingTo(latestEnemyState.x, latestEnemyState.y);
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

        if (Math.abs(bearingTo) > Constants.MAX_GUN_TURN_RATE) {
            // 砲頭がまわり切らないなら発射しません
            return null;
        }

        bot.setFire(firePower);
        battleManager.bulletStack.addLast(
                new BulletHistory(this.getClass().getName(), latestEnemyState.x, latestEnemyState.y, targetEnemyId,
                        latestEnemyState.scannedTurnNum));
        return null;

    }

}
