package okurun.gunner.actions;

import dev.robocode.tankroyale.botapi.graphics.Color;
import okurun.OkuRunBot;
import okurun.battlemanager.BulletHistory;
import okurun.battlemanager.EnemyProfile;
import okurun.battlemanager.EnemyState;
import okurun.gunner.Gunner;
import okurun.predictor.Predictor;

public interface GunAction {
    public static final int LIMIT_TURNS = 30;

    public Gunner.Action action(OkuRunBot bot);

    /**
     * 予測線を画面に描画します
     * ※ 描画にはUI画面でDebug Graphicsを有効にする必要があります
     * 
     * @param bot            ボット
     * @param beforeState    予測前の状態
     * @param predictedState 予測後の状態
     */
    public static void drawPredictLine(OkuRunBot bot, EnemyState beforeState, EnemyState predictedState) {
        bot.drawLine(beforeState.getPosition(), predictedState.getPosition(),
                Color.fromRgba(bot.getPredictor().getPredictModel(bot).getColor(), 100));
    }

    /**
     * 画面に円を描画します
     * ※ 描画にはUI画面でDebug Graphicsを有効にする必要があります
     * 
     * @param bot        ボット
     * @param fireTarget 射撃目標位置
     */
    public static void drawCircle(OkuRunBot bot, EnemyState fireTarget) {
        bot.drawFillCircle(fireTarget.getPosition(), 5, bot.getPredictor().getPredictModel(bot).getColor());
    }

    /**
     * 画面に射撃目標位置を描画します
     * ※ 描画にはUI画面でDebug Graphicsを有効にする必要があります
     * 
     * @param bot        ボット
     * @param fireTarget 射撃目標位置
     * @param firePower  弾丸のパワー
     */
    public static void drawTargetPoint(OkuRunBot bot, EnemyState fireTarget, double firePower) {
        drawTargetPoint(bot, fireTarget, Color.fromRgba(Gunner.getBulletColor(firePower), 150));
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

    /**
     * 弾丸履歴を保存します
     * 
     * @param bot           ボット
     * @param bulletHistory 弾丸履歴
     */
    public static void stackBulletHistory(OkuRunBot bot, BulletHistory bulletHistory) {
        bot.getBattleManager().addBulletStack(bulletHistory);
    }
}
