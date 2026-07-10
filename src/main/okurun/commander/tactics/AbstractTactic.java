package okurun.commander.tactics;

import java.util.concurrent.atomic.AtomicInteger;

import dev.robocode.tankroyale.botapi.events.*;
import okurun.OkuRunBot;
import okurun.commander.Commander;
import okurun.driver.Driver;
import okurun.gunner.Gunner;
import okurun.predictor.Predictor.Model;
import okurun.radaroperator.RadarOperator;

public abstract class AbstractTactic implements Tactic {
    protected final AtomicInteger targetEnemyId = new AtomicInteger(Commander.NO_TARGET);
    protected double[] targetMovePosition = null;
    protected double baseFirePower = 1.5;
    protected Model predictModel = null;
    protected Driver.Action driveAction = null;
    protected Gunner.Action gunAction = null;
    protected RadarOperator.Action radarAction = null;
    protected final AtomicInteger bulletHitCnt = new AtomicInteger(0);

    @Override
    public void action(OkuRunBot bot) {
        setTargetEnemyId(bot);
        setBaseFirePower(bot);
        setPredictModel(bot);
        setTargetMovePosition(bot);
        setDriveActionName(bot);
        setGunActionName(bot);
        setRadarActionName(bot);
    }

    protected abstract void setTargetEnemyId(OkuRunBot bot);

    protected abstract void setPredictModel(OkuRunBot bot);

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
    public Model getPredictModel(OkuRunBot bot) {
        return predictModel;
    }

    @Override
    public Driver.Action getDriveAction(OkuRunBot bot) {
        return driveAction;
    }

    @Override
    public Gunner.Action getGunActionName(OkuRunBot bot) {
        return gunAction;
    }

    @Override
    public RadarOperator.Action getRadarAction(OkuRunBot bot) {
        return radarAction;
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
     * ラウンドが終了した時の処理
     * 
     * @param e   ラウンド終了イベント
     * @param bot ボット
     */
    public void onRoundEnded(RoundEndedEvent e, OkuRunBot bot) {
        final int hitCount = bulletHitCnt.get();
        final double hitPerTurn = (hitCount == 0) ? 0 : (double) hitCount / (double) e.getTurnNumber();
        System.out.println(String.format(
                "%s: hit count: %d, hit/turn: %.3f",
                this.getClass().getSimpleName(),
                hitCount,
                hitPerTurn));
        bulletHitCnt.set(0);
    }

    /**
     * 弾丸が自分に当たった時の処理
     * 
     * @param e   弾丸が自分に当たったイベント
     * @param bot ボット
     */
    @Override
    public void onHitByBullet(HitByBulletEvent e, OkuRunBot bot) {
        bulletHitCnt.incrementAndGet();
    }
}
