package okurun.commander.tactics;

import java.util.List;
import java.util.Map;

import dev.robocode.tankroyale.botapi.Constants;
import dev.robocode.tankroyale.botapi.events.*;
import okurun.OkuRunBot;
import okurun.arenamap.ArenaMap;
import okurun.commander.Commander;
import okurun.commander.Commander.MovePatternId;
import okurun.driver.Driver;
import okurun.enemymanager.EnemyManager;
import okurun.enemymanager.EnemyProfile;
import okurun.gunner.Gunner;
import okurun.predictor.Predictor;
import okurun.predictor.Predictor.PredictModelId;
import okurun.predictor.models.PredictModel;

/**
 * 敵を分析するための戦略
 */
public class AnalysisOneOnOneTactic extends AbstractOneOnOneTactic {

    @Override
    protected void updateMovePatternId(OkuRunBot bot) {
        if (targetEnemyId.get() == Commander.NO_TARGET) {
            movePatternId = MovePatternId.ROUND_AREA;
            return;
        }
        movePatternId = bot.getEnemyManager().getEnemyProfile(targetEnemyId.get()).getMovePatternId();
    }

    @Override
    protected void updateDriveActionId(OkuRunBot bot) {
        final ArenaMap arenaMap = bot.getArenaMap();
        final List<ArenaMap.PotentialCollisionWall> collisionWalls = arenaMap.getPotentialCollisionWalls(bot);
        if (!collisionWalls.isEmpty()) {
            driveActionId = Driver.ActionId.AVOID_WALL;
            return;
        }
        driveActionId = bot.getCommander().getMovePattern(movePatternId).getDependentDriveActionId();
    }

    @Override
    protected void updateGunActionId(OkuRunBot bot) {
        gunActionId = Gunner.ActionId.MAX_POWER;
        baseFirePower = Constants.MIN_FIREPOWER;
        waitForGunTurn = true;
    }

    @Override
    protected void updatePredictModelId(OkuRunBot bot) {
        if (predictModelId == null) {
            predictModelId = PredictModelId.NONE;
        }
        // onBulletFiredでセットするので何もしない
    }

    /**
     * 弾丸が発射された時の処理
     * 
     * @param e   弾丸が発射されたイベント
     * @param bot ボット
     */
    @Override
    public void onBulletFired(BulletFiredEvent e, OkuRunBot bot) {
        // 1発撃つ毎に予測モデルを入れ替える
        final int targetEnemyId = getTargetEnemyId(bot);
        if (targetEnemyId == Commander.NO_TARGET) {
            return;
        }
        final EnemyManager enemyManager = bot.getEnemyManager();
        final EnemyProfile enemyProfile = enemyManager.getEnemyProfile(targetEnemyId);
        final Predictor predictor = bot.getPredictor();
        final Map<PredictModelId, PredictModel> predictModels = predictor.getPredictModels();
        final List<PredictModel> predictModelList = predictModels.values().stream()
                .sorted((a, b) -> a.getAccuracy().getFireCount() - b.getAccuracy().getFireCount()).toList();
        for (PredictModel model : predictModelList) {
            final PredictModelId modelId = model.getId();
            if (modelId == predictModelId) {
                continue;
            }
            if (model.canPredict(bot, enemyProfile)) {
                predictModelId = modelId;
                return;
            }
        }
    }

    /**
     * 弾丸が自分に当たった時の処理
     * 
     * @param e   弾丸が自分に当たったイベント
     * @param bot ボット
     */
    @Override
    public void onHitByBullet(HitByBulletEvent e, OkuRunBot bot) {
    }

}
