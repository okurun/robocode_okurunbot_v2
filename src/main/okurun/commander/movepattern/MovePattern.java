package okurun.commander.movepattern;

import dev.robocode.tankroyale.botapi.events.GameEndedEvent;
import dev.robocode.tankroyale.botapi.events.HitByBulletEvent;
import dev.robocode.tankroyale.botapi.events.RoundEndedEvent;
import okurun.OkuRunBot;
import okurun.commander.Commander.AccelPriority;
import okurun.commander.Commander.HandlePriority;

public interface MovePattern {
    void postAction(OkuRunBot bot);

    /**
     * 次の移動位置を取得します
     * 
     * @param bot ボット
     * @return 次の移動位置
     */
    double[] getMovePosition(OkuRunBot bot);

    /**
     * ハンドルプライオリティを取得します
     * 
     * @param bot ボット
     * @return ハンドルプライオリティ
     */
    HandlePriority getHandlePriority(OkuRunBot bot);

    /**
     * 加速プライオリティを取得します
     * 
     * @param bot ボット
     * @return 加速プライオリティ
     */
    AccelPriority getAccelPriority(OkuRunBot bot);

    /**
     * 最小速度を取得します
     * 
     * @param bot ボット
     * @return 最小速度
     */
    double getMinSpeed(OkuRunBot bot);

    /**
     * ターン毎の命中弾数を取得します
     * 
     * @return ターン毎の命中弾数
     */
    double getHitPerTurn();

    /**
     * トータルのターン毎の命中弾数を取得します
     * 
     * @return トータルのターン毎の命中弾数
     */
    double getTotalHitPerTurn();

    /**
     * ゲームが終了した時の処理
     * 
     * @param e   ゲーム終了イベント
     * @param bot ボット
     */
    void onGameEnded(GameEndedEvent e, OkuRunBot bot);

    /**
     * ラウンドが終了した時の処理
     * 
     * @param e   ラウンド終了イベント
     * @param bot ボット
     */
    void onRoundEnded(RoundEndedEvent e, OkuRunBot bot);

    /**
     * 弾丸が自分に当たった時の処理
     * 
     * @param e   弾丸が自分に当たったイベント
     * @param bot ボット
     */
    void onHitByBullet(HitByBulletEvent e, OkuRunBot bot);
}
