package okurun.predictor.models;

import java.util.Deque;

import dev.robocode.tankroyale.botapi.Constants;
import okurun.OkuRunBot;
import okurun.battlemanager.EnemyState;
import okurun.predictor.Predictor;

/**
 * 予測モデル：等速直線運動 + 定期的な旋回 + 加速/減速
 */
public class SimplePredictModel extends PredictModel {
    @Override
    public EnemyState nextTurnState(OkuRunBot bot, EnemyState enemyState, Deque<EnemyState> stateHistory) {
        if (stateHistory.size() < 1) {
            return null;
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
        return new EnemyState(enemyState.id, enemyState.scannedTurnNum + 1, predictedPos[0], predictedPos[1],
                enemyState.heading + enemyState.turnDegree, velocity, enemyState.energy,
                enemyState.turnDegree, velocity - enemyState.velocity, enemyState.distance);
    }
}
