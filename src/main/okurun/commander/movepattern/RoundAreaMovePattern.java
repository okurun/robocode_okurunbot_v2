package okurun.commander.movepattern;

import okurun.OkuRunBot;
import okurun.arenamap.ArenaMap;
import okurun.commander.Commander.AccelePriority;
import okurun.commander.Commander.HandlePriority;

public class RoundAreaMovePattern extends AbstractMovePattern {

    @Override
    public double[] getMovePosition(OkuRunBot bot) {
        // 隣のエリアへ向かう
        final ArenaMap arenaMap = bot.getArenaMap();
        return arenaMap.getArea(bot).getNeighboringArea(bot).getCenter();
    }

    @Override
    public double getMinSpeed(OkuRunBot bot) {
        return 4;
    }

    @Override
    public HandlePriority getHandlePriority(OkuRunBot bot) {
        return HandlePriority.TARGET;
    }

    @Override
    public AccelePriority getAccelePriority(OkuRunBot bot) {
        return AccelePriority.HANDLE;
    }

}
