package planningpet.common.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/** Theme의 값이 디자인 시스템 문서와 같은지 본다. docs/porting/theme.md 2절. */
class ThemeTest {
    private static final Pattern HEX = Pattern.compile("#([0-9A-Fa-f]{6})");
    private static final Pattern ALPHA = Pattern.compile("알파 (\\d+)");

    private static String section(String doc, String head) {
        return doc.split("## " + head)[1].split("\n## ")[0];
    }

    /** 2절 표 한 줄의 이름마다 "이름=ARGB". 값이 이름보다 적으면 마지막 값을 쓴다. */
    private static List<String> row(String line) {
        String[] cells = line.split("\\|");
        List<String> names = new ArrayList<>();
        Matcher n = Pattern.compile("`([A-Z_]+)`").matcher(cells[1]);
        while (n.find()) {
            names.add(n.group(1));
        }
        List<String> hexes = new ArrayList<>();
        Matcher h = HEX.matcher(cells[2]);
        while (h.find()) {
            hexes.add(h.group(1).toUpperCase());
        }
        if (cells[2].contains("흰색")) {
            hexes.add("FFFFFF");
        }
        Matcher a = ALPHA.matcher(cells[2]);
        int alpha = a.find() ? Integer.parseInt(a.group(1)) : 255;
        List<String> out = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            out.add(names.get(i) + "=" + String.format("%02X", alpha) + hexes.get(Math.min(i, hexes.size() - 1)));
        }
        return out;
    }

    @Test
    void eTheme1ColorsMatchDesignSystemTable() throws IOException, ReflectiveOperationException {
        String doc = Files.readString(Path.of("docs/design-system.md"));
        List<String> table = new ArrayList<>();
        List<String> code = new ArrayList<>();
        for (String line : section(doc, "2. 색").lines().filter(l -> l.startsWith("| `")).toList()) {
            for (String entry : row(line)) {
                table.add(entry);
                Field f = Theme.class.getField(entry.split("=")[0]);
                code.add(f.getName() + "=" + String.format("%08X", ((Color) f.get(null)).getRGB()));
            }
        }
        long colors = List.of(Theme.class.getFields()).stream().filter(f -> f.getType() == Color.class).count();
        assertEquals(colors, table.size(), "2절 표에 없는 색이 있다");
        assertEquals(table, code);
    }

    @Test
    void eTheme2ShapesMatchDesignSystemSection4() throws IOException {
        String shapes = section(Files.readString(Path.of("docs/design-system.md")), "4. 모양과 간격");
        assertEquals(List.of(Theme.CARD_RADIUS, 2 * Theme.BUTTON_RADIUS, Theme.IMAGE_RADIUS, Theme.BUBBLE_RADIUS, Theme.GAP),
                List.of(number(shapes, "\\| 카드 \\| 모서리 (\\d+)"), number(shapes, "모서리는 높이, (\\d+)까지"),
                        number(shapes, "\\| 그림 자르기 \\| 모서리 (\\d+)"), number(shapes, "\\| 말풍선 \\|[^|]*모서리 (\\d+)"),
                        number(shapes, "카드 사이 (\\d+)\\(`GAP`\\)")));
    }

    private static int number(String text, String regex) {
        Matcher m = Pattern.compile(regex).matcher(text);
        m.find();
        return Integer.parseInt(m.group(1));
    }
}
