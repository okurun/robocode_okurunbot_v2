package okurun.driver.actions;

import okurun.OkuRunBot;
import okurun.driver.Driver;

public interface DriveAction {
    class DriveParams implements Cloneable {
        public double leftTurnAngle = 0;
        public double forwardDistance = 0;
        public double maxSpeed = 0;

        @Override
        public DriveParams clone() {
            try {
                return (DriveParams) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new AssertionError();
            }
        }
    }

    /**
     * ターン毎のアクションイベント
     * このイベントはメインスレッドからコールされます
     * 
     * @param bot Bot
     * @return Driver.ActionId 問題なく終了した場合はnullを返す。新たなActionを実行したい場合は新しいActionIdを返す
     */
    Driver.ActionId onAction(OkuRunBot bot);

    /**
     * 現在のアクションのDriveParamsを取得する
     * 
     * @param bot Bot
     */
    DriveParams getDriveParams();
}
