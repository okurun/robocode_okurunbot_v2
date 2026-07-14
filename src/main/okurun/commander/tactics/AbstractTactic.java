package okurun.commander.tactics;

import java.util.concurrent.atomic.AtomicInteger;

import dev.robocode.tankroyale.botapi.events.*;
import okurun.OkuRunBot;
import okurun.commander.Commander;
import okurun.commander.Commander.MovePatternId;
import okurun.driver.Driver;
import okurun.gunner.Gunner;
import okurun.predictor.Predictor.PredictModelId;
import okurun.radaroperator.RadarOperator;

public abstract class AbstractTactic implements Tactic {
    protected final AtomicInteger targetEnemyId = new AtomicInteger(Commander.NO_TARGET);
    protected MovePatternId movePatternId = null;
    protected double baseFirePower = 1.5;
    protected boolean waitForGunTurn = true;
    protected PredictModelId predictModelId = null;
    protected Driver.ActionId driveActionId = null;
    protected Gunner.ActionId gunActionId = null;
    protected RadarOperator.ActionId radarActionId = null;

    @Override
    public void preAction(OkuRunBot bot) {
    }

    @Override
    public void action(OkuRunBot bot) {
        setTargetEnemyId(bot);
        setPredictModelId(bot);
        setMovePatternId(bot);
        setDriveActionId(bot);
        setGunActionId(bot);
        setRadarActionId(bot);
    }

    protected abstract void setTargetEnemyId(OkuRunBot bot);

    protected abstract void setPredictModelId(OkuRunBot bot);

    protected abstract void setMovePatternId(OkuRunBot bot);

    protected abstract void setDriveActionId(OkuRunBot bot);

    protected abstract void setGunActionId(OkuRunBot bot);

    protected abstract void setRadarActionId(OkuRunBot bot);

    @Override
    public int getTargetEnemyId(OkuRunBot bot) {
        return targetEnemyId.get();
    }

    @Override
    public MovePatternId getMovePatternId(OkuRunBot bot) {
        return movePatternId;
    }

    @Override
    public double getBaseFirePower(OkuRunBot bot) {
        return baseFirePower;
    }

    @Override
    public boolean getWaitForGunTurn(OkuRunBot bot) {
        return waitForGunTurn;
    }

    @Override
    public PredictModelId getPredictModelId(OkuRunBot bot) {
        return predictModelId;
    }

    @Override
    public Driver.ActionId getDriveActionId(OkuRunBot bot) {
        return driveActionId;
    }

    @Override
    public Gunner.ActionId getGunActionId(OkuRunBot bot) {
        return gunActionId;
    }

    @Override
    public RadarOperator.ActionId getRadarActionId(OkuRunBot bot) {
        return radarActionId;
    }

    /**
     * ゲームが終了した時の処理
     * 
     * @param e   ゲーム終了イベント
     * @param bot ボット
     */
    @Override
    public void onGameEnded(GameEndedEvent e, OkuRunBot bot) {
    }

    /**
     * ラウンドが終了した時の処理
     * 
     * @param e   ラウンド終了イベント
     * @param bot ボット
     */
    @Override
    public void onRoundEnded(RoundEndedEvent e, OkuRunBot bot) {
        targetEnemyId.set(Commander.NO_TARGET);
    }

    /**
     * 弾丸が発射された時の処理
     * 
     * @param e 弾丸が発射されたイベント
     * @param bot ボット
     */
    @Override
    public void onBulletFired(BulletFiredEvent e, OkuRunBot bot) {
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
