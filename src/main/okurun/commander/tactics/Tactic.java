package okurun.commander.tactics;

import dev.robocode.tankroyale.botapi.events.GameEndedEvent;
import dev.robocode.tankroyale.botapi.events.HitByBulletEvent;
import dev.robocode.tankroyale.botapi.events.RoundEndedEvent;
import okurun.OkuRunBot;
import okurun.commander.Commander.AccelePriority;
import okurun.commander.Commander.HandlePriority;
import okurun.commander.Commander.MovePatternId;
import okurun.driver.Driver;
import okurun.gunner.Gunner;
import okurun.predictor.Predictor.PredictModelId;
import okurun.radaroperator.RadarOperator;

public interface Tactic {
    void preAction(OkuRunBot bot);

    void action(OkuRunBot bot);

    int getTargetEnemyId(OkuRunBot bot);

    MovePatternId getMovePatternId(OkuRunBot bot);

    double getBaseFirePower(OkuRunBot bot);

    boolean getWaitForGunTurn(OkuRunBot bot);

    PredictModelId getPredictModel(OkuRunBot bot);

    Gunner.ActionId getGunActionName(OkuRunBot bot);

    RadarOperator.ActionId getRadarAction(OkuRunBot bot);

    Driver.ActionId getDriveAction(OkuRunBot bot);

    HandlePriority getHandlePriority(OkuRunBot bot);

    AccelePriority getAccelePriority(OkuRunBot bot);

    double getMinSpeed(OkuRunBot bot);

    /**
     * トータルのターン毎の命中弾数を取得します
     * 
     * @return トータルのターン毎の命中弾数
     */
    double getTotalHitPerTurn();

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
