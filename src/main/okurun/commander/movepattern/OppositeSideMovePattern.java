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
import okurun.enemymanager.EnemyProfile;
import okurun.enemymanager.EnemyState;

public class OppositeSideMovePattern extends AbstractMovePattern {

    private boolean isOrbiting = false;
    private boolean clockwise = true;

    /**
     * {@inheritDoc}
     */
    @Override
    public double[] getMovePosition(OkuRunBot bot) {
        final ArenaMap arenaMap = bot.getArenaMap();
        final Commander commander = bot.getCommander();
        if (commander.getTargetEnemyId(bot) == Commander.NO_TARGET) {
            // 現在のエリアを目標エリアとする
            return getMovePosition(bot, arenaMap.getArea(bot));
        }

        final EnemyManager enemyManager = bot.getEnemyManager();
        final EnemyProfile enemyProfile = enemyManager.getEnemyProfile(commander.getTargetEnemyId(bot));
        final EnemyState enemyState = enemyProfile.getLatestState();
        if (enemyState == null) {
            // 現在のエリアを目標エリアとする
            return getMovePosition(bot, arenaMap.getArea(bot));
        }

        // 敵のいるエリアの反対側のエリアを目標エリアとする
        final Area targetArea = arenaMap.getArea(enemyState.getPosition()).getOppositeArea();
        final double[] pos = getMovePosition(bot, targetArea);
        final EnemyState predictedState = bot.getPredictor().predict(bot, enemyProfile, bot.getTurnNumber() + 1);
        if (predictedState == null) {
            // 予測が出来ない場合は敵の位置を考慮しない
            return pos;
        }
        // 敵がいる方向
        final double enemyDirection = bot.normalizeAbsoluteAngle(bot.bearingTo(predictedState.getPosition()));
        // 進行方向
        final double myDirection = bot.normalizeAbsoluteAngle(bot.bearingTo(pos));
        if (Math.abs(enemyDirection - myDirection) < 20) {
            // 進行方向に敵がいる
            if (bot.distanceTo(predictedState.getPosition()) < 120) {
                // 敵との距離が近い場合は目標エリアの隣のエリアへ向かう（衝突を避ける）
                return getMovePosition(bot, targetArea.getNeighboringArea(bot));
            }
        }

        return pos;
    }

    /**
     * 目標エリアへ向かうための目標位置を取得します
     * 
     * @param bot        ボット
     * @param targetArea 目標エリア
     * @return 目標位置
     */
    private double[] getMovePosition(OkuRunBot bot, Area targetArea) {
        final ArenaMap arenaMap = bot.getArenaMap();
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

    /**
     * {@inheritDoc}
     */
    @Override
    public HandlePriority getHandlePriority(OkuRunBot bot) {
        final EnemyManager enemyManager = bot.getEnemyManager();
        final int targetEnemyId = bot.getCommander().getTargetEnemyId(bot);
        if (targetEnemyId == Commander.NO_TARGET) {
            return HandlePriority.TARGET;
        }
        final EnemyProfile enemyProfile = enemyManager.getEnemyProfile(targetEnemyId);
        final EnemyState currentState = bot.getPredictor().predict(bot, enemyProfile, bot.getTurnNumber());
        if (currentState == null) {
            return HandlePriority.TARGET;
        }
        return HandlePriority.AVOID_BULLET;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccelPriority getAccelPriority(OkuRunBot bot) {
        return AccelPriority.AVOID_BULLET;
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
    public Driver.ActionId getDependentDriveActionId() {
        return Driver.ActionId.MOVE_TO;
    }

}
