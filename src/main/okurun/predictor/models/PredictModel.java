package okurun.predictor.models;

import java.util.Deque;

import dev.robocode.tankroyale.botapi.events.*;
import okurun.OkuRunBot;
import okurun.battlemanager.EnemyState;

public interface PredictModel {
    EnemyState nextTurnState(OkuRunBot bot, EnemyState enemyState, Deque<EnemyState> stateHistory);

    void preAction();

    /**
     * ゲームが終了した時の処理
     * 
     * @param e   ゲーム終了イベント
     * @param bot ボット
     */
    void onGameEnded(GameEndedEvent e, OkuRunBot bot);

    /**
     * ラウンドが終わった時の処理
     * 
     * @param e   ラウンドが終わったイベント
     * @param bot ボット
     */
    void onRoundEnded(RoundEndedEvent e, OkuRunBot bot);

    /**
     * 弾丸が発射された時の処理
     * 
     * @param bulletFiredEvent 弾丸が発射されたイベント
     * @param bot              ボット
     */
    void onBulletFired(BulletFiredEvent e, OkuRunBot bot);

    /**
     * 弾丸が敵に命中した時の処理
     * 
     * @param bulletHitBotEvent 弾丸が敵に命中したイベント
     * @param bot               ボット
     */
    void onBulletHit(BulletHitBotEvent e, OkuRunBot bot);

    /**
     * 弾丸が弾丸に命中した時の処理
     * 
     * @param bulletHitBulletEvent 弾丸が弾丸に命中したイベント
     * @param bot                  ボット
     */
    void onBulletHitBullet(BulletHitBulletEvent e, OkuRunBot bot);

    /**
     * 弾丸が壁に当たった時の処理
     * 
     * @param e   弾丸が壁に当たったイベント
     * @param bot ボット
     */
    void onBulletHitWall(BulletHitWallEvent e, OkuRunBot bot);
}
