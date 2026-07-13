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

/**
 * 1v1の状況で積極的に敵へ向かう戦略
 */
public class OneOnOnePositiveTactic extends AbstractOneOnOneTactic {

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
    protected void setMovePatternId(OkuRunBot bot) {
        if (targetEnemyId.get() == Commander.NO_TARGET) {
            // 隣のエリアへ向かう
            movePatternId = MovePatternId.ROUND_AREA;
            return;
        }

        final BattleManager battleManager = bot.getBattleManager();
        final EnemyState latestEnemyState = battleManager.getLatestEnemyState(targetEnemyId.get());
        if (latestEnemyState == null) {
            // 隣のエリアへ向かう
            movePatternId = MovePatternId.ROUND_AREA;
            return;
        }

        // 敵位置の少し横を目指します
        movePatternId = MovePatternId.ENEMY_SIDE;
    }

    @Override
    protected void setGunActionName(OkuRunBot bot) {
        if (targetEnemyId.get() == Commander.NO_TARGET) {
            // ターゲットが設定されていない場合はスキャンを行います
            gunAction = Gunner.ActionId.SCAN;
            return;
        }

        final BattleManager battleManager = bot.getBattleManager();
        final EnemyProfile targetEnemyProfile = battleManager.getEnemyProfile(targetEnemyId.get());
        final EnemyState latesEnemyState = targetEnemyProfile.getLatestState();
        if (latesEnemyState == null) {
            // 敵のステータスが取得できない場合はスキャンを行います
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
            baseFirePower = Constants.MAX_FIREPOWER;
            waitForGunTurn = true;
            return;
        }

        if (targetEnemyId.get() != Commander.NO_TARGET) {
            // ターゲットが設定されている場合は砲頭を敵に向けます
            gunAction = Gunner.ActionId.TRACKING;
            return;
        }

        // 上記意外はスキャンを行います
        gunAction = Gunner.ActionId.SCAN;
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
