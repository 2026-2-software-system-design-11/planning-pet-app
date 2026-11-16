package planningpet.proto;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/** 유저플로우를 모두 돌리고 보고서를 쓴다. qa.sh가 부른다. 인자: 보고서 파일, 캡처 폴더. */
final class QaRun {
    private QaRun() {
    }

    public static void main(String[] args) throws Exception {
        File report = new File(args.length > 0 ? args[0] : "qa-out/report.md");
        QaCore.shots = new File(args.length > 1 ? args[1] : "qa-out");
        Map<String, Runnable> flows = new LinkedHashMap<>();
        Flows12.add(flows);
        Flows345.add(flows);
        for (Map.Entry<String, Runnable> f : flows.entrySet()) {
            try {
                f.getValue().run();
            } catch (Throwable e) {
                QaCore.CHECKS.add(new QaCore.Check(f.getKey(), "-", "흐름 중단", "예외 없이 끝남", "끝남", e.toString(), false));
            }
        }
        QaReport.write(report, flows.keySet());
        long failed = QaCore.CHECKS.stream().filter(c -> !c.ok()).count();
        System.out.println("흐름 " + flows.size() + "개, 확인 " + QaCore.CHECKS.size() + "개, 실패 " + failed + "개");
    }

    /** 위 막대와 보상 상태를 한 줄로: "110P 경험치220 횟수2 성취도2/4". */
    static String st(Qa q) {
        Store s = q.app.store;
        return s.points + "P 경험치" + s.exp + " 횟수" + q.count() + " 성취도" + q.progress();
    }
}
