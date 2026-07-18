package okurun.commander.tactics;

import java.util.List;
import java.util.Map;

import dev.robocode.tankroyale.botapi.Constants;
import dev.robocode.tankroyale.botapi.events.*;
import okurun.OkuRunBot;
import okurun.battlemanager.BattleManager;
import okurun.battlemanager.EnemyProfile;
import okurun.commander.Commander;
import okurun.commander.Commander.MovePatternId;
import okurun.driver.Driver;
import okurun.gunner.Gunner;
import okurun.predictor.Predictor;
import okurun.predictor.Predictor.PredictModelId;
import okurun.predictor.models.PredictModel;

/**
 * 敵を分析するための戦略
 */
public class AnalysisOneOnOneTactic extends AbstractOneOnOneTactic {

    @Override
    protected void setMovePatternId(OkuRunBot bot) {
        if (targetEnemyId.get() == Commander.NO_TARGET) {
            movePatternId = MovePatternId.ROUND_AREA;
            return;
        }
        movePatternId = bot.getBattleManager().getEnemyProfile(targetEnemyId.get()).getMovePatternId();
    }

    @Override
    protected void setDriveActionId(OkuRunBot bot) {
        driveActionId = Driver.ActionId.MOVE_TO;
    }

    @Override
    protected void setGunActionId(OkuRunBot bot) {
        gunActionId = Gunner.ActionId.MAX_POWER;
        baseFirePower = Constants.MIN_FIREPOWER;
        waitForGunTurn = true;
    }

    @Override
    protected void setPredictModelId(OkuRunBot bot) {
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
        final BattleManager battleManager = bot.getBattleManager();
        final EnemyProfile enemyProfile = battleManager.getEnemyProfile(targetEnemyId);
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
