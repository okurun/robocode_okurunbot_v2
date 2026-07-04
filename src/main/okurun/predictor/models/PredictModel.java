package okurun.predictor.models;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okurun.OkuRunBot;
import okurun.battlemanager.EnemyState;

public abstract class PredictModel {
    protected final Map<String, Object> caches = new ConcurrentHashMap<>();

    abstract public EnemyState nextTurnState(OkuRunBot bot, EnemyState enemyState, Deque<EnemyState> stateHistory);

    public void clearCache() {
        caches.clear();
    }
}
