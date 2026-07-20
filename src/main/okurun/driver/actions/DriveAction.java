package okurun.driver.actions;

import okurun.OkuRunBot;
import okurun.driver.Driver;

public interface DriveAction {
    class DriveParams {
        public double leftTurnAngle = 0;
        public double forwardDistance = 0;
        public double maxSpeed = 0;
    }

    Driver.ActionId action(OkuRunBot bot);
}
