package okurun;

import dev.robocode.tankroyale.botapi.graphics.Color;
import okurun.battlemanager.BattleManager;
import okurun.battlemanager.EnemyProfile;
import okurun.battlemanager.EnemyState;
import okurun.commander.Commander;
import okurun.predictor.Predictor;

public class Debugger {
    public void action(OkuRunBot bot) {
        final Commander commander = bot.getCommander();
        if (commander.getTargetEnemyId(bot) == Commander.NO_TARGET) {
            return;
        }
        final BattleManager battleManager = bot.getBattleManager();
        final EnemyProfile enemyProfile = battleManager.getEnemyProfile(commander.getTargetEnemyId(bot));

        drawPredictLine(bot, commander.getPredictModelId(bot), enemyProfile);
        // for (Predictor.Model model: Predictor.Model.values()) {
        // drawPredictLine(bot, model, enemyProfile);
        // }
    }

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
     * ※ 描画にはUI画面でDebug Graphicsを有効にする必要があります
     * 
     * @param pos   座標
     * @param color 描画色
     */
    public void drawTarget(OkuRunBot bot, double[] pos, Color color) {
        drawTarget(bot, pos[0], pos[1], color);
    }

    /**
     * 射撃目標を描画します
     * ※ 描画にはUI画面でDebug Graphicsを有効にする必要があります
     * 
     * @param x     X座標
     * @param y     Y座標
     * @param color 描画色
     */
    private void drawTarget(OkuRunBot bot, double x, double y, Color color) {
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
     * ※ 描画にはUI画面でDebug Graphicsを有効にする必要があります
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
     * ※ 描画にはUI画面でDebug Graphicsを有効にする必要があります
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
     * ※ 描画にはUI画面でDebug Graphicsを有効にする必要があります
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
     * ※ 描画にはUI画面でDebug Graphicsを有効にする必要があります
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
