package okurun.commander.tactics;

import java.util.List;
import java.util.Map;

import dev.robocode.tankroyale.botapi.Constants;
import dev.robocode.tankroyale.botapi.events.*;
import okurun.OkuRunBot;
import okurun.commander.Commander;
import okurun.commander.Commander.MovePatternId;
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

    /**
     * {@inheritDoc}
     */
    @Override
    protected void updateMovePatternId(OkuRunBot bot) {
        if (targetEnemyId.get() == Commander.NO_TARGET) {
            movePatternId = MovePatternId.ROUND_AREA;
            return;
        }
        movePatternId = bot.getEnemyManager().getEnemyProfile(targetEnemyId.get()).getMovePatternId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void updateGunActionId(OkuRunBot bot) {
        gunActionId = Gunner.ActionId.MAX_POWER;
        baseFirePower = Constants.MIN_FIREPOWER;
        waitForGunTurn = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void updatePredictModelId(OkuRunBot bot) {
        if (predictModelId == null) {
            predictModelId = PredictModelId.NONE;
        }
        // onBulletFiredでセットするので何もしない
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    @Override
    public void onHitByBullet(HitByBulletEvent e, OkuRunBot bot) {
    }

}
