package okurun.driver.actions;

import java.util.Random;

import dev.robocode.tankroyale.botapi.Constants;
import dev.robocode.tankroyale.botapi.graphics.Color;
import okurun.Debugger;
import okurun.OkuRunBot;
import okurun.battlemanager.BattleManager;
import okurun.battlemanager.EnemyState;
import okurun.commander.Commander;
import okurun.commander.Commander.AccelePriority;
import okurun.commander.Commander.HandlePriority;
import okurun.driver.Driver;
import okurun.predictor.Predictor;

/**
 * 移動目標へ向かうDriveアクション
 */
public class MoveToDriveAction implements DriveAction {
    private enum Turn {
        LEFT, RIGHT;

        public Turn opposite() {
            return this == LEFT ? RIGHT : LEFT;
        }
    }

    /**
     * 加速情報
     */
    private static class Accele {
        public double distance = 0;
        public double speed = Constants.MAX_SPEED;

        public Accele(double distance, double speed) {
            this.distance = distance;
            this.speed = speed;
        }
    }

    private Turn avoidTurn = Turn.LEFT;
    private Random random = new Random();
    private int randNum = 0;

    @Override
    public Driver.ActionId action(OkuRunBot bot) {
        final Commander commander = bot.getCommander();
        final double[] pos = commander.getTargetMovePosition(bot);
        if (pos == null) {
            // 移動先が設定されていない場合は何も行わない
            return null;
        }

        // 予測を外すための乱数を初期化
        randNum = getRandNum(bot);

        final double bearingTo = getBearingTo(bot, pos);
        bot.setTurnLeft(bearingTo);

        final Accele accele = getAccele(bot, pos, bearingTo);
        bot.setForward(accele.distance);
        bot.setMaxSpeed(accele.speed);

        // 移動目標を描画します
        draw(bot, pos, bearingTo, accele);

        return null;
    }

    /**
     * 目標旋回角を取得する
     * 
     * @param bot
     * @param pos 移動目標
     * @return 目標旋回角
     */
    private double getBearingTo(OkuRunBot bot, double[] pos) {
        double bearingTo = bot.bearingTo(pos[0], pos[1]);
        if (Math.abs(bearingTo) < bot.getMaxTurnRate()) {
            // 旋回に余裕がある（直進に近い状態）
            final Commander commander = bot.getCommander();
            if (commander.getHandlePriority(bot) == HandlePriority.AVOID_BULLET) {
                // 回避行動を行う
                if (randNum == 0) {
                    // 左右を入れ替える
                    avoidTurn = avoidTurn.opposite();
                }
                switch (avoidTurn) {
                    case LEFT:
                        bearingTo += bot.getMaxTurnRate() * 0.5;
                        break;
                    case RIGHT:
                        bearingTo -= bot.getMaxTurnRate() * 0.5;
                        break;
                }
            }
        }
        return bearingTo;
    }

    /**
     * 加速情報を取得する
     * 
     * @param bot       ロボット
     * @param pos       移動目標
     * @param bearingTo 移動目標への旋回角度
     * @return 加速情報
     */
    private Accele getAccele(OkuRunBot bot, double[] pos, double bearingTo) {
        final Accele accele = new Accele(bot.distanceTo(pos[0], pos[1]), Constants.MAX_SPEED);
        final Commander commander = bot.getCommander();
        switch (commander.getAccelePriority(bot)) {
            case AccelePriority.HANDLE:
                // 旋回を優先するため、旋回角度がTURN_RATEより大きい場合は減速する
                final double diffTurnRate = Math.abs(bearingTo) - Math.abs(bot.getTurnRate());
                if (diffTurnRate > 90) {
                    accele.speed = Math.max(commander.getMinSpeed(bot), bot.getSpeed() - 2);
                    accele.distance = -1;
                } else if (diffTurnRate > 0) {
                    accele.speed = Math.max(commander.getMinSpeed(bot), bot.getSpeed() - 1);
                } else {
                    accele.speed = Constants.MAX_SPEED;
                }
                break;
            case AccelePriority.AVOID_BULLET:
                // 予測を外すためにランダムでブレーキをかける
                accele.speed = Constants.MAX_SPEED;
                if (bot.getSpeed() > commander.getMinSpeed(bot)) {
                    if (randNum == 0) {
                        // ブレーキ
                        accele.distance = -1;
                    }
                }
                break;
            default:
                accele.speed = Constants.MAX_SPEED;
                break;
        }
        return accele;
    }

    /**
     * 予測を外すための乱数を取得する
     * 
     * @param bot
     * @return 予測を外すための乱数
     */
    private int getRandNum(OkuRunBot bot) {
        final BattleManager battleManager = bot.getBattleManager();
        final Commander commander = bot.getCommander();
        int randBound = 10;
        final EnemyState enemyState = battleManager.getLatestEnemyState(commander.getTargetEnemyId(bot));
        if (enemyState != null) {
            // 敵との距離によってブレーキの頻度を変える
            final double enemyDistance = bot.distanceTo(enemyState.x, enemyState.y);
            randBound = Math.max(randBound,
                    (int) Math.min(commander.getMinSpeed(bot) + 1,
                            Math.ceil(enemyDistance / bot.calcBulletSpeed(1))));
        }
        return random.nextInt(randBound);
    }

    /**
     * 現在位置から移動目標までの移動目標を描画する
     * 
     * @param bot
     * @param pos       移動目標
     * @param bearingTo 移動目標への旋回角度
     * @param accele    加速情報
     */
    private void draw(OkuRunBot bot, double[] pos, double bearingTo, Accele accele) {
        // 移動目標を描画します
        // ※ 描画にはUI画面でDebug Graphicsを有効にする必要があります
        final Color color = Color.LIGHT_BLUE;
        final Debugger debugger = bot.getDebugger();
        debugger.drawFillCircle(bot, pos, 5, Color.fromRgba(color, 50));
        debugger.drawLine(bot, bot.getPosition(), pos, Color.fromRgba(color, 50));

        final double[] actualPos = Predictor.calcPosition(
                bot.getPosition(), bot.getDirection() + bearingTo, accele.distance, 1);
        debugger.drawLine(bot, bot.getPosition(), actualPos, color);
    }
}
