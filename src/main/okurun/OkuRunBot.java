package okurun;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.events.*;
import dev.robocode.tankroyale.botapi.graphics.Color;
import okurun.arenamap.ArenaMap;
import okurun.battlemanager.BattleManager;
import okurun.commander.Commander;
import okurun.driver.Driver;
import okurun.gunner.Gunner;
import okurun.predictor.Predictor;
import okurun.radaroperator.RadarOperator;

public class OkuRunBot extends Bot {
    public static final double BODY_SIZE = 40;

    public static void main(String[] args) {
        System.out.println("OkuRunBot.main() start");
        new OkuRunBot().start();
    }

    private final ArenaMap arenaMap;
    private final BattleManager battleManager;
    private final Commander commander;
    private final Driver driver;
    private final Gunner gunner;
    private final Predictor predictor;
    private final RadarOperator radarOperator;

    public OkuRunBot() {
        super();
        arenaMap = new ArenaMap();
        battleManager = new BattleManager();
        commander = new Commander();
        driver = new Driver();
        gunner = new Gunner();
        predictor = new Predictor();
        radarOperator = new RadarOperator();
    }

    @Override
    public void run() {
        init();
        while (isRunning()) {
            setTurretColor(Color.YELLOW);
            setBodyColor(Color.WHITE);
            setTracksColor(Color.GRAY);
            clearGraphics();
            action();
            go();
        }
    }

    private void init() {
        arenaMap.init(this);
        battleManager.init(this);
        predictor.init(this);
        commander.init(this);
        radarOperator.init(this);
        gunner.init(this);
        driver.init(this);
    }

    private void action() {
        arenaMap.action(this);
        battleManager.action(this);
        predictor.action(this);
        commander.action(this);
        radarOperator.action(this);
        gunner.action(this);
        driver.action(this);
    }

    public double[] getPosition() {
        return new double[] { getX(), getY() };
    }

