package planningpet.pet.view;

import planningpet.app.model.Store;
import planningpet.app.view.App;
import planningpet.common.view.*;
import planningpet.pet.model.Bond;
import planningpet.pet.model.PetState;
import planningpet.pet.model.Poses;
import planningpet.pet.model.Wardrobe;
import planningpet.reward.model.Level;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

/**
 * 캐릭터(05 캐릭터, D-47~D-51). 배치는 2026-11-23 캐릭터 창 예시를 따른다. 왼쪽은 펫 탭, 방 속 큰 펫(자세와 입혀 볼 장신구),
 * 자세 다섯 칸. 오른쪽은 이름과 이름 변경, 레벨과 친밀도와 경험치, 성장 단계, 상태와 꾸미기와 정보 탭, 꾸미기 적용이다.
 */
public final class CharacterScreen extends JPanel {
    private final App app;
    private final JPanel petTabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    private final JPanel poses = new JPanel(new GridLayout(1, Poses.ALL.size(), 10, 0));
    public final JLabel name = new JLabel();
    public final JButton rename = new JButton("이름 변경");
    public final JLabel level = new JLabel();
    public final JLabel bond = new JLabel();
    private final JLabel expText = new JLabel();
    private final Bar expBar = new Bar(0, 12);
    private final JComponent stages;
    private final CardLayout tabCards = new CardLayout();
    private final JPanel tabBody = new JPanel(tabCards);
    private final Pill[] tabs = {new Pill("상태", Icons.Kind.CHART, 14), new Pill("꾸미기", Icons.Kind.SHIRT, 14),
        new Pill("정보", Icons.Kind.INFO, 14)};
    private final JToggleButton[] slotButtons = new JToggleButton[Wardrobe.SLOTS.size()];
    public final JPanel wears = new JPanel(new GridLayout(1, 4, 10, 0));
    public final JButton apply = new JButton("꾸미기 적용");
    private final JLabel stateText = new JLabel();
    private final JLabel infoText = new JLabel();
    private final JComponent stageView;
    /** 아직 적용하지 않은 장신구(D-51). draftKind의 펫 것이다. */
    public final Map<String, String> draft = new LinkedHashMap<>();
    private String draftKind;
    private String slot = Wardrobe.NECK;

    public CharacterScreen(App app) {
        super(new BorderLayout());
        this.app = app;
        Card main = new Card(new BorderLayout(Theme.GAP + 4, 0), 24);
        main.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        stageView = big();
        stages = growth();
        main.add(left(), BorderLayout.CENTER);
        main.add(right(), BorderLayout.EAST);
        add(main);
        app.watch(this::refresh);
    }

    private JComponent left() {
        JPanel col = new JPanel(new BorderLayout(0, 10));
        col.add(petTabs, BorderLayout.NORTH);
        Card roomCard = new Card(new BorderLayout(), 18);
        roomCard.setBorder(BorderFactory.createEmptyBorder(5, 5, 7, 5));
        roomCard.add(stageView);
        col.add(roomCard, BorderLayout.CENTER);
        poses.setPreferredSize(new Dimension(0, 92));
        col.add(poses, BorderLayout.SOUTH);
        return col;
    }

    /** 방 그림 위 큰 펫. 입혀 보는 장신구(draft)로 그린다. 양옆 화살표는 자세를 바꾼다. */
    private JComponent big() {
