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

    /**
     * 敵ボットのIDを返します
     * 
     * @return 敵ボットのID
     */
    public int getId() {
        return id;
    }

    /**
     * 生存しているかどうかを返します
     * 
     * @return 生存している場合はtrue、そうでない場合はfalse
     */
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
        if (latestState != null && latestState.energy <= 0 && missingTurnNum > 10) {
            // エネルギーが0の状態でさらに一定ターン生存確認出来ていない → 死亡と判定
            return false;
        }
        return true;
    }

    /**
     * 敵ボットが死亡したことを設定します
     */
    public void died() {
        isAlive.set(false);
    }

    /**
     * 最後に生存が確認されたターン数を返します
     * 
     * @return 最後に生存が確認されたターン数
     */
    public int getLastConfirmedTurn() {
        return lastConfirmedTurn.get();
    }

    /**
     * 最後に生存が確認されたターン数を設定します
     * 
     * @param lastConfirmedTurn 最後に生存が確認されたターン数
     */
    public void setLastConfirmedTurn(int lastConfirmedTurn) {
        this.lastConfirmedTurn.set(lastConfirmedTurn);
    }

    /**
     * 敵ボットの状態を追加します
     * 
     * @param state 敵ボットの状態
     */
    public void addState(EnemyState state) {
        stateHistory.addFirst(state);
        if (stateHistory.size() > 30) {
            stateHistory.removeLast();
        }
        lastConfirmedTurn.set(state.scannedTurnNum);
    }

    /**
     * 敵ボットの状態履歴を返します
     * 
     * @return 敵ボットの状態履歴
     */
    public Deque<EnemyState> getStateHistory() {
        // Immutableにするため LinkedList でラップして返す
        return new LinkedList<>(stateHistory);
    }

    /**
     * 最新の敵ボットの状態を返します
     * 
     * @return 最新の敵ボットの状態
     */
    public EnemyState getLatestState() {
        if (stateHistory.isEmpty()) {
            return null;
        }
        return stateHistory.getFirst();
    }
}
