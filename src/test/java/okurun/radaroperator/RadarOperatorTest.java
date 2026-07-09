package okurun.radaroperator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;

import okurun.OkuRunBot;
import okurun.commander.Commander;

@ExtendWith(MockitoExtension.class)
class RadarOperatorTest {
    private RadarOperator radarOperator;

    @Mock
    private OkuRunBot bot;

    @Mock
    private Commander commander;

    @BeforeEach
    void setUp() {
        radarOperator = new RadarOperator();
        radarOperator.init(bot);
    }

    @Test
    void testAction() {
        // actionメソッド内でCommanderからRadarActionNameを取得し実行する処理のモック
        when(bot.getCommander()).thenReturn(commander);

        // アクションを要求しない (nullを返す) 場合に正常に終了するか確認する
        when(commander.getRadarAction(bot)).thenReturn(null);

        radarOperator.action(bot);

        // getRadarActionName が1回呼ばれたことを検証
        verify(commander).getRadarAction(bot);
    }
}
