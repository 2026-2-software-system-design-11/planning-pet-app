package planningpet.common.view;

import java.util.List;
import javax.swing.UIManager;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * 검사마다 앞에서 Theme.install()을 걸고 뒤에서 되돌린다(#518). 클래스에 {@code @ExtendWith(ThemeOn.class)}를 붙인다.
 * install은 앱 전체 기본값을 바꾸므로 되돌리지 않으면 뒤 검사의 부품 모양이 바뀐다. 글꼴을 재거나 배치하지 않는다.
 */
public final class ThemeOn implements BeforeEachCallback, AfterEachCallback {
    @Override
    public void beforeEach(ExtensionContext context) {
        Theme.install();
    }

    @Override
    public void afterEach(ExtensionContext context) {
        undo();
    }

    /** install이 넣은 기본값을 모두 지워 Look and Feel 처음 값으로 돌린다. */
    public static void undo() {
        for (Object key : List.copyOf(UIManager.getLookAndFeelDefaults().keySet())) {
            UIManager.put(key, null);
        }
        UIManager.put("swing.boldMetal", null);
    }
}
