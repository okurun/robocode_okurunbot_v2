package okurun.predictor.models;

import okurun.OkuRunBot;
import okurun.battlemanager.EnemyState;

import java.util.Deque;

import dev.robocode.tankroyale.botapi.graphics.Color;

/**
 * 予測を行わず、単純に最後の観測値を返すモデル
 */
public class NoPredictPredictModel extends AbstractPredictModel {
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
     * @param stateHistory 敵の状態履歴
     * @return 次ターンの敵の状態
     */
    @Override
    public EnemyState nextTurnState(OkuRunBot bot, EnemyState enemyState, Deque<EnemyState> stateHistory) {
        return new EnemyState(enemyState.id, enemyState.scannedTurnNum + 1, enemyState.x, enemyState.y,
                enemyState.heading,
                enemyState.velocity, enemyState.energy, enemyState.turnDegree, enemyState.acceleration,
                enemyState.distance);
    }

}
