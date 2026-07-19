package okurun.commander.movepattern;

import okurun.OkuRunBot;
import okurun.arenamap.ArenaMap;
import okurun.arenamap.ArenaMap.Area;
import okurun.commander.Commander.AccelPriority;
import okurun.commander.Commander.HandlePriority;
import okurun.commander.tactics.Tactic;
import okurun.driver.Driver;

public class SafeAreaMovePattern extends AbstractMovePattern {

    @Override
    public double[] getMovePosition(OkuRunBot bot) {
        // 敵の少ない安全なエリアへ向かう
        final ArenaMap arenaMap = bot.getArenaMap();
        final Area safeArea = arenaMap.getSafeArea(bot);
        // 目的地で停止してしまわないように少しズラす
        return Tactic.calculatePointCUsingTrig(
                bot.getPosition(), safeArea.getCenter(), 30, false);
    }

    @Override
    public HandlePriority getHandlePriority(OkuRunBot bot) {
        return HandlePriority.AVOID_BULLET;
    }

    @Override
    public AccelPriority getAccelPriority(OkuRunBot bot) {
        return AccelPriority.MAX_SPEED;
    }

    @Override
    public double getMinSpeed(OkuRunBot bot) {
        return 2;
    }

    /**
     * このムーブパターンが依存するドライブアクションIDを取得します
     * 
     * @return このムーブパターンが依存するドライブアクションID
     */
    @Override
    public Driver.ActionId getDependentDriveActionId() {
        return Driver.ActionId.MOVE_TO_FORWARD;
    }

}
