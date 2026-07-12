package okurun.predictor.models;

import java.util.Deque;

import dev.robocode.tankroyale.botapi.Constants;
import dev.robocode.tankroyale.botapi.graphics.Color;
import okurun.OkuRunBot;
import okurun.battlemanager.EnemyState;
import okurun.predictor.Predictor;

/**
 * 予測モデル：等速直線運動 + 定期的な旋回 + 加速/減速
 */
public class DynamicPredictModel extends AbstractPredictModel {
    /**
     * モデルの色を取得する
     * 
     * @return モデルの色
     */
    @Override
    public Color getColor() {
        return Color.WHITE;
    }

    /**
     * 次ターンの敵の状態を予測する
     * 
     * @param bot          ボット
     * @param enemyState   敵の状態
     * @param stateHistory 敵の状態履歴
     * @return 次ターンの敵の状態
     */
    @Override
    public EnemyState nextTurnState(OkuRunBot bot, EnemyState enemyState, Deque<EnemyState> stateHistory) {
        if (stateHistory.size() < 1) {
            return null;
        }

        final String cacheName = String.format("nextTurnState_%d_%d", enemyState.id, enemyState.scannedTurnNum);
        if (caches.containsKey(cacheName)) {
            return (EnemyState) caches.get(cacheName);
        }

        double velocity = enemyState.velocity;
        if (enemyState.velocity > 0) {
            if (enemyState.acceleration > 0) {
                velocity += Constants.ACCELERATION;
            } else if (enemyState.acceleration < 0) {
                velocity += Constants.DECELERATION;
            }
        } else if (enemyState.velocity < 0) {
            if (enemyState.acceleration > 0) {
                velocity -= Constants.DECELERATION;
            } else if (enemyState.acceleration < 0) {
                velocity -= Constants.ACCELERATION;
            }
        }
        // 速度の最大値をチェック
        if (velocity < -Constants.MAX_SPEED) {
            velocity = -Constants.MAX_SPEED;
        } else if (velocity > Constants.MAX_SPEED) {
            velocity = Constants.MAX_SPEED;
        }

        final double[] predictedPos = Predictor.calcPosition(enemyState.x, enemyState.y, enemyState.heading,
                velocity, enemyState.turnDegree, 1);
        final EnemyState predictedEnemyState = new EnemyState(enemyState.id, enemyState.scannedTurnNum + 1, predictedPos[0], predictedPos[1],
                enemyState.heading + enemyState.turnDegree, velocity, enemyState.energy,
                enemyState.turnDegree, velocity - enemyState.velocity, enemyState.distance);
        caches.put(cacheName, predictedEnemyState);
        return predictedEnemyState;
    }
}
