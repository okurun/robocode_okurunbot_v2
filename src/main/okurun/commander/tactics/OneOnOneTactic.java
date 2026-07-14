package okurun.commander.tactics;

import dev.robocode.tankroyale.botapi.Constants;
import okurun.OkuRunBot;
import okurun.battlemanager.EnemyProfile;
import okurun.commander.Commander;
import okurun.driver.Driver;
import okurun.gunner.Gunner;

public class OneOnOneTactic extends AbstractOneOnOneTactic {

    @Override
    protected void setMovePatternId(OkuRunBot bot) {
        final int targetEnemyId = getTargetEnemyId(bot);
        if (targetEnemyId == Commander.NO_TARGET) {
            return;
        }

        final EnemyProfile enemyProfile = bot.getBattleManager().getEnemyProfile(targetEnemyId);
        movePatternId = enemyProfile.getMovePatternId();
    }

    @Override
    protected void setDriveActionId(OkuRunBot bot) {
        driveActionId = Driver.ActionId.MOVE_TO;
    }

    @Override
    protected void setGunActionId(OkuRunBot bot) {
        gunActionId = Gunner.ActionId.MAX_POWER;
        baseFirePower = Constants.MAX_FIREPOWER;
        waitForGunTurn = true;
    }

}
