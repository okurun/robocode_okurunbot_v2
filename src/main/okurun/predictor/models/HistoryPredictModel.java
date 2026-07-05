package okurun.predictor.models;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import okurun.OkuRunBot;
import okurun.battlemanager.EnemyState;
import okurun.predictor.Predictor;

public class HistoryPredictModel extends PredictModel {
    private static final int LIMIT_TURN_NUM = 20;
    private static final int DETECT_ZIGZAG_TURN_CHANGE_NUM = 3;
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
        final List<EnemyState> moveHistories = getMoveHistories(bot, enemyState.id, stateHistory);
        if (moveHistories.size() < DETECT_ZIGZAG_TURN_CHANGE_NUM) {
            return null;
        }
        final int turnNumDiff = enemyState.scandTurnNum - moveHistories.getLast().scandTurnNum;
        if (turnNumDiff <= 0) {
            return new EnemyState(enemyState.id, enemyState.scandTurnNum + 1, enemyState.x, enemyState.y, enemyState.heading,
                    enemyState.velocity, enemyState.energy, enemyState.turnDegree, enemyState.acceleration,
                    enemyState.distance);
        }
        final int historyPos = (turnNumDiff - 1) % moveHistories.size();
        for (int i = 0; i < turnNumDiff; i++) {
            moveHistories.addFirst(moveHistories.removeLast());
        }
        final EnemyState moveHistory = moveHistories.get(historyPos);

        final double[] predictedPos = Predictor.calcPosition(enemyState.x, enemyState.y, enemyState.heading,
                moveHistory.velocity, moveHistory.turnDegree, 1);
        return new EnemyState(enemyState.id, enemyState.scandTurnNum + 1, predictedPos[0], predictedPos[1],
                enemyState.heading + enemyState.turnDegree, moveHistory.velocity, enemyState.energy,
                enemyState.turnDegree, moveHistory.velocity - enemyState.velocity, -1);
    }

    @SuppressWarnings("unchecked")
    private List<EnemyState> getMoveHistories(OkuRunBot bot, int enemyId, Deque<EnemyState> stateHistory) {
        if (caches.containsKey("moveHistories" + enemyId)) {
            return (List<EnemyState>) caches.get("moveHistories" + enemyId);
        }
        Turn prevTurn = null; // 前回の旋回方向
        int turnChangeCnt = 0; // 旋回が逆転した回数
        int firstTurnChangeTurnNum = 0; // 最初に旋回した時までのターン数
        final List<EnemyState> history = new ArrayList<>();
        for (EnemyState state: stateHistory) {
            if (bot.getTurnNumber() - state.scandTurnNum > LIMIT_TURN_NUM) {
                // LIMIT_TURN_NUM ターン以上昔の状態は考慮しない
                return List.of();
            }

            final Turn turn = Turn.get(state.turnDegree);
            if (prevTurn == null) {
                prevTurn = turn;
            } else if (turn != Turn.STRAIGHT && turn != prevTurn) {
                // 旋回が逆転
                prevTurn = turn;
                if (turnChangeCnt == 0) {
                    firstTurnChangeTurnNum++;
                }
                turnChangeCnt++;
            }
            if (turnChangeCnt >= DETECT_ZIGZAG_TURN_CHANGE_NUM) {
                // 旋回方向が DETECT_ZIGZAG_TURN_CHANGE_NUM 回逆転したらジグザクに動いていると仮定しそこまでの動きを記録
                break;
            }
            history.addFirst(state);
        }
        // 最初の旋回が始まるまでと同じターン数historyの末尾を削除
        for (int i = 0; i < firstTurnChangeTurnNum; i++) {
            history.removeFirst();
        }
        caches.put("moveHistories" + enemyId, history);
        return history;
    }

    public static boolean canUse(OkuRunBot bot, Deque<EnemyState> stateHistory) {
        if (stateHistory.size() > DETECT_ZIGZAG_TURN_CHANGE_NUM) {
            Turn prevTurn = null; // 前回の旋回方向
            int turnChangeCnt = 0; // 旋回が逆転した回数
            for (EnemyState state: stateHistory) {
                if (bot.getTurnNumber() - state.scandTurnNum > LIMIT_TURN_NUM) {
                    // LIMIT_TURN_NUM ターン以上昔の状態は考慮しない
                    return false;
                }
                final Turn turn = Turn.get(state.turnDegree);
                if (prevTurn == null) {
                    prevTurn = turn;
                } else if (turn != Turn.STRAIGHT && turn != prevTurn) {
                    // 旋回方向が逆転
                    prevTurn = turn;
                    turnChangeCnt++;
                }
                if (turnChangeCnt >= DETECT_ZIGZAG_TURN_CHANGE_NUM) {
                    // 旋回方向が DETECT_ZIGZAG_TURN_CHANGE_NUM 回逆転したらジグザクに動いていると判断
                    return true;
                }
            }
            return false;
        }
        return false;
    }
}
