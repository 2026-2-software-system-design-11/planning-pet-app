package planningpet.home.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import planningpet.common.view.Card;
import planningpet.common.view.Icons;
import planningpet.common.view.Images;
import planningpet.common.view.Theme;
import planningpet.common.view.Ui;
import planningpet.pet.model.PetState;
import planningpet.pet.view.PetArt;
import planningpet.reward.model.Level;

/** 홈 펫 카드(D-59)의 그림 도구: 하늘과 풀밭 장면과 그것이 쓰는 색 섞기, 알파 바꾸기, 카드 왼쪽 펫, 펫 옆 말풍선과 그 글 줄 바꾸기. 값은 디자인 시스템 10절 "홈"에 있다(docs/porting/home.md S3j). */
final class PetCardArt {
    /** 말풍선은 이 줄 수까지 보이고 넘치면 끝을 줄인다. */
    static final int BUBBLE_MAX_LINES = 4;
    /** 펫 칸과 오른쪽 열 사이. */
    static final int GAP = 8;

    private PetCardArt() {
    }

    /** 하늘 그라데이션, 구름 둘, 먼 언덕과 가까운 풀밭, 풀잎(홈 예시 펫 카드의 장면). */
    static void scene(Graphics2D g, Rectangle f, int ground) {
        scene(g, f, ground, Images.CLOUD);
    }

