package okurun.commander;

import java.util.HashMap;
import java.util.Map;

import okurun.OkuRunBot;
import okurun.battlemanager.BattleManager;
import okurun.battlemanager.EnemyState;
import okurun.commander.tactics.*;

public class Commander {
    public static final int NO_TARGET = -1;

    private Map<String, Tactic> tactics = new HashMap<>();
    private Tactic currentTactic = null;

    public void init(OkuRunBot bot) {
        tactics.put(OneOnOneTactic.class.getName(), new OneOnOneTactic());
        tactics.put(SurvivalTactic.class.getName(), new SurvivalTactic());
    }

    public void action(OkuRunBot bot) {
        final BattleManager battleManager = bot.getBattleManager();
        currentTactic = tactics.get(SurvivalTactic.class.getName());
        if (battleManager.getAliveEnemyCount() < 2) {
            // 生存している敵が1機以下
            currentTactic = tactics.get(OneOnOneTactic.class.getName());
        }

        currentTactic.action(bot);
    }

    public int getTargetEnemyId(OkuRunBot bot) {
        return currentTactic.getTargetEnemyId(bot);
    }

    public double[] getTargetMovePosition(OkuRunBot bot) {
        return currentTactic.getTargetMovePosition(bot);
    }

    public double getBaseBulletPower(OkuRunBot bot) {
        return currentTactic.getBaseBulletPower(bot);
    }

    public String getPredictorModelName(OkuRunBot bot) {
        return currentTactic.getPredictorModelName(bot);
    }

    public String getGunActionName(OkuRunBot bot) {
        return currentTactic.getGunActionName(bot);
    }

    public String getRadarActionName(OkuRunBot bot) {
        return currentTactic.getRadarActionName(bot);
    }

    public String getDriveActionName(OkuRunBot bot) {
        return currentTactic.getDriveActionName(bot);
    }

    public boolean isZigzagAllowed(OkuRunBot bot) {
        return currentTactic.isZigzagAllowed(bot);
    }

    /**
     * 相手の相対角度を計算します
     * 
     * @param bot
     * @param enamyState 攻撃対象の現在の状態
     * @return 相手の相対角度（-180度 〜 180度）
     */
    public static double getEnemyLateralAngle(OkuRunBot bot, EnemyState enamyState) {
        return getEnemyLateralAngle(bot.getX(), bot.getY(), enamyState.x, enamyState.y, enamyState.heading);
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
}
