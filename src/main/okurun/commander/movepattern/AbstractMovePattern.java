package okurun.commander.movepattern;

import dev.robocode.tankroyale.botapi.events.*;
import okurun.OkuRunBot;
import okurun.commander.EvasionPerformance;

abstract class AbstractMovePattern implements MovePattern {

    protected final EvasionPerformance evasionPerformance = new EvasionPerformance();
    protected final EvasionPerformance totalEvasionPerformance = new EvasionPerformance();

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPostAction(OkuRunBot bot) {
        evasionPerformance.incrementTurns();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getHitPerTurn() {
        return evasionPerformance.getHitPerTurn();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getTotalHitPerTurn() {
        return totalEvasionPerformance.getHitPerTurn();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onGameEnded(GameEndedEvent e, OkuRunBot bot) {
        if (totalEvasionPerformance.getTurns() > 0) {
            System.out.println(String.format(
                    "%% TotalMovePatternEvasionPerformance(%s): hit count: %d, hit/turn: %.3f",
                    this.getClass().getSimpleName(),
                    totalEvasionPerformance.getHitCount(),
                    totalEvasionPerformance.getHitPerTurn()));
        }
        totalEvasionPerformance.reset();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRoundEnded(RoundEndedEvent e, OkuRunBot bot) {
        totalEvasionPerformance.add(evasionPerformance);
        if (evasionPerformance.getTurns() > 0) {
            System.out.println(String.format(
                    "++ MovePatternEvasionPerformance(%s): hit count: %d, hit/turn: %.3f",
                    this.getClass().getSimpleName(),
                    evasionPerformance.getHitCount(),
                    evasionPerformance.getHitPerTurn()));
        }
        evasionPerformance.reset();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onHitByBullet(HitByBulletEvent e, OkuRunBot bot) {
        evasionPerformance.incrementHitCount();
    }
}
