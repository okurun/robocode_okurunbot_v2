package okurun.predictor.models;

import okurun.OkuRunBot;
import okurun.battlemanager.EnemyState;

import java.util.Deque;

/**
 * 予測を行わず、単純に最後の観測値を返すモデル
 */
public class NoPredictPredictModel extends AbstractPredictModel {

    @Override
    public EnemyState nextTurnState(OkuRunBot bot, EnemyState enemyState, Deque<EnemyState> stateHistory) {
        return new EnemyState(enemyState.id, enemyState.scannedTurnNum + 1, enemyState.x, enemyState.y,
                enemyState.heading,
                enemyState.velocity, enemyState.energy, enemyState.turnDegree, enemyState.acceleration,
                enemyState.distance);
    }

}
