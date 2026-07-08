package okurun.predictor;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

import dev.robocode.tankroyale.botapi.BulletState;
import dev.robocode.tankroyale.botapi.events.*;
import okurun.OkuRunBot;
import okurun.arenamap.ArenaMap;
import okurun.battlemanager.BattleManager;
import okurun.battlemanager.BulletHistory;
import okurun.battlemanager.EnemyProfile;
import okurun.battlemanager.EnemyState;
import okurun.predictor.models.*;

/**
 * 予測士クラス
 */
public class Predictor {
    private final Map<String, PredictModel> predictModels = new HashMap<>();
    private final Map<Integer, Map<String, Map<Integer, EnemyState>>> predictedDataCache = new HashMap<>();
    private final Deque<Double> hitDistanceHistory = new ConcurrentLinkedDeque<>();

    public final Map<String, PredictModelAccuracy> modelAccuracies = new HashMap<>();
    public final Map<String, PredictModelAccuracy> modelTotalAccuracies = new HashMap<>();

    public void init(OkuRunBot bot) {
        predictedDataCache.clear();
        predictModels.put(SimplePredictModel.class.getName(), new SimplePredictModel());
        predictModels.put(HistoryPredictModel.class.getName(), new HistoryPredictModel());
        for (final String modelName : predictModels.keySet()) {
            modelAccuracies.put(modelName, new PredictModelAccuracy());
        }
    }

    public void action(OkuRunBot bot) {
        predictedDataCache.clear();
        for (PredictModel model : predictModels.values()) {
            model.clearCache();
        }
    }

    /**
     * 指定したターンの敵の座標を予測します
     * 
     * @param bot           ボット
     * @param enemyProfile  敵プロファイル
     * @param targetTurnNum 予測するターン数
     * @return 予測した敵の状態
     */
    public EnemyState predict(OkuRunBot bot, EnemyProfile enemyProfile, int targetTurnNum) {
        return predict(bot, enemyProfile, targetTurnNum, bot.getCommander().getPredictorModelName(bot));
    }

    /**
     * 指定したターンの敵の座標を予測します
     * 
     * @param bot           ボット
     * @param enemyProfile  敵プロファイル
     * @param targetTurnNum 予測するターン数
     * @param modelName     使用するモデルの名前
     * @return 予測した敵の状態
     */
    private EnemyState predict(OkuRunBot bot, EnemyProfile enemyProfile, int targetTurnNum, String modelName) {
        final PredictModel predictModel = predictModels.get(modelName);
        if (predictModel == null) {
            return null;
        }

        final EnemyState latestEnemyState = enemyProfile.getLatestState();
        if (enemyProfile.getLatestState() == null) {
            return null;
        }
        if (latestEnemyState.scannedTurnNum > targetTurnNum) {
            // 予測ターンが最新の観測ターンよりも過去の場合は、予測できません
            return null;
        }
        if (latestEnemyState.scannedTurnNum == targetTurnNum) {
            // 敵の最新の観測データが予測ターン数と一致する場合は、そのまま返します
            return latestEnemyState;
        }

        // キャッシュがない場合は作成します
        Map<String, Map<Integer, EnemyState>> modelPredictedDataCache = predictedDataCache.get(enemyProfile.getId());
        if (modelPredictedDataCache == null) {
            modelPredictedDataCache = new HashMap<>();
            predictedDataCache.put(enemyProfile.getId(), modelPredictedDataCache);
        }
        Map<Integer, EnemyState> cache = modelPredictedDataCache.get(modelName);
        if (cache == null) {
            cache = new HashMap<>();
            modelPredictedDataCache.put(modelName, cache);
        }

        final ArenaMap arenaMap = bot.getArenaMap();
        final Deque<EnemyState> statusHistory = enemyProfile.getStateHistory();
        EnemyState enemyState = latestEnemyState;
        while (enemyState.scannedTurnNum < targetTurnNum) {
            if (!cache.containsKey(enemyState.scannedTurnNum + 1)) {
                // キャッシュがない場合は予測モデルを使って次のターンの敵の状態を予測します
                enemyState = predictModel.nextTurnState(bot, enemyState, statusHistory);
                if (enemyState == null) {
                    return null;
                }
                if (enemyState.x < 0 || enemyState.x > arenaMap.getWidth() || enemyState.y < 0
                        || enemyState.y > arenaMap.getHeight()) {
                    // アリーナの外側にはみ出してしまった場合は、予測を中止します
                    return null;
                }
                // 予測結果をキャッシュします
                cache.put(enemyState.scannedTurnNum, enemyState);
            } else {
                enemyState = cache.get(enemyState.scannedTurnNum + 1);
            }
        }

        return cache.get(targetTurnNum);
    }

