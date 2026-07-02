package okurun.commander.tactics;

import java.util.List;

import dev.robocode.tankroyale.botapi.events.HitByBulletEvent;
import okurun.OkuRunBot;
import okurun.arenamap.ArenaMap;
import okurun.battlemanager.BattleManager;
import okurun.battlemanager.EnemyProfile;
import okurun.battlemanager.EnemyState;
import okurun.commander.Commander;
import okurun.commander.Commander.AccelePriority;
import okurun.commander.Commander.HandlePriority;
import okurun.driver.actions.*;
import okurun.gunner.actions.*;
import okurun.predictor.Predictor;
import okurun.predictor.models.*;
import okurun.radaroperator.actions.*;

public class OneOnOnePositiveTactic implements Tactic {
    private int targetEnemyId = Commander.NO_TARGET;
    private double[] targetMovePosition = null;
    private double baseBulletPower = 2;
    private String predictorModelName = SimplePredictModel.class.getName();
    private String driveActionName = MoveToDriveAction.class.getName();
    private String gunActionName = NormalGunAction.class.getName();
    private String radarActionName = TargetScanRadarAction.class.getName();
    

    @Override
    public void action(OkuRunBot bot) {
        setTargetEnemyId(bot);
        setBaseBulletPower(bot);
        setPredictorModelName(bot);
        setTargetMovePosition(bot);
        setDriveActionName(bot);
        setGunActionName(bot);
        setRadarActionName(bot);
    }

    @Override
    public int getTargetEnemyId(OkuRunBot bot) {
        return targetEnemyId;
    }

    /**
     * ターゲットを設定します
     * 
     * @param bot
     */
    private void setTargetEnemyId(OkuRunBot bot) {
        final BattleManager battleManager = bot.getBattleManager();
        final EnemyProfile alivalEnemy = battleManager.getAlivalEnemy(bot);
        if (alivalEnemy != null) {
            // 敵の位置を把握している
            targetEnemyId = alivalEnemy.getId();
            return;
        }
        targetEnemyId = Commander.NO_TARGET;
    }

    @Override
    public double[] getTargetMovePosition(OkuRunBot bot) {
        return targetMovePosition;
    }

    private void setTargetMovePosition(OkuRunBot bot) {
        if (targetEnemyId == Commander.NO_TARGET) {
            return;
        }

        final BattleManager battleManager = bot.getBattleManager();
        final EnemyProfile targetEnemyProfile = battleManager.getEnemyProfile(targetEnemyId);
        if (targetEnemyProfile == null) {
            return;
        }
        final EnemyState latestEnemyState = targetEnemyProfile.getLatestState();
        if (latestEnemyState == null) {
            return;
        }

        final Predictor predictor = bot.getPredictor();
        EnemyState predictedEnemyState = predictor.predict(bot, targetEnemyProfile, bot.getTurnNumber());
        if (predictedEnemyState == null) {
            predictedEnemyState = latestEnemyState;
        }

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
    public double getBaseBulletPower(OkuRunBot bot) {
        return baseBulletPower;
    }

    private void setBaseBulletPower(OkuRunBot bot) {
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
    public String getPredictorModelName(OkuRunBot bot) {
        return predictorModelName;
    }

    private void setPredictorModelName(OkuRunBot bot) {
        predictorModelName = SimplePredictModel.class.getName();
    }

    @Override
    public String getGunActionName(OkuRunBot bot) {
        return gunActionName;
    }

    private void setGunActionName(OkuRunBot bot) {
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
    public String getRadarActionName(OkuRunBot bot) {
        return radarActionName;
    }

    private void setRadarActionName(OkuRunBot bot) {
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
    public String getDriveActionName(OkuRunBot bot) {
        return driveActionName;
    }

    private void setDriveActionName(OkuRunBot bot) {
        final ArenaMap arenaMap = bot.getArenaMap();
        final List<ArenaMap.PotentialCollisionWall> collisionWalls = arenaMap.getPotentialCollisionWalls(bot);
        if (!collisionWalls.isEmpty()) {
            driveActionName = AvoidWallDriveAction.class.getName();
            return;
        }
        driveActionName = MoveToDriveAction.class.getName();
    }

    @Override
    public HandlePriority getHandlePriority(OkuRunBot bot) {
        return HandlePriority.TARGET;
    }

    @Override
    public AccelePriority getAccelePriority(OkuRunBot bot) {
        return AccelePriority.MAX_SPEED;
    }

    @Override
    public double getMinSpeed(OkuRunBot bot) {
        return 6;
    }

    @Override
    public void onHitByBullet(HitByBulletEvent hitByBulletEvent) {
    }
}
