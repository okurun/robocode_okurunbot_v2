package okurun.battlemanager;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import okurun.OkuRunBot;

/**
 * 敵ボットのプロファイル
 */
public class EnemyProfile {
    private final int id;
    private final Deque<EnemyState> stateHistory = new ConcurrentLinkedDeque<>();

    private final AtomicBoolean isAlive = new AtomicBoolean(true);
    private final AtomicInteger lastConfirmedTurn = new AtomicInteger(0);

    public EnemyProfile(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public boolean isAlive() {
        return isAlive.get();
    }

    /**
     * 生存していて、一定期間観測出来ているか判定します
     * 
     * @param bot Bot
     * @return 生存していて、一定期間観測出来ている場合 → true
     */
    public boolean isAliveAndNotMissing(OkuRunBot bot) {
        if (!isAlive.get()) {
            // 既に死亡している
            return false;
        }
        final int missingTurnNum = bot.getTurnNumber() - lastConfirmedTurn.get();
        if (missingTurnNum > 100) {
            // 長時間生存確認出来ていない
            return false;
        }
        final EnemyState latestState = getLatestState();
        if (latestState != null && latestState.energy <= 0 && missingTurnNum > 30) {
            // エネルギーが0の状態でさらに一定ターン生存確認出来ていない → 死亡と判定
            return false;
        }
        return true;
    }

    public void died() {
        isAlive.set(false);
    }

    public int getLastConfirmedTurn() {
        return lastConfirmedTurn.get();
    }

    public void setLastConfirmedTurn(int lastConfirmedTurn) {
        this.lastConfirmedTurn.set(lastConfirmedTurn);
    }

    public void addState(EnemyState state) {
        stateHistory.addFirst(state);
        if (stateHistory.size() > 30) {
            stateHistory.removeLast();
        }
        lastConfirmedTurn.set(state.scandTurnNum);
    }

    public Deque<EnemyState> getStateHistory() {
        // Immutableにするため LinkedList でラップして返す
        return new LinkedList<>(stateHistory);
    }

    public EnemyState getLatestState() {
        if (stateHistory.isEmpty()) {
            return null;
        }
        return stateHistory.getFirst();
    }
}
