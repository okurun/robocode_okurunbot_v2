package okurun.commander;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import dev.robocode.tankroyale.botapi.events.*;
import okurun.OkuRunBot;
import okurun.Tool;
import okurun.commander.movepattern.*;
import okurun.commander.tactics.*;
import okurun.driver.Driver;
import okurun.enemymanager.EnemyManager;
import okurun.enemymanager.EnemyProfile;
import okurun.enemymanager.EnemyState;
import okurun.gunner.Gunner;
import okurun.predictor.Predictor.PredictModelId;
import okurun.radaroperator.RadarOperator;

/**
 * 指揮官クラス
 * 戦況に応じて戦略を決定し、各コンポーネントに指示を出します。
 */
public class Commander {
    public static enum TacticId {
        SURVIVAL,
        ONE_ON_ONE_ANALYSIS,
        ONE_ON_ONE,
    }

    public static enum MovePatternId {
        ENEMY_SIDE,
        ROUND_AREA,
        SAFE_AREA,
        OPPOSITE_SIDE,
    }

    /**
     * ハンドリングの優先順位
     */
    public static enum HandlePriority {
        TARGET,
        AVOID_BULLET,
    }

    /**
     * 加速の優先順位
     */
    public static enum AccelPriority {
        MAX_SPEED,
        HANDLE,
        AVOID_BULLET,
    }

    public static final int NO_TARGET = -1;

    private Map<TacticId, Tactic> tactics = new HashMap<>();
    private Map<MovePatternId, MovePattern> movePatterns = new HashMap<>();
    private Tactic currentTactic = null;
    private final Map<String, Object> caches = new ConcurrentHashMap<>();
    private AtomicBoolean isWon = new AtomicBoolean(false);

    public Commander() {
        tactics.put(TacticId.ONE_ON_ONE_ANALYSIS, new AnalysisOneOnOneTactic());
        tactics.put(TacticId.ONE_ON_ONE, new OneOnOneTactic());
        tactics.put(TacticId.SURVIVAL, new SurvivalTactic());

        movePatterns.put(MovePatternId.ENEMY_SIDE, new EnemySideMovePattern());
        movePatterns.put(MovePatternId.ROUND_AREA, new RoundAreaMovePattern());
        movePatterns.put(MovePatternId.SAFE_AREA, new SafeAreaMovePattern());
        movePatterns.put(MovePatternId.OPPOSITE_SIDE, new OppositeSideMovePattern());
    }

    private void setCurrentTactic(OkuRunBot bot) {
        final EnemyManager enemyManager = bot.getEnemyManager();
        if (enemyManager.getAliveAndNotMissingEnemyCount(bot) <= 1) {
            // 生存している敵が1機のみ
            final EnemyProfile enemyProfile = enemyManager.getAliveEnemy(bot);
            if (enemyProfile == null) {
                currentTactic = tactics.get(TacticId.ONE_ON_ONE);
                return;
            }
            final EnemyState latestEnemyState = enemyProfile.getLatestState();
            if (latestEnemyState == null) {
                currentTactic = tactics.get(TacticId.ONE_ON_ONE);
                return;
            }
            currentTactic = tactics.get(enemyProfile.getTacticId());
            return;
        }
        currentTactic = tactics.get(TacticId.SURVIVAL);
    }

    public int getTargetEnemyId(OkuRunBot bot) {
        return currentTactic.getTargetEnemyId(bot);
    }

    public double[] getTargetMovePosition(OkuRunBot bot) {
        final MovePattern movePattern = movePatterns.get(currentTactic.getMovePatternId(bot));
        return movePattern.getMovePosition(bot);
    }

    public double getBaseFirePower(OkuRunBot bot) {
        return currentTactic.getBaseFirePower(bot);
    }

    public boolean getWaitForGunTurn(OkuRunBot bot) {
        return currentTactic.getWaitForGunTurn(bot);
    }

    public PredictModelId getPredictModelId(OkuRunBot bot) {
        return currentTactic.getPredictModelId(bot);
    }

    public Gunner.ActionId getGunActionName(OkuRunBot bot) {
        return currentTactic.getGunActionId(bot);
    }

    public RadarOperator.ActionId getRadarAction(OkuRunBot bot) {
        return currentTactic.getRadarActionId(bot);
    }

    public Driver.ActionId getDriveAction(OkuRunBot bot) {
        return currentTactic.getDriveActionId(bot);
    }

    public AccelPriority getAccelPriority(OkuRunBot bot) {
        final MovePattern movePattern = movePatterns.get(currentTactic.getMovePatternId(bot));
        return movePattern.getAccelPriority(bot);
    }

    public HandlePriority getHandlePriority(OkuRunBot bot) {
        final MovePattern movePattern = movePatterns.get(currentTactic.getMovePatternId(bot));
        return movePattern.getHandlePriority(bot);
    }

    public double getMinSpeed(OkuRunBot bot) {
        final MovePattern movePattern = movePatterns.get(currentTactic.getMovePatternId(bot));
        return movePattern.getMinSpeed(bot);
    }

    public Map<MovePatternId, MovePattern> getMovePatterns() {
        return movePatterns;
    }

