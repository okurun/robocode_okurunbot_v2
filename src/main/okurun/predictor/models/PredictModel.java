package okurun.predictor.models;

import java.util.Deque;

import dev.robocode.tankroyale.botapi.events.*;
import dev.robocode.tankroyale.botapi.graphics.Color;
import okurun.OkuRunBot;
import okurun.battlemanager.EnemyState;

public interface PredictModel {
    /**
     * モデルの色を取得する
     * 
     * @return モデルの色
     */
    Color getColor();

    /**
     * 次ターンの敵の状態を予測する
     * 
     * @param bot          ボット
     * @param enemyState   敵の状態
     * @param stateHistory 敵の状態履歴
     * @return 次ターンの敵の状態
     */
    EnemyState nextTurnState(OkuRunBot bot, EnemyState enemyState, Deque<EnemyState> stateHistory);

    /**
     * アクションの前に実行される処理
     */
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
