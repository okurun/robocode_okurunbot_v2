package okurun.predictor.models;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dev.robocode.tankroyale.botapi.events.*;
import okurun.OkuRunBot;
import okurun.predictor.PredictModelAccuracy;

public abstract class AbstractPredictModel implements PredictModel {
    protected final Map<String, Object> caches = new ConcurrentHashMap<>();
    protected PredictModelAccuracy modelAccuracy = new PredictModelAccuracy();
    protected PredictModelAccuracy modelTotalAccuracy = new PredictModelAccuracy();

    public void preAction() {
        caches.clear();
    }

    /**
     * このモデルの命中率を取得する
     * 
     * @return 命中率
     */
    @Override
    public PredictModelAccuracy getAccuracy() {
        return modelAccuracy;
    }

    /**
     * このモデルの累計命中率を取得する
     * 
     * @return 累計命中率
     */
    @Override
    public PredictModelAccuracy getTotalAccuracy() {
        return modelTotalAccuracy;
    }

    /**
     * ゲームが終了した時の処理
     * 
     * @param e ゲーム終了イベント
     * @param bot              ボット
     */
    public void onGameEnded(GameEndedEvent e, OkuRunBot bot) {
        System.out.println(String.format(
            "## TotalPredictModelAccuracy(%s): %s",
            this.getClass().getSimpleName(),
            modelTotalAccuracy.getAccuracyString()
        ));
        modelTotalAccuracy.reset();
    }

    /**
     * ラウンドが終わった時の処理
     * 
     * @param e   ラウンドが終わったイベント
     * @param bot ボット
     */
    public void onRoundEnded(RoundEndedEvent e, OkuRunBot bot) {
        System.out.println(String.format(
            "== PredictModelAccuracy(%s): %s",
            this.getClass().getSimpleName(),
            modelAccuracy.getAccuracyString()
        ));
        modelTotalAccuracy.addFireCount(modelAccuracy.getFireCount());
        modelTotalAccuracy.addHitCount(modelAccuracy.getHitCount());
        modelTotalAccuracy.addMissCount(modelAccuracy.getMissCount());
        modelAccuracy.reset();
    }

    /**
     * 弾丸が発射された時の処理
     * 
     * @param bulletFiredEvent 弾丸が発射されたイベント
     * @param bot              ボット
     */
    public void onBulletFired(BulletFiredEvent e, OkuRunBot bot) {
        modelAccuracy.incrementFireCount();
    }

    /**
     * 弾丸が敵に命中した時の処理
     * 
     * @param bulletHitBotEvent 弾丸が敵に命中したイベント
     * @param bot               ボット
     */
    public void onBulletHit(BulletHitBotEvent e, OkuRunBot bot) {
        modelAccuracy.incrementHitCount();
    }

    /**
     * 弾丸が弾丸に命中した時の処理
     * 
     * @param bulletHitBulletEvent 弾丸が弾丸に命中したイベント
     * @param bot                  ボット
     */
    public void onBulletHitBullet(BulletHitBulletEvent e, OkuRunBot bot) {
        modelAccuracy.incrementMissCount();
    }

    /**
     * 弾丸が壁に当たった時の処理
     * 
     * @param e   弾丸が壁に当たったイベント
     * @param bot ボット
     */
    public void onBulletHitWall(BulletHitWallEvent e, OkuRunBot bot) {
        modelAccuracy.incrementMissCount();
    }
}
