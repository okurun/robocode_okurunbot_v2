package okurun.predictor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dev.robocode.tankroyale.botapi.Constants;
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
    public static enum PredictModelId {
        ZIGZAG, HISTORY, DYNAMIC, SIMPLE, NONE
    }

    private final Map<PredictModelId, PredictModel> predictModels = new HashMap<>();
    private final Map<Integer, PredictModelId> bulletModels = new ConcurrentHashMap<>();

    public void preAction(OkuRunBot bot) {
        for (PredictModel model : predictModels.values()) {
            model.preAction();
        }
    }

    public void action(OkuRunBot bot) {
    }

    public void postAction(OkuRunBot bot) {
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
        return predict(bot, enemyProfile, targetTurnNum, bot.getCommander().getPredictModelId(bot));
    }

    /**
     * 指定したターンの敵の座標を予測します
     * 
     * @param bot           ボット
     * @param enemyProfile  敵プロファイル
     * @param targetTurnNum 予測するターン数
     * @param model         使用するモデル
     * @return 予測した敵の状態
     * @throws RuntimeException 予測モデルが存在しない場合
     */
    public EnemyState predict(OkuRunBot bot, EnemyProfile enemyProfile, int targetTurnNum, PredictModelId model) {
        final PredictModel predictModel = predictModels.get(model);
        if (predictModel == null) {
            throw new RuntimeException("Predict model is not found: " + model);
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

        final ArenaMap arenaMap = bot.getArenaMap();
        EnemyState enemyState = latestEnemyState;
        while (enemyState.scannedTurnNum < targetTurnNum) {
            // キャッシュがない場合は予測モデルを使って次のターンの敵の状態を予測します
            enemyState = predictModel.nextTurnState(bot, enemyState, enemyProfile);
            if (enemyState == null) {
                return null;
            }
            if (enemyState.x < 0 || enemyState.x > arenaMap.getWidth() || enemyState.y < 0
                    || enemyState.y > arenaMap.getHeight()) {
                // アリーナの外側にはみ出してしまった場合は、予測を中止します
                return null;
            }
        }
        return enemyState;
    }

    public PredictModel getPredictModel(OkuRunBot bot) {
        return getPredictModel(bot.getCommander().getPredictModelId(bot));
    }

    public PredictModel getPredictModel(PredictModelId model) {
        return predictModels.get(model);
    }

    public Map<PredictModelId, PredictModel> getPredictModels() {
        return new HashMap<>(predictModels);
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
        final double v = Math.min(Math.max(velocity, -Constants.MAX_SPEED), Constants.MAX_SPEED);
        final double rad = Math.toRadians(heading);
        final double newX = x + v * Math.cos(rad) * diffTurnNum;
        final double newY = y + v * Math.sin(rad) * diffTurnNum;
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

    /**
     * ゲームが開始された時の処理
     * 
     * @param e   ゲーム開始イベント
     * @param bot Bot
     */
    public void onGameStarted(GameStartedEvent e, OkuRunBot bot) {
        try {
            predictModels.put(PredictModelId.SIMPLE, new SimplePredictModel());
            predictModels.put(PredictModelId.DYNAMIC, new DynamicPredictModel());
            predictModels.put(PredictModelId.ZIGZAG, new ZigzagPredictModel());
            predictModels.put(PredictModelId.HISTORY, new HistoryPredictModel());
            predictModels.put(PredictModelId.NONE, new NonePredictModel());
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }

    /**
     * ゲームが終了した時の処理
     * 
     * @param e   ゲーム終了イベント
     * @param bot ボット
     */
    public void onGameEnded(GameEndedEvent e, OkuRunBot bot) {
        try {
            for (final PredictModelId model : predictModels.keySet()) {
                predictModels.get(model).onGameEnded(e, bot);
            }
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }

    /**
     * ラウンドが終わった時の処理
     * 
     * @param e   ラウンドが終わったイベント
     * @param bot ボット
     */
    public void onRoundEnded(RoundEndedEvent e, OkuRunBot bot) {
        try {
            bulletModels.clear();
            for (final PredictModelId model : predictModels.keySet()) {
                predictModels.get(model).onRoundEnded(e, bot);
            }
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }

    /**
     * 弾丸が発射された時の処理
     * 
     * @param bulletFiredEvent 弾丸が発射されたイベント
     * @param bot              ボット
     */
    public void onBulletFired(BulletFiredEvent e, OkuRunBot bot) {
        try {
            final int bulletId = e.getBullet().getBulletId();
            final BattleManager battleManager = bot.getBattleManager();
            final BulletHistory bulletHistory = battleManager.getBulletHistory(bulletId);
            if (bulletHistory == null) {
                System.out.println(e.getTurnNumber() + " onBulletFired: bulletHistory is null");
                return;
            }
            bulletModels.put(bulletId, bulletHistory.predictModel);
            predictModels.get(bulletHistory.predictModel).onBulletFired(e, bot);
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }

    /**
     * 弾丸が敵ボットに当たった時の処理
     * 
     * @param bulletHitBotEvent 弾丸が敵ボットに当たったイベント
     * @param bot               ボット
     */
    public void onBulletHit(BulletHitBotEvent e, OkuRunBot bot) {
        try {
            final int bulletId = e.getBullet().getBulletId();
            final PredictModelId model = bulletModels.remove(bulletId);
            if (model == null) {
                System.out.println("Warning: " + e.getTurnNumber() + " onBulletHit: model is null");
                return;
            }
            predictModels.get(model).onBulletHit(e, bot);
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }

    /**
     * 弾丸が弾丸に当たった時の処理
     * 
     * @param bulletHitBulletEvent 弾丸が弾丸に当たったイベント
     * @param bot                  ボット
     */
    public void onBulletHitBullet(BulletHitBulletEvent e, OkuRunBot bot) {
        try {
            final int bulletId = e.getBullet().getBulletId();
            final PredictModelId model = bulletModels.remove(bulletId);
            if (model == null) {
                System.out.println("Warning: " + e.getTurnNumber() + " onBulletHitBullet: model is null");
                return;
            }
            predictModels.get(model).onBulletHitBullet(e, bot);
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }

    /**
     * 弾丸が壁に当たった時の処理
     * 
     * @param e   弾丸が壁に当たったイベント
     * @param bot ボット
     */
    public void onBulletHitWall(BulletHitWallEvent e, OkuRunBot bot) {
        try {
            final int bulletId = e.getBullet().getBulletId();
            final PredictModelId model = bulletModels.remove(bulletId);
            if (model == null) {
                System.out.println("Warning: " + e.getTurnNumber() + " onBulletHitWall: model is null");
                return;
            }
            predictModels.get(model).onBulletHitWall(e, bot);
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }
}
