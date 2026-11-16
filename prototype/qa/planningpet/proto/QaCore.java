package planningpet.proto;

import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** QA 기록. 흐름과 단계마다 확인한 값의 기대와 실제를 남기고, 틀린 단계는 화면을 그려 둔다. */
class QaCore {
    record Check(String flow, String step, String action, String what, String expected, String actual, boolean ok) {
    }

    static final List<Check> CHECKS = new ArrayList<>();
    static File shots = new File("qa-out");
    static final List<String> SHOTS = new ArrayList<>();

    App app;
    final String flow;
    private String step;
    private String action;
    private boolean stepFailed;

    QaCore(String flow) {
        this.flow = flow;
    }

    /** 다음 단계를 시작한다. 앞 단계에서 틀린 값이 있었으면 그 화면을 그려 둔다. */
    void step(String no, String what) {
        finishStep();
        step = no;
        action = what;
        stepFailed = false;
    }

    void eq(String what, Object expected, Object actual) {
        String e = String.valueOf(expected);
        String a = String.valueOf(actual);
        record(what, e, a, e.equals(a));
    }

    /** 실제 글자에 기대 글자가 들어 있는지 본다. */
    void has(String what, String part, String actual) {
        record(what, "…" + part + "…", actual, actual != null && actual.contains(part));
    }

    void yes(String what, boolean actual) {
        record(what, "true", String.valueOf(actual), actual);
    }

    private void record(String what, String e, String a, boolean ok) {
        CHECKS.add(new Check(flow, step, action, what, e, a, ok));
        stepFailed |= !ok;
    }

    void end() {
        finishStep();
        step = null;
    }

    private void finishStep() {
        if (step == null || !stepFailed || app == null) {
            return;
        }
        try {
            shots.mkdirs();
            File f = new File(shots, flow + "-" + step + ".png");
            Snap.save(app.root, new Dimension(1000, 680), f);
            SHOTS.add(f.getName());
        } catch (Exception e) {
            System.out.println("화면을 그리지 못함: " + flow + " " + step + " " + e);
        }
    }
}
