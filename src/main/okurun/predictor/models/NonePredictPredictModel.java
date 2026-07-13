package okurun.predictor.models;

import okurun.OkuRunBot;
import okurun.arenamap.ArenaMap;
import okurun.battlemanager.EnemyProfile;
import okurun.battlemanager.EnemyState;

import dev.robocode.tankroyale.botapi.graphics.Color;

/**
 * 予測を行わず、単純に最後の観測値を返すモデル
 */
public class NonePredictPredictModel extends AbstractPredictModel {
    /**
     * モデルの色を取得する
     * 
     * @return モデルの色
     */
    @Override
    public Color getColor() {
        return Color.GRAY;
    }

    /**
     * 次ターンの敵の状態を予測する
     * 
     * @param bot          ボット
     * @param enemyState   敵の状態
     * @param enemyProfile 敵プロファイル
     * @return 次ターンの敵の状態
     */
    @Override
    public EnemyState nextTurnState(OkuRunBot bot, EnemyState enemyState, EnemyProfile enemyProfile) {
        if (enemyState == null) {
            // 敵情報がなければアリーナ中央を予測位置とする
            final ArenaMap arenaMap = bot.getArenaMap();
            final double[] center = arenaMap.getCenter();
            return new EnemyState(enemyProfile.getId(), bot.getTurnNumber(), center[0], center[1],
                    0, 0, 100, 0, 0, bot.distanceTo(center));
        }
        return new EnemyState(enemyState.id, enemyState.scannedTurnNum + 1, enemyState.x, enemyState.y,
                enemyState.heading,
                enemyState.velocity, enemyState.energy, enemyState.turnDegree, enemyState.acceleration,
                enemyState.distance);
    }

    /**
     * 指定された敵をこのモデルで予測できるかどうかを判定する
     * 
     * @param bot          ボット
     * @param enemyProfile 敵プロファイル
     * @return trueなら予測できる
     */
    public boolean canPredict(OkuRunBot bot, EnemyProfile enemyProfile) {
        return enemyProfile.getStateHistory().size() > 0;
    }
}
