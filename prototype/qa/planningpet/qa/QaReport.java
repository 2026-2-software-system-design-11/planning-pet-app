package planningpet.qa;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** QA 결과를 마크다운 보고서로 쓴다. 틀린 확인만 자세히 적는다. */
final class QaReport {
    private QaReport() {
    }

    static void write(File file, Collection<String> flows) throws Exception {
        List<QaCore.Check> all = QaCore.CHECKS;
        long failed = all.stream().filter(c -> !c.ok()).count();
        long cleanFlows = flows.stream().filter(f -> all.stream().noneMatch(c -> c.flow().equals(f) && !c.ok())).count();
        List<String> out = new ArrayList<>();
        out.add("# 프로토타입 유저플로우 QA 결과");
        out.add("");
        out.add("실행일 " + LocalDate.now() + ". `prototype/qa.sh`가 [user-flows.md](../user-flows.md)의 흐름을 창 없이 돌려 이 파일을 만든다. 손으로 고치지 않는다.");
        out.add("");
        out.add("- 흐름 " + flows.size() + "개 가운데 모두 통과 " + cleanFlows + "개");
        out.add("- 확인 " + all.size() + "개 가운데 통과 " + (all.size() - failed) + "개, 실패 " + failed + "개");
        out.add("");
        out.add("| 흐름 | 확인 | 실패 |");
        out.add("|---|---|---|");
        for (String f : flows) {
            long n = all.stream().filter(c -> c.flow().equals(f)).count();
            long bad = all.stream().filter(c -> c.flow().equals(f) && !c.ok()).count();
            out.add("| " + f + " | " + n + " | " + (bad == 0 ? "0" : "**" + bad + "**") + " |");
        }
        out.add("");
        out.add("## 실패한 확인");
        out.add("");
        if (failed == 0) {
            out.add("없음");
        } else {
            out.add("| 흐름 | 단계 | 동작 | 확인 | 기대 | 실제 |");
            out.add("|---|---|---|---|---|---|");
            for (QaCore.Check c : all) {
                if (!c.ok()) {
                    out.add("| " + c.flow() + " | " + c.step() + " | " + cell(c.action()) + " | " + cell(c.what())
                            + " | " + cell(c.expected()) + " | " + cell(c.actual()) + " |");
                }
            }
            out.add("");
            out.add("틀린 단계의 화면: " + String.join(", ", QaCore.SHOTS.stream().map(s -> "[" + s + "](shots/" + s + ")").toList()));
        }
        file.getParentFile().mkdirs();
        Files.writeString(file.toPath(), String.join("\n", out) + "\n");
    }

    private static String cell(String s) {
        return s.replace("|", "\\|").replace("\n", " ");
    }
}
