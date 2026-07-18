package okurun.predictor.models;

import dev.robocode.tankroyale.botapi.graphics.Color;
import okurun.OkuRunBot;
import okurun.enemymanager.EnemyProfile;
import okurun.enemymanager.EnemyState;
import okurun.predictor.Predictor;
import okurun.predictor.Predictor.PredictModelId;

/**
 * 予測モデル：等速直線運動のみ
 */
public class SimplePredictModel extends AbstractPredictModel {
    /**
     * このモデルのIDを取得する
     * 
     * @return モデルID
     */
    @Override
    public PredictModelId getId() {
        return PredictModelId.SIMPLE;
    }

    /**
     * モデルの色を取得する
     * 
     * @return モデルの色
     */
    @Override
    public Color getColor() {
        return Color.BLUE;
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

        double velocity = enemyState.velocity;
        
        final double[] predictedPos = Predictor.calcPosition(enemyState.x, enemyState.y, enemyState.heading,
                velocity, 0, 1);
        final EnemyState predictedEnemyState = new EnemyState(enemyState.id, enemyState.scannedTurnNum + 1, predictedPos[0], predictedPos[1],
                enemyState.heading, velocity, enemyState.energy,
                0, 0, enemyState.distance);
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
        return enemyProfile.getStateHistory().size() > 0;
    }

}
