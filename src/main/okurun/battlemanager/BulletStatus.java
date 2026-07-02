package okurun.battlemanager;

import dev.robocode.tankroyale.botapi.BulletState;

/**
 * 発射された弾丸の情報を保持するクラス
 */
public class BulletStatus {
    public final String predictModel;
    public final double targetX;
    public final double targetY;
    public final int targetEnemyId;
    public final int predictTurnNum;
    public BulletState bulletState;

    public BulletStatus(String predictModel, double targetX, double targetY, int targetEnemyId, int predictTurnNum) {
        this.predictModel = predictModel;
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetEnemyId = targetEnemyId;
        this.predictTurnNum = predictTurnNum;
    }
}
