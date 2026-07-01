package okurun.predictor;

import java.util.concurrent.atomic.AtomicInteger;

public class PredictionAccuracy {
    private final AtomicInteger fireCount = new AtomicInteger(0);
    private final AtomicInteger hitCount = new AtomicInteger(0);
    private final AtomicInteger missCount = new AtomicInteger(0);

    public void incrementFireCount() {
        fireCount.incrementAndGet();
    }

    public void incrementHitCount() {
        hitCount.incrementAndGet();
    }

    public void incrementMissCount() {
        missCount.incrementAndGet();
    }

    public String getAccuracyString() {
        return "fireCount: " + fireCount.get() + ", hitRate: " + getHitRate() + "%, missRate: " + getMissRate() + "%";
    }

    private double getHitRate() {
        if (fireCount.get() == 0 || hitCount.get() == 0) {
            return 0;
        }
        return roundRate((double) hitCount.get() / fireCount.get());
    }

    private double getMissRate() {
        if (fireCount.get() == 0 || missCount.get() == 0) {
            return 0;
        }
        return roundRate((double) missCount.get() / fireCount.get());
    }

    private static double roundRate(double rate) {
        if (rate < 0) {
            return 0;
        }
        return Math.round(rate * 1000) / 10;
    }

}
