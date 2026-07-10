package okurun.commander.tactics;

import dev.robocode.tankroyale.botapi.graphics.Color;
import okurun.OkuRunBot;
import okurun.battlemanager.BattleManager;
import okurun.battlemanager.EnemyProfile;
import okurun.battlemanager.EnemyState;
import okurun.commander.Commander;
import okurun.predictor.Predictor.Model;
import okurun.predictor.models.*;
import okurun.radaroperator.RadarOperator;

public abstract class AbstractOneOnOneTactic extends AbstractTactic {
    @Override
    public void action(OkuRunBot bot) {
        super.action(bot);
        final Commander commander = bot.getCommander();
        final int targetEnemyId = commander.getTargetEnemyId(bot);
        if (targetEnemyId == Commander.NO_TARGET) {
            return;
        }
        final BattleManager battleManager = bot.getBattleManager();
        final EnemyProfile targetEnemyProfile = battleManager.getEnemyProfile(targetEnemyId);
        if (targetEnemyProfile == null) {
            return;
        }
        final EnemyState enemyState = targetEnemyProfile.getLatestState();
        if (enemyState == null) {
            return;
        }
        final double diffEnergy = bot.getEnergy() - enemyState.energy;
        if (diffEnergy > 0) {
            bot.setTurretColor(Color.RED);
        } else if (diffEnergy < 0) {
            bot.setTurretColor(Color.BLUE);
        }
    }

    @Override
    protected void setRadarActionName(OkuRunBot bot) {
        if (targetEnemyId.get() == Commander.NO_TARGET) {
            radarAction = RadarOperator.Action.ALL_SCAN;
            return;
        }

        final BattleManager battleManager = bot.getBattleManager();
        final EnemyProfile targetEnemyProfile = battleManager.getEnemyProfile(targetEnemyId.get());
        if (targetEnemyProfile == null) {
            radarAction = RadarOperator.Action.ALL_SCAN;
            return;
        }
        final EnemyState latestEnemyState = targetEnemyProfile.getLatestState();
        if (latestEnemyState == null || latestEnemyState.scannedTurnNum < bot.getTurnNumber() - 5) {
            radarAction = RadarOperator.Action.ALL_SCAN;
            return;
        }

        radarAction = RadarOperator.Action.TARGET_SCAN;
    }

    @Override
    protected void setPredictModel(OkuRunBot bot) {
        if (targetEnemyId.get() != Commander.NO_TARGET) {
            final BattleManager battleManager = bot.getBattleManager();
            final EnemyProfile targetEnemyProfile = battleManager.getEnemyProfile(targetEnemyId.get());
            if (targetEnemyProfile != null) {
                if (HistoryPredictModel.canUse(bot, targetEnemyProfile.getStateHistory())) {
                    bot.setScanColor(Color.fromRgba(Color.LIGHT_BLUE, 2));
                    predictModel = Model.HISTORY;
                    return;
                }
            }
        }
        predictModel = Model.SIMPLE;
    }
}
