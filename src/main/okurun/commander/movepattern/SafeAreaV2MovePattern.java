package okurun.commander.movepattern;

import okurun.OkuRunBot;
import okurun.arenamap.ArenaMap;
import okurun.arenamap.ArenaMap.Area;
import okurun.commander.Commander.AccelPriority;
import okurun.commander.Commander.HandlePriority;
import okurun.commander.tactics.Tactic;
import okurun.driver.Driver;

public class SafeAreaV2MovePattern extends AbstractMovePattern {
    private boolean isOrbiting = false;
    private boolean clockwise = true;

    @Override
    public double[] getMovePosition(OkuRunBot bot) {
        // 敵の少ない安全なエリアへ向かう
        final ArenaMap arenaMap = bot.getArenaMap();
        final Area safeArea = arenaMap.getSafeArea(bot);

        final Area currentArea = arenaMap.getArea(bot);
        if (safeArea != currentArea) {
            // 現在のエリアと安全エリアが違う場合は安全エリアの中心を目指す
            isOrbiting = false; // フラグはリセット
            return safeArea.getCenter();
        }

        final double distance = 100;
        if (isOrbiting) {
            final double[] pos = Tactic.calculatePointCUsingTrig(arenaMap.getCenter(), safeArea.getCenter(), distance, clockwise);
            if (bot.distanceTo(pos) > distance / 2) {
                // 目標から遠い場合はそのまま
                return pos;
            }
            // 目標に近づいた場合は方向転換して反対方向へ進む
            clockwise = !clockwise;
            return Tactic.calculatePointCUsingTrig(arenaMap.getCenter(), safeArea.getCenter(), distance, clockwise);
        }

        // 中心付近に近づくまでは中心を目標にする
        final double[] pos = safeArea.getCenter();
        isOrbiting = bot.distanceTo(pos) < distance; // 中心に近づいたらフラグを立てる
        return safeArea.getCenter();
    }

    @Override
    public HandlePriority getHandlePriority(OkuRunBot bot) {
        return HandlePriority.AVOID_BULLET;
    }

    @Override
    public AccelPriority getAccelPriority(OkuRunBot bot) {
        return AccelPriority.HANDLE;
    }

    @Override
    public double getMinSpeed(OkuRunBot bot) {
        return 4;
    }

    /**
     * このムーブパターンが依存するドライブアクションIDを取得します
     * 
     * @return このムーブパターンが依存するドライブアクションID
     */
    @Override
    public Driver.ActionId getDependentDriveActionId() {
        return Driver.ActionId.MOVE_TO;
    }

}
