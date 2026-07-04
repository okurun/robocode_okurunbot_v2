package okurun.commander.tactics;

import dev.robocode.tankroyale.botapi.events.*;
import okurun.OkuRunBot;
import okurun.commander.Commander;

public abstract class AbstractTactic implements Tactic {
    protected int targetEnemyId = Commander.NO_TARGET;
    protected double[] targetMovePosition = null;
    protected double baseBulletPower = 1.5;
    protected String predictorModelName = null;
    protected String driveActionName = null;
    protected String gunActionName = null;
    protected String radarActionName = null;

    @Override
    public void action(OkuRunBot bot) {
        setTargetEnemyId(bot);
        setBaseBulletPower(bot);
        setPredictorModelName(bot);
        setTargetMovePosition(bot);
        setDriveActionName(bot);
        setGunActionName(bot);
        setRadarActionName(bot);
    }

    protected abstract void setTargetEnemyId(OkuRunBot bot);
    protected abstract void setBaseBulletPower(OkuRunBot bot);
    protected abstract void setPredictorModelName(OkuRunBot bot);
    protected abstract void setTargetMovePosition(OkuRunBot bot);
    protected abstract void setDriveActionName(OkuRunBot bot);
    protected abstract void setGunActionName(OkuRunBot bot);
    protected abstract void setRadarActionName(OkuRunBot bot);

    @Override
    public int getTargetEnemyId(OkuRunBot bot) {
        return targetEnemyId;
    }

    @Override
    public double[] getTargetMovePosition(OkuRunBot bot) {
        return targetMovePosition;
    }

    @Override
    public double getBaseBulletPower(OkuRunBot bot) {
        return baseBulletPower;
    }

    @Override
    public String getPredictorModelName(OkuRunBot bot) {
        return predictorModelName;
    }

    @Override
    public String getDriveActionName(OkuRunBot bot) {
        return driveActionName;
    }

    @Override
    public String getGunActionName(OkuRunBot bot) {
        return gunActionName;
    }

    @Override
    public String getRadarActionName(OkuRunBot bot) {
        return radarActionName;
    }

    /**
     * 弾丸が自分に当たった時の処理
     * 
     * @param e   弾丸が自分に当たったイベント
     * @param bot ボット
     */
    @Override
    public void onHitByBullet(HitByBulletEvent e, OkuRunBot bot) {
    }
}
