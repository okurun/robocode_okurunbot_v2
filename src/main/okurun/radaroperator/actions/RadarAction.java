package okurun.radaroperator.actions;

import okurun.OkuRunBot;
import okurun.radaroperator.RadarOperator;

public interface RadarAction {
    public RadarOperator.ActionId action(OkuRunBot bot);
}