    /** 구름 그림을 받는다. 그림이 없으면(null) 구름 없이 그린다. 그림 파일을 못 읽은 때를 검사하려고 연다(home.md S3j). */
    static void scene(Graphics2D g, Rectangle f, int ground, BufferedImage cloud) {
        int x = f.x;
        int y = f.y;
        int w = f.width;
        int h = f.height;
        g.setPaint(new GradientPaint(0, y, Theme.SKY_TOP, 0, ground, Theme.SKY_BOTTOM));
        g.fillRect(x, y, w, h);
        if (cloud != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            int cw = Math.max(60, w / 6);
            int ch = cw * cloud.getHeight() / cloud.getWidth();
            g.drawImage(cloud, x + w / 14, y + h / 12, cw, ch, null);
            // 둘째 구름은 글이 없는 곳(펫 머리 위쪽 오른편)에 작게 둔다. 오른쪽 열에 두면 이름과 레벨 글을 가린다.
            int cw2 = cw * 3 / 5;
            g.drawImage(cloud, x + (int) (w * 0.25), y + h / 5, cw2, cw2 * cloud.getHeight() / cloud.getWidth(), null);
        }
        // 먼 언덕: 화면 오른쪽으로 갈수록 낮아지는 흐린 초록 둔덕 둘.
        Path2D far = new Path2D.Double();
        far.moveTo(x, ground - h * 0.20);
        far.curveTo(x + w * 0.18, ground - h * 0.30, x + w * 0.40, ground - h * 0.22, x + w * 0.55, ground - h * 0.17);
        far.curveTo(x + w * 0.72, ground - h * 0.12, x + w * 0.86, ground - h * 0.21, x + w, ground - h * 0.16);
        far.lineTo(x + w, y + h);
        far.lineTo(x, y + h);
        far.closePath();
        g.setPaint(new GradientPaint(0, (float) (ground - h * 0.30), withAlpha(Theme.GRASS_FAR, 200), 0, ground,
                withAlpha(Theme.GRASS_FAR, 120)));
        g.fill(far);
        // 가까운 풀밭: 펫이 앉은 왼쪽이 조금 높고 오른쪽으로 내려가는 둔덕.
        Path2D near = new Path2D.Double();
        near.moveTo(x, ground - h * 0.06);
        near.curveTo(x + w * 0.25, ground - h * 0.10, x + w * 0.45, ground - h * 0.04, x + w * 0.62, ground + h * 0.01);
        near.curveTo(x + w * 0.80, ground + h * 0.05, x + w * 0.92, ground + h * 0.02, x + w, ground + h * 0.03);
        near.lineTo(x + w, y + h);
        near.lineTo(x, y + h);
        near.closePath();
        // 예시의 풀밭은 노란빛(#E0EC9C)이라 아래로 가며 먼 언덕 색을 조금만 섞는다.
        g.setPaint(new GradientPaint(0, (float) (ground - h * 0.10), Theme.GRASS, 0, y + h, mix(Theme.GRASS, Theme.GRASS_FAR, 0.35)));
        g.fill(near);
        // 풀잎: 왼쪽 풀밭 가장자리에 세 갈래 잎 몇 개.
        g.setColor(Theme.GRASS_DEEP);
        g.setStroke(new BasicStroke(Math.max(1.4f, h / 140f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        double[] tufts = {0.06, 0.33, 0.47};
        for (double tx : tufts) {
            double bx = x + w * tx;
            double by = ground - h * 0.03 + (tx > 0.4 ? h * 0.02 : 0);
            double s = h / 26.0;
            g.draw(new java.awt.geom.Line2D.Double(bx, by, bx - s * 0.5, by - s));
            g.draw(new java.awt.geom.Line2D.Double(bx + s * 0.25, by, bx + s * 0.3, by - s * 1.3));
            g.draw(new java.awt.geom.Line2D.Double(bx + s * 0.5, by, bx + s * 1.0, by - s * 0.9));
        }
    }

    /** 펫 칸 폭: 틀 폭의 0.4(118 이상 260 이하). 오른쪽 열은 이 칸 뒤 GAP에서 시작한다. */
    static int petWidth(Rectangle f) {
        return (int) Math.max(118, Math.min(f.width * 0.40, 260));
    }

    /** 땅 선: 틀 아래에서 높이의 1/11(14 이상). */
    static int ground(Rectangle f) {
        return f.y + f.height - Math.max(14, f.height / 11);
    }

    /** 펫 상자 한 변: 3단계 그림이 위쪽 여백을 빼고 꽉 차는 크기. */
    static int petSize(Rectangle f) {
        return (int) Math.min(petWidth(f) * 1.7, (ground(f) - f.y - 14) / 0.97);
    }

    /** 홈 카드 비율(D-93)로 키운 상자에 그린 펫의 저장된 자세의 높이. 장신구는 보지 않는다(home.md S3m E-PCARD-3). */
    static int drawnHeight(PetState p, int size) {
        return (int) (size * PetArt.cardScale(Level.of(p.getExp()).getStage()) * PetArt.heightRatio(p.getKind(), p.getPose()));
    }

    /** 펫 그림 높이를 카드 칸(틀 위에서 땅 선까지) 높이로 나눈 값. 1단계도 3분의 2를 넘는다(D-93). */
    static double petFill(PetState p, Rectangle f) {
        return drawnHeight(p, petSize(f)) / (double) (ground(f) - f.y);
    }

    /**
     * 펫: 틀 왼쪽 펫 칸에 발을 땅 선에 붙이고, 발 아래 옅은 그림자와 오른쪽 귀 옆 강조선을 그린다. 단계는 상자 안의 크기로 보인다(D-93).
     * t는 움직임 시각(초)이다. 펫은 저장된 자세(D-50)로 그린다(character.md S8b).
     */
    static void pet(Graphics2D g, PetState p, Rectangle f, double t) {
        int ground = ground(f);
        int cx = f.x + petWidth(f) / 2 + 6;
        int size = petSize(f);
        g.setColor(new Color(0x5E, 0x8A, 0x3C, 45));
        g.fillOval(cx - size * 3 / 10, ground - size / 26, size * 3 / 5, size / 13);
        double box = size * PetArt.cardScale(Level.of(p.getExp()).getStage());
        PetArt.drawAt(g, p.getKind(), p.getPose(), new HashSet<>(p.getWear().values()), t, 99, cx, ground, box);
        // 강조선은 오른쪽 귀 옆에 작게 둔다. 더 오른쪽은 이름과 흰 작은 카드 자리다.
        int sp = Math.max(14, size / 8);
        int infoX = f.x + petWidth(f) + GAP;
        Icons.draw(g, Icons.Kind.SPARK, Math.min(cx + size / 6, infoX - sp - 4),
                Math.max(f.y + 4, ground - drawnHeight(p, size) - sp / 3), sp, Theme.SPARK);
    }

    static Color mix(Color a, Color b, double k) {
        return new Color((int) (a.getRed() * (1 - k) + b.getRed() * k), (int) (a.getGreen() * (1 - k) + b.getGreen() * k),
                (int) (a.getBlue() * (1 - k) + b.getBlue() * k));
    }

    static Color withAlpha(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }

    /**
     * 펫 옆 말풍선(D-59): (x, top)에서 폭 w, bottom 위까지. 꼬리가 왼쪽의 펫을 가리킨다. 폭에서 줄을 바꾸고 넘치면 끝을 줄인다.
     * 그린 자리를 돌려주고, 글을 줄였으면 cut[0]을 true로 한다. 글이 없으면 그리지 않고 null이다.
     */
    static Rectangle bubble(Graphics2D g, String text, int x, int top, int w, int bottom, boolean[] cut) {
        cut[0] = false;
        if (text.isBlank()) {
            return null;
        }
        g.setFont(Theme.bold(13));
        FontMetrics fm = g.getFontMetrics();
        int heart = 14;
        int textW = w - 14 - heart - 6 - 12;
        int lineH = fm.getHeight();
        int room = Math.max(1, Math.min(BUBBLE_MAX_LINES, (bottom - top - 16) / lineH));
        List<String> lines = wrap(text, fm, textW);
        if (lines.size() > room) {
            cut[0] = true;
            List<String> shown = new ArrayList<>(lines.subList(0, room));
            shown.set(room - 1, Ui.fit(shown.get(room - 1) + " " + String.join(" ", lines.subList(room, lines.size())), fm, textW));
            lines = shown;
        }
        int bh = lines.size() * lineH + 16;
        int by = top;
        int bx = x;
        Card.shadow(g, bx, by, w, bh, Theme.BUBBLE_RADIUS);
        int tailY = by + Math.min(bh / 2, 20);
        Path2D tail = new Path2D.Double();
        tail.moveTo(bx + 2, tailY - 7);
        tail.lineTo(bx - 9, tailY + 1);
        tail.lineTo(bx + 2, tailY + 6);
        tail.closePath();
        g.setColor(Theme.CARD_TOP);
        g.fillRoundRect(bx, by, w, bh, Theme.BUBBLE_RADIUS, Theme.BUBBLE_RADIUS);
        g.fill(tail);
        g.setColor(Theme.CARD_EDGE);
        g.setStroke(new BasicStroke(1f));
        g.drawRoundRect(bx, by, w - 1, bh - 1, Theme.BUBBLE_RADIUS, Theme.BUBBLE_RADIUS);
        g.draw(new java.awt.geom.Line2D.Double(bx, tailY - 7, bx - 9, tailY + 1));
        g.draw(new java.awt.geom.Line2D.Double(bx - 9, tailY + 1, bx, tailY + 6));
        Icons.draw(g, Icons.Kind.HEART, bx + 12, by + 8 + (lineH - heart) / 2, heart, Theme.HEART);
        g.setColor(Theme.INK);
        for (int i = 0; i < lines.size(); i++) {
            g.drawString(lines.get(i), bx + 12 + heart + 6, by + 8 + fm.getAscent() + i * lineH);
        }
        return new Rectangle(bx, by, w, bh);
    }

    /** 띄어쓰기 단위로 max 폭에서 줄을 바꾼다. 띄어쓰기 없이 긴 글은 글자(코드 포인트) 단위로 자른다(home.md S3k E-WRAP-5). */
    static List<String> wrap(String text, FontMetrics fm, int max) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            // 한 글자는 폭을 넘어도 자르지 않는다. 자를 자리가 없어 끝나지 않던 것을 막는다(home.md S3k E-WRAP-6).
            while (word.codePointCount(0, word.length()) > 1 && fm.stringWidth(word) > max) {
                int cut = word.codePointCount(0, word.length()) - 1;
                while (cut > 1 && fm.stringWidth(word.substring(0, word.offsetByCodePoints(0, cut))) > max) {
                    cut--;
                }
                int end = word.offsetByCodePoints(0, cut);
                if (line.length() > 0) {
                    lines.add(line.toString());
                    line = new StringBuilder();
                }
                lines.add(word.substring(0, end));
                word = word.substring(end);
            }
            String next = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(next) > max && line.length() > 0) {
                lines.add(line.toString());
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(next);
            }
        }
        lines.add(line.toString());
        return lines;
    }
}
