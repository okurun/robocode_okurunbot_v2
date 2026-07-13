package okurun.gunner.actions;

import okurun.OkuRunBot;
import okurun.gunner.Gunner;

/**
 * レーダーを早く回すための砲頭を回すアクション
 */
public class ScanGunAction implements GunAction {

    @Override
    public Gunner.ActionId action(OkuRunBot bot) {
        bot.setAdjustGunForBodyTurn(false);
        bot.setTurnGunLeft(360);

        return null;
    }

}
