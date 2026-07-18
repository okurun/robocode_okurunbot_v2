package okurun;

import dev.robocode.tankroyale.botapi.graphics.Color;
import okurun.commander.Commander;
import okurun.enemymanager.EnemyManager;
import okurun.enemymanager.EnemyProfile;
import okurun.enemymanager.EnemyState;
import okurun.gunner.BulletHistory;
import okurun.gunner.Gunner;
import okurun.predictor.Predictor;

public class Debugger {
    /**
     * ターン毎のアクションの前にコールされるイベント
     * このイベントはメインスレッドからコールされます
     * 
     * @param bot Bot
     */
    public void onPreAction(OkuRunBot bot) {
    }

    /**
     * ターン毎のアクションイベント
     * このイベントはメインスレッドからコールされます
     * 
     * @param bot Bot
     */
    public void onAction(OkuRunBot bot) {
        // デバッグ情報を画面に描きます
        // ※ 描画にはUI画面でDebug Graphicsを有効にする必要があります

        final Commander commander = bot.getCommander();
        if (commander.getTargetEnemyId(bot) == Commander.NO_TARGET) {
            return;
        }
        final EnemyManager enemyManager = bot.getEnemyManager();
        final EnemyProfile enemyProfile = enemyManager.getEnemyProfile(commander.getTargetEnemyId(bot));

        // 敵の行動予測線を引きます
        drawPredictLine(bot, commander.getPredictModelId(bot), enemyProfile);
        // for (Predictor.Model model: Predictor.Model.values()) {
        // drawPredictLine(bot, model, enemyProfile);
        // }

        // 射撃目標位置を描きます
        final Gunner gunner = bot.getGunner();
        for (final BulletHistory bulletHistory : gunner.getBulletHistories()) {
            Color color = Gunner.getBulletColor(bulletHistory.power);
            if (bulletHistory.predictTurnNum > bot.getTurnNumber()) {
                color = Color.fromRgba(color, 200);
            } else if (bulletHistory.predictTurnNum < bot.getTurnNumber()) {
                color = Color.fromRgba(color, 255 - (bot.getTurnNumber() - bulletHistory.predictTurnNum) * 20);
            }
            bot.getDebugger().drawFireTarget(bot, bulletHistory.getTargetPosition(), color);
        }

        // 移動目標を描画します
        drawMoveToTarget(bot, commander.getTargetMovePosition(bot));
    }

    /**
     * ターン毎のアクションの後にコールされるイベント
     * このイベントはメインスレッドからコールされます
     * 
     * @param bot Bot
     */
    public void onPostAction(OkuRunBot bot) {
    }

    /**
     * 現在位置から移動目標までの移動目標を描画する
     * 
     * @param bot
     * @param pos 移動目標
     */
    private void drawMoveToTarget(OkuRunBot bot, double[] pos) {
        final Color color = Color.LIGHT_BLUE;
        final Debugger debugger = bot.getDebugger();
        debugger.drawFillCircle(bot, pos, 5, Color.fromRgba(color, 60));
        debugger.drawLine(bot, bot.getPosition(), pos, Color.fromRgba(color, 60));
    }

    /**
     * 敵の行動予測線を引く
     * 予測される位置に線を描画します
     * 
     * @param bot
     * @param model
     * @param enemyProfile
     */
    private void drawPredictLine(OkuRunBot bot, Predictor.PredictModelId model, EnemyProfile enemyProfile) {
        final Predictor predictor = bot.getPredictor();
        final Color color = predictor.getPredictModel(model).getColor();
        final int turnNum = bot.getTurnNumber();
        EnemyState prevState = enemyProfile.getLatestState();
        try {
            for (int i = 0; i < 30; i++) {
                final EnemyState predictedState = predictor.predict(bot, enemyProfile, turnNum + i);
                if (predictedState == null) {
                    break;
                }
                drawLine(bot, prevState.getPosition(), predictedState.getPosition(), color);
                prevState = predictedState;
            }
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
        }
    }

    /**
     * 射撃目標を描画します
     * 
     * @param pos   座標
     * @param color 描画色
     */
    public void drawFireTarget(OkuRunBot bot, double[] pos, Color color) {
        drawFireTarget(bot, pos[0], pos[1], color);
    }

    /**
     * 射撃目標を描画します
     * 
     * @param x     X座標
     * @param y     Y座標
     * @param color 描画色
     */
    private void drawFireTarget(OkuRunBot bot, double x, double y, Color color) {
        var g = bot.getGraphics();
        g.setFillColor(Color.fromRgba(Color.WHITE, 30));
        g.setStrokeColor(Color.fromRgba(color, 80));
        g.setStrokeWidth(1);
        g.fillCircle(x, y, 10);
        g.drawCircle(x, y, 6);
        g.drawLine(x - 12, y, x + 12, y);
        g.drawLine(x, y - 12, x, y + 12);
    }

    /**
     * 画面に円を描画します
     * 
     * @param pos    座標
     * @param radius 半径
     * @param color  描画色
     */
    public void drawFillCircle(OkuRunBot bot, double[] pos, double radius, Color color) {
        drawFillCircle(bot, pos[0], pos[1], radius, color);
    }

    /**
     * 画面に円を描画します
     * 
     * @param x      X座標
     * @param y      Y座標
     * @param radius 半径
     * @param color  描画色
     */
    private void drawFillCircle(OkuRunBot bot, double x, double y, double radius, Color color) {
        bot.normalizeAbsoluteAngle(100);
        var g = bot.getGraphics();
        g.setFillColor(color);
        g.setStrokeWidth(0);
        g.fillCircle(x, y, radius);
    }

    /**
     * 画面に直線を描画します
     * 
     * @param pos1  始点座標
     * @param pos2  終点座標
     * @param color 描画色
     */
    public void drawLine(OkuRunBot bot, double[] pos1, double[] pos2, Color color) {
        drawLine(bot, pos1[0], pos1[1], pos2[0], pos2[1], color);
    }

    /**
     * 画面に直線を描画します
     * 
     * @param x1    始点X座標
     * @param y1    始点Y座標
     * @param x2    終点X座標
     * @param y2    終点Y座標
     * @param color 描画色
     */
    private void drawLine(OkuRunBot bot, double x1, double y1, double x2, double y2, Color color) {
        var g = bot.getGraphics();
        g.setStrokeColor(color);
        g.setStrokeWidth(2);
        g.drawLine(x1, y1, x2, y2);
    }
}
