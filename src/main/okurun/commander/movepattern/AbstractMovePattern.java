package okurun.commander.movepattern;

import dev.robocode.tankroyale.botapi.events.*;
import okurun.OkuRunBot;
import okurun.commander.EvasionPerformance;

abstract class AbstractMovePattern implements MovePattern {
    protected final EvasionPerformance evasionPerformance = new EvasionPerformance();
    protected final EvasionPerformance totalEvasionPerformance = new EvasionPerformance();

    @Override
    public void onPostAction(OkuRunBot bot) {
        evasionPerformance.incrementTurns();
    }

    /**
     * ターン毎の命中弾数を計算します
     * 
     * @param turnNumber ターン数
     * @return ターン毎の命中弾数
     */
    @Override
    public double getHitPerTurn() {
        return evasionPerformance.getHitPerTurn();
    }

    /**
     * トータルのターン毎の命中弾数を計算します
     * 
     * @return トータルのターン毎の命中弾数
     */
    @Override
    public double getTotalHitPerTurn() {
        return totalEvasionPerformance.getHitPerTurn();
    }

    /**
     * ゲームが終了した時の処理
     * 
     * @param e   ゲーム終了イベント
     * @param bot ボット
     */
    @Override
    public void onGameEnded(GameEndedEvent e, OkuRunBot bot) {
        if (totalEvasionPerformance.getTurns() > 0) {
            System.out.println(String.format(
                    "%% TotalMovePatternEvasionPerformance(%s): hit count: %d, hit/turn: %.3f",
                    this.getClass().getSimpleName(),
                    totalEvasionPerformance.getHitCount(),
                    totalEvasionPerformance.getHitPerTurn()));
        }
        totalEvasionPerformance.reset();
    }

    /**
     * ラウンドが終了した時の処理
     * 
     * @param e   ラウンド終了イベント
     * @param bot ボット
     */
    @Override
    public void onRoundEnded(RoundEndedEvent e, OkuRunBot bot) {
        totalEvasionPerformance.add(evasionPerformance);
        if (evasionPerformance.getTurns() > 0) {
            System.out.println(String.format(
                    "++ MovePatternEvasionPerformance(%s): hit count: %d, hit/turn: %.3f",
                    this.getClass().getSimpleName(),
                    evasionPerformance.getHitCount(),
                    evasionPerformance.getHitPerTurn()));
        }
        evasionPerformance.reset();
    }

    /**
     * 弾丸が自分に当たった時の処理
     * 
     * @param e   弾丸が自分に当たったイベント
     * @param bot ボット
     */
    @Override
    public void onHitByBullet(HitByBulletEvent e, OkuRunBot bot) {
        evasionPerformance.incrementHitCount();
    }
}
