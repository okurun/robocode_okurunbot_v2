package okurun;

import dev.robocode.tankroyale.botapi.graphics.Color;
import okurun.battlemanager.BattleManager;
import okurun.battlemanager.EnemyProfile;
import okurun.battlemanager.EnemyState;
import okurun.commander.Commander;
import okurun.predictor.Predictor;

public class Debugger {
    public void action(OkuRunBot bot) {
        final Commander commander = bot.getCommander();
        if (commander.getTargetEnemyId(bot) == Commander.NO_TARGET) {
            return;
        }
        final BattleManager battleManager = bot.getBattleManager();
        final EnemyProfile enemyProfile = battleManager.getEnemyProfile(commander.getTargetEnemyId(bot));

        drawPredictLine(bot, commander.getPredictModel(bot), enemyProfile);
        // for (Predictor.Model model: Predictor.Model.values()) {
        //     drawPredictLine(bot, model, enemyProfile);
        // }
    }

    private void drawPredictLine(OkuRunBot bot, Predictor.Model model, EnemyProfile enemyProfile) {
        final Predictor predictor = bot.getPredictor();
        final Color color = predictor.getPredictModel(model).getColor();
        final int turnNum = bot.getTurnNumber();
        EnemyState prevState = enemyProfile.getLatestState();
        try {
            for (int i = 0; i < 30; i++) {
                final EnemyState predictedState = predictor.predict(bot, enemyProfile, turnNum + i);
                if (predictedState == null) {
                    break;
                }
                bot.drawLine(prevState.getPosition(), predictedState.getPosition(), color);
                prevState = predictedState;
            }            
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
        }
    }
}
