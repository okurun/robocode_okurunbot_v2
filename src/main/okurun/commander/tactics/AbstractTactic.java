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
    protected boolean waitForGunTurn = true;
    protected Model predictModel = null;
    protected Driver.Action driveAction = null;
    protected Gunner.Action gunAction = null;
    protected RadarOperator.Action radarAction = null;
    protected final AtomicInteger bulletHitCnt = new AtomicInteger(0);
    protected final AtomicInteger totalBulletHitCnt = new AtomicInteger(0);
    protected final AtomicInteger totalTurns = new AtomicInteger(0);

    @Override
    public void preAction(OkuRunBot bot) {
    }

    @Override
    public void action(OkuRunBot bot) {
        setTargetEnemyId(bot);
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
    public boolean getWaitForGunTurn(OkuRunBot bot) {
        return waitForGunTurn;
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

    protected void reset() {
        bulletHitCnt.set(0);
        targetEnemyId.set(Commander.NO_TARGET);
    }

    /**
     * ターン毎の命中弾数を計算します
     * 
     * @param turnNumber ターン番号
     * @return ターン毎の命中弾数
     */
    private double getHitPerTurn(int turnNumber) {
        if (bulletHitCnt.get() == 0) {
            return 0;
        }
        return (double) bulletHitCnt.get() / (double) turnNumber;
    }

    /**
     * トータルのターン毎の命中弾数を計算します
     * 
     * @return トータルのターン毎の命中弾数
     */
    @Override
    public double getTotalHitPerTurn() {
        if (totalBulletHitCnt.get() == 0) {
            return 0;
        }
        return (double) totalBulletHitCnt.get() / (double) totalTurns.get();
    }

    /**
     * ゲームが終了した時の処理
     * 
     * @param e   ゲーム終了イベント
     * @param bot ボット
     */
    public void onGameEnded(GameEndedEvent e, OkuRunBot bot) {
        System.out.println(String.format(
                "## TotalTacticSummary(%s): hit count: %d, hit/turn: %.3f",
                this.getClass().getSimpleName(),
                totalBulletHitCnt.get(),
                getTotalHitPerTurn()));
        totalBulletHitCnt.set(0);
        totalTurns.set(0);
    }

    /**
     * ラウンドが終了した時の処理
     * 
     * @param e   ラウンド終了イベント
     * @param bot ボット
     */
    public void onRoundEnded(RoundEndedEvent e, OkuRunBot bot) {
        totalBulletHitCnt.addAndGet(bulletHitCnt.get());
        totalTurns.addAndGet(e.getTurnNumber());
        System.out.println(String.format(
                "== TacticSummary(%s): hit count: %d, hit/turn: %.3f",
                this.getClass().getSimpleName(),
                bulletHitCnt.get(),
                getHitPerTurn(e.getTurnNumber())));
        reset();
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
