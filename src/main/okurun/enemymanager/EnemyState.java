package okurun.enemymanager;

/**
 * スキャンした敵ボットの情報を保持するクラス
 */
public class EnemyState {
    public final int id;
    public final int scannedTurnNum;
    public final double x;
    public final double y;
    public final double heading;
    public final double velocity;
    public final double energy;
    public final double turnDegree;
    public final double acceleration;
    public final double distance;

    public EnemyState(int id, int scannedTurnNum, double x, double y, double heading, double velocity, double energy,
            double turnDegree, double acceleration, double distance) {
        this.id = id;
        this.scannedTurnNum = scannedTurnNum;
        this.x = x;
        this.y = y;
        this.heading = heading;
        this.velocity = velocity;
        this.energy = energy;
        this.turnDegree = turnDegree;
        this.acceleration = acceleration;
        this.distance = distance;
    }

    public double[] getPosition() {
        return new double[] { x, y };
    }
}
