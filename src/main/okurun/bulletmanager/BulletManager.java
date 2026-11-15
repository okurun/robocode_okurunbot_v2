package okurun.bulletmanager;

import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import dev.robocode.tankroyale.botapi.BulletState;
import dev.robocode.tankroyale.botapi.Constants;
import dev.robocode.tankroyale.botapi.events.BulletFiredEvent;
import dev.robocode.tankroyale.botapi.events.BulletHitBotEvent;
import dev.robocode.tankroyale.botapi.events.BulletHitBulletEvent;
import dev.robocode.tankroyale.botapi.events.BulletHitWallEvent;
import dev.robocode.tankroyale.botapi.events.GameEndedEvent;
import dev.robocode.tankroyale.botapi.events.RoundEndedEvent;
import dev.robocode.tankroyale.botapi.events.TickEvent;
import okurun.OkuRunBot;
import okurun.commander.Commander;
import okurun.driver.actions.DriveAction.DriveParams;
import okurun.enemymanager.EnemyProfile;
import okurun.enemymanager.EnemyState;

public class BulletManager {
    private final Deque<EnemyBulletWave> enemyBulletWaves = new ConcurrentLinkedDeque<>();
    private final Map<Integer, MyBullet> myBulletHitStates = new ConcurrentHashMap<>();
    private final Map<Integer, MyBullet> myFlyingBullets = new ConcurrentHashMap<>();
    private final Map<Integer, MyBullet> myBulletHistory = new ConcurrentHashMap<>();

    /**
     * ターン毎のアクションの前にコールされるイベント
     * このイベントはメインスレッドからコールされます
     * 
     * @param bot Bot
     */
    public void onPreAction(OkuRunBot bot) {
        // 登録済みの波を更新
        enemyBulletWaves.removeIf(wave -> {
            wave.update(bot);
            return wave.isOver(bot);
        });

        // 前ターンに敵が発砲したなら波を追加する
        addNewEnemyBulletWave(bot);
    }

    /**
     * ターン毎のアクションの後にコールされるイベント
     * このイベントはメインスレッドからコールされます
     * 
     * @param bot Bot
     */
    public void onPostAction(OkuRunBot bot) {
    }

