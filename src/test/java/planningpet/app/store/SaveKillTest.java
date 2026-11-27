package planningpet.app.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import planningpet.app.model.AppData;
import planningpet.app.model.LoadResult;
import planningpet.schedule.model.Category;
import planningpet.schedule.model.EditScope;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Task;
import planningpet.schedule.model.TaskDraft;

/** 06 21번 T-STORE-6. 새 JVM의 SaveLoop를 저장 도중 강제 종료(SIGKILL)하고 읽기를 20번 되풀이한다. 창을 열지 않는다. */
class SaveKillTest {
    static final int TASKS = 20_000;
    private static final Clock CLOCK = Clock.systemDefaultZone();
    @TempDir
    Path folder;

    @Test
    void T_STORE_6_killDuringSave() throws Exception {
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        for (int run = 0; run < 20; run++) {
            Process p = new ProcessBuilder(java, "-Djava.awt.headless=true", "-cp", System.getProperty("java.class.path"),
                    SaveLoop.class.getName(), folder.toString()).redirectError(ProcessBuilder.Redirect.DISCARD).start();
            BufferedReader out = new BufferedReader(new InputStreamReader(p.getInputStream()));
            int last = Integer.parseInt(out.readLine());
            Thread.sleep(run * 15L);
            p.toHandle().destroyForcibly();
            p.waitFor();
            for (String line = out.readLine(); line != null; line = out.readLine()) {
                last = Integer.parseInt(line);
            }
            LoadResult r = new SqliteDataStore(folder, CLOCK).load();
            assertEquals(LoadResult.Status.LOADED, r.status());
            int g = r.data().getPoints();
            assertTrue(g == last || g == last + 1, "읽은 세대 " + g + ", 마친 세대 " + last);
            List<String> titles = IntStream.range(0, TASKS).mapToObj(i -> title(g, i)).toList();
            assertEquals(titles, r.data().getBook().getTasks().stream().map(Task::getTitle).toList());
        }
    }

    /** 세대 g의 i번째 할 일 제목. */
    static String title(int g, int i) {
        return g + "-" + i;
    }

    /**
     * 새 JVM에서 도는 저장 되풀이. 데이터 폴더(첫 인자)를 읽거나 할 일 2만 개로 시작하고, 세대마다 포인트를 세대 번호로, 모든 제목을
     * title(세대, 순번)으로 바꿔 저장하기를 강제로 꺼질 때까지 되풀이한다. 저장을 마칠 때마다 세대 번호를 한 줄 찍는다.
     */
    static final class SaveLoop {
        public static void main(String[] args) throws IOException {
            SqliteDataStore store = new SqliteDataStore(Path.of(args[0]), CLOCK);
            AppData loaded = store.load().data();
            AppData data = loaded != null ? loaded : new AppData(CLOCK);
            for (int i = data.getBook().getTasks().size(); i < TASKS; i++) {
                data.getBook().add(draft(data.today(), title(0, i), false));
            }
            for (int g = data.getPoints() + 1; ; g++) {
                data.setPoints(g);
                List<Task> tasks = data.getBook().getTasks();
                for (int i = 0; i < tasks.size(); i++) {
                    data.getBook().update(tasks.get(i), draft(data.today(), title(g, i), true), EditScope.ONLY_THIS);
                }
                store.save(data);
                System.out.println(g);
                System.out.flush();
            }
        }

        private static TaskDraft draft(LocalDate today, String title, boolean editing) {
            return new TaskDraft(Kind.TASK, title, Category.OTHER, today, null, null, 30, "", null, null, false, null,
                    today, editing);
        }
    }
}