    public PredictModel getPredictModel(String modelName) {
        return predictModels.get(modelName);
    }

    /**
     * ゲームが終了した時の処理
     * 
     * @param e ゲーム終了イベント
     */
    public void onGameEnded(GameEndedEvent e) {
        for (final String modelName : modelTotalAccuracies.keySet()) {
            final PredictModelAccuracy totalAccuracy = modelTotalAccuracies.get(modelName);
            System.out.println("Total accuracy: " + modelName + "(" + totalAccuracy.getAccuracyString() + ")");
            totalAccuracy.reset();
        }
    }

    /**
     * ラウンドが終わった時の処理
     * 
     * @param e   ラウンドが終わったイベント
     * @param bot ボット
     */
    public void onRoundEnded(RoundEndedEvent e, OkuRunBot bot) {
        for (final String modelName : predictModels.keySet()) {
            final PredictModelAccuracy predictionAccuracy = modelAccuracies.get(modelName);
            System.out.println(modelName + "(" + predictionAccuracy.getAccuracyString() + ")");
            if (modelTotalAccuracies.containsKey(modelName)) {
                final PredictModelAccuracy totalAccuracy = modelTotalAccuracies.get(modelName);
                totalAccuracy.addFireCount(predictionAccuracy.getFireCount());
                totalAccuracy.addHitCount(predictionAccuracy.getHitCount());
                totalAccuracy.addMissCount(predictionAccuracy.getMissCount());
            } else {
                modelTotalAccuracies.put(modelName, predictionAccuracy);
            }
        }
        int count = 0;
        double total = 0;
        for (double distance : hitDistanceHistory) {
            total += distance;
            count++;
        }
        if (count > 0 && total > 0) {
            System.out.println("Average hit distance: " + total / count);
        }
    }

    /**
     * 弾丸が発射された時の処理
     * 
     * @param bulletFiredEvent 弾丸が発射されたイベント
     * @param bot              ボット
     */
    public void onBulletFired(BulletFiredEvent e, OkuRunBot bot) {
        final BattleManager battleManager = bot.getBattleManager();
        final BulletHistory bulletHistory = battleManager.bulletHistories.get(e.getBullet().getBulletId());
        if (bulletHistory == null) {
            System.out.println(e.getTurnNumber() + " onBulletFired: bulletHistory is null");
            return;
        }
        final PredictModelAccuracy predictionAccuracy = modelAccuracies.get(bulletHistory.predictModel);
        if (predictionAccuracy == null) {
            System.out.println(e.getTurnNumber() + " onBulletFired: predictionAccuracy is null");
            return;
        }
        predictionAccuracy.incrementFireCount();
    }

    /**
     * 弾丸が敵ボットに当たった時の処理
     * 
     * @param bulletHitBotEvent 弾丸が敵ボットに当たったイベント
     * @param bot               ボット
     */
    public void onBulletHit(BulletHitBotEvent e, OkuRunBot bot) {
        final BattleManager battleManager = bot.getBattleManager();
        final BulletState bulletState = e.getBullet();
        final int bulletId = bulletState.getBulletId();
        final BulletHistory bulletHistory = battleManager.bulletHistories.get(bulletId);
        if (bulletHistory == null) {
            System.out.println("Warning: " + e.getTurnNumber() + " onBulletHit: bulletHistory is null");
            return;
        }
        final PredictModelAccuracy predictionAccuracy = modelAccuracies.get(bulletHistory.predictModel);
        if (predictionAccuracy == null) {
            System.out.println("Warning: " + e.getTurnNumber() + " onBulletHit: predictionAccuracy is null: "
                    + bulletHistory.predictModel);
            return;
        }
        if (e.getVictimId() == bulletHistory.targetEnemyId) {
            predictionAccuracy.incrementHitCount();
            hitDistanceHistory.add(bulletHistory.distance);
        } else {
            predictionAccuracy.incrementMissCount();
        }
    }

