package okurun.commander.tactics;

import java.util.List;

import dev.robocode.tankroyale.botapi.events.HitByBulletEvent;
import okurun.OkuRunBot;
import okurun.arenamap.ArenaMap;
import okurun.arenamap.ArenaMap.Area;
import okurun.battlemanager.BattleManager;
import okurun.battlemanager.EnemyProfile;
import okurun.battlemanager.EnemyState;
import okurun.commander.Commander;
import okurun.commander.Commander.AccelePriority;
import okurun.commander.Commander.HandlePriority;
import okurun.driver.Driver;
import okurun.gunner.Gunner;
import okurun.predictor.Predictor;
import okurun.predictor.Predictor.Model;
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
                    bot.getTurnNumber());
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
    protected void setTargetMovePosition(OkuRunBot bot) {
        // 敵の少ない安全なエリアへ向かう
        final ArenaMap arenaMap = bot.getArenaMap();
        final Area safeArea = arenaMap.getSafeArea(bot);
        // 目的地で停止してしまわないように少しズラす
        targetMovePosition = Tactic.calculatePointCUsingTrig(
                bot.getPosition(), safeArea.getCenter(), 30, false);
    }

    @Override
    protected void setBaseFirePower(OkuRunBot bot) {
        super.setBaseFirePower(bot);

        // 敵の数が多い時はパワーを下げる
        final int aliveEnemyCount = bot.getBattleManager().getAliveAndNotMissingEnemyCount(bot);
        if (aliveEnemyCount > 2) {
            baseFirePower -= 1;
        } else if (aliveEnemyCount > 1) {
            baseFirePower = -0.5;
        }
    }

    @Override
    protected void setPredictModel(OkuRunBot bot) {
        predictModel = Model.SIMPLE;
    }

    @Override
    protected void setGunActionName(OkuRunBot bot) {
        if (targetEnemyId.get() != Commander.NO_TARGET) {
            final BattleManager battleManager = bot.getBattleManager();
            final EnemyProfile targetEnemyProfile = battleManager.getEnemyProfile(targetEnemyId.get());
            if (targetEnemyProfile == null) {
                // 全体スキャンを優先する
                gunAction = Gunner.Action.SCAN;
                return;
            }
            final EnemyState latesEnemyState = targetEnemyProfile.getLatestState();
            if (latesEnemyState == null) {
                // 全体スキャンを優先する
                gunAction = Gunner.Action.SCAN;
                return;
            }
            if (latesEnemyState.energy <= 0) {
                // 敵のエネルギーが0以下なら止めを刺します
                gunAction = Gunner.Action.EXECUTION;
                return;
            }
            gunAction = Gunner.Action.AUTO;
            return;
        }

        // 全体スキャンを行う
        gunAction = Gunner.Action.SCAN;
    }

    @Override
    protected void setRadarActionName(OkuRunBot bot) {
        if (targetEnemyId.get() != Commander.NO_TARGET) {
            final BattleManager battleManager = bot.getBattleManager();
            final EnemyProfile targetEnemyProfile = battleManager.getEnemyProfile(targetEnemyId.get());
            if (targetEnemyProfile == null) {
                // 全体スキャンをする
                radarAction = RadarOperator.Action.ALL_SCAN;
                return;
            }
            final Predictor predictor = bot.getPredictor();
            final EnemyState predictedEnemyState = predictor.predict(bot, targetEnemyProfile, bot.getTurnNumber());
            if (predictedEnemyState != null) {
                // ターゲットの位置を探る
                radarAction = RadarOperator.Action.TARGET_SCAN;
                return;
            }
        }
        radarAction = RadarOperator.Action.ALL_SCAN;
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
    public HandlePriority getHandlePriority(OkuRunBot bot) {
        return HandlePriority.AVOID_BULLET;
    }

    @Override
    public AccelePriority getAccelePriority(OkuRunBot bot) {
        return AccelePriority.MAX_SPEED;
    }

    @Override
    public double getMinSpeed(OkuRunBot bot) {
        return 2;
    }

    /**
     * 弾丸が自分に当たった時の処理
     * 
     * @param e   弾丸が自分に当たったイベント
     * @param bot ボット
     */
    @Override
    public void onHitByBullet(HitByBulletEvent e, OkuRunBot bot) {
        targetEnemyId.set(e.getBullet().getOwnerId());
    }
}
