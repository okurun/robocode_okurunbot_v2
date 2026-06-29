package okurun.predictor.models;

import java.util.Deque;

import okurun.OkuRunBot;
import okurun.battlemanager.EnemyState;

public abstract class PredictModel {
    abstract public EnemyState nextTurnState(OkuRunBot bot, EnemyState enemyState, Deque<EnemyState> stateHistory);
}
