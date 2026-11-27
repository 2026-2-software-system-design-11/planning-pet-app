package planningpet.app.view;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import planningpet.common.view.Snap;

/**
 * 앱의 창 틀. 번호는 docs/porting/frame.md 2절. 기준 그림은 프로토타입 App을 Theme.install 뒤에 만들어 글을 모두 비우고
 * 그렸다. 창(JFrame)은 headless 검사에서 만들 수 없어 창 내용과 창 아이콘을 그려 맞춘다.
 */
class AppWindowTest {
    @Test
    void eWin11WindowIcon() throws IOException {
        Snap.assertLooksLikePrototype("frame-icon", AppWindow.icon());
    }
}
