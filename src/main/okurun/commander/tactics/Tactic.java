package okurun.commander.tactics;

import dev.robocode.tankroyale.botapi.events.*;
import okurun.OkuRunBot;
import okurun.commander.Commander.MovePatternId;
import okurun.driver.Driver;
import okurun.gunner.Gunner;
import okurun.predictor.Predictor.PredictModelId;
import okurun.radaroperator.RadarOperator;

public interface Tactic {
    int getTargetEnemyId(OkuRunBot bot);

    MovePatternId getMovePatternId(OkuRunBot bot);

    double getBaseFirePower(OkuRunBot bot);

    boolean getWaitForGunTurn(OkuRunBot bot);

    PredictModelId getPredictModelId(OkuRunBot bot);

    Gunner.ActionId getGunActionId(OkuRunBot bot);

    RadarOperator.ActionId getRadarActionId(OkuRunBot bot);

    Driver.ActionId getDriveActionId(OkuRunBot bot);

    /**
     * ターン毎のアクションの前にコールされるイベント
     * このイベントはメインスレッドからコールされます
     * 
     * @param bot Bot
     */
    void onPreAction(OkuRunBot bot);

    /**
     * ターン毎のアクションイベント
     * このイベントはメインスレッドからコールされます
     * 
     * @param bot Bot
     */
    void onAction(OkuRunBot bot);

    /**
     * ターン毎のアクションの後にコールされるイベント
     * このイベントはメインスレッドからコールされます
     * 
     * @param bot Bot
     */
    void onPostAction(OkuRunBot bot);

    /**
     * ゲームが終了した時の処理
     * 
     * @param e   ゲーム終了イベント
     * @param bot ボット
     */
    void onGameEnded(GameEndedEvent e, OkuRunBot bot);

    /**
     * ラウンドが終了した時の処理
     * 
     * @param e   ラウンド終了イベント
     * @param bot ボット
     */
    void onRoundEnded(RoundEndedEvent e, OkuRunBot bot);

    /**
     * 弾丸が発射された時の処理
     * 
     * @param e   弾丸が発射されたイベント
     * @param bot ボット
     */
    void onBulletFired(BulletFiredEvent e, OkuRunBot bot);

    /**
     * 弾丸が自分に当たった時の処理
     * 
     * @param e   弾丸が自分に当たったイベント
     * @param bot ボット
     */
    void onHitByBullet(HitByBulletEvent e, OkuRunBot bot);

    /**
     * 三角関数を使用して、角度が90度で指定された距離となる点Cの座標を計算します。
     * 
     * @param pointA    点Aの座標 [x, y]
     * @param pointB    点Bの座標 [x, y]
     * @param distance  距離
     * @param clockwise 時計回り（右側）の場合はtrue、反時計回り（左側）の場合はfalse
     * @return 点Cの座標 [x, y]
     */
    public static double[] calculatePointCUsingTrig(double[] pointA, double[] pointB, double distance,
            boolean clockwise) {
        // 自分(A)からターゲット(B)への絶対角度（ラジアン）を計算
        // ※ ロボコード基準（上が0度、右が90度）のため、atan2の引数は x, y の順です
        double angleAB = Math.atan2(pointB[0] - pointA[0], pointB[1] - pointA[1]);

        // BCの角度は90度（時計回りは +Math.PI / 2、反時計回りは -Math.PI / 2）
        double angleBC = angleAB + (clockwise ? Math.PI / 2 : -Math.PI / 2);

        // 指定された距離移動した座標を算出
        double cx = pointB[0] + distance * Math.sin(angleBC);
        double cy = pointB[1] + distance * Math.cos(angleBC);

        return new double[] { cx, cy };
    }
}