    /**
     * ラウンド終了時の処理
     * 
     * @param e
     * @param bot
     */
    public void onRoundEnded(RoundEndedEvent e, OkuRunBot bot) {
        try {
            enemyBulletWaves.clear();
            myFlyingBullets.clear();
            myBulletHistory.clear();
            myBulletHitStates.clear();
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
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }

    /**
     * 1ターン中の一定時間経過毎に呼ばれる
     * 
     * @param e   1ターン中の一定時間経過イベント
     * @param bot Bot
     */
    public void onTick(TickEvent e, OkuRunBot bot) {
        try {
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }

    /**
     * 自分が弾を発射した時の処理
     * 
     * @param e   弾が発射されたイベント
     * @param bot Bot
     */
    public void onBulletFired(BulletFiredEvent e, OkuRunBot bot) {
        try {
            addFlyingMyBullet(e.getBullet(), e.getTurnNumber());
            final MyBullet flyingBullet = getFlyingMyBullet(e.getBullet().getBulletId());
            if (flyingBullet == null) {
                return;
            }
            bot.getEnemyManager().getEnemyProfile(flyingBullet.targetEnemyId).onBulletFired(e, bot);
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }

    /**
     * 弾丸が敵ボットに当たった時の処理
     * 
     * @param e   弾丸が敵ボットに当たったイベント
     * @param bot
     */
    public void onBulletHit(BulletHitBotEvent e, OkuRunBot bot) {
        try {
            final BulletState bulletState = e.getBullet();
            final MyBullet myBullet = myFlyingBullets.get(bulletState.getBulletId());
            if (myBullet == null) {
                return;
            }
            myBullet.damage = e.getDamage();
            myBullet.hitTurnNumber = e.getTurnNumber();
            myBullet.hitEnemyId = e.getVictimId();
            myBulletHitStates.put(e.getVictimId(), myBullet);
            removeFlyingMyBullets(e.getBullet().getBulletId());
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }

    /**
     * 弾が弾に当たった時の処理
     * 
     * @param e   弾が弾に当たったイベント
     * @param bot Bot
     */
    public void onBulletHitBullet(BulletHitBulletEvent e, OkuRunBot bot) {
        try {
            removeFlyingMyBullets(e.getBullet().getBulletId());
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }

    /**
     * 弾が壁に当たった時の処理
     * 
     * @param e   弾が壁に当たったイベント
     * @param bot Bot
     */
    public void onBulletHitWall(BulletHitWallEvent e, OkuRunBot bot) {
        try {
            removeFlyingMyBullets(e.getBullet().getBulletId());
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }

    /**
     * 波一覧を返します
     * 
     * @return
     */
    public List<EnemyBulletWave> getEnemyBulletWaves() {
        return enemyBulletWaves.stream().toList();
    }

    /**
     * 一番近い波を返します
     * 
     * @param bot
     * @return
     */
    public EnemyBulletWave getNearestEnemyBulletWave(OkuRunBot bot) {
        int minTurn = Integer.MAX_VALUE;
        EnemyBulletWave nearestWave = null;
        for (EnemyBulletWave wave : enemyBulletWaves) {
            final int turn = wave.getCountDown();
            if (turn < minTurn) {
                minTurn = turn;
                nearestWave = wave;
            }
        }
        return nearestWave;
    }

    /**
     * 発砲履歴を返します
     * 
     * @return
     */
    public Collection<MyBullet> getMyFlyingBullets() {
        return myFlyingBullets.values();
    }

    /**
     * 飛んでいる弾丸を取得します
     * 
     * @param bulletId 弾丸ID
     * @return 弾丸履歴
     */
    public MyBullet getFlyingMyBullet(int bulletId) {
        return myFlyingBullets.get(bulletId);
    }

    /**
     * 弾丸履歴を追加します
     * 
     * @param bullet 弾丸履歴
     */
    public void addMyBulletHistory(MyBullet bullet) {
        myBulletHistory.put(bullet.firedTurnNumber, bullet);
    }

    /**
     * 敵の発砲を検知して新しい波を追加します
     * 
     * @param bot
     */
    private void addNewEnemyBulletWave(OkuRunBot bot) {
        final Commander commander = bot.getCommander();
        final int targetEnemyId = commander.getTargetEnemyId(bot);
        if (targetEnemyId == Commander.NO_TARGET) {
            return;
        }

        final EnemyProfile enemy = bot.getEnemyManager().getEnemyProfile(targetEnemyId);
        final EnemyState latestState = enemy.getLatestState();
        final EnemyState prevState = enemy.getPrevState();
        if (latestState == null || prevState == null) {
            return;
        }
        if (bot.getTurnNumber() - latestState.scannedTurnNum > 1) {
            return;
        }
        if (latestState.scannedTurnNum - prevState.scannedTurnNum > 2) {
            return;
        }

        double diffEnergy = prevState.energy - latestState.energy;

        final MyBullet bulletHitState = myBulletHitStates.get(targetEnemyId);
        if (bulletHitState != null && bulletHitState.hitTurnNumber >= bot.getTurnNumber() - 1) {
            // 直前ターンに敵が被弾していたらそのダメージを差し引く
            diffEnergy -= bulletHitState.damage;
        }

        if (diffEnergy > Constants.MAX_FIREPOWER) {
            return;
        }
        if (diffEnergy <= 0) {
            return;
        }

        // 壁への衝突かどうかを判定
        if (latestState.velocity == 0 && prevState.velocity > 0) {
            return;
        }

        // 突撃かどうかの判定
        if (bot.distanceTo(latestState.getPosition()) <= (Constants.BOUNDING_CIRCLE_RADIUS + 1) * 2) {
            return;
        }

        // 壁への衝突かどうかを判定
        if (latestState.velocity == 0 && prevState.velocity > 0) {
            return;
        }

        // 前ターンに発砲したので波を作成
        final DriveParams prevDriveParams = bot.getDriver().getPreDriveParams();
        final boolean moveForward = (prevDriveParams == null) ? true : (prevDriveParams.forwardDistance >= 0);
        final boolean moveLeft = (prevDriveParams == null) ? true : (prevDriveParams.leftTurnAngle >= 0);
        enemyBulletWaves.addFirst(new EnemyBulletWave(targetEnemyId, prevState.x, prevState.y,
                prevState.scannedTurnNum, diffEnergy, bot.calcBulletSpeed(diffEnergy),
                moveForward, moveLeft));
    }

    /**
     * 飛んでいる弾丸を追加します
     * 
     * @param bulletState
     */
    private void addFlyingMyBullet(BulletState bulletState, int turnNumber) {
        if (myFlyingBullets.containsKey(bulletState.getBulletId())) {
            return;
        }
        final MyBullet bullet = myBulletHistory.get(turnNumber);
        if (bullet == null) {
            System.out.println("Warning: addFlyingMyBullet(): bullet is null: " + turnNumber);
            return;
        }
        bullet.bulletId = bulletState.getBulletId();
        bullet.power = bulletState.getPower();
        myFlyingBullets.put(bullet.bulletId, bullet);
    }

    /**
     * 飛んでいる弾丸を履歴に移動する
     * 
     * @param bulletId
     */
    private void removeFlyingMyBullets(int bulletId) {
        final MyBullet myBullet = myFlyingBullets.get(bulletId);
        if (myBullet == null) {
            return;
        }
        myFlyingBullets.remove(bulletId);
        // myBulletHistory.put(bulletId, myBullet);
    }
}