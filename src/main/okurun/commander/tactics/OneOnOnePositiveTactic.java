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
import okurun.driver.Driver;
import okurun.gunner.Gunner;
import okurun.predictor.Predictor;

/**
 * 1v1の状況で積極的に敵へ向かう戦略
 */
public class OneOnOnePositiveTactic extends AbstractOneOnOneTactic {
    private static final double LOW_ENERGY_THRESHOLD = 10;

    @Override
    protected void setTargetEnemyId(OkuRunBot bot) {
        final BattleManager battleManager = bot.getBattleManager();
        final EnemyProfile alivalEnemy = battleManager.getAliveEnemy(bot);
        if (alivalEnemy != null && alivalEnemy.getLatestState() != null) {
            // 敵の位置を把握している
            targetEnemyId.set(alivalEnemy.getId());
            return;
        }
        targetEnemyId.set(Commander.NO_TARGET);
    }

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

        if (bot.getEnergy() > LOW_ENERGY_THRESHOLD) {
            // 敵位置の少し横を目指します
            // 距離は自分と敵のエネルギー差を考慮して調整します
            final double distance = Math.max(0, 200 - ((bot.getEnergy() - latestEnemyState.energy) * 5));
            final boolean clockwise = true;
            targetMovePosition = Tactic.calculatePointCUsingTrig(
                    bot.getPosition(), predictedEnemyState.getPosition(), distance, clockwise);
            if (!bot.getArenaMap().isInsideArena(targetMovePosition)) {
                // 目標位置がアリーナの外なら逆サイドから回り込みます
                targetMovePosition = Tactic.calculatePointCUsingTrig(
                        bot.getPosition(), predictedEnemyState.getPosition(), distance, !clockwise);
            }
            return;
        }

        // 残りエネルギーが少ない時は敵へ突撃します
        targetMovePosition = predictedEnemyState.getPosition();
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
        final EnemyState latesEnemyState = targetEnemyProfile.getLatestState();
        if (latesEnemyState == null) {
            // 敵のステータスが取得できない場合はスキャンを行います
            gunAction = Gunner.Action.SCAN;
            return;
        }
        if (latesEnemyState.energy <= 0) {
            // 敵のエネルギーが0以下の場合は止めを刺します
            gunAction = Gunner.Action.EXECUTION;
            return;
        }
        if (targetEnemyProfile.isNoMove(bot) && latesEnemyState.distance > OkuRunBot.BODY_SIZE) {
            // 敵が動いていない、かつ離れている場合は射撃します
            gunAction = Gunner.Action.EXECUTION;
            return;
        }

        if (bot.getGunHeat() <= bot.getGunCoolingRate() * 3) {
            // 3ターン以内に射撃可能であれば射撃を行います
            if (latesEnemyState.distance < OkuRunBot.BODY_SIZE + 10) {
                gunAction = Gunner.Action.RAPID_FIRE;
                baseFirePower = Constants.MAX_FIREPOWER;
                return;
            }
            gunAction = Gunner.Action.MAX_POWER;
            baseFirePower = Constants.MAX_FIREPOWER;
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

    @Override
    public HandlePriority getHandlePriority(OkuRunBot bot) {
        final BattleManager battleManager = bot.getBattleManager();
        final EnemyState enemyState = battleManager.getLatestEnemyState(targetEnemyId.get());
        if (enemyState == null) {
            return HandlePriority.TARGET;
        }

        final Commander commander = bot.getCommander();
        final double enemyLateralAngle = Math.abs(commander.getEnemyLateralAngle(bot, enemyState));
        if (enemyLateralAngle <= 30 || enemyLateralAngle >= 120) {
            // 敵が自分からみて縦方向にいる場合はジグザク走行する
            return HandlePriority.AVOID_BULLET;
        }

        return HandlePriority.TARGET;
    }

    @Override
    public AccelePriority getAccelePriority(OkuRunBot bot) {
        final BattleManager battleManager = bot.getBattleManager();
        final EnemyState enemyState = battleManager.getLatestEnemyState(targetEnemyId.get());
        if (enemyState == null) {
            return AccelePriority.MAX_SPEED;
        }

        final Commander commander = bot.getCommander();
        final double enemyLateralAngle = Math.abs(commander.getEnemyLateralAngle(bot, enemyState));
        if (enemyLateralAngle >= 60 && enemyLateralAngle <= 120) {
            // 敵が自分からみて横方向にいる場合はランダムにブレーキをかける
            return AccelePriority.AVOID_BULLET;
        }

        return AccelePriority.MAX_SPEED;
    }

}
