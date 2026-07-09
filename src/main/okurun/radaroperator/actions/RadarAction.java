package okurun.radaroperator.actions;

import okurun.OkuRunBot;
import okurun.radaroperator.RadarOperator;

public interface RadarAction {
    public RadarOperator.Action action(OkuRunBot bot);
}
