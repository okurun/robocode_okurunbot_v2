package okurun;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.BulletState;
import dev.robocode.tankroyale.botapi.events.*;
import dev.robocode.tankroyale.botapi.graphics.Color;
import okurun.arenamap.ArenaMap;
import okurun.battlemanager.BattleManager;
import okurun.battlemanager.BulletStatus;
import okurun.battlemanager.EnemyProfile;
import okurun.commander.Commander;
import okurun.driver.Driver;
import okurun.gunner.Gunner;
import okurun.predictor.PredictionAccuracy;
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
        System.out.println("OkuRunBot.run() start");
        init();
        while (isRunning()) {
            action();
        }
    }

    private void init() {
        System.out.println("OkuRunBot.init() start");
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
        g.drawLine(x - 11, y, x + 11, y);
        g.drawLine(x, y - 11, x, y + 11);
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
        System.out.println("OkuRunBot.onConnected()");
    }

    @Override
    public void onDisconnected(DisconnectedEvent disconnectedEvent) {
        System.out.println("OkuRunBot.onDisconnected()");
    }

    @Override
    public void onConnectionError(ConnectionErrorEvent connectionErrorEvent) {
        System.out.println("OkuRunBot.onConnectionError(): " + connectionErrorEvent.getError());
    }

    @Override
    public void onGameStarted(GameStartedEvent gameStartedEvent) {
        System.out.println("OkuRunBot.onGameStarted()");
    }

    @Override
    public void onGameEnded(GameEndedEvent gameEndedEvent) {
        System.out.println("OkuRunBot.onGameEnded()");
    }

    @Override
    public void onRoundStarted(RoundStartedEvent roundStartedEvent) {
    }

    @Override
    public void onRoundEnded(RoundEndedEvent roundEndedEvent) {
        battleManager.onRoundEnded(roundEndedEvent, this);
        predictor.onRoundEnded(roundEndedEvent, this);
    }

    @Override
    public void onTick(TickEvent tickEvent) {
        // System.out.println("OkuRunBot.onTick()");
    }

    @Override
    public void onBotDeath(BotDeathEvent botDeathEvent) {
        System.out.println(getTurnNumber() + ": OkuRunBot.onBotDeath(): " + botDeathEvent.getVictimId());
        battleManager.onBotDeath(botDeathEvent, this);
    }

    @Override
    public void onDeath(DeathEvent deathEvent) {
        System.out.println(getTurnNumber() + ": OkuRunBot.onDeath(): " + deathEvent.toString());
    }

    @Override
    public void onHitBot(HitBotEvent botHitBotEvent) {
        if (botHitBotEvent.getEnergy() <= 0) {
            final EnemyProfile enemyProfile = battleManager.getEnemyProfile(botHitBotEvent.getVictimId());
            if (enemyProfile != null) {
                enemyProfile.died();
            }
        }
    }

    @Override
    public void onHitWall(HitWallEvent botHitWallEvent) {
        setTracksColor(Color.RED);
    }

    @Override
    public void onBulletFired(BulletFiredEvent bulletFiredEvent) {
        battleManager.setLastFiredTurnNum(bulletFiredEvent.getTurnNumber());
        final BulletStatus bulletStatus = battleManager.bulletStack.pollFirst();
        if (bulletStatus != null) {
            bulletStatus.bulletState = bulletFiredEvent.getBullet();
            battleManager.bullets.put(bulletStatus.bulletState.getBulletId(), bulletStatus);
            final PredictionAccuracy predictionAccuracy = predictor.predictionAccuracies.get(bulletStatus.predictModel);
            if (predictionAccuracy == null) {
                System.out.println(getTurnNumber() + " onBulletFired: predictionAccuracy is null");
            }
            predictionAccuracy.incrementFireCount();
        }
    }

    @Override
    public void onHitByBullet(HitByBulletEvent hitByBulletEvent) {
    }

    @Override
    public void onBulletHit(BulletHitBotEvent bulletHitBotEvent) {
        final BulletState bulletState = bulletHitBotEvent.getBullet();
        final int bulletId = bulletState.getBulletId();
        final BulletStatus bulletStatus = battleManager.bullets.get(bulletId);

        battleManager.bullets.remove(bulletId);
        if (bulletHitBotEvent.getEnergy() <= 0) {
            final EnemyProfile enemyProfile = battleManager.getEnemyProfile(bulletHitBotEvent.getVictimId());
            if (enemyProfile != null) {
                enemyProfile.died();
            }
        }

        if (bulletHitBotEvent.getVictimId() == bulletStatus.targetEnemyId) {
            predictor.predictionAccuracies.get(bulletStatus.predictModel).incrementHitCount();
        } else {
            predictor.predictionAccuracies.get(bulletStatus.predictModel).incrementMissCount();
        }

    }

    @Override
    public void onBulletHitBullet(BulletHitBulletEvent bulletHitBulletEvent) {
        final BulletState bulletState = bulletHitBulletEvent.getBullet();
        final int bulletId = bulletState.getBulletId();
        final BulletStatus bulletStatus = battleManager.bullets.get(bulletId);

        battleManager.bullets.remove(bulletId);
        predictor.predictionAccuracies.get(bulletStatus.predictModel).incrementMissCount();
    }

    @Override
    public void onBulletHitWall(BulletHitWallEvent bulletHitWallEvent) {
        final BulletState bulletState = bulletHitWallEvent.getBullet();
        final int bulletId = bulletState.getBulletId();
        final BulletStatus bulletStatus = battleManager.bullets.get(bulletId);

        battleManager.bullets.remove(bulletId);
        predictor.predictionAccuracies.get(bulletStatus.predictModel).incrementMissCount();
    }

    @Override
    public void onScannedBot(ScannedBotEvent scannedBotEvent) {
        battleManager.onScannedBot(scannedBotEvent, this);
    }

    @Override
    public void onSkippedTurn(SkippedTurnEvent skippedTurnEvent) {
        System.out.println(getTurnNumber() + ": OkuRunBot.onSkippedTurn()");
    }

    @Override
    public void onWonRound(WonRoundEvent wonRoundEvent) {
        System.out.println("OkuRunBot.onWonRound()");
    }

    @Override
    public void onCustomEvent(CustomEvent customEvent) {
        System.out.println("OkuRunBot.onCustomEvent()");
    }

    @Override
    public void onTeamMessage(TeamMessageEvent teamMessageEvent) {
        System.out.println("OkuRunBot.onTeamMessage()");
    }
}
