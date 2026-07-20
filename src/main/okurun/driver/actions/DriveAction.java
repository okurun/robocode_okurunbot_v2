package okurun.driver.actions;

import okurun.OkuRunBot;
import okurun.driver.Driver;

public interface DriveAction {
    class DriveParams implements Cloneable {
        public double leftTurnAngle = 0;
        public double forwardDistance = 0;
        public double maxSpeed = 0;

        @Override
        public DriveParams clone() {
            try {
                return (DriveParams) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new AssertionError();
            }
        }
    }

    Driver.ActionId action(OkuRunBot bot);

}
