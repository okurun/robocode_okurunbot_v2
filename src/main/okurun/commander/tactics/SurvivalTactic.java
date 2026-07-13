package okurun.commander.tactics;

import java.util.List;

import dev.robocode.tankroyale.botapi.Constants;
import okurun.OkuRunBot;
import okurun.arenamap.ArenaMap;
import okurun.battlemanager.BattleManager;
import okurun.battlemanager.EnemyProfile;
import okurun.battlemanager.EnemyState;
import okurun.commander.Commander;
import okurun.commander.Commander.AccelePriority;
import okurun.commander.Commander.HandlePriority;
import okurun.commander.Commander.MovePatternId;
import okurun.driver.Driver;
import okurun.gunner.Gunner;
import okurun.predictor.Predictor;
import okurun.predictor.Predictor.PredictModelId;
import okurun.radaroperator.RadarOperator;

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
            targetEnemyId.set(zeroEnergyEnemy.getId());
            return;
        }

        final EnemyProfile nearestEnemy = battleManager.getNearestAliveEnemy(bot);
        if (nearestEnemy != null) {
            final Predictor predictor = bot.getPredictor();
            final EnemyState predictedEnemyState = predictor.predict(bot, nearestEnemy,
                    bot.getTurnNumber());
            if (predictedEnemyState != null) {
                final double distance = (nearestEnemy.getId() == targetEnemyId.get()) ? 300 : 200;
                if (bot.distanceTo(predictedEnemyState.x, predictedEnemyState.y) < distance) {
                    // 近距離の敵がいたらターゲットにします
                    targetEnemyId.set(nearestEnemy.getId());
                    return;
                }
            }
        }
        targetEnemyId.set(Commander.NO_TARGET);
    }

    @Override
    protected void setMovePatternId(OkuRunBot bot) {
        movePatternId = MovePatternId.SAFE_AREA;
    }

    @Override
    protected void setPredictModel(OkuRunBot bot) {
        if (targetEnemyId.get() != Commander.NO_TARGET) {
            final BattleManager battleManager = bot.getBattleManager();
            final Predictor predictor = bot.getPredictor();
            final EnemyProfile enemyProfile = battleManager.getEnemyProfile(targetEnemyId.get());
            final PredictModelId[] models = new PredictModelId[] { PredictModelId.SIMPLE };
            for (PredictModelId model : models) {
                if (predictor.getPredictModel(model).canPredict(bot, enemyProfile)) {
                    predictModel = model;
                    return;
                }
            }
        }

        predictModel = PredictModelId.NONE;
    }

    @Override
    protected void setGunActionName(OkuRunBot bot) {
        if (targetEnemyId.get() == Commander.NO_TARGET) {
            gunAction = Gunner.ActionId.SCAN;
            return;
        }

        final BattleManager battleManager = bot.getBattleManager();
        final EnemyProfile targetEnemyProfile = battleManager.getEnemyProfile(targetEnemyId.get());
        final EnemyState latesEnemyState = targetEnemyProfile.getLatestState();
        if (latesEnemyState == null) {
            // 全体スキャンを優先する
            gunAction = Gunner.ActionId.SCAN;
            return;
        }
        if (latesEnemyState.energy <= 0) {
            // 敵のエネルギーが0以下の場合は止めを刺します
            gunAction = Gunner.ActionId.EXECUTION;
            waitForGunTurn = true;
            return;
        }
        if (targetEnemyProfile.isNoMove(bot) && latesEnemyState.distance > OkuRunBot.BODY_SIZE) {
            // 敵が動いていない、かつ離れている場合は射撃します
            gunAction = Gunner.ActionId.EXECUTION;
            waitForGunTurn = true;
            return;
        }

        if (bot.getGunHeat() <= bot.getGunCoolingRate() * 3) {
            // 3ターン以内に射撃可能であれば射撃を行います
            if (latesEnemyState.distance < OkuRunBot.BODY_SIZE + 10) {
                gunAction = Gunner.ActionId.MAX_POWER;
                baseFirePower = Constants.MAX_FIREPOWER;
                waitForGunTurn = false;
                return;
            }
            gunAction = Gunner.ActionId.MAX_POWER;
            baseFirePower = 2;
            waitForGunTurn = true;
            return;
        }
        gunAction = Gunner.ActionId.TRACKING;
    }

    @Override
    protected void setRadarActionName(OkuRunBot bot) {
        if (targetEnemyId.get() != Commander.NO_TARGET) {
            final BattleManager battleManager = bot.getBattleManager();
            final Predictor predictor = bot.getPredictor();
            final EnemyState predictedEnemyState = predictor.predict(bot,
                    battleManager.getEnemyProfile(targetEnemyId.get()), bot.getTurnNumber());
            if (predictedEnemyState != null) {
                // ターゲットの位置を探る
                radarAction = RadarOperator.ActionId.TARGET_SCAN;
                return;
            }
        }
        radarAction = RadarOperator.ActionId.ALL_SCAN;
    }

    @Override
    protected void setDriveActionName(OkuRunBot bot) {
        final ArenaMap arenaMap = bot.getArenaMap();
        final List<ArenaMap.PotentialCollisionWall> collisionWalls = arenaMap.getPotentialCollisionWalls(bot);
        if (!collisionWalls.isEmpty()) {
            driveAction = Driver.ActionId.AVOID_WALL;
            return;
        }
        driveAction = Driver.ActionId.MOVE_TO;
    }

    @Override
    public HandlePriority getHandlePriority(OkuRunBot bot) {
        return HandlePriority.AVOID_BULLET;
    }

    @Override
    public AccelePriority getAccelePriority(OkuRunBot bot) {
        return AccelePriority.MAX_SPEED;
    }

    @Override
    public double getMinSpeed(OkuRunBot bot) {
        return 2;
    }

}
