package planningpet.pet.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.HashSet;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import planningpet.app.model.AppData;
import planningpet.common.view.Card;
import planningpet.common.view.Pill;
import planningpet.common.view.Theme;
import planningpet.pet.controller.PetController;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.PetState;
import planningpet.reward.model.Level;

/**
 * 캐릭터 창 안(05 캐릭터 창, D-47~D-51, D-58). 큰 카드에 왼쪽 열을 두고 위에 펫 탭을 놓는다. 큰 펫 무대, 자세 다섯 칸(S8e2),
 * 오른쪽 카드(S8f)는 그 몫이 더한다. 데이터가 바뀌면 다시 그린다(docs/porting/character.md S8e).
 */
final class CharacterScreen extends JPanel {
    private final AppData data;
    private final PetController pets;
    /** 펫 탭 줄. 고양이, 강아지 차례다. */
    final JPanel petTabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    /** 왼쪽 열(펫 탭). */
    final JPanel left = new JPanel(new BorderLayout(0, 10));

    CharacterScreen(AppData data, PetController pets) {
        super(new BorderLayout());
        this.data = data;
        this.pets = pets;
        Card main = new Card(new BorderLayout(Theme.GAP + 4, 0), 24);
        main.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        main.add(left(), BorderLayout.CENTER);
        add(main);
        // 창(D-58)은 메인 창 밖이라 앱 틀의 투명 처리가 닿지 않는다. 다시 그린 부품도 투명하게 한다.
        data.onChange(() -> {
            refresh();
            Theme.clear(this);
        });
        Theme.clear(this);
    }

    private JComponent left() {
        left.add(petTabs, BorderLayout.NORTH);
        return left;
    }

    /** 펫 탭을 다시 만든다. */
    void refresh() {
        PetState p = data.activePet();
        petTabs.removeAll();
        ButtonGroup g = new ButtonGroup();
        for (PetKind kind : PetKind.values()) {
            PetState q = data.pet(kind);
            if (q == null) {
                continue;
            }
            // D-81: 지은 이름과 레벨, 종류는 이름 옆에 작게.
            Pill tab = new Pill(q.getName(), null, 16).small(kind.label())
                    .picture(PetArt.face(kind, new HashSet<>(q.getWear().values())))
                    .second("Lv. " + Level.of(q.getExp()).getLevel());
            tab.setPreferredSize(new Dimension(Math.max(200, tab.getPreferredSize().width), 58));
            tab.setSelected(q == p);
            tab.addActionListener(e -> {
                if (kind != data.getActiveKind()) {
                    pets.select(kind);
                } else {
                    refresh();
                }
            });
            g.add(tab);
            petTabs.add(tab);
        }
        petTabs.revalidate();
    }
}