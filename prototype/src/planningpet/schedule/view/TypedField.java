package planningpet.schedule.view;

import planningpet.common.view.Theme;
import planningpet.common.view.Ui;
import java.text.ParseException;
import java.util.function.Function;
import javax.swing.*;
import javax.swing.text.*;

/** 칸에 쳐 넣은 글을 Typed로 읽어 넣고 맞췄으면 흐린 글, 받지 않았으면 빨간 글을 칸 아래에 보인다(D-98). */
public final class TypedField {
    public final JLabel notice = new JLabel();
    private final Runnable relayout;
    private Object about; // 글이 말하는 칸 값
    private String plain = ""; // 접기 전 글
    /** 목록 칸에 친 글과 그 글로 채워 보인 값. */
    private String raw = "";
    private String shown;
    /** 칸에 글을 쳐 넣어 읽은 적이 있는가. 일정 입력 창은 그 뒤 30분 안내를 숨긴다(D-98). */
    public boolean typed;
    /** 마지막으로 읽은 값과 그때 옮길 날 수("밤 4시", D-98). */
    private Object daysFor;
    private int days;
    /** 친 글을 모두 지워 비운 채 벗어나면 되돌릴 치기 앞의 칸 아래 글과 옮길 날(D-98). 치기 앞에 글이 없었으면 null. */
    private Runnable undo;
    /** 0보다 크면 글을 이 너비(px) 안에서 낱말 사이로 접는다(시각 고치기 창, D-98). */
    public java.util.function.IntSupplier wrap = () -> 0;

    public TypedField(JSpinner spinner, Function<String, Typed.Read> reader, Runnable relayout) {
        this.relayout = relayout;
        notice.setFont(Theme.font(12));
        notice.setVerticalAlignment(JLabel.TOP);
        notice.setVisible(false);
        JFormattedTextField field = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
        DocumentFilter filter = new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int off, int len, String text, AttributeSet a) throws BadLocationException {
                // 목록 칸은 쳐 넣는 동안 친 글로 채운 값을 보인다("14"는 "14:00", "2"는 "14:00", D-98). 채운 값이 보이는 동안
                // 다음 글자는 칸에 보이는 글이 아니라 친 글 뒤에 붙인다("2", "1"은 "21:00", D-98).
                Document d = fb.getDocument();
                String now = d.getText(0, d.getLength());
                boolean filled = now.equals(shown) && off == raw.length() && off + len == now.length();
                String typed = text == null || text.isEmpty() || off + len != now.length() ? null : (filled ? raw : now.substring(0, off)) + text;
                String match = !(spinner.getModel() instanceof SpinnerListModel) || typed == null ? null : Typed.fill(typed);
                shown = match;
                raw = match == null ? "" : typed;
                if (match != null) {
                    fb.replace(0, now.length(), match, a);
                    field.select(typed.length(), match.length());
                } else if (filled && typed != null) {
                    fb.replace(0, now.length(), typed, a);
                } else {
                    super.replace(fb, off, len, text, a);
                }
                edited(fb.getDocument().getText(0, fb.getDocument().getLength()), true);
            }

            @Override
            public void remove(FilterBypass fb, int off, int len) throws BadLocationException {
                // D-98: 채운 값이 보이는 동안 지우기는 친 글의 마지막 글자를 지우고 남은 글로 다시 채워 보인다("14" 뒤 Backspace는 "13:00").
                String now = fb.getDocument().getText(0, fb.getDocument().getLength());
                if (now.equals(shown) && !raw.isEmpty() && off == raw.length() && off + len == now.length()) {
                    String left = raw.substring(0, raw.length() - 1);
                    shown = Typed.fill(left);
                    raw = shown == null ? "" : left;
                    fb.replace(0, now.length(), shown == null ? left : shown, null);
                    field.select(left.length(), field.getText().length());
                } else {
                    shown = null;
                    super.remove(fb, off, len);
                }
                edited(fb.getDocument().getText(0, fb.getDocument().getLength()), true);
            }
        };
        field.setFormatterFactory(new DefaultFormatterFactory(
                new JFormattedTextField.AbstractFormatter() {
                    @Override
                    public Object stringToValue(String text) throws ParseException {
                        Object now = spinner.getValue();
                        if (text.equals(String.valueOf(now))) {
                            return now;
                        }
                        // D-98: 친 글을 모두 지워 비운 채 벗어나면 빨간 글 없이 치기 앞의 상태(값, 칸 아래 글, 옮길 날)로 돌아간다.
                        if (text.isBlank()) {
                            if (undo != null) {
                                undo.run();
                            }
                            undo = null;
                            return now;
                        }
                        undo = null;
                        typed = true;
                        Typed.Read r = reader.apply(text);
                        show(r.notice(), r.rejected() ? Theme.SUNDAY : Theme.INK_DIM, r.rejected() ? now : r.value());
                        if (r.rejected()) {
                            throw new ParseException(text, 0);
                        }
                        days = r.days();
                        daysFor = r.value();
                        return r.value();
                    }

                    @Override
                    public String valueToString(Object value) {
                        return value == null ? "" : value.toString();
                    }

                    @Override
                    protected DocumentFilter getDocumentFilter() {
                        return filter;
                    }
                }));
        spinner.addChangeListener(e -> edited(spinner.getValue(), false));
    }

    /** 칸 아래 글을 지운다(칸이 꺼질 때). */
    public void clear() {
        undo = null;
        show(null, null, null);
    }

    /** 칸 값이 마지막으로 읽은 값이면 그때 옮길 날 수, 아니면 0. */
    public int days(Object value) {
        return value.equals(daysFor) ? days : 0;
    }

    /** 칸의 글이나 값이 글이 말하는 값과 다르면(쳐 넣기 시작, 화살표) 글을 지운다. 다른 칸을 고칠 때는 남는다. */
    private void edited(Object now, boolean typing) {
        if (about != null && !about.toString().equals(now.toString())) {
            if (typing) {
                String text = plain;
                java.awt.Color color = notice.getForeground();
                Object value = about;
                int d = days;
                undo = () -> {
                    show(text, color, value);
                    days = d;
                };
            }
            show(null, null, null);
        }
        if (!typing) {
            undo = null;
        }
    }

    /** 글을 보이거나(text가 null이면) 지운다. 글이 늘거나 줄면 창 크기를 다시 맞춘다. */
    private void show(String text, java.awt.Color color, Object value) {
        about = text == null ? null : value;
        plain = text == null ? "" : text;
        // D-98: 글이 사라지면("밤 4시" 뒤 화살표) 그 읽기로 옮길 날도 없어진다.
        days = text == null ? 0 : days;
        notice.setForeground(color);
        notice.setVisible(text != null);
        refold();
    }

    /** 보이는 글을 지금의 wrap 폭으로 다시 접고 창 크기를 맞춘다. 값을 넣은 뒤 칸 묶음 폭이 바뀌면 부른다. */
    public void refold() {
        int w = wrap.getAsInt();
        notice.setText(w > 0 && !plain.isEmpty() ? Ui.wrap(plain, notice, w) : plain);
        relayout.run();
    }
}
