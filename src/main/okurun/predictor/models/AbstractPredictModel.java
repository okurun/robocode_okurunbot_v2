package okurun.predictor.models;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dev.robocode.tankroyale.botapi.events.*;
import okurun.OkuRunBot;
import okurun.predictor.ModelAccuracy;

public abstract class AbstractPredictModel implements PredictModel {

    protected final Map<String, Object> caches = new ConcurrentHashMap<>();
    protected ModelAccuracy modelAccuracy = new ModelAccuracy();
    protected ModelAccuracy modelTotalAccuracy = new ModelAccuracy();

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPreAction(OkuRunBot bot) {
        caches.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelAccuracy getAccuracy() {
        return modelAccuracy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelAccuracy getTotalAccuracy() {
        return modelTotalAccuracy;
    }

    /**
     * {@inheritDoc}
     */
    public void onGameEnded(GameEndedEvent e, OkuRunBot bot) {
        if (modelTotalAccuracy.getFireCount() > 0) {
            System.out.println(String.format(
                    "## TotalPredictModelAccuracy(%s): %s",
                    this.getClass().getSimpleName(),
                    modelTotalAccuracy.getAccuracyString()));
        }
        modelTotalAccuracy.reset();
    }

    /**
     * {@inheritDoc}
     */
    public void onRoundEnded(RoundEndedEvent e, OkuRunBot bot) {
        if (modelAccuracy.getFireCount() > 0) {
            System.out.println(String.format(
                    "== PredictModelAccuracy(%s): %s",
                    this.getClass().getSimpleName(),
                    modelAccuracy.getAccuracyString()));
        }
        modelTotalAccuracy.addFireCount(modelAccuracy.getFireCount());
        modelTotalAccuracy.addHitCount(modelAccuracy.getHitCount());
        modelTotalAccuracy.addMissCount(modelAccuracy.getMissCount());
        modelAccuracy.reset();
    }

    /**
     * {@inheritDoc}
     */
    public void onBulletFired(BulletFiredEvent e, OkuRunBot bot) {
        modelAccuracy.incrementFireCount();
    }

    /**
     * {@inheritDoc}
     */
    public void onBulletHit(BulletHitBotEvent e, OkuRunBot bot) {
        modelAccuracy.incrementHitCount();
    }

    /**
     * {@inheritDoc}
     */
    public void onBulletHitBullet(BulletHitBulletEvent e, OkuRunBot bot) {
        modelAccuracy.incrementMissCount();
    }

    /**
     * {@inheritDoc}
     */
    public void onBulletHitWall(BulletHitWallEvent e, OkuRunBot bot) {
        modelAccuracy.incrementMissCount();
    }

}