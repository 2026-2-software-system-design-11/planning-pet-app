package planningpet.schedule.view;

import java.io.IOException;
import javax.swing.JCheckBox;
import org.junit.jupiter.api.Test;
import planningpet.common.view.Snap;

/** 목록 줄의 둥근 체크. 번호는 docs/porting/task-rows.md S3r 2절. 기준 그림은 proto/rchk-*.png이고 글이 없다. */
class RoundCheckTest {
    private static JCheckBox box(boolean on) {
        JCheckBox b = RoundCheck.style(new JCheckBox());
        b.setSelected(on);
        return b;
    }

    private static void looks(String name, JCheckBox b, int w, int h) throws IOException {
        Snap.assertLaidOutLooksLikePrototype("rchk-" + name, b, w, h);
    }

    @Test
    void eRchk1OffAndOn() throws IOException {
        looks("off", box(false), 24, 24);
    }
}
