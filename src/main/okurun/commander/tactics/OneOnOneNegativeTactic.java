package okurun.commander.tactics;

import java.util.List;

import okurun.OkuRunBot;
import okurun.arenamap.ArenaMap;
import okurun.battlemanager.BattleManager;
import okurun.battlemanager.EnemyProfile;
import okurun.battlemanager.EnemyState;
import okurun.commander.Commander;
import okurun.driver.Driver;
import okurun.gunner.Gunner;
import okurun.predictor.Predictor;
import okurun.radaroperator.RadarOperator;

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
            targetEnemyId.set(aliveEnemy.getId());
            return;
        }
        targetEnemyId.set(Commander.NO_TARGET);
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
        if (targetEnemyId.get() == Commander.NO_TARGET) {
            // 隣のエリアへ向かう
            targetMovePosition = arenaMap.getArea(bot).getNeighboringArea(bot).getCenter();
            return;
        }

        final BattleManager battleManager = bot.getBattleManager();
        final EnemyProfile targetEnemyProfile = battleManager.getEnemyProfile(targetEnemyId.get());
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

        final double targetDistance = 125;
        final double distanceToEnemy = bot.directionTo(predictedEnemyState.getPosition());
        final double degreesToEnemy = bot.bearingTo(predictedEnemyState.getPosition());
        final double a;
        if (distanceToEnemy > targetDistance) {
            a = 80;
        } else {
            a = 100;
        }
        double bearingTo = degreesToEnemy;
        if (degreesToEnemy < 0) {
            bearingTo += a;
        } else {
            bearingTo -= a;
        }
        targetMovePosition = Predictor.calcPosition(bot.getPosition(),
                bot.normalizeAbsoluteAngle(bot.getDirection() + bearingTo), 200, 1);
        if (!arenaMap.isInsideArena(targetMovePosition)) {
            targetMovePosition = Predictor.calcPosition(bot.getPosition(),
                    bot.normalizeAbsoluteAngle(bot.getDirection() + bearingTo + 180), 200, 1);
        }
    }

    @Override
    protected void setGunActionName(OkuRunBot bot) {
        if (targetEnemyId.get() == Commander.NO_TARGET) {
            // ターゲットが設定されていない場合はスキャンを行います
            gunAction = Gunner.Action.SCAN;
            return;
        }

        final BattleManager battleManager = bot.getBattleManager();
        final EnemyProfile targetEnemyProfile = battleManager.getEnemyProfile(targetEnemyId.get());
        if (targetEnemyProfile == null) {
            gunAction = Gunner.Action.SCAN;
            return;
        }
        final EnemyState latesEnemyState = targetEnemyProfile.getLatestState();
        if (latesEnemyState == null) {
            // 敵のステータスが取得できない場合はスキャンを行います
            gunAction = Gunner.Action.SCAN;
            return;
        }
        if (latesEnemyState.energy <= 0) {
            // 敵のエネルギーが0以下なら止めを刺します
            gunAction = Gunner.Action.EXECUTION;
            return;
        }

        if (bot.getGunHeat() <= bot.getGunCoolingRate() * 3) {
            // 3ターン以内に射撃可能であれば射撃を行います
            if (bot.getEnergy() < 30) {
                // 逆転を目指して連射をする
                gunAction = Gunner.Action.RAPID_FIRE;
                return;
            } else if (Math.abs(bot.getCommander().getEnemyLateralAngle(bot, latesEnemyState)) > 160) {
                // 相手がこちらを向いている時は連射する
                gunAction = Gunner.Action.RAPID_FIRE;
                return;
            }
            gunAction = Gunner.Action.NORMAL;
            return;
        }

        if (targetEnemyId.get() != Commander.NO_TARGET) {
            // ターゲットが設定されている場合は砲頭を敵に向けます
            gunAction = Gunner.Action.TRACKING;
            return;
        }

        // 上記意外はスキャンを行います
        gunAction = Gunner.Action.SCAN;
    }

    @Override
    protected void setRadarActionName(OkuRunBot bot) {
        if (targetEnemyId.get() == Commander.NO_TARGET) {
            radarAction = RadarOperator.Action.ALL_SCAN;
            return;
        }

        final BattleManager battleManager = bot.getBattleManager();
        final EnemyProfile targetEnemyProfile = battleManager.getEnemyProfile(targetEnemyId.get());
        if (targetEnemyProfile == null) {
            radarAction = RadarOperator.Action.ALL_SCAN;
            return;
        }
        final EnemyState latestEnemyState = targetEnemyProfile.getLatestState();
        if (latestEnemyState == null || latestEnemyState.scannedTurnNum < bot.getTurnNumber() - 5) {
            radarAction = RadarOperator.Action.ALL_SCAN;
            return;
        }

        radarAction = RadarOperator.Action.TARGET_SCAN;
    }

    @Override
    protected void setDriveActionName(OkuRunBot bot) {
        final ArenaMap arenaMap = bot.getArenaMap();
        final List<ArenaMap.PotentialCollisionWall> collisionWalls = arenaMap.getPotentialCollisionWalls(bot);
        if (!collisionWalls.isEmpty()) {
            driveAction = Driver.Action.AVOID_WALL;
            return;
        }
        driveAction = Driver.Action.MOVE_TO;
    }

    @Override
    public double getMinSpeed(OkuRunBot bot) {
        return 4;
    }
}
