package okurun.predictor;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import dev.robocode.tankroyale.botapi.events.RoundEndedEvent;
import okurun.OkuRunBot;
import okurun.arenamap.ArenaMap;
import okurun.battlemanager.EnemyProfile;
import okurun.battlemanager.EnemyState;
import okurun.predictor.models.*;

public class Predictor {
    private final ArenaMap arenaMap;
    private final Map<String, PredictModel> predictModels = new HashMap<>();
    private final Map<Integer, Map<String, Map<Integer, EnemyState>>> predictedDataCache = new HashMap<>();

    public final Map<String, PredictionAccuracy> predictionAccuracies = new HashMap<>();

    public Predictor(ArenaMap arenaMap) {
        this.arenaMap = arenaMap;
    }

    public void init(OkuRunBot bot) {
        predictedDataCache.clear();
        predictModels.put(SimplePredictModel.class.getName(), new SimplePredictModel());
        for (final String modelName : predictModels.keySet()) {
            predictionAccuracies.put(modelName, new PredictionAccuracy());
        }
    }

    public void action(OkuRunBot bot) {
        predictedDataCache.clear();
    }

    /**
     * 指定したターンの敵の座標を予測します
     * 
     * @param bot            ボット
     * @param enemyProfile   敵プロファイル
     * @param predictTurnNum 予測するターン数
     * @return 予測した敵の状態
     */
    public EnemyState predict(OkuRunBot bot, EnemyProfile enemyProfile, int predictTurnNum) {
        return predict(bot, enemyProfile, predictTurnNum, bot.getCommander().getPredictorModelName(bot));
    }

    /**
     * 指定したターンの敵の座標を予測します
     * 
     * @param bot            ボット
     * @param enemyProfile   敵プロファイル
     * @param predictTurnNum 予測するターン数
     * @param modelName      使用するモデルの名前
     * @return 予測した敵の状態
     */
    private EnemyState predict(OkuRunBot bot, EnemyProfile enemyProfile, int predictTurnNum, String modelName) {
        final PredictModel predictModel = predictModels.get(modelName);
        if (predictModel == null) {
            return null;
        }

        final EnemyState latestEnemyState = enemyProfile.getLatestState();
        if (enemyProfile.getLatestState() == null) {
            return null;
        }
        if (latestEnemyState.scandTurnNum > predictTurnNum) {
            // 予測ターンが最新の観測ターンよりも過去の場合は、予測できません
            return null;
        }
        if (latestEnemyState.scandTurnNum == predictTurnNum) {
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

        final Deque<EnemyState> statusHistory = enemyProfile.getStateHistory();
        EnemyState enemyState = latestEnemyState;
        while (enemyState.scandTurnNum < predictTurnNum) {
            if (!cache.containsKey(enemyState.scandTurnNum + 1)) {
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
                cache.put(enemyState.scandTurnNum, enemyState);
            } else {
                enemyState = cache.get(enemyState.scandTurnNum + 1);
            }
        }

        return cache.get(predictTurnNum);
    }

    public PredictModel getPredictModel(String modelName) {
        return predictModels.get(modelName);
    }

    public void onRoundEnded(RoundEndedEvent roundEndedEvent, OkuRunBot bot) {
        for (final String modelName : predictModels.keySet()) {
            final PredictionAccuracy predictionAccuracy = predictionAccuracies.get(modelName);
            System.out.println(modelName + "(" + predictionAccuracy.getAccuracyString() + ")");
        }
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
