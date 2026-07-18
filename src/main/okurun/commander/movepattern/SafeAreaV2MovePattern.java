package okurun.commander.movepattern;

import java.util.Random;

import okurun.OkuRunBot;
import okurun.arenamap.ArenaMap;
import okurun.arenamap.ArenaMap.Area;
import okurun.commander.Commander.AccelPriority;
import okurun.commander.Commander.HandlePriority;
import okurun.driver.Driver;
import okurun.predictor.Predictor;

public class SafeAreaV2MovePattern extends AbstractMovePattern {
    private boolean flg = false;
    private int a = 1;
    private final Random rand = new Random();

    @Override
    public double[] getMovePosition(OkuRunBot bot) {
        // 敵の少ない安全なエリアへ向かう
        final ArenaMap arenaMap = bot.getArenaMap();
        final Area safeArea = arenaMap.getSafeArea(bot);

        final Area currentArea = arenaMap.getArea(bot);
        if (safeArea != currentArea) {
            flg = false;
            return safeArea.getCenter();
        }

        if (flg) {
            if (rand.nextInt(30) == 0) {
                a = -a;
            }
            final double directionTo = bot.directionTo(arenaMap.getCenter()) + (90 * a);
            return Predictor.calcPosition(bot.getPosition(), bot.normalizeAbsoluteAngle(directionTo), 100, 1);
        }

        final double[] pos = safeArea.getCenter();
        flg = bot.distanceTo(pos) < 100;
        return safeArea.getCenter();
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
        return Driver.ActionId.MOVE_TO_V2;
    }

}
