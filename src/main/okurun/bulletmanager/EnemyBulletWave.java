package okurun.bulletmanager;

import okurun.OkuRunBot;
import okurun.predictor.Predictor;

public class EnemyBulletWave {
    public final int enemyId;
    public final double x, y;
    public final int firedTurnNumber;
    public final double power;
    public final double speed;
    public final boolean moveForward;
    public final boolean turnLeft;
    private double distanceTraveled;
    private int countDown = 0;

    public EnemyBulletWave(int enemyId, double x, double y, int turn, double power, double speed, boolean moveForward,
            boolean moveLeft) {
        this.enemyId = enemyId;
        this.x = x;
        this.y = y;
        this.firedTurnNumber = turn;
        this.power = power;
        this.speed = speed;
        this.moveForward = moveForward;
        this.turnLeft = moveLeft;
        distanceTraveled = speed * 2;
    }

    public double[] getPosition() {
        return new double[] { x, y };
    }

    public double getDistanceTraveled() {
        return distanceTraveled;
    }

    public void update(OkuRunBot bot) {
        distanceTraveled += speed;

        double traveled = distanceTraveled;
        double[] pos = bot.getPosition();
        int turn = 0;
        double distance = Math.hypot(x - pos[0], y - pos[1]) - traveled;
        while (distance > 0) {
            pos = Predictor.calcPosition(pos, bot.getDirection(), bot.getSpeed(), 1);
            traveled += speed;
            distance = Math.hypot(x - pos[0], y - pos[1]) - traveled;
            turn++;
        }
        countDown = turn;
    }

    public boolean isOver(OkuRunBot bot) {
        return distanceTraveled > bot.distanceTo(x, y);
    }

    public int getCountDown() {
        return countDown;
    }
}
