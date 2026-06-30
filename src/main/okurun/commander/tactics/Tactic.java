package okurun.commander.tactics;

import okurun.OkuRunBot;
import okurun.commander.Commander.AccelePriority;
import okurun.commander.Commander.HandlePriority;

public interface Tactic {
    void action(OkuRunBot bot);

    int getTargetEnemyId(OkuRunBot bot);

    double[] getTargetMovePosition(OkuRunBot bot);

    double getBaseBulletPower(OkuRunBot bot);

    String getPredictorModelName(OkuRunBot bot);

    String getGunActionName(OkuRunBot bot);

    String getRadarActionName(OkuRunBot bot);

    String getDriveActionName(OkuRunBot bot);

    HandlePriority getHandlePriority(OkuRunBot bot);

    AccelePriority getAccelePriority(OkuRunBot bot);

    double getMinSpeed(OkuRunBot bot);

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
