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
        predictor = new Predictor(arenaMap);
        radarOperator = new RadarOperator();
    }

    @Override
    public void run() {
        init();
        while (isRunning()) {
            action();
        }
    }

    private void init() {
        setBodyColor(Color.WHITE);
        setTurretColor(Color.RED);
        setTracksColor(Color.GRAY);
        arenaMap.init(getArenaHeight(), getArenaWidth());
        battleManager.init(this);
        predictor.init(this);
        commander.init(this);
        radarOperator.init(this);
        gunner.init(this);
        driver.init(this);
    }

    private void action() {
        clearGraphics();
        arenaMap.action(this);
        battleManager.action(this);
        predictor.action(this);
        commander.action(this);
        radarOperator.action(this);
        gunner.action(this);
        driver.action(this);
        go();
    }

    public double[] getPosition() {
        return new double[] { getX(), getY() };
    }

    public double bearingTo(double[] pos) {
        return bearingTo(pos[0], pos[1]);
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

    public void drawTarget(double x, double y, Color color) {
        var g = getGraphics();
        g.setFillColor(Color.fromRgba(color, 50));
        g.setStrokeColor(Color.fromRgba(color, 150));
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
     * @param x      X座標
     * @param y      Y座標
     * @param radius 半径
     * @param color  描画色
     */
    public void drawCircle(double x, double y, double radius, Color color) {
        var g = getGraphics();
        g.setFillColor(color);
        g.setStrokeColor(color);
        g.setStrokeWidth(1);
        g.fillCircle(x, y, radius);
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
    public void drawLine(double x1, double y1, double x2, double y2, Color color) {
        var g = getGraphics();
        g.setStrokeColor(color);
        g.setStrokeWidth(2);
        g.drawLine(x1, y1, x2, y2);
    }

    @Override
    public void onConnected(ConnectedEvent connectedEvent) {
        System.out.println("onConnected()");
    }

    @Override
    public void onDisconnected(DisconnectedEvent disconnectedEvent) {
        System.out.println("onDisconnected()");
    }

    @Override
    public void onConnectionError(ConnectionErrorEvent connectionErrorEvent) {
        System.out.println("onConnectionError(): " + connectionErrorEvent.getError());
    }

    @Override
    public void onGameStarted(GameStartedEvent gameStartedEvent) {
        System.out.println("onGameStarted()");
    }

    @Override
    public void onGameEnded(GameEndedEvent gameEndedEvent) {
        System.out.println("onGameEnded()");
    }

    @Override
    public void onRoundStarted(RoundStartedEvent roundStartedEvent) {
        System.out.println("onRoundStarted()");
    }

    @Override
    public void onRoundEnded(RoundEndedEvent roundEndedEvent) {
        System.out.println("onRoundEnded()");
        battleManager.onRoundEnded(roundEndedEvent, this);
        predictor.onRoundEnded(roundEndedEvent, this);
    }

    @Override
    public void onTick(TickEvent tickEvent) {
        // System.out.println(tickEvent.getTurnNumber() + ": onTick()");
    }

    @Override
    public void onBotDeath(BotDeathEvent botDeathEvent) {
        System.out.println(botDeathEvent.getTurnNumber() + ": onBotDeath(): " + botDeathEvent.getVictimId());
        battleManager.onBotDeath(botDeathEvent, this);
    }

    @Override
    public void onDeath(DeathEvent deathEvent) {
        System.out.println(deathEvent.getTurnNumber() + ": onDeath(): " + deathEvent.toString());
    }

    @Override
    public void onHitBot(HitBotEvent botHitBotEvent) {
        battleManager.onHitBot(botHitBotEvent, this);
    }

    @Override
    public void onHitWall(HitWallEvent botHitWallEvent) {
        System.out.println(botHitWallEvent.getTurnNumber() + ": onHitWall()");
    }

    @Override
    public void onBulletFired(BulletFiredEvent bulletFiredEvent) {
        battleManager.onBulletFired(bulletFiredEvent, this);
        predictor.onBulletFired(bulletFiredEvent, this);
    }

    @Override
    public void onHitByBullet(HitByBulletEvent hitByBulletEvent) {
        battleManager.onHitByBullet(hitByBulletEvent, this);
        commander.onHitByBullet(hitByBulletEvent, this);
    }

    @Override
    public void onBulletHit(BulletHitBotEvent bulletHitBotEvent) {
        battleManager.onBulletHit(bulletHitBotEvent, this);
        predictor.onBulletHit(bulletHitBotEvent, this);
    }

    @Override
    public void onBulletHitBullet(BulletHitBulletEvent bulletHitBulletEvent) {
        battleManager.onBulletHitBullet(bulletHitBulletEvent, this);
        predictor.onBulletHitBullet(bulletHitBulletEvent, this);
    }

    @Override
    public void onBulletHitWall(BulletHitWallEvent bulletHitWallEvent) {
        battleManager.onBulletHitWall(bulletHitWallEvent, this);
        predictor.onBulletHitWall(bulletHitWallEvent, this);
    }

    @Override
    public void onScannedBot(ScannedBotEvent scannedBotEvent) {
        battleManager.onScannedBot(scannedBotEvent, this);
    }

    @Override
    public void onSkippedTurn(SkippedTurnEvent skippedTurnEvent) {
        System.out.println(skippedTurnEvent.getTurnNumber() + ": onSkippedTurn()");
    }

    @Override
    public void onWonRound(WonRoundEvent wonRoundEvent) {
        System.out.println("onWonRound()");
    }

    @Override
    public void onCustomEvent(CustomEvent customEvent) {
        System.out.println(customEvent.getTurnNumber() + ": onCustomEvent()");
    }

    @Override
    public void onTeamMessage(TeamMessageEvent teamMessageEvent) {
        System.out.println(
                teamMessageEvent.getTurnNumber() + ": onTeamMessage(): " + teamMessageEvent.getMessage());
    }
}
