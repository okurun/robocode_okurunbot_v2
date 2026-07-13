package okurun.radaroperator.actions;

import okurun.OkuRunBot;
import okurun.radaroperator.RadarOperator;

/**
 * 360度スキャンをします
 */
public class AllScanRadarAction implements RadarAction {

    @Override
    public RadarOperator.ActionId action(OkuRunBot bot) {
        bot.setAdjustRadarForGunTurn(false);
        bot.setTurnRadarLeft(360);
        return null;
    }

}
