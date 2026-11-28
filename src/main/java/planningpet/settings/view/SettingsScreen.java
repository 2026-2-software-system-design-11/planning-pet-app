package planningpet.settings.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.util.function.IntSupplier;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import planningpet.app.model.AppData;
import planningpet.common.view.Ui;
import planningpet.settings.controller.SettingsController;
import planningpet.settings.model.Autostart;

/** 설정(05 설정, UC-18). 값은 프로토타입 settings/view/SettingsScreen이다(docs/porting/settings.md S10b). */
public final class SettingsScreen extends JPanel {
    /** 05 설정 그림의 버전. */
    static final String VERSION = "0.1.0";
    /** 05 설정 1번. 실행 JAR와 Linux에서 체크박스 아래에 보인다(Linux 글은 잠정, S10b). */
    static final String UNAVAILABLE = "설치 파일로 설치했을 때만 쓸 수 있어요";
    private final AppData data;
    private final Autostart os;
    private int row;
    final JCheckBox autostart = new JCheckBox("컴퓨터 켤 때 같이 실행");
    final JLabel autostartHint = Ui.hint(UNAVAILABLE);
    final JButton open = new JButton("폴더 열기");
    /** D-86. 켜면 홈 달력 칸의 수에 고정 일정도 센다. */
    final JCheckBox calendarFixed = new JCheckBox("달력에 고정 일정 보이기");
    /** D-91. 한 주 시작. 기본은 월요일. */
    final JRadioButton monday = new JRadioButton("월요일", true);
    final JRadioButton sunday = new JRadioButton("일요일");
    /** D-74. 예시 일정을 한 번에 지운다. 예시가 없으면 흐리다. */
    final JButton sampleButton = new JButton("예시 지우기");
    final JLabel sampleCount = Ui.hint("");
    /** 남은 예시 일정의 수(D-74). 세는 일은 C5가 넣는다. */
    public IntSupplier samples = () -> 0;
    /** "예시 지우기"를 누르면 부른다(D-74). 확인과 지우기는 C5가 넣는다. */
    public Runnable clearSamples = () -> { };

    /** folder는 데이터 폴더(06 21번)다. 데이터 변경 알림에 refresh를 거는 일은 화면을 넣는 쪽(Main)이 한다. */
    public SettingsScreen(AppData data, SettingsController settings, Autostart os, Path folder) {
        super(new GridBagLayout());
        this.data = data;
        this.os = os;
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        autostart.setEnabled(os.available());
        autostartHint.setVisible(!os.available());
        Path full = folder.toAbsolutePath();
        line(new JLabel("<html><b>시작</b></html>"), autostart);
        line(new JLabel(), autostartHint);
        line(new JLabel("<html><b>데이터 위치</b></html>"), Ui.flow(new JLabel(full.toString()), open));
        line(new JLabel("<html><b>달력</b></html>"), calendarFixed);
        line(new JLabel(), Ui.hint("켜면 수업 같은 고정 일정도 홈 달력 칸의 수에 들어가요"));
        calendarFixed.addActionListener(e -> settings.setCalendarFixed(calendarFixed.isSelected()));
        ButtonGroup week = new ButtonGroup();
        week.add(monday);
        week.add(sunday);
        line(new JLabel("<html><b>한 주 시작</b></html>"), Ui.flow(monday, sunday));
        line(new JLabel(), Ui.hint("달력, 일정관리, 통계의 한 주가 이 요일부터 시작해요"));
        monday.addActionListener(e -> settings.setWeekStart(DayOfWeek.MONDAY));
        sunday.addActionListener(e -> settings.setWeekStart(DayOfWeek.SUNDAY));
        sampleButton.addActionListener(e -> clearSamples.run());
        line(new JLabel("<html><b>예시 일정</b></html>"), Ui.flow(sampleButton, sampleCount));
        line(new JLabel("<html><b>버전</b></html>"), new JLabel(VERSION));
        GridBagConstraints filler = new GridBagConstraints();
        filler.gridy = row;
        filler.weighty = 1;
        add(new JLabel(), filler);
        refresh();
    }

    /** 설정 값, 운영체제 등록(06 25번, 부를 때마다 다시 읽는다), 남은 예시 일정을 보인다. */
    public void refresh() {
        autostart.setSelected(os.registered());
        calendarFixed.setSelected(data.isCalendarFixed());
        monday.setSelected(data.getWeekStart() == DayOfWeek.MONDAY);
        sunday.setSelected(data.getWeekStart() == DayOfWeek.SUNDAY);
        int count = samples.getAsInt();
        sampleButton.setEnabled(count > 0);
        sampleCount.setText(count > 0 ? "예시 일정 " + count + "개가 남아 있어요" : "예시 일정이 없어요");
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
