package okurun;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.events.*;
import dev.robocode.tankroyale.botapi.graphics.Color;
import okurun.arenamap.ArenaMap;
import okurun.commander.Commander;
import okurun.driver.Driver;
import okurun.enemymanager.EnemyManager;
import okurun.gunner.Gunner;
import okurun.predictor.Predictor;
import okurun.radaroperator.RadarOperator;

public class OkuRunBot extends Bot {
    public static final double BODY_SIZE = 40;

    public static void main(String[] args) {
        new OkuRunBot().start();
    }

    private final ArenaMap arenaMap;
    private final EnemyManager enemyManager;
    private final Commander commander;
    private final Driver driver;
    private final Gunner gunner;
    private final Predictor predictor;
    private final RadarOperator radarOperator;
    private final Debugger debugger;

    public OkuRunBot() {
        super();
        arenaMap = new ArenaMap();
        enemyManager = new EnemyManager();
        commander = new Commander();
        driver = new Driver();
        gunner = new Gunner();
        predictor = new Predictor();
        radarOperator = new RadarOperator();
        debugger = new Debugger();
    }

    @Override
    public void run() {
        System.out.println("- run()");
        while (isRunning()) {
            onPreAction();
            onAction();
            onPostAction();
            go();
        }
    }

    public double[] getPosition() {
        return new double[] { getX(), getY() };
    }

    public double bearingTo(double[] pos) {
        return bearingTo(pos[0], pos[1]);
    }

    public double gunBearingTo(double[] pos) {
        return gunBearingTo(pos[0], pos[1]);
    }

    public double directionTo(double[] pos) {
        return directionTo(pos[0], pos[1]);
    }

    public double distanceTo(double[] pos) {
        return distanceTo(pos[0], pos[1]);
    }

    public ArenaMap getArenaMap() {
        return arenaMap;
    }

    public EnemyManager getEnemyManager() {
        return enemyManager;
    }

    public Commander getCommander() {
        return commander;
    }

    public Driver getDriver() {
        return driver;
    }

    public Gunner getGunner() {
        return gunner;
    }

    public Predictor getPredictor() {
        return predictor;
    }

    public RadarOperator getRadarOperator() {
        return radarOperator;
    }

    public Debugger getDebugger() {
        return debugger;
    }

