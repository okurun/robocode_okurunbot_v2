package okurun.commander.tactics;

import java.util.concurrent.atomic.AtomicInteger;

import dev.robocode.tankroyale.botapi.events.*;
import okurun.OkuRunBot;
import okurun.commander.Commander;

public abstract class AbstractTactic implements Tactic {
    protected final AtomicInteger targetEnemyId = new AtomicInteger(Commander.NO_TARGET);
    protected double[] targetMovePosition = null;
    protected double baseFirePower = 1.5;
    protected String predictorModelName = null;
    protected String driveActionName = null;
    protected String gunActionName = null;
    protected String radarActionName = null;

    @Override
    public void action(OkuRunBot bot) {
        setTargetEnemyId(bot);
        setBaseFirePower(bot);
        setPredictorModelName(bot);
        setTargetMovePosition(bot);
        setDriveActionName(bot);
        setGunActionName(bot);
        setRadarActionName(bot);
    }

    protected abstract void setTargetEnemyId(OkuRunBot bot);

    protected abstract void setPredictorModelName(OkuRunBot bot);

    protected abstract void setTargetMovePosition(OkuRunBot bot);

    protected abstract void setDriveActionName(OkuRunBot bot);

    protected abstract void setGunActionName(OkuRunBot bot);

    protected abstract void setRadarActionName(OkuRunBot bot);

    @Override
    public int getTargetEnemyId(OkuRunBot bot) {
        return targetEnemyId.get();
    }

    @Override
    public double[] getTargetMovePosition(OkuRunBot bot) {
        return targetMovePosition;
    }

    @Override
    public double getBaseFirePower(OkuRunBot bot) {
        return baseFirePower;
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

    protected void setBaseFirePower(OkuRunBot bot) {
        baseFirePower = 2;

        // 自分のエネルギーが少ない時はパワーを下げる
        if (bot.getEnergy() < 60) {
            baseFirePower -= (60 - bot.getEnergy()) * 0.03;
        } else if (bot.getEnergy() > 100) {
            baseFirePower += (bot.getEnergy() - 100) * 0.03;
        }
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
