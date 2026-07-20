package okurun.predictor.models;

import java.util.Deque;

import dev.robocode.tankroyale.botapi.Constants;
import dev.robocode.tankroyale.botapi.graphics.Color;
import okurun.OkuRunBot;
import okurun.enemymanager.EnemyProfile;
import okurun.enemymanager.EnemyState;
import okurun.predictor.Predictor;
import okurun.predictor.Predictor.PredictModelId;

/**
 * 過去の動きを再現することで予測するモデル
 */
public class HistoryPredictModel extends AbstractPredictModel {

    public static final int HISTORY_POS = 30;

    /**
     * {@inheritDoc}
     */
    @Override
    public PredictModelId getId() {
        return PredictModelId.HISTORY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Color getColor() {
        return Color.YELLOW;
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    @Override
    public boolean canPredict(OkuRunBot bot, EnemyProfile enemyProfile) {
        if (enemyProfile.getStateHistory().size() < HISTORY_POS) {
            return false;
        }
        // 連続した履歴でないと予測できない
        int prevTurnNum = 0;
        for (EnemyState enemyState : enemyProfile.getStateHistory()) {
            if (prevTurnNum == 0) {
                prevTurnNum = enemyState.scannedTurnNum;
                continue;
            }
            if (prevTurnNum - 1 != enemyState.scannedTurnNum) {
                return false;
            }
            prevTurnNum = enemyState.scannedTurnNum;
        }
        return true;
    }

}
