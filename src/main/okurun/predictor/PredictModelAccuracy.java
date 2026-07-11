package okurun.predictor;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 予測モデルの命中精度を管理するクラス
 */
public class PredictModelAccuracy {
    private final AtomicInteger fireCount = new AtomicInteger(0);
    private final AtomicInteger hitCount = new AtomicInteger(0);
    private final AtomicInteger missCount = new AtomicInteger(0);

    public void reset() {
        fireCount.set(0);
        hitCount.set(0);
        missCount.set(0);
    }

    public int getFireCount() {
        return fireCount.get();
    }

    public int getHitCount() {
        return hitCount.get();
    }

    public int getMissCount() {
        return missCount.get();
    }

    /**
     * 発射回数をインクリメントします
     */
    public void incrementFireCount() {
        fireCount.incrementAndGet();
    }

    /**
     * ヒット回数をインクリメントします
     */
    public void incrementHitCount() {
        hitCount.incrementAndGet();
    }

    /**
     * ミス回数をインクリメントします
     */
    public void incrementMissCount() {
        missCount.incrementAndGet();
    }

    /**
     * 発射回数を加算します
     */
    public void addFireCount(int count) {
        fireCount.addAndGet(count);
    }

    /**
     * ヒット回数を加算します
     */
    public void addHitCount(int count) {
        hitCount.addAndGet(count);
    }

    /**
     * ミス回数を加算します
     */
    public void addMissCount(int count) {
        missCount.addAndGet(count);
    }

    /**
     * Accuracyを取得します
     * 
     * @return Accuracy
     */
    public String getAccuracyString() {
        final double hitRate = getHitRate();
        final double missRate = getMissRate();
        final double unknownRate = 1 - (hitRate + missRate);
        return String.format(
                "fireCount: %d, hitRate: %.1f%%, missRate: %.1f%%, unknownRate: %.1f%%",
                fireCount.get(),
                hitRate * 100,
                missRate * 100,
                unknownRate * 100);
    }

    /**
     * ヒット率を計算します
     * 
     * @return ヒット率
     */
    private double getHitRate() {
        if (fireCount.get() == 0 || hitCount.get() == 0) {
            return 0;
        }
        return (double) hitCount.get() / (double) fireCount.get();
    }

    /**
     * ミス率を計算します
     * 
     * @return ミス率
     */
    private double getMissRate() {
        if (fireCount.get() == 0 || missCount.get() == 0) {
            return 0;
        }
        return (double) missCount.get() / (double) fireCount.get();
    }

}
