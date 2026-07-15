package okurun.predictor.models;

import java.util.Deque;

import dev.robocode.tankroyale.botapi.Constants;
import dev.robocode.tankroyale.botapi.graphics.Color;
import okurun.OkuRunBot;
import okurun.battlemanager.EnemyProfile;
import okurun.battlemanager.EnemyState;
import okurun.predictor.Predictor;
import okurun.predictor.Predictor.PredictModelId;

public class HistoryPredictModel extends AbstractPredictModel {
    public static final int HISTORY_POS = 30;

    /**
     * このモデルのIDを取得する
     * 
     * @return モデルID
     */
    @Override
    public PredictModelId getId() {
        return PredictModelId.HISTORY;
    }

    /**
     * モデルの色を取得する
     * 
     * @return モデルの色
     */
    @Override
    public Color getColor() {
        return Color.YELLOW;
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
        final Deque<EnemyState> stateHistory = enemyProfile.getStateHistory(HISTORY_POS);
        if (stateHistory.size() < HISTORY_POS) {
            return null;
        }

        final String cacheName = String.format("nextTurnState_%d_%d", enemyState.id, enemyState.scannedTurnNum);
        if (caches.containsKey(cacheName)) {
            return (EnemyState) caches.get(cacheName);
        }

        final int diff = enemyState.scannedTurnNum - stateHistory.getFirst().scannedTurnNum + 1;
        final int pos = diff % HISTORY_POS;
        final EnemyState[] historyArray = stateHistory.reversed().toArray(new EnemyState[0]);
        EnemyState moveHistory = historyArray[pos];
        final double[] predictedPos = Predictor.calcPosition(enemyState.x, enemyState.y, enemyState.heading,
                moveHistory.velocity + moveHistory.acceleration, moveHistory.turnDegree, 1);
        final EnemyState predictedEnemyState = new EnemyState(enemyState.id, enemyState.scannedTurnNum + 1,
                predictedPos[0], predictedPos[1],
                enemyState.heading + moveHistory.turnDegree,
                Math.min(Math.min(moveHistory.acceleration + enemyState.velocity, Constants.MAX_SPEED),
                        -Constants.MAX_SPEED),
                enemyState.energy,
                moveHistory.turnDegree, moveHistory.acceleration, enemyState.distance);
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
        return enemyProfile.getStateHistory().size() >= HISTORY_POS;
    }

}