    public MovePatternId getMovePatternId(OkuRunBot bot) {
        return currentTactic.getMovePatternId(bot);
    }

    public MovePattern getMovePattern(MovePatternId movePatternId) {
        return movePatterns.get(movePatternId);
    }

    /**
     * 自分から見て相手の相対角度を計算します
     * 
     * @param bot
     * @param enamyState 攻撃対象の現在の状態
     * @return 相手の相対角度（-180度 〜 180度）180度はこちらを向いている
     */
    public double getEnemyLateralAngle(OkuRunBot bot, EnemyState enamyState) {
        if (caches.containsKey("enemyLateralAngle" + enamyState.id)) {
            return (double) caches.get("enemyLateralAngle" + enamyState.id);
        }
        final double enemyLateralAngle = Tool.getEnemyLateralAngle(bot.getX(), bot.getY(), enamyState.x, enamyState.y,
                enamyState.heading);
        caches.put("enemyLateralAngle" + enamyState.id, enemyLateralAngle);
        return enemyLateralAngle;
    }

    /**
     * ターン毎のアクションの前にコールされるイベント
     * このイベントはメインスレッドからコールされます
     * 
     * @param bot Bot
     */
    public void onPreAction(OkuRunBot bot) {
        caches.clear();
        if (currentTactic != null) {
            currentTactic.onAction(bot);
        }
    }

    /**
     * ターン毎のアクションイベント
     * このイベントはメインスレッドからコールされます
     * 
     * @param bot Bot
     */
    public void onAction(OkuRunBot bot) {
        setCurrentTactic(bot);
        if (currentTactic != null) {
            currentTactic.onAction(bot);
        }
    }

    /**
     * ターン毎のアクションの後にコールされるイベント
     * このイベントはメインスレッドからコールされます
     * 
     * @param bot Bot
     */
    public void onPostAction(OkuRunBot bot) {
        movePatterns.get(currentTactic.getMovePatternId(bot)).onPostAction(bot);
        if (currentTactic != null) {
            currentTactic.onPostAction(bot);
        }
    }

    /**
     * ゲームが開始された時の処理
     * 
     * @param e   ゲーム開始イベント
     * @param bot Bot
     */
    public void onGameStarted(GameStartedEvent e, OkuRunBot bot) {
    }

    /**
     * ゲームが終了した時の処理
     * 
     * @param e   ゲーム終了イベント
     * @param bot ボット
     */
    public void onGameEnded(GameEndedEvent e, OkuRunBot bot) {
        for (final Tactic tactic : tactics.values()) {
            tactic.onGameEnded(e, bot);
        }
        for (final MovePattern movePattern : movePatterns.values()) {
            movePattern.onGameEnded(e, bot);
        }
    }

    /**
     * ラウンドが終了した時の処理
     * 
     * @param e   ラウンド終了イベント
     * @param bot ボット
     */
    public void onRoundEnded(RoundEndedEvent e, OkuRunBot bot) {
        final int targetEnemyId = getTargetEnemyId(bot);
        for (final Tactic tactic : tactics.values()) {
            tactic.onRoundEnded(e, bot);
        }
        for (final MovePattern movePattern : movePatterns.values()) {
            movePattern.onRoundEnded(e, bot);
        }

        if (targetEnemyId != Commander.NO_TARGET) {
            final EnemyProfile enemyProfile = bot.getEnemyManager().getEnemyProfile(targetEnemyId);
            if (!isWon.get()) {
                // 敗北した場合、ムーブパターンを変更する
                System.out.println("*** I lost. I intend to consider changing my move pattern.");
                // 一番被弾率の低いムーブパターンを採用する（全体累計で評価）
                final MovePatternId movePatternId = movePatterns.entrySet().stream()
                        .sorted((a, b) -> Double.compare(a.getValue().getTotalHitPerTurn(),
                                b.getValue().getTotalHitPerTurn()))
                        .map(Map.Entry::getKey)
                        .findFirst().get();
                System.out.println("*** " + enemyProfile.getMovePatternId() + " -> " + movePatternId);
                enemyProfile.setMovePatternId(movePatternId);
            }
        }
        isWon.set(false);
    }

    /**
     * 弾丸が発射された時の処理
     * 
     * @param e   弾丸が発射されたイベント
     * @param bot ボット
     */
    public void onBulletFired(BulletFiredEvent e, OkuRunBot bot) {
        currentTactic.onBulletFired(e, bot);
    }

    /**
     * 弾丸が自分に当たった時の処理
     * 
     * @param e   弾丸が自分に当たったイベント
     * @param bot ボット
     */
    public void onHitByBullet(HitByBulletEvent e, OkuRunBot bot) {
        currentTactic.onHitByBullet(e, bot);
        final MovePattern movePattern = movePatterns.get(currentTactic.getMovePatternId(bot));
        movePattern.onHitByBullet(e, bot);
    }

    /**
     * ラウンドで勝利した時の処理
     * 
     * @param e   ラウンドで勝利したイベント
     * @param bot ボット
     */
    public void onWonRound(WonRoundEvent e, OkuRunBot bot) {
        isWon.set(true);
    }
}
