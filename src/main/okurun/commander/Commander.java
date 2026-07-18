package okurun.commander;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import dev.robocode.tankroyale.botapi.events.*;
import okurun.OkuRunBot;
import okurun.battlemanager.BattleManager;
import okurun.battlemanager.EnemyProfile;
import okurun.battlemanager.EnemyState;
import okurun.commander.movepattern.*;
import okurun.commander.tactics.*;
import okurun.driver.Driver;
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
        // SAFE_AREA_V2,
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
        // movePatterns.put(MovePatternId.SAFE_AREA_V2, new SafeAreaV2MovePattern());
    }

    private void setCurrentTactic(OkuRunBot bot) {
        final BattleManager battleManager = bot.getBattleManager();
        if (battleManager.getAliveAndNotMissingEnemyCount(bot) <= 1) {
            // 生存している敵が1機のみ
            final EnemyProfile enemyProfile = battleManager.getAliveEnemy(bot);
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
        final double enemyLateralAngle = getEnemyLateralAngle(bot.getX(), bot.getY(), enamyState.x, enamyState.y,
                enamyState.heading);
        caches.put("enemyLateralAngle" + enamyState.id, enemyLateralAngle);
        return enemyLateralAngle;
    }

    /**
     * 自分から見て相手が何度横に向いているかを計算します。
     * 
     * @param myX          自分のX座標
     * @param myY          自分のY座標
     * @param enemyX       相手のX座標
     * @param enemyY       相手のY座標
     * @param enemyHeading 相手の向いている方向（度数法：0〜360）
     * @return 相手の相対角度（-180度 〜 180度）
     *         0度: 相手が自分から遠ざかる方向を向いている
     *         180度（または-180度）: 相手が自分の方（真正面）を向いている
     *         90度: 相手が自分から見て右に直角に動こうとしている
     *         -90度: 相手が自分から見て左に直角に動こうとしている
     */
    private static double getEnemyLateralAngle(double myX, double myY, double enemyX, double enemyY,
            double enemyHeading) {
        // 1. 自分から見た相手への絶対角度（アブソリュート・ベアリング）を計算
        // Robocodeでは上が0度なので、atan2(x, y) の順になります
        double absoluteBearing = Math.toDegrees(Math.atan2(enemyX - myX, enemyY - myY));

        // 2. 相手の進行方向と、自分から見た相手の角度の差を求める
        double relativeAngle = enemyHeading - absoluteBearing;

        // 3. 角度を -180度 〜 180度 の範囲に正規化する
        while (relativeAngle <= -180) {
            relativeAngle += 360;
        }
        while (relativeAngle > 180) {
            relativeAngle -= 360;
        }

        return relativeAngle;
    }

    /**
     * 敵との相対的な接近速度を計算します。
     * マイナスの場合は近づいており、プラスの場合は遠ざかっています。
     * 
     * @param bot
     * @param enemyState 攻撃対象の現在の状態
     * @return 敵との相対的な接近速度
     */
    public static double getApproachVelocity(OkuRunBot bot, EnemyState enemyState) {
        // Tank Royale APIの direction/heading は「度(Degrees)」で返ってくるため、
        // 計算用に「ラジアン(Radians)」に変換して渡す必要があります。
        return getApproachVelocity(
                bot.getX(), bot.getY(), Math.toRadians(bot.getDirection()), bot.getSpeed(),
                enemyState.x, enemyState.y, Math.toRadians(enemyState.heading), enemyState.velocity);
    }

    /**
     * 敵との相対的な接近速度を計算します。
     * マイナスの場合は近づいており、プラスの場合は遠ざかっています。
     *
     * @param myX                 自分のX座標
     * @param myY                 自分のY座標
     * @param myHeadingRadians    自分の向いている方向（ラジアン）
     * @param myVelocity          自分の速度
     * @param enemyX              敵のX座標
     * @param enemyY              敵のY座標
     * @param enemyHeadingRadians 敵の向いている方向（ラジアン）
     * @param enemyVelocity       敵の速度
     * @return 接近速度（マイナスなら接近、プラスなら離脱）
     */
    private static double getApproachVelocity(
            double myX, double myY, double myHeadingRadians, double myVelocity,
            double enemyX, double enemyY, double enemyHeadingRadians, double enemyVelocity) {

        // 自分の速度ベクトル（Robocodeは上が0度なので、X=sin, Y=cos）
        double myVx = myVelocity * Math.sin(myHeadingRadians);
        double myVy = myVelocity * Math.cos(myHeadingRadians);

        // 敵の速度ベクトル
        double enemyVx = enemyVelocity * Math.sin(enemyHeadingRadians);
        double enemyVy = enemyVelocity * Math.cos(enemyHeadingRadians);

        // 自分から見た敵の相対速度ベクトル
        double relVx = enemyVx - myVx;
        double relVy = enemyVy - myVy;

        // 自分から敵への絶対角度（ラジアン）
        // （Robocodeでは上が0度なので、atan2の引数はX, Yの順）
        double absBearingRadians = Math.atan2(enemyX - myX, enemyY - myY);

        // 自分から敵へ向かう単位ベクトル（視線ベクトル）
        double losX = Math.sin(absBearingRadians);
        double losY = Math.cos(absBearingRadians);

        // 相対速度ベクトルを視線ベクトルに投影（内積）して、距離の変化速度を出す
        double separationVelocity = (relVx * losX) + (relVy * losY);

        // 近づいているときが負、遠ざかるときが正
        return separationVelocity;
    }

    /**
     * ターン毎のアクションの前にコールされるイベント
     * このイベントはメインスレッドからコールされます
     * 
     * @param bot Bot
     */
    public void onPreAction(OkuRunBot bot) {
        caches.clear();
        for (Tactic tactic : tactics.values()) {
            tactic.preAction(bot);
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
            currentTactic.action(bot);
        }
    }

    /**
     * ターン毎のアクションの後にコールされるイベント
     * このイベントはメインスレッドからコールされます
     * 
     * @param bot Bot
     */
    public void onPostAction(OkuRunBot bot) {
        movePatterns.get(currentTactic.getMovePatternId(bot)).postAction(bot);
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
            final EnemyProfile enemyProfile = bot.getBattleManager().getEnemyProfile(targetEnemyId);
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
