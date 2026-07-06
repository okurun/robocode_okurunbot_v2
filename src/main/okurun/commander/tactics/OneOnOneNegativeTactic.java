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
 * 1v1の状況で逃げながら逆転を狙う戦略
 */
public class OneOnOneNegativeTactic extends AbstractOneOnOneTactic {

    @Override
    protected void setTargetEnemyId(OkuRunBot bot) {
        final BattleManager battleManager = bot.getBattleManager();
        final EnemyProfile aliveEnemy = battleManager.getAliveEnemy(bot);
        if (aliveEnemy != null && aliveEnemy.getLatestState() != null) {
            // 敵の位置を把握している
            targetEnemyId = aliveEnemy.getId();
            return;
        }
        targetEnemyId = Commander.NO_TARGET;
    }

    /**
     * 移動先のポイントを取得します
     * 
     * @param bot Bot
     * @return 移動先のポイント
     */
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

        final double degreesToEnemy = bot.bearingTo(predictedEnemyState.getPosition());
        final double bearingTo = degreesToEnemy + ((degreesToEnemy < 0) ? 90 : +90);
        targetMovePosition = Predictor.calcPosition(bot.getPosition(),
                bot.normalizeAbsoluteAngle(bot.getDirection() + bearingTo), 200, 1);
        if (!arenaMap.isInsideArena(targetMovePosition)) {
            targetMovePosition = Predictor.calcPosition(bot.getPosition(),
                    bot.normalizeAbsoluteAngle(bot.getDirection() + bearingTo + 180), 200, 1);
        }
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
            gunActionName = ScanGunAction.class.getName();
            return;
        }
        final EnemyState latesEnemyState = targetEnemyProfile.getLatestState();
        if (latesEnemyState == null) {
            // 敵のステータスが取得できない場合はスキャンを行います
            gunActionName = ScanGunAction.class.getName();
            return;
        }
        if (latesEnemyState.energy <= 0) {
            // 敵のエネルギーが0以下なら止めを刺します
            gunActionName = ExecutionGunAction.class.getName();
            return;
        }

        if (bot.getGunHeat() <= bot.getGunCoolingRate() * 2) {
            // 2ターン以内に射撃可能であれば射撃を行います
            if (bot.getEnergy() < 30) {
                // 逆転を目指して連射をする
                gunActionName = RapidFireGunAction.class.getName();
                return;
            } else if (Math.abs(bot.getCommander().getEnemyLateralAngle(bot, latesEnemyState)) > 160) {
                // 相手がこちらを向いている時は連射する
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
        if (latestEnemyState == null || latestEnemyState.scannedTurnNum < bot.getTurnNumber() - 5) {
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
        return 4;
    }
}
