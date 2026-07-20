package okurun.radaroperator.actions;

import okurun.OkuRunBot;
import okurun.radaroperator.RadarOperator;

public interface RadarAction {

    /**
     * ターン毎のアクションイベント
     * このイベントはメインスレッドからコールされます
     * 
     * @param bot Bot
     * @return RadarOperator.ActionId 問題なく終了した場合はnullを返す。新たなActionを実行したい場合は新しいActionIdを返す
     */
    public RadarOperator.ActionId onAction(OkuRunBot bot);

}
