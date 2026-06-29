package okurun.gunner.actions;

import dev.robocode.tankroyale.botapi.graphics.Color;
import okurun.OkuRunBot;
import okurun.battlemanager.EnemyProfile;
import okurun.battlemanager.EnemyState;
import okurun.predictor.Predictor;

public interface GunAction {
    public String action(OkuRunBot bot);

    /**
     * 予測線を画面に描画します
     * ※ 描画にはUI画面でDebug Graphicsを有効にする必要があります
     * 
     * @param bot            ボット
     * @param beforeState    予測前の状態
     * @param predictedState 予測後の状態
     */
    public static void drawPredictLine(OkuRunBot bot, EnemyState beforeState, EnemyState predictedState) {
        bot.drawLine(beforeState.x, beforeState.y, predictedState.x, predictedState.y,
                Color.fromRgba(Color.WHITE, 100));
    }

    /**
     * 画面に射撃目標位置を描画します
     * ※ 描画にはUI画面でDebug Graphicsを有効にする必要があります
     * 
     * @param bot         ボット
     * @param fireTarget  射撃目標位置
     * @param bulletPower 弾丸のパワー
     */
    public static void drawTargetPoint(OkuRunBot bot, EnemyState fireTarget, int bulletPower) {
        final Color color;
        switch ((int) bulletPower) {
            case 1:
                color = Color.YELLOW;
                break;
            case 2:
                color = Color.ORANGE;
                break;
            case 3:
                color = Color.RED;
                break;
            default:
                color = Color.WHITE;
                break;
        }
        drawTargetPoint(bot, fireTarget, Color.fromRgba(color, 50));
    }

    /**
     * 画面に射撃目標位置を描画します
     * ※ 描画にはUI画面でDebug Graphicsを有効にする必要があります
     * 
     * @param bot        ボット
     * @param fireTarget 射撃目標位置
     * @param color      描画色
     */
    private static void drawTargetPoint(OkuRunBot bot, EnemyState fireTarget, Color color) {
        bot.drawCircle(fireTarget.x, fireTarget.y, 10, color);
    }

    /**
     * 射撃目標位置を計算します
     * 
     * @param bot                ボット
     * @param targetEnemyProfile 敵プロファイル
     * @param bulletPower        弾丸のパワー
     * @return 射撃目標位置
     */
    public static EnemyState getFireTarget(OkuRunBot bot, EnemyProfile targetEnemyProfile, double bulletPower) {
        final Predictor predictor = bot.getPredictor();
        final double bulletSpeed = bot.calcBulletSpeed(bulletPower);
        EnemyState predictedState = predictor.predict(bot, targetEnemyProfile, bot.getTurnNumber() + 1);
        if (predictedState == null) {
            return null;
        }

        // デバッグ用に予測線を描きます
        // ※ 描画にはUI画面でDebug Graphicsを有効にする必要があります
        drawPredictLine(bot, targetEnemyProfile.getLatestState(), predictedState);

        // 敵に着弾可能なターンまで敵の位置を予測します
        int turnNum = 0;
        EnemyState beforeState = predictedState;
        while (bot.distanceTo(predictedState.x, predictedState.y) > bulletSpeed * turnNum) {
            predictedState = predictor.predict(bot, targetEnemyProfile, predictedState.scandTurnNum + 1);
            if (predictedState == null) {
                return null;
            }
            drawPredictLine(bot, beforeState, predictedState);
            beforeState = predictedState;
            turnNum++;
            if (turnNum > 100) {
                System.out.println("Error: turnNum is too large");
                return null;
            }
        }
        return predictedState;
    }
}
