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

/**
 * 1v1の状況で積極的に敵へ向かう戦略
 */
public class OneOnOnePositiveTactic extends AbstractOneOnOneTactic {

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
    protected void setGunActionId(OkuRunBot bot) {
        if (targetEnemyId.get() == Commander.NO_TARGET) {
            // ターゲットが設定されていない場合はスキャンを行います
            gunActionId = Gunner.ActionId.SCAN;
            return;
        }

        final BattleManager battleManager = bot.getBattleManager();
        final EnemyProfile targetEnemyProfile = battleManager.getEnemyProfile(targetEnemyId.get());
        final EnemyState latestEnemyState = targetEnemyProfile.getLatestState();
        if (latestEnemyState == null) {
            // 敵のステータスが取得できない場合はスキャンを行います
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
            baseFirePower = Constants.MAX_FIREPOWER;
            waitForGunTurn = true;
            return;
        }

        if (targetEnemyId.get() != Commander.NO_TARGET) {
            // ターゲットが設定されている場合は砲頭を敵に向けます
            gunActionId = Gunner.ActionId.TRACKING;
            return;
        }

        // 上記意外はスキャンを行います
        gunActionId = Gunner.ActionId.SCAN;
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
