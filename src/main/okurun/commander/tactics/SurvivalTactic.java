package okurun.commander.tactics;

import java.util.List;

import okurun.OkuRunBot;
import okurun.arenamap.ArenaMap;
import okurun.arenamap.ArenaMap.Area;
import okurun.battlemanager.BattleManager;
import okurun.battlemanager.EnemyProfile;
import okurun.battlemanager.EnemyState;
import okurun.commander.Commander;
import okurun.commander.Commander.AccelePriority;
import okurun.commander.Commander.HandlePriority;
import okurun.driver.actions.*;
import okurun.gunner.actions.*;
import okurun.predictor.Predictor;
import okurun.predictor.models.*;
import okurun.radaroperator.actions.*;

/**
 * 敵が複数いる時の生存戦略
 */
public class SurvivalTactic extends AbstractTactic {

    @Override
    protected void setTargetEnemyId(OkuRunBot bot) {
        final BattleManager battleManager = bot.getBattleManager();

        final EnemyProfile zeroEnergyEnemy = battleManager.getZeroEnergyEnemy(bot);
        if (zeroEnergyEnemy != null) {
            // エネルギーが0の敵をターゲットにします
            targetEnemyId = zeroEnergyEnemy.getId();
            return;
        }

        final EnemyProfile nearestEnemy = battleManager.getNearestAliveEnemy(bot);
        if (nearestEnemy != null) {
            final Predictor predictor = bot.getPredictor();
            final EnemyState predictedEnemyState = predictor.predict(bot, nearestEnemy,
                    bot.getTurnNumber());
            if (predictedEnemyState != null) {
                final double distance = bot.distanceTo(predictedEnemyState.x, predictedEnemyState.y);
                if (distance < 200) {
                    // 近距離の敵がいたらターゲットにします
                    targetEnemyId = nearestEnemy.getId();
                    return;
                }
            }
        }
        targetEnemyId = Commander.NO_TARGET;
    }

    @Override
    protected void setTargetMovePosition(OkuRunBot bot) {
        // 敵の少ない安全なエリアへ向かう
        final ArenaMap arenaMap = bot.getArenaMap();
        final Area safeArea = arenaMap.getSafeArea(bot);
        // 目的地で停止してしまわないように少しズラす
        targetMovePosition = Tactic.calculatePointCUsingTrig(
                bot.getPosition(), safeArea.getCenter(), 30, false);
    }

    @Override
    protected void setBaseBulletPower(OkuRunBot bot) {
        super.setBaseBulletPower(bot);

        // 敵の数が多い時はパワーを下げる
        final int alivalEnemyCount = bot.getBattleManager().getAliveAndNotMissingEnemyCount(bot);
        if (alivalEnemyCount > 2) {
            baseBulletPower -= 1;
        } else if (alivalEnemyCount > 1) {
            baseBulletPower =- 0.5;
        }
    }

    @Override
    protected void setPredictorModelName(OkuRunBot bot) {
        predictorModelName = SimplePredictModel.class.getName();
    }

    @Override
    protected void setGunActionName(OkuRunBot bot) {
        if (targetEnemyId != Commander.NO_TARGET) {
            final BattleManager battleManager = bot.getBattleManager();
            final EnemyProfile targetEnemyProfile = battleManager.getEnemyProfile(targetEnemyId);
            if (targetEnemyProfile == null) {
                // 全体スキャンを優先する
                gunActionName = ScanGunAction.class.getName();
                return;
            }
            final EnemyState latesEnemyState = targetEnemyProfile.getLatestState();
            if (latesEnemyState == null) {
                // 全体スキャンを優先する
                gunActionName = ScanGunAction.class.getName();
                return;
            }
            if (latesEnemyState.energy <= 0) {
                // 敵のエネルギーが0以下なら止めを刺します
                gunActionName = ExecutionGunAction.class.getName();
                return;
            }
            gunActionName = NormalGunAction.class.getName();
            return;
        }

        // 全体スキャンを行う
        gunActionName = ScanGunAction.class.getName();
    }

    @Override
    protected void setRadarActionName(OkuRunBot bot) {
        if (targetEnemyId != Commander.NO_TARGET) {
            final BattleManager battleManager = bot.getBattleManager();
            final EnemyProfile targetEnemyProfile = battleManager.getEnemyProfile(targetEnemyId);
            if (targetEnemyProfile == null) {
                // 全体スキャンをする
                radarActionName = AllScanRadarAction.class.getName();
                return;
            }
            final Predictor predictor = bot.getPredictor();
            final EnemyState predictedEnemyState = predictor.predict(bot, targetEnemyProfile, bot.getTurnNumber());
            if (predictedEnemyState != null) {
                // ターゲットの位置を探る
                radarActionName = TargetScanRadarAction.class.getName();
                return;
            }
        }
        radarActionName = AllScanRadarAction.class.getName();
    }

    @Override
    protected void setDriveActionName(OkuRunBot bot) {
        final ArenaMap arenaMap = bot.getArenaMap();
        final List<ArenaMap.PotentialCollisionWall> collisionWalls = arenaMap.getPotentialCollisionWalls(bot);
        if (!collisionWalls.isEmpty()) {
            driveActionName = AvoidWallDriveAction.class.getName();
            return;
        }
        driveActionName = MoveToDriveAction.class.getName();
    }

    @Override
    public HandlePriority getHandlePriority(OkuRunBot bot) {
        return HandlePriority.AVOID_BULLET;
    }

    @Override
    public AccelePriority getAccelePriority(OkuRunBot bot) {
        return AccelePriority.AVOID_BULLET;
    }

    @Override
    public double getMinSpeed(OkuRunBot bot) {
        return 2;
    }
}
