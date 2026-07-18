package okurun.gunner.actions;

import okurun.OkuRunBot;
import okurun.enemymanager.EnemyProfile;
import okurun.enemymanager.EnemyState;
import okurun.gunner.Gunner;
import okurun.predictor.Predictor;
import okurun.predictor.Predictor.PredictModelId;

public interface GunAction {
    public static final int LIMIT_TURNS = 30;

    public Gunner.ActionId action(OkuRunBot bot);

    /**
     * 画面に円を描画します
     * ※ 描画にはUI画面でDebug Graphicsを有効にする必要があります
     * 
     * @param bot        ボット
     * @param fireTarget 射撃目標位置
     */
    public static void drawCircle(OkuRunBot bot, EnemyState fireTarget) {
        bot.getDebugger().drawFillCircle(bot, fireTarget.getPosition(), 5,
                bot.getPredictor().getPredictModel(bot).getColor());
    }

    /**
     * 射撃目標位置を計算します
     * 
     * @param bot                ボット
     * @param targetEnemyProfile 敵プロファイル
     * @param bulletPower        弾丸のパワー
     * @return 射撃目標位置
     */
    public static EnemyState getFireTarget(OkuRunBot bot, EnemyProfile targetEnemyProfile, double firePower) {
        final EnemyState latestEnemyState = targetEnemyProfile.getLatestState();
        if (latestEnemyState == null) {
            return null;
        }
        if (bot.getCommander().getPredictModelId(bot) == PredictModelId.NONE) {
            // NonePredictModelの場合は単純に最新位置と同じ位置で返す
            return new EnemyState(latestEnemyState.id, latestEnemyState.scannedTurnNum + 1,
                    latestEnemyState.x, latestEnemyState.y,
                    latestEnemyState.heading,
                    latestEnemyState.velocity, latestEnemyState.energy, latestEnemyState.turnDegree,
                    latestEnemyState.acceleration,
                    latestEnemyState.distance);
        }
        final Predictor predictor = bot.getPredictor();
        final double bulletSpeed = bot.calcBulletSpeed(firePower);
        int turnCnt = 0;

        EnemyState predictedState = null;
        EnemyState prevState = latestEnemyState;
        while (turnCnt < LIMIT_TURNS) {
            predictedState = predictor.predict(bot, targetEnemyProfile, prevState.scannedTurnNum + 1);
            if (predictedState == null) {
                return null;
            }
            prevState = predictedState;
            if (predictedState.scannedTurnNum <= bot.getTurnNumber()) {
                continue;
            }
            turnCnt++;
            if (predictedState.scannedTurnNum + turnCnt <= bot.getTurnNumber()) {
                continue;
            }
            final double distance = bot.distanceTo(predictedState.getPosition());
            if (distance == 0) {
                break;
            }
            if (distance <= ((predictedState.scannedTurnNum + turnCnt) - bot.getTurnNumber()) * bulletSpeed) {
                break;
            }
        }
        if (turnCnt >= LIMIT_TURNS) {
            return null;
        }
        return predictedState;
    }
}