    /**
     * ターン毎のアクションの前にコールされるイベント
     * このイベントはメインスレッドからコールされます
     */
    private void onPreAction() {
        setTurretColor(Color.YELLOW);
        setBodyColor(Color.WHITE);
        setTracksColor(Color.GRAY);
        setScanColor(Color.fromRgba(Color.WHITE, 30));

        try {
            arenaMap.onPreAction(this);
            enemyManager.onPreAction(this);
            predictor.onPreAction(this);
            commander.onPreAction(this);
            radarOperator.onPreAction(this);
            gunner.onPreAction(this);
            driver.onPreAction(this);
            debugger.onPreAction(this);
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }

    /**
     * ターン毎のアクションイベント
     * このイベントはメインスレッドからコールされます
     */
    private void onAction() {
        try {
            arenaMap.onAction(this);
            enemyManager.onAction(this);
            predictor.onAction(this);
            commander.onAction(this);
            radarOperator.onAction(this);
            gunner.onAction(this);
            driver.onAction(this);
            debugger.onAction(this);
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }

    /**
     * ターン毎のアクションの後にコールされるイベント
     * このイベントはメインスレッドからコールされます
     */
    private void onPostAction() {
        try {
            arenaMap.onPostAction(this);
            enemyManager.onPostAction(this);
            predictor.onPostAction(this);
            commander.onPostAction(this);
            radarOperator.onPostAction(this);
            gunner.onPostAction(this);
            driver.onPostAction(this);
            debugger.onPostAction(this);
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }

    /**
     * サーバーから接続した時の処理
     * 
     * @param e 接続イベント
     */
    @Override
    public void onConnected(ConnectedEvent e) {
        System.out.println("- onConnected()");
    }

    /**
     * サーバーから切断した時の処理
     * 
     * @param e 切断イベント
     */
    @Override
    public void onDisconnected(DisconnectedEvent e) {
        System.out.println("- onDisconnected()");
    }

    /**
     * 接続エラーが発生した時の処理
     * 
     * @param e 接続エラーイベント
     */
    @Override
    public void onConnectionError(ConnectionErrorEvent e) {
        System.out.println("!!! onConnectionError(): " + e.getError());
    }

    /**
     * ゲームが開始された時の処理
     * 
     * @param e ゲーム開始イベント
     */
    @Override
    public void onGameStarted(GameStartedEvent e) {
        System.out.println("- onGameStarted()");
        try {
            arenaMap.onGameStarted(e, this);
            enemyManager.onGameStarted(e, this);
            predictor.onGameStarted(e, this);
            commander.onGameStarted(e, this);
            radarOperator.onGameStarted(e, this);
            gunner.onGameStarted(e, this);
            driver.onGameStarted(e, this);
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }

    /**
     * ゲームが終了した時の処理
     * 
     * @param e ゲーム終了イベント
     */
    @Override
    public void onGameEnded(GameEndedEvent e) {
        try {
            predictor.onGameEnded(e, this);
            commander.onGameEnded(e, this);
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }

    /**
     * ラウンドが開始した時の処理
     * 
     * @param e ラウンド開始イベント
     */
    @Override
    public void onRoundStarted(RoundStartedEvent e) {
        System.out.println("------ onRoundStarted(" + e.getRoundNumber() + ") ------");
    }

    /**
     * ラウンドが終了した時の処理
     * 
     * @param e ラウンド終了イベント
     */
    @Override
    public void onRoundEnded(RoundEndedEvent e) {
        System.out.println("------ onRoundEnded(" + e.getRoundNumber() + "): " + e.getTurnNumber() + " -----");
        try {
            enemyManager.onRoundEnded(e, this);
            commander.onRoundEnded(e, this);
            predictor.onRoundEnded(e, this);
            gunner.onRoundEnded(e, this);
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }

    /**
     * 1ターン中の一定時間経過毎に呼ばれる
     * 
     * @param e 1ターン中の一定時間経過イベント
     */
    @Override
    public void onTick(TickEvent e) {
        try {
            enemyManager.onTick(e, this);
            gunner.onTick(e, this);
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }

    /**
     * 敵ボットが死んだ時の処理
     * 
     * @param e 敵ボットが死んだイベント
     */
    @Override
    public void onBotDeath(BotDeathEvent e) {
        System.out.println("- onBotDeath(" + e.getTurnNumber() + "): " + e.getVictimId());
        try {
            enemyManager.onBotDeath(e, this);
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }

    /**
     * 自分が死んだ時の処理
     * 
     * @param e 自分が死んだイベント
     */
    @Override
    public void onDeath(DeathEvent e) {
        System.out.println("- onDeath(" + e.getTurnNumber() + "): " + e.toString());
    }

    /**
     * 自分が敵ボットにぶつかった時の処理
     * 
     * @param e 敵ボットにぶつかったイベント
     */
    @Override
    public void onHitBot(HitBotEvent e) {
        try {
            enemyManager.onHitBot(e, this);
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }

    /**
     * 壁にぶつかった時の処理
     * 
     * @param e 壁にぶつかったイベント
     */
    @Override
    public void onHitWall(HitWallEvent e) {
        System.out.println("!!!! onHitWall(" + e.getTurnNumber() + ") !!!!");
    }

    /**
     * 弾丸が発射された時の処理
     * 
     * @param e 弾丸が発射されたイベント
     */
    @Override
    public void onBulletFired(BulletFiredEvent e) {
        try {
            gunner.onBulletFired(e, this); // 弾丸管理のために最初に実行します
            enemyManager.onBulletFired(e, this);
            predictor.onBulletFired(e, this);
            commander.onBulletFired(e, this);
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }

    /**
     * 自分が敵の弾に当った時の処理
     * 
     * @param e 当たった弾丸のイベント
     */
    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        try {
            enemyManager.onHitByBullet(e, this);
            commander.onHitByBullet(e, this);
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }

    /**
     * 弾丸が敵ボットに当たった時の処理
     * 
     * @param e 弾丸が敵ボットに当たったイベント
     */
    @Override
    public void onBulletHit(BulletHitBotEvent e) {
        try {
            predictor.onBulletHit(e, this);
            enemyManager.onBulletHit(e, this);
            gunner.onBulletHit(e, this);
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }

    /**
     * 弾丸が弾丸に当たった時の処理
     * 
     * @param e 弾丸が弾丸に当たったイベント
     */
    @Override
    public void onBulletHitBullet(BulletHitBulletEvent e) {
        try {
            predictor.onBulletHitBullet(e, this);
            enemyManager.onBulletHitBullet(e, this);
            gunner.onBulletHitBullet(e, this);
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }

    /**
     * 弾丸が壁に当たった時の処理
     * 
     * @param e 弾丸が壁に当たったイベント
     */
    @Override
    public void onBulletHitWall(BulletHitWallEvent e) {
        try {
            predictor.onBulletHitWall(e, this);
            enemyManager.onBulletHitWall(e, this);
            gunner.onBulletHitWall(e, this);
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }

    /**
     * 敵ボットがスキャンされた時の処理
     * 
     * @param e 敵ボットがスキャンされたイベント
     */
    @Override
    public void onScannedBot(ScannedBotEvent e) {
        try {
            enemyManager.onScannedBot(e, this);
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }

    /**
     * ターンがスキップされた時の処理
     * 
     * @param e ターンがスキップされたイベント
     */
    @Override
    public void onSkippedTurn(SkippedTurnEvent e) {
        System.out.println("!!! onSkippedTurn(" + e.getTurnNumber() + ") !!!");
    }

    /**
     * ラウンドで勝利した時の処理
     * 
     * @param e ラウンドで勝利したイベント
     */
    @Override
    public void onWonRound(WonRoundEvent e) {
        System.out.println("@=@=@ onWonRound() @=@=@");
        try {
            commander.onWonRound(e, this);
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }

    /**
     * カスタムイベントが発生した時の処理
     * 
     * @param e カスタムイベント
     */
    @Override
    public void onCustomEvent(CustomEvent e) {
        System.out.println("- onCustomEvent(" + e.getTurnNumber() + ")");
    }

    /**
     * チームメッセージを受信した時の処理
     * 
     * @param e チームメッセージイベント
     */
    @Override
    public void onTeamMessage(TeamMessageEvent e) {
        System.out.println(
                "onTeamMessage(" + e.getTurnNumber() + "): " + e.getMessage());
    }

}