    /**
     * 弾丸が弾丸に当たった時の処理
     * 
     * @param bulletHitBulletEvent 弾丸が弾丸に当たったイベント
     * @param bot                  ボット
     */
    public void onBulletHitBullet(BulletHitBulletEvent e, OkuRunBot bot) {
        final BattleManager battleManager = bot.getBattleManager();
        final BulletState bulletState = e.getBullet();
        final int bulletId = bulletState.getBulletId();
        final BulletHistory bulletHistory = battleManager.bulletHistories.get(bulletId);
        if (bulletHistory == null) {
            System.out.println(e.getTurnNumber() + " onBulletHitBullet: bulletHistory is null");
            return;
        }
        final PredictModelAccuracy predictionAccuracy = modelAccuracies.get(bulletHistory.predictModel);
        if (predictionAccuracy == null) {
            System.out.println(e.getTurnNumber() + " onBulletHitBullet: predictionAccuracy is null");
            return;
        }
        predictionAccuracy.incrementMissCount();
    }

    /**
     * 弾丸が壁に当たった時の処理
     * 
     * @param e   弾丸が壁に当たったイベント
     * @param bot ボット
     */
    public void onBulletHitWall(BulletHitWallEvent e, OkuRunBot bot) {
        final BattleManager battleManager = bot.getBattleManager();
        final BulletState bulletState = e.getBullet();
        final int bulletId = bulletState.getBulletId();
        final BulletHistory bulletHistory = battleManager.bulletHistories.get(bulletId);
        if (bulletHistory == null) {
            System.out.println(e.getTurnNumber() + " onBulletHitWall: bulletHistory is null");
            return;
        }
        final PredictModelAccuracy predictionAccuracy = modelAccuracies.get(bulletHistory.predictModel);
        if (predictionAccuracy == null) {
            System.out.println(e.getTurnNumber() + " onBulletHitWall: predictionAccuracy is null");
            return;
        }
        predictionAccuracy.incrementMissCount();
    }

    /**
     * 指定した状態から指定したターン後の敵の状態を計算します
     * 
     * @param pos         敵の現在の座標
     * @param heading     敵の現在の向き
     * @param velocity    敵の現在の移動速度
     * @param diffTurnNum 予測するターン数
     * @return 予測した敵の状態
     */
    public static double[] calcPosition(double[] pos, double heading, double velocity, int diffTurnNum) {
        return calcPosition(pos[0], pos[1], heading, velocity, diffTurnNum);
    }

    /**
     * 指定した状態から指定したターン後の敵の状態を計算します（等速直線運動）
     * 
     * @param x           敵の現在のx座標
     * @param y           敵の現在のy座標
     * @param heading     敵の現在の向き
     * @param velocity    敵の現在の移動速度
     * @param diffTurnNum 予測するターン数
     * @return 予測した敵の状態
     */
    public static double[] calcPosition(double x, double y, double heading, double velocity, int diffTurnNum) {
        final double rad = Math.toRadians(heading);
        final double newX = x + velocity * Math.cos(rad) * diffTurnNum;
        final double newY = y + velocity * Math.sin(rad) * diffTurnNum;
        return new double[] { newX, newY };
    }

    /**
     * 指定した状態から指定したターン後の敵の状態を計算します（等速直線運動 + 定期的な旋回）
     * 
     * @param pos         敵の現在の座標
     * @param heading     敵の現在の向き
     * @param velocity    敵の現在の移動速度
     * @param turnDegree  1ターンあたりの旋回角度
     * @param diffTurnNum 予測するターン数
     * @return 予測した敵の状態
     */
    public static double[] calcPosition(double[] pos, double heading, double velocity, double turnDegree,
            int diffTurnNum) {
        return calcPosition(pos[0], pos[1], heading, velocity, turnDegree, diffTurnNum);
    }

    /**
     * 指定した状態から指定したターン後の敵の状態を計算します（等速直線運動 + 定期的な旋回）
     * 
     * @param x           敵の現在のx座標
     * @param y           敵の現在のy座標
     * @param heading     敵の現在の向き
     * @param velocity    敵の現在の移動速度
     * @param turnDegree  1ターンあたりの旋回角度
     * @param diffTurnNum 予測するターン数
     * @return 予測した敵の状態
     */
    public static double[] calcPosition(double x, double y, double heading, double velocity, double turnDegree,
            int diffTurnNum) {
        double newX = x;
        double newY = y;
        double newHeading = heading + turnDegree;
        for (int i = 0; i < diffTurnNum; i++) {
            final double[] pos = calcPosition(newX, newY, newHeading, velocity, 1);
            newX = pos[0];
            newY = pos[1];
            newHeading += turnDegree;
        }
        return new double[] { newX, newY };
    }
}
