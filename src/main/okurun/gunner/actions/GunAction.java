package okurun.gunner.actions;

import dev.robocode.tankroyale.botapi.graphics.Color;
import okurun.OkuRunBot;
import okurun.battlemanager.EnemyProfile;
import okurun.battlemanager.EnemyState;
import okurun.predictor.Predictor;

public interface GunAction {
    public static final int LIMIT_TURNS = 100;

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
        bot.drawLine(beforeState.getPosition(), predictedState.getPosition(), Color.fromRgba(Color.WHITE, 100));
    }

    /**
     * 画面に射撃目標位置を描画します
     * ※ 描画にはUI画面でDebug Graphicsを有効にする必要があります
     * 
     * @param bot         ボット
     * @param fireTarget  射撃目標位置
     * @param bulletPower 弾丸のパワー
     */
    public static void drawTargetPoint(OkuRunBot bot, EnemyState fireTarget, double bulletPower) {
        // 弾丸のパワーに応じて色分け
        final Color color;
        if (bulletPower >= 3) {
            color = Color.RED;
        } else if (bulletPower >= 2) {
            color = Color.ORANGE;
        } else if (bulletPower >= 1) {
            color = Color.YELLOW;
        } else {
            color = Color.WHITE;
        }
        drawTargetPoint(bot, fireTarget, Color.fromRgba(color, 150));
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
        bot.drawTarget(fireTarget.getPosition(), color);
    }

    /**
     * 射撃目標位置を計算します
     * 
     * @param bot                ボット
     * @param targetEnemyProfile 敵プロファイル
     * @param bulletPower        弾丸のパワー
     * @return 射撃目標位置
     */
    public static EnemyState getFireTarget(OkuRunBot bot, EnemyProfile targetEnemyProfile, double firePower) {
        final EnemyState latestEnemyState = targetEnemyProfile.getLatestState();
        if (latestEnemyState == null) {
            return null;
        }
        final Predictor predictor = bot.getPredictor();
        final double bulletSpeed = bot.calcBulletSpeed(firePower);
        int turnCnt = 0;

        EnemyState predictedState = null;
        EnemyState prevState = latestEnemyState;
        while (turnCnt < LIMIT_TURNS) {
            predictedState = predictor.predict(bot, targetEnemyProfile, prevState.scannedTurnNum + 1);
            if (predictedState == null) {
                return null;
            }
            drawPredictLine(bot, prevState, predictedState);
            prevState = predictedState;
            if (predictedState.scannedTurnNum <= bot.getTurnNumber()) {
                continue;
            }
            turnCnt++;
            if (predictedState.scannedTurnNum + turnCnt <= bot.getTurnNumber()) {
                continue;
            }
            final double distance = bot.distanceTo(predictedState.getPosition());
            if (distance == 0) {
                break;
            }
            if (distance <= ((predictedState.scannedTurnNum + turnCnt) - bot.getTurnNumber()) * bulletSpeed) {
                break;
            }
        }
        if (turnCnt >= LIMIT_TURNS) {
            return null;
        }
        return predictedState;
    }
}
