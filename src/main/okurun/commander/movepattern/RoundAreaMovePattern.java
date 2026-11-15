package okurun.commander.movepattern;

import okurun.OkuRunBot;
import okurun.arenamap.ArenaMap;
import okurun.arenamap.ArenaMap.Area;
import okurun.commander.Commander;
import okurun.commander.Commander.AccelPriority;
import okurun.commander.Commander.HandlePriority;
import okurun.driver.Driver;
import okurun.enemymanager.EnemyManager;
import okurun.enemymanager.EnemyProfile;
import okurun.enemymanager.EnemyState;

public class RoundAreaMovePattern extends AbstractMovePattern {

    /**
     * {@inheritDoc}
     */
    @Override
    public double[] getMovePosition(OkuRunBot bot) {
        // 隣のエリアへ向かう
        final ArenaMap arenaMap = bot.getArenaMap();
        final Area neigeringArea = arenaMap.getArea(bot).getNeighboringArea(bot);
        double[] pos = neigeringArea.getCenter();
        final int targetEnemyId = bot.getCommander().getTargetEnemyId(bot);
        if (targetEnemyId == Commander.NO_TARGET) {
            return pos;
        }
        final EnemyState enemyState = bot.getEnemyManager().getLatestEnemyState(targetEnemyId);
        if (enemyState == null) {
            return pos;
        }

        final double bearingToTargetPos = bot.bearingTo(pos);
        final double bearingToEnemy = bot.bearingTo(enemyState.getPosition());
        if (Math.abs(bearingToTargetPos - bearingToEnemy) < 20) {
            // 進行方向に敵がいる場合は反対側に向かう
            if (bot.distanceTo(enemyState.getPosition()) < 150) {
                pos = neigeringArea.getOppositeArea().getCenter();
            }
        }
        return pos;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getMinSpeed(OkuRunBot bot) {
        return 4;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HandlePriority getHandlePriority(OkuRunBot bot) {
        return HandlePriority.AVOID_BULLET;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccelPriority getAccelPriority(OkuRunBot bot) {
        final EnemyManager enemyManager = bot.getEnemyManager();
        if (enemyManager.getAliveAndNotMissingEnemyCount(bot) > 1) {
            return AccelPriority.HANDLE;
        }
        final int targetEnemyId = bot.getCommander().getTargetEnemyId(bot);
        if (targetEnemyId == Commander.NO_TARGET) {
            return AccelPriority.HANDLE;
        }
        final EnemyProfile enemyProfile = enemyManager.getEnemyProfile(targetEnemyId);
        final EnemyState currentState = bot.getPredictor().predict(bot, enemyProfile, bot.getTurnNumber());
        if (currentState == null) {
            return AccelPriority.HANDLE;
        }
        return AccelPriority.AVOID_BULLET;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Driver.ActionId getDependentDriveActionId() {
        return Driver.ActionId.MOVE_TO;
    }

}
