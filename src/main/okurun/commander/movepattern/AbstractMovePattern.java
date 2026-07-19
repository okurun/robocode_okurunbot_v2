package okurun.commander.movepattern;

import java.util.concurrent.atomic.AtomicInteger;

import dev.robocode.tankroyale.botapi.events.*;
import okurun.OkuRunBot;

abstract class AbstractMovePattern implements MovePattern {
    protected final AtomicInteger bulletHitCnt = new AtomicInteger(0);
    protected final AtomicInteger turns = new AtomicInteger(0);
    protected final AtomicInteger totalBulletHitCnt = new AtomicInteger(0);
    protected final AtomicInteger totalTurns = new AtomicInteger(0);

    @Override
    public void onPostAction(OkuRunBot bot) {
        turns.incrementAndGet();
    }

    /**
     * ターン毎の命中弾数を計算します
     * 
     * @param turnNumber ターン数
     * @return ターン毎の命中弾数
     */
    @Override
    public double getHitPerTurn() {
        if (bulletHitCnt.get() == 0 || turns.get() == 0) {
            return 0;
        }
        return (double) bulletHitCnt.get() / (double) turns.get();
    }

    /**
     * トータルのターン毎の命中弾数を計算します
     * 
     * @return トータルのターン毎の命中弾数
     */
    @Override
    public double getTotalHitPerTurn() {
        if (totalBulletHitCnt.get() == 0 || totalTurns.get() == 0) {
            return 0;
        }
        return (double) totalBulletHitCnt.get() / (double) totalTurns.get();
    }

    /**
     * ゲームが終了した時の処理
     * 
     * @param e   ゲーム終了イベント
     * @param bot ボット
     */
    @Override
    public void onGameEnded(GameEndedEvent e, OkuRunBot bot) {
        if (totalTurns.get() > 0) {
            System.out.println(String.format(
                    "%% TotalMovePatternSummary(%s): hit count: %d, hit/turn: %.3f",
                    this.getClass().getSimpleName(),
                    totalBulletHitCnt.get(),
                    getTotalHitPerTurn()));
        }
        totalBulletHitCnt.set(0);
        totalTurns.set(0);
    }

    /**
     * ラウンドが終了した時の処理
     * 
     * @param e   ラウンド終了イベント
     * @param bot ボット
     */
    @Override
    public void onRoundEnded(RoundEndedEvent e, OkuRunBot bot) {
        totalBulletHitCnt.addAndGet(bulletHitCnt.get());
        totalTurns.addAndGet(turns.get());
        if (turns.get() > 0) {
            System.out.println(String.format(
                    "++ MovePatternSummary(%s): hit count: %d, hit/turn: %.3f",
                    this.getClass().getSimpleName(),
                    bulletHitCnt.get(),
                    getHitPerTurn()));
        }
        bulletHitCnt.set(0);
        turns.set(0);
    }

    /**
     * 弾丸が自分に当たった時の処理
     * 
     * @param e   弾丸が自分に当たったイベント
     * @param bot ボット
     */
    @Override
    public void onHitByBullet(HitByBulletEvent e, OkuRunBot bot) {
        bulletHitCnt.incrementAndGet();
    }
}