    public double bearingTo(double[] pos) {
        return bearingTo(pos[0], pos[1]);
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

    public BattleManager getBattleManager() {
        return battleManager;
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

    private void clearGraphics() {
        var g = getGraphics();
        g.clear();
    }

    /**
     * 射撃目標を描画します
     * ※ 描画にはUI画面でDebug Graphicsを有効にする必要があります
     * 
     * @param pos   座標
     * @param color 描画色
     */
    public void drawTarget(double[] pos, Color color) {
        drawTarget(pos[0], pos[1], color);
    }

    /**
     * 射撃目標を描画します
     * ※ 描画にはUI画面でDebug Graphicsを有効にする必要があります
     * 
     * @param x     X座標
     * @param y     Y座標
     * @param color 描画色
     */
    private void drawTarget(double x, double y, Color color) {
        var g = getGraphics();
        g.setFillColor(Color.fromRgba(color, 30));
        g.setStrokeColor(Color.fromRgba(color, 60));
        g.setStrokeWidth(1);
        g.fillCircle(x, y, 10);
        g.drawCircle(x, y, 6);
        g.drawLine(x - 12, y, x + 12, y);
        g.drawLine(x, y - 12, x, y + 12);
    }

    /**
     * 画面に円を描画します
     * ※ 描画にはUI画面でDebug Graphicsを有効にする必要があります
     * 
     * @param pos    座標
     * @param radius 半径
     * @param color  描画色
     */
    public void drawFillCircle(double[] pos, double radius, Color color) {
        drawFillCircle(pos[0], pos[1], radius, color);
    }

    /**
     * 画面に円を描画します
     * ※ 描画にはUI画面でDebug Graphicsを有効にする必要があります
     * 
     * @param x      X座標
     * @param y      Y座標
     * @param radius 半径
     * @param color  描画色
     */
    private void drawFillCircle(double x, double y, double radius, Color color) {
        this.normalizeAbsoluteAngle(100);
        var g = getGraphics();
        g.setFillColor(color);
        g.setStrokeWidth(0);
        g.fillCircle(x, y, radius);
    }

    /**
     * 画面に直線を描画します
     * ※ 描画にはUI画面でDebug Graphicsを有効にする必要があります
     * 
     * @param pos1  始点座標
     * @param pos2  終点座標
     * @param color 描画色
     */
    public void drawLine(double[] pos1, double[] pos2, Color color) {
        drawLine(pos1[0], pos1[1], pos2[0], pos2[1], color);
    }

    /**
     * 画面に直線を描画します
     * ※ 描画にはUI画面でDebug Graphicsを有効にする必要があります
     * 
     * @param x1    始点X座標
     * @param y1    始点Y座標
     * @param x2    終点X座標
     * @param y2    終点Y座標
     * @param color 描画色
     */
    private void drawLine(double x1, double y1, double x2, double y2, Color color) {
        var g = getGraphics();
        g.setStrokeColor(color);
        g.setStrokeWidth(2);
        g.drawLine(x1, y1, x2, y2);
    }

    /**
     * サーバーから接続した時の処理
     * 
     * @param e 接続イベント
     */
    @Override
    public void onConnected(ConnectedEvent e) {
        System.out.println("onConnected()");
    }

    /**
     * サーバーから切断した時の処理
     * 
     * @param e 切断イベント
     */
    @Override
    public void onDisconnected(DisconnectedEvent e) {
        System.out.println("onDisconnected()");
    }

    /**
     * 接続エラーが発生した時の処理
     * 
     * @param e 接続エラーイベント
     */
    @Override
    public void onConnectionError(ConnectionErrorEvent e) {
        System.out.println("onConnectionError(): " + e.getError());
    }

    /**
     * ゲームが開始された時の処理
     * 
     * @param e ゲーム開始イベント
     */
    @Override
    public void onGameStarted(GameStartedEvent e) {
        System.out.println("onGameStarted()");
    }

    /**
     * ゲームが終了した時の処理
     * 
     * @param e ゲーム終了イベント
     */
    @Override
    public void onGameEnded(GameEndedEvent e) {
        predictor.onGameEnded(e);
    }

    /**
     * ラウンドが開始した時の処理
     * 
     * @param e ラウンド開始イベント
     */
    @Override
    public void onRoundStarted(RoundStartedEvent e) {
        System.out.println("onRoundStarted()");
    }

    /**
     * ラウンドが終了した時の処理
     * 
     * @param e ラウンド終了イベント
     */
    @Override
    public void onRoundEnded(RoundEndedEvent e) {
        System.out.println("onRoundEnded()");
        battleManager.onRoundEnded(e, this);
        predictor.onRoundEnded(e, this);
    }

    /**
     * 1ターン中の一定時間経過毎に呼ばれる
     * 
     * @param e 1ターン中の一定時間経過イベント
     */
    @Override
    public void onTick(TickEvent e) {
        // System.out.println(e.getTurnNumber() + ": onTick()");
    }

    /**
     * 敵ボットが死んだ時の処理
     * 
     * @param e 敵ボットが死んだイベント
     */
    @Override
    public void onBotDeath(BotDeathEvent e) {
        System.out.println(e.getTurnNumber() + ": onBotDeath(): " + e.getVictimId());
        battleManager.onBotDeath(e, this);
    }

    /**
     * 自分が死んだ時の処理
     * 
     * @param e 自分が死んだイベント
     */
    @Override
    public void onDeath(DeathEvent e) {
        System.out.println(e.getTurnNumber() + ": onDeath(): " + e.toString());
    }

    /**
     * 自分が敵ボットにぶつかった時の処理
     * 
     * @param e 敵ボットにぶつかったイベント
     */
    @Override
    public void onHitBot(HitBotEvent e) {
        battleManager.onHitBot(e, this);
    }

    /**
     * 壁にぶつかった時の処理
     * 
     * @param e 壁にぶつかったイベント
     */
    @Override
    public void onHitWall(HitWallEvent e) {
        System.out.println(e.getTurnNumber() + ": !!!! onHitWall() !!!!");
    }

    /**
     * 弾丸が発射された時の処理
     * 
     * @param e 弾丸が発射されたイベント
     */
    @Override
    public void onBulletFired(BulletFiredEvent e) {
        battleManager.onBulletFired(e, this);
        predictor.onBulletFired(e, this);
    }

    /**
     * 自分が敵の弾に当った時の処理
     * 
     * @param e 当たった弾丸のイベント
     */
    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        battleManager.onHitByBullet(e, this);
        commander.onHitByBullet(e, this);
    }

    /**
     * 弾丸が敵ボットに当たった時の処理
     * 
     * @param e 弾丸が敵ボットに当たったイベント
     */
    @Override
    public void onBulletHit(BulletHitBotEvent e) {
        predictor.onBulletHit(e, this);
        battleManager.onBulletHit(e, this);
    }

    /**
     * 弾丸が弾丸に当たった時の処理
     * 
     * @param e 弾丸が弾丸に当たったイベント
     */
    @Override
    public void onBulletHitBullet(BulletHitBulletEvent e) {
        predictor.onBulletHitBullet(e, this);
        battleManager.onBulletHitBullet(e, this);
    }

    /**
     * 弾丸が壁に当たった時の処理
     * 
     * @param e 弾丸が壁に当たったイベント
     */
    @Override
    public void onBulletHitWall(BulletHitWallEvent e) {
        predictor.onBulletHitWall(e, this);
        battleManager.onBulletHitWall(e, this);
    }

    /**
     * 敵ボットがスキャンされた時の処理
     * 
     * @param e 敵ボットがスキャンされたイベント
     */
    @Override
    public void onScannedBot(ScannedBotEvent e) {
        battleManager.onScannedBot(e, this);
    }

    /**
     * ターンがスキップされた時の処理
     * 
     * @param e ターンがスキップされたイベント
     */
    @Override
    public void onSkippedTurn(SkippedTurnEvent e) {
        System.out.println(e.getTurnNumber() + ": !!!! #### onSkippedTurn() #### !!!!");
    }

    /**
     * ラウンドで勝利した時の処理
     * 
     * @param e ラウンドで勝利したイベント
     */
    @Override
    public void onWonRound(WonRoundEvent e) {
        System.out.println("--- onWonRound() ---");
    }

    /**
     * カスタムイベントが発生した時の処理
     * 
     * @param e カスタムイベント
     */
    @Override
    public void onCustomEvent(CustomEvent e) {
        System.out.println(e.getTurnNumber() + ": onCustomEvent()");
    }

    /**
     * チームメッセージを受信した時の処理
     * 
     * @param e チームメッセージイベント
     */
    @Override
    public void onTeamMessage(TeamMessageEvent e) {
        System.out.println(
                e.getTurnNumber() + ": onTeamMessage(): " + e.getMessage());
    }
}
