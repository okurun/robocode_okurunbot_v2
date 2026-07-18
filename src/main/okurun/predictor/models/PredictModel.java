package okurun.predictor.models;

import dev.robocode.tankroyale.botapi.events.*;
import dev.robocode.tankroyale.botapi.graphics.Color;
import okurun.OkuRunBot;
import okurun.enemymanager.EnemyProfile;
import okurun.enemymanager.EnemyState;
import okurun.predictor.PredictModelAccuracy;
import okurun.predictor.Predictor.PredictModelId;

public interface PredictModel {
    /**
     * このモデルのIDを取得する
     * 
     * @return モデルID
     */
    PredictModelId getId();

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
     * @param enemyProfile 敵プロファイル
     * @return 次ターンの敵の状態
     */
    EnemyState nextTurnState(OkuRunBot bot, EnemyState enemyState, EnemyProfile enemyProfile);

    /**
     * 指定された敵をこのモデルで予測できるかどうかを判定する
     * 
     * @param bot          ボット
     * @param enemyProfile 敵プロファイル
     * @return trueなら予測できる
     */
    boolean canPredict(OkuRunBot bot, EnemyProfile enemyProfile);

    /**
     * アクションの前に実行される処理
     */
    void preAction();

    /**
     * このモデルの命中率を取得する
     * 
     * @return 命中率
     */
    PredictModelAccuracy getAccuracy();

    /**
     * このモデルの累計命中率を取得する
     * 
     * @return 累計命中率
     */
    PredictModelAccuracy getTotalAccuracy();

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
