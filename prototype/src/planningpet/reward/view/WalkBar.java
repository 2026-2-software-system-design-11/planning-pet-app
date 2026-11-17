package planningpet.reward.view;

import planningpet.app.model.Store;
import planningpet.common.view.Theme;
import planningpet.pet.view.Pet;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;

/** 성취도 막대(D-22, 06의 13번). 오늘 할 일을 끝낸 비율만큼 펫이 걸어가 있다. */
public final class WalkBar extends JComponent {
    private final Store store;

    public WalkBar(Store store) {
        this.store = store;
        store.onChange(this::repaint);
        setPreferredSize(new Dimension(600, 140));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        var today = store.checkables(store.today());
        long done = today.stream().filter(t -> t.done).count();
        int left = 70;
        int right = getWidth() - 60;
        int y = getHeight() - 16;
        g.setColor(Theme.INK);
        g.setFont(Theme.bold(14));
        g.drawString("성취도", 8, y + 5);
        g.drawString(today.isEmpty() ? "-" : done + "/" + today.size(), right + 14, y + 5);
        g.setStroke(new BasicStroke(3));
        g.setColor(Theme.TRACK);
        g.drawLine(left, y, right, y);
        double ratio = today.isEmpty() ? 0 : done / (double) today.size();
        int catX = left + (int) ((right - left) * ratio);
        g.setColor(Theme.MINT);
        g.drawLine(left, y, catX, y);
        for (int i = 1; i <= today.size(); i++) {
            int x = left + (right - left) * i / today.size();
            g.setColor(i <= done ? Theme.MINT : Color.WHITE);
            g.fillOval(x - 5, y - 5, 10, 10);
            g.setColor(Theme.MINT);
            g.drawOval(x - 5, y - 5, 10, 10);
        }
        Pet.drawAt(g, store, catX, y - 6, 56);
        drawBubble(g, catX, y - 6 - 56);
        g.dispose();
    }

    /** D-34. 펫 바로 위의 말풍선. 꼬리는 펫을 가리키고, 양 끝에서는 말풍선만 안쪽으로 맞춘다. */
    private void drawBubble(Graphics2D g, int petX, int petTop) {
        if (store.talk == null || store.talk.isBlank()) {
            return;
        }
        g.setFont(Theme.font(14));
        java.awt.FontMetrics fm = g.getFontMetrics();
        java.util.List<String> lines = wrap(store.talk, fm, 380);
        int w = lines.stream().mapToInt(fm::stringWidth).max().orElse(0) + 28;
        int h = lines.size() * fm.getHeight() + 14;
        int bottom = petTop - 8;
        int x = Math.max(4, Math.min(petX - w / 2, getWidth() - w - 4));
        int top = Math.max(2, bottom - h);
        h = bottom - top;
        int tail = Math.max(x + 14, Math.min(petX, x + w - 14));
        java.awt.Polygon arrow = new java.awt.Polygon(new int[] {tail - 7, tail + 7, petX}, new int[] {bottom - 1, bottom - 1, petTop - 1}, 3);
        g.setStroke(new BasicStroke(1));
        g.setColor(Theme.CARD_TOP);
        g.fillRoundRect(x, top, w, h, Theme.BUBBLE_RADIUS, Theme.BUBBLE_RADIUS);
        g.fillPolygon(arrow);
        g.setColor(Theme.CARD_LINE);
        g.drawRoundRect(x, top, w, h, Theme.BUBBLE_RADIUS, Theme.BUBBLE_RADIUS);
        g.drawLine(tail - 7, bottom, petX, petTop - 1);
        g.drawLine(tail + 7, bottom, petX, petTop - 1);
        g.setColor(Theme.INK);
        for (int i = 0; i < lines.size(); i++) {
            g.drawString(lines.get(i), x + 14, top + 7 + fm.getAscent() + i * fm.getHeight());
        }
    }

    /** 띄어쓰기 단위로 max 너비에서 줄을 바꾼다. */
    static java.util.List<String> wrap(String text, java.awt.FontMetrics fm, int max) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            while (fm.stringWidth(word) > max) {
                // 띄어쓰기 없이 긴 글은 글자 단위로 자른다.
                int cut = word.length() - 1;
                while (cut > 1 && fm.stringWidth(word.substring(0, cut)) > max) {
                    cut--;
                }
                if (line.length() > 0) {
                    lines.add(line.toString());
                    line = new StringBuilder();
                }
                lines.add(word.substring(0, cut));
                word = word.substring(cut);
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
