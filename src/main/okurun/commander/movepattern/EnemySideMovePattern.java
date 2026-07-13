package okurun.commander.movepattern;

import okurun.OkuRunBot;
import okurun.battlemanager.BattleManager;
import okurun.battlemanager.EnemyProfile;
import okurun.battlemanager.EnemyState;
import okurun.commander.Commander;
import okurun.commander.tactics.Tactic;
import okurun.predictor.Predictor;

public class EnemySideMovePattern implements MovePattern {

    @Override
    public double[] getMovePosition(OkuRunBot bot) {
        final Commander commander = bot.getCommander();
        final BattleManager battleManager = bot.getBattleManager();
        final EnemyProfile targetEnemyProfile = battleManager.getEnemyProfile(commander.getTargetEnemyId(bot));
        final EnemyState latestEnemyState = targetEnemyProfile.getLatestState();
        if (latestEnemyState == null) {
            throw new RuntimeException("LatestEnemyState is null");
        }

        final Predictor predictor = bot.getPredictor();
        EnemyState predictedEnemyState = predictor.predict(bot, targetEnemyProfile, bot.getTurnNumber());
        if (predictedEnemyState == null) {
            predictedEnemyState = latestEnemyState;
        }

        // 敵位置の少し横を目指します
        // 距離は自分と敵のエネルギー差を考慮して調整します
        final double distance = Math.max(0, 200 - ((bot.getEnergy() - latestEnemyState.energy) * 5));
        final boolean clockwise = true;
        double[] targetMovePosition = Tactic.calculatePointCUsingTrig(
                bot.getPosition(), predictedEnemyState.getPosition(), distance, clockwise);
        if (!bot.getArenaMap().isInsideArena(targetMovePosition)) {
            // 目標位置がアリーナの外なら逆サイドから回り込みます
            targetMovePosition = Tactic.calculatePointCUsingTrig(
                    bot.getPosition(), predictedEnemyState.getPosition(), distance, !clockwise);
        }
        return targetMovePosition;
    }

}
