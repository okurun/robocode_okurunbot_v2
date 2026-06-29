package okurun.battlemanager;

public class EnemyState {
    public final int id;
    public final int scandTurnNum;
    public final double x;
    public final double y;
    public final double heading;
    public final double velocity;
    public final double energy;
    public final double turnDegree;
    public final double acceleration;
    public final double distance;

    public EnemyState(int id, int scandTurnNum, double x, double y, double heading, double velocity, double energy,
            double turnDegree, double acceleration, double distance) {
        this.id = id;
        this.scandTurnNum = scandTurnNum;
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

    public int[] getPositionInt() {
        return new int[] { (int) Math.round(x), (int) Math.round(y) };
    }
}
