package planningpet.check;

import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

/** user-flows 06 커버리지 절의 수와 번호 목록이 06 검사 표, 흐름 단계 표의 검사 줄 칸과 맞는지 본다(#259). */
public final class CoverageCheck {
    private static final Pattern ID = Pattern.compile("T-[A-Z]+-\\d+(?:(?:부터 |~)T-[A-Z]+-\\d+(?:까지)?)?");
    private static final Pattern STEP = Pattern.compile("(UF-\\d+-\\d+)\\(([^)]*)\\)");
    private static final List<String> bad = new ArrayList<>();
    private static final Map<String, String> kind = new LinkedHashMap<>();

    public static void main(String[] args) throws Exception {
        Map<String, Integer> rows = new LinkedHashMap<>();
        int sec = 0;
        for (String l : Files.readAllLines(Path.of(args[0]))) {
            Matcher h = Pattern.compile("^## (\\d+)\\.").matcher(l);
            if (h.find()) sec = Integer.parseInt(h.group(1));
            else if (l.startsWith("## ")) sec = 0;
            Matcher m = Pattern.compile("^\\| *(T-[A-Z]+-\\d+)\\b").matcher(l);
            if (m.find() && rows.put(m.group(1), sec) != null) bad.add(m.group(1) + ": 06 표에 두 번 있음");
        }
        String[] doc = Files.readString(Path.of(args[1])).split("\n## 06 커버리지\n");
        Set<String> steps = new TreeSet<>();
        String uf = null;
        for (String l : doc[0].split("\n")) {
            Matcher h = Pattern.compile("^### (UF-\\d+-\\d+)").matcher(l);
            if (h.find()) uf = h.group(1);
            Matcher r = Pattern.compile("^\\| *(\\d[\\d-]*) *\\|.*\\|([^|]*)\\| *$").matcher(l);
            if (uf != null && r.find()) for (String id : ids(r.group(2))) steps.add(id + " " + uf + "(" + r.group(1) + ")");
        }
        Set<String> cov = new TreeSet<>();
        for (String l : doc[1].split("\n")) {
            Matcher r = Pattern.compile("^\\| (\\d+) [^|]*\\|([^|]*)\\|([^|]*)\\|").matcher(l);
            if (!r.find()) continue;
            for (String cell : new String[] {r.group(2), r.group(3)}) {
                List<int[]> at = new ArrayList<>();
                for (Matcher m = ID.matcher(cell); m.find(); ) at.add(new int[] {m.start(), m.end()});
                List<String> run = new ArrayList<>();
                for (int i = 0; i < at.size(); i++) {
                    run.addAll(ids(cell.substring(at.get(i)[0], at.get(i)[1])));
                    String tail = cell.substring(at.get(i)[1], i + 1 < at.size() ? at.get(i + 1)[0] : cell.length());
                    // 쉼표로 이어진 번호는 한 묶음이고, 묶음 뒤의 글이 분류다(": UF" 흐름, "수동", "실패 주입", 그 밖은 단위).
                    if (tail.equals(", ")) continue;
                    for (String id : run) {
                        if (rows.containsKey(id) && rows.get(id) != Integer.parseInt(r.group(1)))
                            bad.add(id + ": 06의 " + rows.get(id) + "절인데 커버리지 표 " + r.group(1) + " 행에 있음");
                        if (tail.startsWith(": UF")) {
                            kind.put(id, "흐름");
                            Matcher s = STEP.matcher(tail.substring(2).split("\\. ")[0]);
                            while (s.find()) for (String n : s.group(2).split(", ")) cov.add(id + " " + s.group(1) + "(" + n + ")");
                        } else if (!tail.startsWith("의") && !"흐름".equals(kind.get(id)))
                            kind.put(id, tail.contains("수동") ? "수동" : tail.contains("실패 주입") ? "실패 주입" : "단위");
                        else kind.putIfAbsent(id, null);
                    }
                    run.clear();
                }
            }
        }
        for (String id : rows.keySet()) if (!kind.containsKey(id)) bad.add(id + ": 06의 " + rows.get(id) + "절에 있는데 커버리지 표에 없음");
            else if (kind.get(id) == null) bad.add(id + ": 커버리지 표에 흐름 단계도 흐름 밖 분류도 없음");
        for (String id : kind.keySet()) if (!rows.containsKey(id)) bad.add(id + ": 커버리지 표에 있는데 06 표에 없음");
        for (String s : steps) if (!cov.contains(s)) bad.add(s + ": 흐름 단계 표의 검사 줄 칸에 있는데 커버리지 표에 없음");
        for (String s : cov) if (!steps.contains(s)) bad.add(s + ": 커버리지 표에 있는데 흐름 단계 표의 검사 줄 칸에 없음");
        String sum = "전체 " + count(doc[1], "06 검사 줄 (\\d+)개", "전체", rows.size())
                + ", 흐름 " + count(doc[1], "흐름 단계에서 확인하는 줄은 (\\d+)개", "흐름", -1)
                + ", 단위 " + count(doc[1], "흐름 밖: 단위 (\\d+)개", "단위", -1)
                + ", 실패 주입 " + count(doc[1], "실패 주입 단위 (\\d+)개", "실패 주입", -1)
                + ", 수동 " + count(doc[1], "수동과 실제 앱 (\\d+)개", "수동", -1);
        System.out.println(bad.isEmpty() ? "06 커버리지: " + sum : "06 커버리지 어긋남:\n  " + String.join("\n  ", bad));
        if (!bad.isEmpty()) System.exit(1);
    }

    /** "T-A-1", "T-A-1부터 T-A-3까지", "T-A-1~T-A-3"을 번호 목록으로 푼다. */
    static List<String> ids(String text) {
        List<String> out = new ArrayList<>();
        Matcher m = ID.matcher(text);
        while (m.find()) {
            String[] p = m.group().replace("까지", "").split("부터 |~");
            int from = Integer.parseInt(p[0].replaceAll(".*-", ""));
            int to = p.length == 1 ? from : Integer.parseInt(p[1].replaceAll(".*-", ""));
            for (int i = from; i <= to; i++) out.add(p[0].replaceAll("\\d+$", "") + i);
        }
        return out;
    }

    /** 절에 적힌 수를 센 값(real, -1이면 그 분류의 번호 수)과 대조하고 센 값을 돌려준다. */
    static int count(String text, String re, String name, int real) {
        if (real < 0) real = (int) kind.values().stream().filter(name::equals).count();
        Matcher m = Pattern.compile(re).matcher(text);
        if (!m.find()) bad.add(name + " 수 문장을 찾지 못함(" + re + ")");
        else if (Integer.parseInt(m.group(1)) != real) bad.add(name + " 수: 적힌 값 " + m.group(1) + ", 06 표에서 센 값 " + real);
        return real;
    }
}
