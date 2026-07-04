package okurun.predictor.models;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import dev.robocode.tankroyale.botapi.graphics.Color;
import okurun.OkuRunBot;
import okurun.battlemanager.EnemyState;
import okurun.predictor.Predictor;

public class HistoryPredictModel extends PredictModel {
    private static enum Turn {
        LEFT, RIGHT, STRAIGHT;

        public static Turn get(double angle) {
            if (angle > 0) {
                return LEFT;
            } else if (angle < 0) {
                return RIGHT;
            } else {
                return STRAIGHT;
            }
        }
    }

    @Override
    public EnemyState nextTurnState(OkuRunBot bot, EnemyState enemyState, Deque<EnemyState> stateHistory) {
        bot.setGunColor(Color.BLUE);
        final List<EnemyState> moveHistories = getMoveHistories(enemyState.id, stateHistory);
        if (moveHistories.size() < 2) {
            return null;
        }
        final int turnNumDiff = enemyState.scandTurnNum - moveHistories.getLast().scandTurnNum;
        if (turnNumDiff <= 0) {
            return enemyState;
        }
        final int historyPos = (turnNumDiff - 1) % moveHistories.size();
        final EnemyState moveHistory = moveHistories.get(historyPos);

        final double[] predictedPos = Predictor.calcPosition(enemyState.x, enemyState.y, enemyState.heading,
                moveHistory.velocity, moveHistory.turnDegree, 1);
        return new EnemyState(enemyState.id, enemyState.scandTurnNum + 1, predictedPos[0], predictedPos[1],
                enemyState.heading + enemyState.turnDegree, moveHistory.velocity, enemyState.energy,
                enemyState.turnDegree, moveHistory.velocity - enemyState.velocity, -1);
    }

    @SuppressWarnings("unchecked")
    private List<EnemyState> getMoveHistories(int enemyId, Deque<EnemyState> stateHistory) {
        if (caches.containsKey("moveHistories" + enemyId)) {
            return (List<EnemyState>) caches.get("moveHistories" + enemyId);
        }
        Turn prevTurn = null; // 前回の旋回方向
        int turnNumCnt = 0; // 旋回が逆転した回数
        int firstTurnNumCnt = 0; // 最初に旋回した時のターン番号
        final List<EnemyState> history = new ArrayList<>();
        for (EnemyState state: stateHistory) {
            final Turn turn = Turn.get(state.turnDegree);
            if (prevTurn != null && turn != Turn.STRAIGHT && turn != prevTurn) {
                // 旋回が逆転
                prevTurn = turn;
                if (turnNumCnt == 0) {
                    firstTurnNumCnt++;
                }
                turnNumCnt++;
            }
            if (turnNumCnt >= 3) {
                // 旋回方向が3回逆転したらジグザクに動いていると仮定しそこまでの動きを記録
                break;
            }
            history.addFirst(state);
        }
        // 最初の旋回が始まるまでと同じターン数historyの末尾を削除
        for (int i = 0; i < firstTurnNumCnt; i++) {
            history.removeFirst();
        }
        caches.put("moveHistories" + enemyId, history);
        return history;
    }

    public static boolean canUse(OkuRunBot bot, Deque<EnemyState> stateHistory) {
        if (stateHistory.size() > 3) {
            int prevScandTurnNum = bot.getTurnNumber();
            Turn prevTurn = null; // 前回の旋回方向
            int turnNumCnt = 0; // 旋回が逆転した回数
            for (EnemyState state: stateHistory) {
                if (state.scandTurnNum == prevScandTurnNum) {
                    continue;
                }
                if (state.scandTurnNum + 1 != prevScandTurnNum) {
                    return false;
                }
                final Turn turn = Turn.get(state.turnDegree);
                if (prevTurn != null && turn != Turn.STRAIGHT && turn != prevTurn) {
                    // 旋回が逆転
                    prevTurn = turn;
                    turnNumCnt++;
                }
                if (turnNumCnt >= 3) {
                    // 旋回方向が3回逆転したらジグザクに動いていると仮定
                    return true;
                }
                prevScandTurnNum = state.scandTurnNum;
            }
            return false;
        }
        return false;
    }
}
