package okurun.radaroperator.actions;

import okurun.OkuRunBot;
import okurun.radaroperator.RadarOperator;

/**
 * 360度スキャンをします
 */
public class AllScanRadarAction implements RadarAction {

    /**
     * {@inheritDoc}
     */
    @Override
    public RadarOperator.ActionId onAction(OkuRunBot bot) {
        bot.setAdjustRadarForGunTurn(false);
        bot.setTurnRadarLeft(360);
        return null;
    }

}
