package okurun.predictor.models;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import dev.robocode.tankroyale.botapi.Constants;
import dev.robocode.tankroyale.botapi.graphics.Color;
import okurun.OkuRunBot;
import okurun.enemymanager.EnemyProfile;
import okurun.enemymanager.EnemyState;
import okurun.predictor.Predictor;
import okurun.predictor.Predictor.PredictModelId;

/**
 * ジグザグ走行を再現して予測するモデル
 */
public class ZigzagPredictModel extends AbstractPredictModel {
    private static final int LIMIT_TURN_NUM = 20;
    private static final int DETECT_ZIGZAG_TURN_CHANGE_NUM = 5;

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

    /**
     * このモデルのIDを取得する
     * 
     * @return モデルID
     */
    @Override
    public PredictModelId getId() {
        return PredictModelId.ZIGZAG;
    }

    /**
     * モデルの色を取得する
     * 
     * @return モデルの色
     */
    @Override
    public Color getColor() {
        return Color.RED;
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
        final Deque<EnemyState> stateHistory = enemyProfile.getStateHistory();
        final List<EnemyState> moveHistories = getMoveHistories(bot, enemyState.id, stateHistory);
        if (moveHistories.size() < DETECT_ZIGZAG_TURN_CHANGE_NUM) {
            return null;
        }

        final String cacheName = String.format("nextTurnState_%d_%d", enemyState.id, enemyState.scannedTurnNum);
        if (caches.containsKey(cacheName)) {
            return (EnemyState) caches.get(cacheName);
        }

        final int turnNumDiff = enemyState.scannedTurnNum - moveHistories.getLast().scannedTurnNum;
        if (turnNumDiff <= 0) {
            return new EnemyState(enemyState.id, enemyState.scannedTurnNum + 1, enemyState.x, enemyState.y,
                    enemyState.heading,
                    enemyState.velocity, enemyState.energy, enemyState.turnDegree, enemyState.acceleration,
                    enemyState.distance);
        }
        final int historyPos = (turnNumDiff - 1) % moveHistories.size();
        final EnemyState moveHistory = moveHistories.get(historyPos);

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

    @SuppressWarnings("unchecked")
    private List<EnemyState> getMoveHistories(OkuRunBot bot, int enemyId, Deque<EnemyState> stateHistory) {
        if (caches.containsKey("moveHistories" + enemyId)) {
            return (List<EnemyState>) caches.get("moveHistories" + enemyId);
        }
        Turn prevTurn = null; // 前回の旋回方向
        int turnChangeCnt = 0; // 旋回が逆転した回数
        int firstTurnChangeTurnNum = 0; // 最初に旋回した時までのターン数
        final List<EnemyState> history = new ArrayList<>();
        for (EnemyState state : stateHistory) {
            if (bot.getTurnNumber() - state.scannedTurnNum > LIMIT_TURN_NUM) {
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

    /**
     * 指定された敵をこのモデルで予測できるかどうかを判定する
     * 
     * @param bot          ボット
     * @param enemyProfile 敵プロファイル
     * @return trueなら予測できる
     */
    public boolean canPredict(OkuRunBot bot, EnemyProfile enemyProfile) {
        final Deque<EnemyState> stateHistory = enemyProfile.getStateHistory();
        if (stateHistory.size() > DETECT_ZIGZAG_TURN_CHANGE_NUM) {
            int prevTurnNumber = 0;
            Turn prevTurn = null; // 前回の旋回方向
            int turnChangeCnt = 0; // 旋回が逆転した回数
            for (EnemyState state : stateHistory) {
                if (prevTurnNumber == 0) {
                    prevTurnNumber = state.scannedTurnNum;
                } else if (prevTurnNumber - 1 != state.scannedTurnNum) {
                    // ターンが飛んでいる
                    return false;
                } else {
                    prevTurnNumber = state.scannedTurnNum;
                }
                if (bot.getTurnNumber() - state.scannedTurnNum > LIMIT_TURN_NUM) {
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
