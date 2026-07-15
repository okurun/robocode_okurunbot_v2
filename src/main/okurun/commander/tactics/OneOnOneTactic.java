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

public class OneOnOneTactic extends AbstractOneOnOneTactic {

    @Override
    protected void setMovePatternId(OkuRunBot bot) {
        final int targetEnemyId = getTargetEnemyId(bot);
        if (targetEnemyId == Commander.NO_TARGET) {
            movePatternId = MovePatternId.ROUND_AREA;
            return;
        }

        final EnemyProfile enemyProfile = bot.getBattleManager().getEnemyProfile(targetEnemyId);
        movePatternId = enemyProfile.getMovePatternId();
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

        gunActionId = Gunner.ActionId.MAX_POWER;
        baseFirePower = Constants.MAX_FIREPOWER;
        waitForGunTurn = true;
    }

}
