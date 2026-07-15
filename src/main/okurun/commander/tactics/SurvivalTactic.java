package okurun.commander.tactics;

import java.util.List;

import dev.robocode.tankroyale.botapi.Constants;
import okurun.OkuRunBot;
import okurun.arenamap.ArenaMap;
import okurun.battlemanager.BattleManager;
import okurun.battlemanager.EnemyProfile;
import okurun.battlemanager.EnemyState;
import okurun.commander.Commander;
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
                    bot.getTurnNumber(), PredictModelId.SIMPLE);
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
    protected void setPredictModelId(OkuRunBot bot) {
        if (targetEnemyId.get() != Commander.NO_TARGET) {
            final BattleManager battleManager = bot.getBattleManager();
            final Predictor predictor = bot.getPredictor();
            final EnemyProfile enemyProfile = battleManager.getEnemyProfile(targetEnemyId.get());
            final PredictModelId[] models = new PredictModelId[] { PredictModelId.SIMPLE };
            for (PredictModelId model : models) {
                if (predictor.getPredictModel(model).canPredict(bot, enemyProfile)) {
                    predictModelId = model;
                    return;
                }
            }
        }

        predictModelId = PredictModelId.NONE;
    }

    @Override
    protected void setGunActionId(OkuRunBot bot) {
        if (targetEnemyId.get() == Commander.NO_TARGET) {
            gunActionId = Gunner.ActionId.SCAN;
            return;
        }

        final BattleManager battleManager = bot.getBattleManager();
        final EnemyProfile targetEnemyProfile = battleManager.getEnemyProfile(targetEnemyId.get());
        final EnemyState latestEnemyState = targetEnemyProfile.getLatestState();
        if (latestEnemyState == null) {
            // 全体スキャンを優先する
            gunActionId = Gunner.ActionId.SCAN;
            return;
        }
        if (latestEnemyState.energy <= 0) {
            // 敵のエネルギーが0以下の場合は止めを刺します
            gunActionId = Gunner.ActionId.EXECUTION;
            waitForGunTurn = true;
            return;
        }
        if (targetEnemyProfile.isNotMoving(bot) && latestEnemyState.distance > OkuRunBot.BODY_SIZE) {
            // 敵が動いていない、かつ離れている場合は射撃します
            gunActionId = Gunner.ActionId.EXECUTION;
            waitForGunTurn = true;
            return;
        }

        if (bot.getGunHeat() <= bot.getGunCoolingRate() * 3) {
            // 3ターン以内に射撃可能であれば射撃を行います
            if (latestEnemyState.distance < OkuRunBot.BODY_SIZE + 10) {
                gunActionId = Gunner.ActionId.MAX_POWER;
                baseFirePower = Constants.MAX_FIREPOWER;
                waitForGunTurn = false;
                return;
            }
            gunActionId = Gunner.ActionId.MAX_POWER;
            baseFirePower = 2;
            waitForGunTurn = true;
            return;
        }
        gunActionId = Gunner.ActionId.TRACKING;
    }

    @Override
    protected void setRadarActionId(OkuRunBot bot) {
        if (targetEnemyId.get() != Commander.NO_TARGET) {
            final BattleManager battleManager = bot.getBattleManager();
            final Predictor predictor = bot.getPredictor();
            final EnemyState predictedEnemyState = predictor.predict(bot,
                    battleManager.getEnemyProfile(targetEnemyId.get()), bot.getTurnNumber());
            if (predictedEnemyState != null) {
                // ターゲットの位置を探る
                radarActionId = RadarOperator.ActionId.TARGET_SCAN;
                return;
            }
        }
        radarActionId = RadarOperator.ActionId.ALL_SCAN;
    }

    @Override
    protected void setDriveActionId(OkuRunBot bot) {
        final ArenaMap arenaMap = bot.getArenaMap();
        final List<ArenaMap.PotentialCollisionWall> collisionWalls = arenaMap.getPotentialCollisionWalls(bot);
        if (!collisionWalls.isEmpty()) {
            driveActionId = Driver.ActionId.AVOID_WALL;
            return;
        }
        driveActionId = Driver.ActionId.MOVE_TO;
    }

}
