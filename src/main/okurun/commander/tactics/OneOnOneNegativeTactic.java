package okurun.commander.tactics;

import java.util.List;

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
     * 移動先のポイントを設定します
     * 
     * @param bot Bot
     */
    @Override
    protected void setTargetMovePosition(OkuRunBot bot) {
        // 隣のエリアへ向かう
        final ArenaMap arenaMap = bot.getArenaMap();
        targetMovePosition = arenaMap.getArea(bot).getNeighboringArea(bot).getCenter();
    }


    @Override
    protected void setBaseFirePower(OkuRunBot bot) {
        baseFirePower = 2;
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
            gunAction = Gunner.Action.RAPID_FIRE;
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
        return HandlePriority.AVOID_BULLET;
    }

    @Override
    public AccelePriority getAccelePriority(OkuRunBot bot) {
        return AccelePriority.HANDLE;
    }
}
