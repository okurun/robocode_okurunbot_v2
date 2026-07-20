package okurun.gunner.actions;

import okurun.OkuRunBot;
import okurun.gunner.Gunner;

/**
 * レーダーを早く回すための砲頭を回すアクション
 */
public class ScanGunAction implements GunAction {

    /**
     * {@inheritDoc}
     */
    @Override
    public Gunner.ActionId onAction(OkuRunBot bot) {
        bot.setAdjustGunForBodyTurn(false);
        bot.setTurnGunLeft(360);

        return null;
    }

}
