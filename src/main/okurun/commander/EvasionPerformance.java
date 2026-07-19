package okurun.commander;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 回避性能を評価するクラス
 * 命中弾数とターン数を保持し、命中弾数/ターンを計算します
 */
public class EvasionPerformance {
    private final AtomicInteger hitCnt = new AtomicInteger(0);
    private final AtomicInteger turns = new AtomicInteger(0);

    /**
     * 情報をリセットします
     */
    public void reset() {
        hitCnt.set(0);
        turns.set(0);
    }

    /**
     * 命中弾数を返します
     * 
     * @return 命中弾数
     */
    public int getHitCnt() {
        return hitCnt.get();
    }

    /**
     * ターン数を返します
     * 
     * @return ターン数
     */
    public int getTurns() {
        return turns.get();
    }

    public void incrementHitCount() {
        hitCnt.incrementAndGet();
    }

    public void incrementTurns() {
        turns.incrementAndGet();
    }

    /**
     * 回避性能情報を加算します
     * 
     * @param evasionPerformance 回避性能情報
     */
    public void add(EvasionPerformance evasionPerformance) {
        hitCnt.addAndGet(evasionPerformance.getHitCnt());
        turns.addAndGet(evasionPerformance.getTurns());
    }

    /**
     * ターン毎の命中弾数を計算します
     * 
     * @param turnNumber ターン数
     * @return ターン毎の命中弾数
     */
    public double getHitPerTurn() {
        if (hitCnt.get() == 0 || turns.get() == 0) {
            return 0;
        }
        return (double) hitCnt.get() / (double) turns.get();
    }
}
