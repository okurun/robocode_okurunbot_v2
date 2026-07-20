package okurun.commander.movepattern;

import okurun.OkuRunBot;
import okurun.arenamap.ArenaMap;
import okurun.arenamap.ArenaMap.Area;
import okurun.commander.Commander;
import okurun.commander.Commander.AccelPriority;
import okurun.commander.Commander.HandlePriority;
import okurun.commander.tactics.Tactic;
import okurun.driver.Driver;
import okurun.enemymanager.EnemyManager;
import okurun.enemymanager.EnemyState;

public class OppositeSideMovePattern extends AbstractMovePattern {
    private boolean isOrbiting = false;
    private boolean clockwise = true;

    @Override
    public double[] getMovePosition(OkuRunBot bot) {
        final ArenaMap arenaMap = bot.getArenaMap();
        final Commander commander = bot.getCommander();
        final Area targetArea;
        if (commander.getTargetEnemyId(bot) == Commander.NO_TARGET) {
            // 敵の少ないエリアを目標エリアとする
            targetArea = arenaMap.getSafeArea(bot);
        } else {
            final EnemyManager enemyManager = bot.getEnemyManager();
            final EnemyState enemyState = enemyManager.getLatestEnemyState(commander.getTargetEnemyId(bot));
            if (enemyState == null) {
                // 敵の少ないエリアを目標エリアとする
                targetArea = arenaMap.getSafeArea(bot);
            } else {
                // 敵のいるエリアの反対側のエリアを目標エリアとする
                targetArea = arenaMap.getArea(enemyState.getPosition()).getOppositeArea();
            }
        }

        final Area currentArea = arenaMap.getArea(bot);
        if (targetArea != currentArea) {
            // 現在のエリアと目標エリアが違う場合は目標エリアの中心を目指す
            isOrbiting = false; // フラグはリセット
            return targetArea.getCenter();
        }

        final double distance = 100;
        if (isOrbiting) {
            final double[] pos = Tactic.calculatePointCUsingTrig(arenaMap.getCenter(), targetArea.getCenter(), distance,
                    clockwise);
            if (bot.distanceTo(pos) > distance / 2) {
                // 目標から遠い場合はそのまま
                return pos;
            }
            // 目標に近づいた場合は方向転換して反対方向へ進む
            clockwise = !clockwise;
            return Tactic.calculatePointCUsingTrig(arenaMap.getCenter(), targetArea.getCenter(), distance, clockwise);
        }

        // 中心付近に近づくまでは中心を目標にする
        final double[] pos = targetArea.getCenter();
        isOrbiting = bot.distanceTo(pos) < distance; // 中心に近づいたらフラグを立てる
        return targetArea.getCenter();
    }

    @Override
    public HandlePriority getHandlePriority(OkuRunBot bot) {
        return HandlePriority.TARGET;
    }

    @Override
    public AccelPriority getAccelPriority(OkuRunBot bot) {
        return AccelPriority.HANDLE;
    }

    @Override
    public double getMinSpeed(OkuRunBot bot) {
        return 4;
    }

    /**
     * このムーブパターンが依存するドライブアクションIDを取得します
     * 
     * @return このムーブパターンが依存するドライブアクションID
     */
    @Override
    public Driver.ActionId getDependentDriveActionId() {
        return Driver.ActionId.MOVE_TO;
    }

}
