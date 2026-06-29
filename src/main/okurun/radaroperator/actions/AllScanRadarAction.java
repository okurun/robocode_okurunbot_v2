package okurun.radaroperator.actions;

import okurun.OkuRunBot;

/**
 * 360度スキャンをします
 */
public class AllScanRadarAction implements RadarAction {

    @Override
    public String action(OkuRunBot bot) {
        bot.setAdjustRadarForGunTurn(false);
        bot.setTurnRadarLeft(360);
        return null;
    }

}
