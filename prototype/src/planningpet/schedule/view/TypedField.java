package planningpet.schedule.view;

import planningpet.common.view.Theme;
import java.text.ParseException;
import java.util.function.Function;
import javax.swing.*;
import javax.swing.text.*;

/** 칸에 쳐 넣은 글을 Typed로 읽어 넣고 맞췄으면 흐린 글, 받지 않았으면 빨간 글을 칸 아래에 보인다(D-98). */
public final class TypedField {
    public final JLabel notice = new JLabel();
    private final Runnable relayout;
    private Object about; // 글이 말하는 칸 값
    /** 칸에 글을 쳐 넣어 읽은 적이 있는가. 일정 입력 창은 그 뒤 30분 안내를 숨긴다(S83). */
    public boolean typed;
    /** 0보다 크면 글을 이 너비(px) 안에서 접는다(시각 고치기 창, D-98). */
    public java.util.function.IntSupplier wrap = () -> 0;

    public TypedField(JSpinner spinner, Function<String, Typed.Read> reader, Runnable relayout) {
        this.relayout = relayout;
        notice.setFont(Theme.font(12));
        notice.setVerticalAlignment(JLabel.TOP);
        notice.setVisible(false);
        DocumentFilter filter = new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int off, int len, String text, AttributeSet a) throws BadLocationException {
                // 목록 칸은 쳐 넣는 동안 그 앞부분으로 시작하는 첫 값을 채워 보인다("14"는 "14:00", 지금 동작).
                Document d = fb.getDocument();
                String typed = text == null || text.isEmpty() || off + len != d.getLength() ? null : d.getText(0, off) + text;
                Object match = !(spinner.getModel() instanceof SpinnerListModel list) || typed == null ? null
                        : list.getList().stream().filter(x -> x.toString().startsWith(typed)).findFirst().orElse(null);
                if (match != null) {
                    fb.replace(0, d.getLength(), match.toString(), a);
                    ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().select(off + text.length(), match.toString().length());
                } else {
                    super.replace(fb, off, len, text, a);
                }
                edited(fb.getDocument().getText(0, fb.getDocument().getLength()));
            }

            @Override
            public void remove(FilterBypass fb, int off, int len) throws BadLocationException {
                super.remove(fb, off, len);
                edited(fb.getDocument().getText(0, fb.getDocument().getLength()));
            }
        };
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setFormatterFactory(new DefaultFormatterFactory(
                new JFormattedTextField.AbstractFormatter() {
                    @Override
                    public Object stringToValue(String text) throws ParseException {
                        Object now = spinner.getValue();
                        if (text.equals(String.valueOf(now))) {
                            return now;
                        }
                        typed = true;
                        Typed.Read r = reader.apply(text);
                        show(r.notice(), r.rejected() ? Theme.SUNDAY : Theme.INK_DIM, r.rejected() ? now : r.value());
                        if (r.rejected()) {
                            throw new ParseException(text, 0);
                        }
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
        spinner.addChangeListener(e -> edited(spinner.getValue()));
    }

    /** 칸의 글이나 값이 글이 말하는 값과 다르면(쳐 넣기 시작, 화살표) 글을 지운다. 다른 칸을 고칠 때는 남는다. */
    private void edited(Object now) {
        if (about != null && !about.toString().equals(now.toString())) {
            show(null, null, null);
        }
    }

    /** 글을 보이거나(text가 null이면) 지운다. 글이 늘거나 줄면 창 크기를 다시 맞춘다. */
    private void show(String text, java.awt.Color color, Object value) {
        about = text == null ? null : value;
        int w = wrap.getAsInt();
        notice.setText(text == null ? "" : w > 0 ? "<html>" + text + "</html>" : text);
        notice.setPreferredSize(null);
        if (text != null && w > 0) {
            View v = (View) notice.getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey);
            v.setSize(w, 0);
            notice.setPreferredSize(new java.awt.Dimension(w, (int) Math.ceil(v.getPreferredSpan(View.Y_AXIS))));
        }
        notice.setForeground(color);
        notice.setVisible(text != null);
        relayout.run();
    }
}
