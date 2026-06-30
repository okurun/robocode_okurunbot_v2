package okurun.predictor;

import java.util.concurrent.atomic.AtomicInteger;

public class PredictionAccuracy {
    private final AtomicInteger fireCount = new AtomicInteger(0);
    private final AtomicInteger hitCount = new AtomicInteger(0);
    private final AtomicInteger missCount = new AtomicInteger(0);

    public PredictionAccuracy() {
        fireCount.set(0);
        hitCount.set(0);
        missCount.set(0);
    }

    public void incrementFireCount() {
        final int cnt = fireCount.incrementAndGet();
        System.out.println("incrementFireCount: " + cnt);
    }

    public void incrementHitCount() {
        final int cnt = hitCount.incrementAndGet();
        System.out.println("incrementHitCount: " + cnt);
    }

    public void incrementMissCount() {
        final int cnt = missCount.incrementAndGet();
        System.out.println("incrementMissCount: " + cnt);
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
