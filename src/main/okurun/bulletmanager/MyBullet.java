package okurun.bulletmanager;

import okurun.enemymanager.EnemyState;
import okurun.predictor.Predictor.PredictModelId;

/**
 * 発射された弾丸の情報を保持するクラス
 */
public class MyBullet {
    public final PredictModelId predictModel;
    public final int firedTurnNumber;
    public final int targetEnemyId;
    public final int predictTurnNum;
    public final EnemyState fireTarget;
    public final EnemyState firedTurnEnemyState;
    public int bulletId = 0;
    public double power = 0;
    public double damage = 0;
    public int endTurnNumber = -1;
    public int hitEnemyId = -1;

    public MyBullet(PredictModelId predictModel, int firedTurnNumber, int predictTurnNum, EnemyState fireTarget,
            EnemyState firedTurnEnemyState) {
        this.predictModel = predictModel;
        this.firedTurnNumber = firedTurnNumber;
        this.targetEnemyId = fireTarget.id;
        this.fireTarget = fireTarget;
        this.firedTurnEnemyState = firedTurnEnemyState;
        this.predictTurnNum = predictTurnNum;
    }

    public double[] getTargetPosition() {
        return fireTarget.getPosition();
    }
}
