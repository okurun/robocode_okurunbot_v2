package okurun.commander.tactics;

import java.util.List;

import okurun.OkuRunBot;
import okurun.arenamap.ArenaMap;
import okurun.battlemanager.BattleManager;
import okurun.battlemanager.EnemyProfile;
import okurun.battlemanager.EnemyState;
import okurun.commander.Commander;
import okurun.driver.actions.*;
import okurun.gunner.actions.*;
import okurun.predictor.Predictor;
import okurun.radaroperator.actions.*;

/**
 * 1v1の状況で積極的に攻める戦略
 */
public class OneOnOnePositiveTactic extends AbstractOneOnOneTactic {

    @Override
    protected void setTargetEnemyId(OkuRunBot bot) {
        final BattleManager battleManager = bot.getBattleManager();
        final EnemyProfile alivalEnemy = battleManager.getAlivalEnemy(bot);
        if (alivalEnemy != null && alivalEnemy.getLatestState() != null) {
            // 敵の位置を把握している
            targetEnemyId = alivalEnemy.getId();
            return;
        }
        targetEnemyId = Commander.NO_TARGET;
    }

    @Override
    protected void setTargetMovePosition(OkuRunBot bot) {
        final ArenaMap arenaMap = bot.getArenaMap();
        if (targetEnemyId == Commander.NO_TARGET) {
            // 隣のエリアへ向かう
            targetMovePosition = arenaMap.getArea(bot).getNeighboringArea(bot).getCenter();
            return;
        }

        final BattleManager battleManager = bot.getBattleManager();
        final EnemyProfile targetEnemyProfile = battleManager.getEnemyProfile(targetEnemyId);
        if (targetEnemyProfile == null) {
            // 隣のエリアへ向かう
            targetMovePosition = arenaMap.getArea(bot).getNeighboringArea(bot).getCenter();
            return;
        }
        final EnemyState latestEnemyState = targetEnemyProfile.getLatestState();
        if (latestEnemyState == null) {
            // 隣のエリアへ向かう
            targetMovePosition = arenaMap.getArea(bot).getNeighboringArea(bot).getCenter();
            return;
        }

        final Predictor predictor = bot.getPredictor();
        EnemyState predictedEnemyState = predictor.predict(bot, targetEnemyProfile, bot.getTurnNumber());
        if (predictedEnemyState == null) {
            predictedEnemyState = latestEnemyState;
        }

        // 敵位置の少し横を目指します
        // 距離は自分と敵のエネルギー差を考慮して調整します
        final double distance = Math.max(0, 200 - ((bot.getEnergy() - latestEnemyState.energy) * 5));
        final boolean clockwise = true;
        targetMovePosition = Tactic.calculatePointCUsingTrig(
                bot.getPosition(), predictedEnemyState.getPosition(), distance, clockwise);
        if (!bot.getArenaMap().isInsideArena(targetMovePosition)) {
            targetMovePosition = Tactic.calculatePointCUsingTrig(
                    bot.getPosition(), predictedEnemyState.getPosition(), distance, !clockwise);
        }
    }

    @Override
    protected void setBaseBulletPower(OkuRunBot bot) {
        double bulletPower = 2;

        // 自分のエネルギーが少ない時はパワーを下げる
        if (bot.getEnergy() < 20) {
            bulletPower -= 1;
        } else if (bot.getEnergy() < 50) {
            bulletPower -= 0.5;
        } else if (bot.getEnergy() > 150) {
            bulletPower += 1;
        } else if (bot.getEnergy() > 100) {
            bulletPower += 0.5;
        }

        baseBulletPower = bulletPower;
    }

    @Override
    protected void setGunActionName(OkuRunBot bot) {
        if (targetEnemyId == Commander.NO_TARGET) {
            // ターゲットが設定されていない場合はスキャンを行います
            gunActionName = ScanGunAction.class.getName();
            return;
        }

        final BattleManager battleManager = bot.getBattleManager();
        final EnemyProfile targetEnemyProfile = battleManager.getEnemyProfile(targetEnemyId);
        if (targetEnemyProfile == null) {
            gunActionName =  ScanGunAction.class.getName();
            return;
        }
        final EnemyState latesEnemyState = targetEnemyProfile.getLatestState();
        if (latesEnemyState == null) {
            // 敵のステータスが取得できない場合はスキャンを行います
            gunActionName =  ScanGunAction.class.getName();
            return;
        }
        if (latesEnemyState.energy <= 0) {
            // 敵のエネルギーが0以下なら止めを刺します
            gunActionName =  ExecutionGunAction.class.getName();
            return;
        }

        if (bot.getGunHeat() <= bot.getGunCoolingRate() * 2) {
            // 2ターン以内に射撃可能であれば射撃を行います
            if (bot.getTurnNumber() - battleManager.getLastFiredTurnNum() > 100) {
                // 射撃できない状態が続いていたら連射を選択する
                gunActionName = RapidFireGunAction.class.getName();
                return;
            }
            gunActionName = NormalGunAction.class.getName();
            return;
        }

        if (targetEnemyId != Commander.NO_TARGET) {
            // ターゲットが設定されている場合は砲頭を敵に向けます
            gunActionName = TrackingGunAction.class.getName();
            return;
        }

        // 上記意外はスキャンを行います
        gunActionName = ScanGunAction.class.getName();
    }

    @Override
    protected void setRadarActionName(OkuRunBot bot) {
        if (targetEnemyId == Commander.NO_TARGET) {
            radarActionName = AllScanRadarAction.class.getName();
            return;
        }

        final BattleManager battleManager = bot.getBattleManager();
        final EnemyProfile targetEnemyProfile = battleManager.getEnemyProfile(targetEnemyId);
        if (targetEnemyProfile == null) {
            radarActionName = AllScanRadarAction.class.getName();
            return;
        }
        final EnemyState latestEnemyState = targetEnemyProfile.getLatestState();
        if (latestEnemyState == null || latestEnemyState.scandTurnNum < bot.getTurnNumber() - 5) {
            radarActionName = AllScanRadarAction.class.getName();
            return;
        }

        radarActionName = TargetScanRadarAction.class.getName();
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
    public double getMinSpeed(OkuRunBot bot) {
        return 6;
    }

}
