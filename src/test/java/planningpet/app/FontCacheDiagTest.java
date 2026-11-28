package planningpet.app;

import java.awt.Font;
import java.awt.FontMetrics;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.text.StyleContext;
import org.junit.jupiter.api.Test;
import planningpet.app.model.AppData;
import planningpet.app.model.DataStore;
import planningpet.app.model.LoadResult;
import planningpet.app.model.LoadResult.Reason;
import planningpet.app.view.Tray;
import planningpet.common.view.Snap;
import planningpet.common.view.Theme;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.PetState;
import planningpet.pet.view.PickDialog;
import planningpet.schedule.model.Category;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Task;
import planningpet.schedule.view.TaskRows;

/**
 * 임시 진단(#496, 병합하지 않음): FontMetrics 캐시가 HTML 라벨의 합성 글꼴을 남겨 TitleLabel의 이모지 줄이기가 바뀌는지 한 검사 안에서
 * 차례대로 본다. 값은 표준 출력에 찍고 검사는 늘 통과한다.
 */
class FontCacheDiagTest {
    private static final ZoneId ZONE = ZoneId.systemDefault();
    private static final Object[][] CASES = {{"😀공부", 5}, {"😀공부", 30}, {"가나😀다라", 48}, {"가나😀다라", 49}};

    private static String font2D(Font f) {
        try {
            Method m = Font.class.getDeclaredMethod("getFont2D");
            m.setAccessible(true);
            Object f2d = m.invoke(f);
            return f2d.getClass().getSimpleName();
        } catch (ReflectiveOperationException | RuntimeException e) {
            return "?" + e;
        }
    }

    private static Task task(String title) {
        return new Task("a", 1, Kind.TASK, title, LocalDate.of(2026, 11, 16), null, 30, "", false, Category.OTHER, null, null, null, null,
                false, null, 0, 0, null, null);
    }

    private static void measure(String when) throws ReflectiveOperationException {
        Constructor<TaskRows.TitleLabel> make = TaskRows.TitleLabel.class.getDeclaredConstructor(Task.class);
        make.setAccessible(true);
        for (Object[] c : CASES) {
            TaskRows.TitleLabel l = make.newInstance(task((String) c[0]));
            l.setSize((Integer) c[1], 24);
            FontMetrics fm = l.getFontMetrics(l.getFont());
            String shown = l.shown();
            System.out.println("DIAG " + when + " '" + c[0] + "' w=" + c[1] + " shown='" + shown + "' fmFont=" + fm.getFont().getClass().getSimpleName()
                    + " fmFont2D=" + font2D(fm.getFont()) + " labelFont2D=" + font2D(l.getFont()) + " width(title)=" + fm.stringWidth((String) c[0])
                    + " width(shown)=" + fm.stringWidth(shown) + " width(😀)=" + fm.stringWidth("😀") + " frc="
                    + fm.getFontRenderContext().getAntiAliasingHint() + "/" + fm.getFontRenderContext().getFractionalMetricsHint());
        }
    }

    private static JComponent open(AppData data, Clock clock) {
        JComponent[] root = new JComponent[1];
        Main.start(new DataStore() {
            public LoadResult load() { return LoadResult.loaded(data); }
            public void save(AppData d) { }
        }, Path.of("."), clock, new Main.Shell() {
            public void unreadable(Reason r, Path f) { }
            public void pick(PickDialog p, Runnable c) { }
            public void main(JComponent r) { root[0] = r; }
            public void timer(Tray t) { }
            public void saveFailed(String t) { }
            public void exit(int c) { }
        });
        Snap.layOut(root[0], 1000, 680);
        return root[0];
    }

    @Test
    void diag() throws ReflectiveOperationException {
        System.out.println("DIAG java " + System.getProperty("java.version") + " os " + System.getProperty("os.name"));
        measure("before");
        Font sc = StyleContext.getDefaultStyleContext().getFont(Theme.font(14).getFamily(), Font.PLAIN, 14);
        System.out.println("DIAG styleContext " + sc.getClass().getSimpleName() + " equal=" + sc.equals(Theme.font(14)) + " font2D=" + font2D(sc)
                + " width(😀) by own metrics=" + new JLabel().getFontMetrics(sc).stringWidth("😀"));
        Theme.install();
        Clock clock = Clock.fixed(LocalDateTime.of(2026, 11, 16, 10, 0).atZone(ZONE).toInstant(), ZONE);
        AppData data = new AppData(clock);
        data.addPet(new PetState(PetKind.CAT, "나비"));
        data.setActiveKind(PetKind.CAT);
        open(data, clock);
        Clock at = Clock.fixed(LocalDateTime.of(2026, 11, 19, 9, 0).atZone(ZONE).toInstant(), ZONE);
        LocalDate d12 = LocalDate.of(2026, 11, 12);
        AppData list = new AppData(at, new ArrayList<>(List.of(
                new Task("a", 1, Kind.TASK, "졸업 작품 기획서", d12, null, 60, "", false, Category.OTHER, null, null, LocalDate.of(2026, 11, 19),
                        d12, false, null, 0, 0, null, null),
                new Task("b", 2, Kind.TASK, "E사 지원서", LocalDate.of(2026, 11, 18), LocalTime.of(9, 0), 120, "", false, Category.OTHER,
                        null, null, LocalDate.of(2026, 11, 22), LocalDate.of(2026, 11, 16), false, null, 0, 0, null, null))), List.of());
        list.addPet(new PetState(PetKind.CAT, "나비"));
        list.setActiveKind(PetKind.CAT);
        open(list, at);
        measure("installed");
        for (Object key : List.copyOf(UIManager.getLookAndFeelDefaults().keySet())) {
            UIManager.put(key, null);
        }
        UIManager.put("swing.boldMetal", null);
        measure("after");
    }
}
