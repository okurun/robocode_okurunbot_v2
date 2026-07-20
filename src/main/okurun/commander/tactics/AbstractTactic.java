package okurun.commander.tactics;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import dev.robocode.tankroyale.botapi.events.*;
import okurun.OkuRunBot;
import okurun.arenamap.ArenaMap;
import okurun.commander.Commander;
import okurun.commander.Commander.MovePatternId;
import okurun.driver.Driver;
import okurun.gunner.Gunner;
import okurun.predictor.Predictor.PredictModelId;
import okurun.radaroperator.RadarOperator;

public abstract class AbstractTactic implements Tactic {
    protected final AtomicInteger targetEnemyId = new AtomicInteger(Commander.NO_TARGET);
    protected MovePatternId movePatternId = MovePatternId.ROUND_AREA;
    protected PredictModelId predictModelId = PredictModelId.SIMPLE;
    protected Driver.ActionId driveActionId = Driver.ActionId.MOVE_TO_FORWARD;
    protected Gunner.ActionId gunActionId = Gunner.ActionId.SCAN;
    protected RadarOperator.ActionId radarActionId = RadarOperator.ActionId.ALL_SCAN;
    protected double baseFirePower = 1.5;
    protected boolean waitForGunTurn = true;

    protected abstract void updateTargetEnemyId(OkuRunBot bot);

    protected abstract void updatePredictModelId(OkuRunBot bot);

    protected abstract void updateMovePatternId(OkuRunBot bot);

    protected abstract void updateGunActionId(OkuRunBot bot);

    protected abstract void updateRadarActionId(OkuRunBot bot);

    protected void updateDriveActionId(OkuRunBot bot) {
        final ArenaMap arenaMap = bot.getArenaMap();
        final List<ArenaMap.PotentialCollisionWall> collisionWalls = arenaMap.getPotentialCollisionWalls(bot);
        if (!collisionWalls.isEmpty()) {
            // 衝突する可能性のある壁がある場合は壁回避を行います
            driveActionId = Driver.ActionId.AVOID_WALL;
            return;
        }
        // 移動パターンの依存するドライブアクションを取得します
        driveActionId = bot.getCommander().getMovePattern(movePatternId).getDependentDriveActionId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTargetEnemyId(OkuRunBot bot) {
        return targetEnemyId.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MovePatternId getMovePatternId(OkuRunBot bot) {
        return movePatternId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getBaseFirePower(OkuRunBot bot) {
        return baseFirePower;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getWaitForGunTurn(OkuRunBot bot) {
        return waitForGunTurn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PredictModelId getPredictModelId(OkuRunBot bot) {
        return predictModelId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Driver.ActionId getDriveActionId(OkuRunBot bot) {
        return driveActionId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Gunner.ActionId getGunActionId(OkuRunBot bot) {
        return gunActionId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RadarOperator.ActionId getRadarActionId(OkuRunBot bot) {
        return radarActionId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPreAction(OkuRunBot bot) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAction(OkuRunBot bot) {
        updateTargetEnemyId(bot);
        updatePredictModelId(bot);
        updateMovePatternId(bot);
        updateDriveActionId(bot);
        updateGunActionId(bot);
        updateRadarActionId(bot);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPostAction(OkuRunBot bot) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onGameEnded(GameEndedEvent e, OkuRunBot bot) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRoundEnded(RoundEndedEvent e, OkuRunBot bot) {
        targetEnemyId.set(Commander.NO_TARGET);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBulletFired(BulletFiredEvent e, OkuRunBot bot) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onHitByBullet(HitByBulletEvent e, OkuRunBot bot) {
    }
}
