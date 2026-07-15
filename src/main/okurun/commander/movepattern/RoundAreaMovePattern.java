package okurun.commander.movepattern;

import okurun.OkuRunBot;
import okurun.arenamap.ArenaMap;
import okurun.arenamap.ArenaMap.Area;
import okurun.battlemanager.EnemyState;
import okurun.commander.Commander;
import okurun.commander.Commander.AccelPriority;
import okurun.commander.Commander.HandlePriority;

public class RoundAreaMovePattern extends AbstractMovePattern {

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
        final EnemyState enemyState = bot.getBattleManager().getLatestEnemyState(targetEnemyId);
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

    @Override
    public double getMinSpeed(OkuRunBot bot) {
        return 4;
    }

    @Override
    public HandlePriority getHandlePriority(OkuRunBot bot) {
        return HandlePriority.TARGET;
    }

    @Override
    public AccelPriority getAccelPriority(OkuRunBot bot) {
        return AccelPriority.HANDLE;
    }

}
