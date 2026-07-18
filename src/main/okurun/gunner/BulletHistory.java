package okurun.gunner;

import okurun.predictor.Predictor.PredictModelId;

/**
 * 発射された弾丸の情報を保持するクラス
 */
public class BulletHistory {
    public final PredictModelId predictModel;
    public final double targetX;
    public final double targetY;
    public final int targetEnemyId;
    public final int predictTurnNum;
    public final double distance;
    public int bulletId = 0;
    public double power = 0;

    public BulletHistory(PredictModelId predictModel, double targetX, double targetY, int targetEnemyId,
            int predictTurnNum, double distance) {
        this.predictModel = predictModel;
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetEnemyId = targetEnemyId;
        this.predictTurnNum = predictTurnNum;
        this.distance = distance;
    }

    public double[] getTargetPosition() {
        return new double[] { targetX, targetY };
    }
}
