package okurun.commander.movepattern;

import okurun.OkuRunBot;
import okurun.arenamap.ArenaMap;

public class RoundAreaMovePattern implements MovePattern {

    @Override
    public double[] getMovePosition(OkuRunBot bot) {
        // 隣のエリアへ向かう
        final ArenaMap arenaMap = bot.getArenaMap();
        return arenaMap.getArea(bot).getNeighboringArea(bot).getCenter();
    }

}
