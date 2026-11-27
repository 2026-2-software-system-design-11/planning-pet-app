package planningpet.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/** 07 "층과 의존 방향"(D-109)을 소스로 검사한다. docs/porting/build.md 3절. */
class LayerTest {
    /** 층마다 쓰면 안 되는 다른 층. 같은 층 안의 의존은 도메인이 달라도 된다. */
    private static final Map<String, Set<String>> FORBIDDEN = Map.of(
            "model", Set.of("view", "controller", "store"),
            "controller", Set.of("view", "store"),
            "store", Set.of("view", "controller"),
            "view", Set.of("store"));
    private static final Pattern LAYER = Pattern.compile("\\bplanningpet\\.\\w+\\.(model|view|controller|store)\\b");
    private static final Pattern UI = Pattern.compile("\\b(javax\\.swing|java\\.awt)\\b");
    private static final Pattern COMMENT = Pattern.compile("//[^\\n]*|/\\*.*?\\*/", Pattern.DOTALL);

    /** layer 층의 소스가 쓰면 안 되는 이름들. */
    static List<String> violations(String layer, String source) {
        String code = COMMENT.matcher(source).replaceAll("");
        List<String> found = new ArrayList<>();
        Matcher m = LAYER.matcher(code);
        while (m.find()) {
            if (FORBIDDEN.get(layer).contains(m.group(1))) {
                found.add(m.group());
            }
        }
        Matcher ui = UI.matcher(code);
        while (layer.equals("model") && ui.find()) {
            found.add(ui.group());
        }
        return found;
    }

    @Test
    void sourcesFollowLayers() throws IOException {
        Path root = Path.of("src/main/java/planningpet");
        List<String> bad = new ArrayList<>();
        int checked = 0;
        try (Stream<Path> walk = Files.walk(root)) {
            for (Path file : walk.filter(p -> p.toString().endsWith(".java")).toList()) {
                Path rel = root.relativize(file);
                if (rel.getNameCount() < 3) {
                    continue; // 도메인 폴더 바로 아래(진입점 Main)는 층 밖이다(07).
                }
                String layer = rel.getName(1).toString();
                if (!FORBIDDEN.containsKey(layer)) {
                    bad.add(rel + ": 네 층이 아닌 폴더(D-109)");
                    continue;
                }
                checked++;
                violations(layer, Files.readString(file)).forEach(v -> bad.add(rel + ": " + v));
            }
        }
        assertTrue(checked > 0, "검사한 층 파일이 없다");
        assertEquals(List.of(), bad);
    }

    @Test
    void modelUsesNoSwingAwtOrOtherLayers() {
        assertEquals(List.of("planningpet.app.view", "planningpet.app.store", "javax.swing", "java.awt"),
                violations("model", "import planningpet.app.view.AppWindow;\nplanningpet.app.store.X x;\n"
                        + "import javax.swing.JPanel;\nimport java.awt.Color;"));
    }

    @Test
    void otherLayersUseOnlyAllowedLayers() {
        assertEquals(List.of("planningpet.home.view", "planningpet.app.store"),
                violations("controller", "import planningpet.home.view.*;\nimport planningpet.app.store.Schema;"));
        assertEquals(List.of("planningpet.app.view", "planningpet.app.controller"),
                violations("store", "import planningpet.app.view.AppWindow;\nimport planningpet.app.controller.AppController;"));
        assertEquals(List.of("planningpet.app.store"),
                violations("view", "import planningpet.app.store.SqliteDataStore;\nimport javax.swing.JPanel;"));
    }

    @Test
    void allowedUsesAndCommentsPass() {
        assertEquals(List.of(), violations("view", "import planningpet.app.controller.AppController;\n"
                + "import planningpet.pet.view.PetArt;\nimport planningpet.app.model.AppData;"));
        assertEquals(List.of(), violations("model", "// javax.swing 안 씀\n/* planningpet.app.view */\n"
                + "import planningpet.schedule.model.Task;"));
    }
}
