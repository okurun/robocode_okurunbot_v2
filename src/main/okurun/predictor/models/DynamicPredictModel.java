package okurun.predictor.models;

import dev.robocode.tankroyale.botapi.Constants;
import dev.robocode.tankroyale.botapi.graphics.Color;
import okurun.OkuRunBot;
import okurun.battlemanager.EnemyProfile;
import okurun.battlemanager.EnemyState;
import okurun.predictor.Predictor;
import okurun.predictor.Predictor.PredictModelId;

/**
 * 予測モデル：等速直線運動 + 定期的な旋回 + 加速/減速
 */
public class DynamicPredictModel extends AbstractPredictModel {
    private static enum AccelertaionType {
        INCREASE,
        DECREASE
    }

    /**
     * このモデルのIDを取得する
     * 
     * @return モデルID
     */
    @Override
    public PredictModelId getId() {
        return PredictModelId.DYNAMIC;
    }

    /**
     * モデルの色を取得する
     * 
     * @return モデルの色
     */
    @Override
    public Color getColor() {
        return Color.GREEN;
    }

    /**
     * 次ターンの敵の状態を予測する
     * 
     * @param bot          ボット
     * @param enemyState   敵の状態
     * @param enemyProfile 敵プロファイル
     * @return 次ターンの敵の状態
     */
    @Override
    public EnemyState nextTurnState(OkuRunBot bot, EnemyState enemyState, EnemyProfile enemyProfile) {
        if (enemyProfile.getStateHistory().size() < 1) {
            return null;
        }

        final String cacheName = String.format("nextTurnState_%d_%d", enemyState.id, enemyState.scannedTurnNum);
        if (caches.containsKey(cacheName)) {
            return (EnemyState) caches.get(cacheName);
        }

        // 加減速を考慮した速度の計算
        AccelertaionType accelertaionType = null;
        double velocity = enemyState.velocity;
        if (enemyState.velocity > 0) {
            if (enemyState.acceleration > 0) {
                velocity += Constants.ACCELERATION;
                accelertaionType = AccelertaionType.INCREASE;
            } else if (enemyState.acceleration < 0) {
                velocity += Constants.DECELERATION;
                accelertaionType = AccelertaionType.DECREASE;
            }
        } else if (enemyState.velocity < 0) {
            if (enemyState.acceleration > 0) {
                velocity -= Constants.DECELERATION;
                accelertaionType = AccelertaionType.DECREASE;
            } else if (enemyState.acceleration < 0) {
                velocity -= Constants.ACCELERATION;
                accelertaionType = AccelertaionType.INCREASE;
            }
        }
        // 速度の最大値をチェック
        if (velocity < -Constants.MAX_SPEED) {
            velocity = -Constants.MAX_SPEED;
        } else if (velocity > Constants.MAX_SPEED) {
            velocity = Constants.MAX_SPEED;
        }

        // 旋回角を計算する
        double turnDegree = enemyState.turnDegree;
        double maxTurnRate = bot.calcMaxTurnRate(velocity);
        if (Math.abs(turnDegree) >= maxTurnRate) {
            // 加速しているときは最大旋回角まで旋回角を減らす
            turnDegree = (turnDegree > 0) ? maxTurnRate : -maxTurnRate;
        } else if (accelertaionType == AccelertaionType.DECREASE) {
            if (Math.abs(enemyState.turnDegree) - bot.calcMaxTurnRate(enemyState.velocity) < 1) {
                // 減速しているときは最大旋回角まで旋回角を増やす
                turnDegree = (turnDegree > 0) ? maxTurnRate : -maxTurnRate;
            }
        }

        final double[] predictedPos = Predictor.calcPosition(enemyState.x, enemyState.y, enemyState.heading,
                velocity, turnDegree, 1);
        final EnemyState predictedEnemyState = new EnemyState(enemyState.id, enemyState.scannedTurnNum + 1, predictedPos[0], predictedPos[1],
                enemyState.heading + turnDegree, velocity, enemyState.energy,
                turnDegree, velocity - enemyState.velocity, enemyState.distance);
        caches.put(cacheName, predictedEnemyState);
        return predictedEnemyState;
    }

    /**
     * 指定された敵をこのモデルで予測できるかどうかを判定する
     * 
     * @param bot          ボット
     * @param enemyProfile 敵プロファイル
     * @return trueなら予測できる
     */
    public boolean canPredict(OkuRunBot bot, EnemyProfile enemyProfile) {
        return enemyProfile.getStateHistory().size() >= 2;
    }

}
