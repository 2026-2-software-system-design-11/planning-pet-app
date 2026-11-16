package planningpet.proto;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** 설정(05 설정, UC-18). 프로토타입은 실행 JAR라 컴퓨터 켤 때 실행이 비활성이다(D-29). */
public final class SettingsScreen extends JPanel {
    private int row;

    public SettingsScreen() {
        super(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        JCheckBox autostart = new JCheckBox("컴퓨터 켤 때 같이 실행");
        autostart.setEnabled(false);
        JButton open = new JButton("폴더 열기");
        open.setEnabled(false);
        line(new JLabel("<html><b>시작</b></html>"), autostart);
        line(new JLabel(), Ui.hint("설치 파일로 설치했을 때만 쓸 수 있어요"));
        line(new JLabel("<html><b>데이터 위치</b></html>"), Ui.flow(
                new JLabel("~/Library/Application Support/PlanningPet"), open));
        line(new JLabel(), Ui.hint("프로토타입은 아무것도 저장하지 않아요"));
        line(new JLabel("<html><b>버전</b></html>"), new JLabel("0.1 프로토타입"));
        GridBagConstraints filler = new GridBagConstraints();
        filler.gridy = row;
        filler.weighty = 1;
        add(new JLabel(), filler);
    }

    private void line(JComponent label, JComponent value) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row++;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(6, 0, 6, 16);
        add(label, c);
        c.gridx = 1;
        c.weightx = 1;
        add(value, c);
    }
}
