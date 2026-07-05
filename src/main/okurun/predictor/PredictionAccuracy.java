package okurun.predictor;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 予測モデルの命中精度を管理するクラス
 */
public class PredictionAccuracy {
    private final AtomicInteger fireCount = new AtomicInteger(0);
    private final AtomicInteger hitCount = new AtomicInteger(0);
    private final AtomicInteger missCount = new AtomicInteger(0);

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
     * Accuracyを取得します
     * 
     * @return Accuracy
     */
    public String getAccuracyString() {
        return "fireCount: " + fireCount.get() + ", hitRate: " + getHitRate() + "%, missRate: " + getMissRate() + "%";
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
        return roundRate((double) hitCount.get() / fireCount.get());
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
        return roundRate((double) missCount.get() / fireCount.get());
    }

    /**
     * 確率を丸めます
     * 
     * @param rate 確率
     * @return 丸めた確率
     */
    private static double roundRate(double rate) {
        if (rate < 0) {
            return 0;
        }
        return Math.round(rate * 1000) / 10.0;
    }

}
