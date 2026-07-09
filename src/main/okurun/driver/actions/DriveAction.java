package okurun.driver.actions;

import okurun.OkuRunBot;
import okurun.driver.Driver;

public interface DriveAction {
    Driver.Action action(OkuRunBot bot);
}
