package okurun.commander.tactics;

import java.util.List;

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
 * 1v1の状況でエリアを巡回しながら戦う戦略
 */
public class OneOnOneGoRoundAreaTactic extends AbstractOneOnOneTactic {

    @Override
    protected void setMovePatternId(OkuRunBot bot) {
        movePatternId = MovePatternId.ROUND_AREA;
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
        final EnemyState latesEnemyState = targetEnemyProfile.getLatestState();
        if (latesEnemyState == null) {
            // 敵のステータスが取得できない場合はスキャンを行います
            gunActionId = Gunner.ActionId.SCAN;
            return;
        }
        if (latesEnemyState.energy <= 0) {
            // 敵のエネルギーが0以下の場合は止めを刺します
            gunActionId = Gunner.ActionId.EXECUTION;
            waitForGunTurn = true;
            return;
        }
        if (targetEnemyProfile.isNoMove(bot) && latesEnemyState.distance > OkuRunBot.BODY_SIZE) {
            // 敵が動いていない、かつ離れている場合は射撃します
            gunActionId = Gunner.ActionId.EXECUTION;
            waitForGunTurn = true;
            return;
        }

        if (bot.getGunHeat() <= bot.getGunCoolingRate() * 3) {
            // 3ターン以内に射撃可能であれば射撃を行います
            gunActionId = Gunner.ActionId.MAX_POWER;
            baseFirePower = 1.5;
            waitForGunTurn = false;
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
