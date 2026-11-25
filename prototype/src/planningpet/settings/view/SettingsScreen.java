package planningpet.settings.view;

import planningpet.app.view.App;
import planningpet.common.view.Ui;
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
    /** D-86. 켜면 홈 달력 칸의 수에 고정 일정도 센다. */
    public final JCheckBox calendarFixed = new JCheckBox("달력에 고정 일정 보이기");
    /** D-91. 한 주 시작. 기본은 월요일. */
    public final javax.swing.JRadioButton monday = new javax.swing.JRadioButton("월요일", true);
    public final javax.swing.JRadioButton sunday = new javax.swing.JRadioButton("일요일");
    /** D-74. 예시 일정을 한 번에 지운다. 예시가 없으면 흐리다. */
    public final JButton clearSamples = new JButton("예시 지우기");
    public final JLabel sampleCount = Ui.hint("");

    public SettingsScreen(App app) {
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
        line(new JLabel("<html><b>달력</b></html>"), calendarFixed);
        line(new JLabel(), Ui.hint("켜면 수업 같은 고정 일정도 홈 달력 칸의 수에 들어가요"));
        calendarFixed.addActionListener(e -> app.settings.showFixedOnCalendar(calendarFixed.isSelected()));
        javax.swing.ButtonGroup week = new javax.swing.ButtonGroup();
        week.add(monday);
        week.add(sunday);
        line(new JLabel("<html><b>한 주 시작</b></html>"), Ui.flow(monday, sunday));
        line(new JLabel(), Ui.hint("달력, 일정관리, 통계의 한 주가 이 요일부터 시작해요"));
        monday.addActionListener(e -> app.settings.weekStart(java.time.DayOfWeek.MONDAY));
        sunday.addActionListener(e -> app.settings.weekStart(java.time.DayOfWeek.SUNDAY));
        clearSamples.addActionListener(e -> app.schedule.clearSamples());
        line(new JLabel("<html><b>예시 일정</b></html>"), Ui.flow(clearSamples, sampleCount));
        line(new JLabel("<html><b>버전</b></html>"), new JLabel("0.1 프로토타입"));
        GridBagConstraints filler = new GridBagConstraints();
        filler.gridy = row;
        filler.weighty = 1;
        add(new JLabel(), filler);
        app.watch(() -> {
            calendarFixed.setSelected(app.store.calendarFixed);
            monday.setSelected(app.store.weekStart == java.time.DayOfWeek.MONDAY);
            sunday.setSelected(app.store.weekStart == java.time.DayOfWeek.SUNDAY);
            int samples = app.schedule.samples().size();
            clearSamples.setEnabled(samples > 0);
            sampleCount.setText(samples > 0 ? "예시 일정 " + samples + "개가 남아 있어요" : "예시 일정이 없어요");
        });
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
