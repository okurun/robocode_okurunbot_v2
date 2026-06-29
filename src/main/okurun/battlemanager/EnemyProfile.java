package okurun.battlemanager;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedDeque;

public class EnemyProfile {
    private final int id;
    private final Deque<EnemyState> stateHistory = new ConcurrentLinkedDeque<>();

    private volatile boolean isAlive = true;

    public EnemyProfile(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void died() {
        System.out.println("EnemyProfile.died(): id=" + id);
        this.isAlive = false;
    }

    public void addState(EnemyState state) {
        stateHistory.addFirst(state);
        if (stateHistory.size() > 30) {
            stateHistory.removeLast();
        }
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
