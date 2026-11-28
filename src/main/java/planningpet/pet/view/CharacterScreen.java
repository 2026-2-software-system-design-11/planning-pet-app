package planningpet.pet.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import planningpet.app.model.AppData;
import planningpet.common.view.Card;
import planningpet.common.view.Icons;
import planningpet.common.view.Images;
import planningpet.common.view.Pill;
import planningpet.common.view.Theme;
import planningpet.pet.controller.PetController;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.PetState;
import planningpet.pet.model.Pose;
import planningpet.pet.model.Slot;
import planningpet.pet.model.WearId;
import planningpet.reward.model.Level;
import planningpet.shop.model.ItemId;

/**
 * 캐릭터 창 안(05 캐릭터 창, D-47~D-51, D-58). 큰 카드에 왼쪽 열(펫 탭, 방 속 큰 펫)을 둔다. 자세 다섯 칸(S8e2),
 * 오른쪽 카드(S8f)는 그 몫이 더한다. 데이터가 바뀌면 다시 그린다(docs/porting/character.md S8e).
 */
final class CharacterScreen extends JPanel {
    private final AppData data;
    private final PetController pets;
    /** 펫 탭 줄. 고양이, 강아지 차례다. */
    final JPanel petTabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    /** 방 그림 위 큰 펫 무대. */
    final JComponent stage = stage();
    /** 왼쪽 열(펫 탭, 방 카드). */
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
        Card roomCard = new Card(new BorderLayout(), 18);
        roomCard.setBorder(BorderFactory.createEmptyBorder(5, 5, 7, 5));
        roomCard.add(stage);
        left.add(roomCard, BorderLayout.CENTER);
        return left;
    }

    /** 큰 펫의 상자 한 변: 무대 폭의 0.62배와 높이의 0.74배 가운데 작은 쪽. */
    static int petSize(int w, int h) {
        return (int) Math.min(w * 0.62, h * 0.74);
    }

    /** 큰 펫이 발을 붙이는 바닥: 무대 높이의 0.9. */
    static int ground(int h) {
        return h * 9 / 10;
    }

    /** 단계를 정해 자세 pose로 그린다(D-50). 상자는 size에 단계 비율을 곱한 것이고 움직이지 않는다(t=0). */
    static void drawStage(Graphics2D g, PetKind kind, Pose pose, Map<Slot, WearId> wear, int stage, int x, int ground,
            int size) {
        Set<WearId> worn = new HashSet<>(wear.values());
        PetArt.drawAt(g, kind, pose, worn, 0, 99, x, ground, size * PetArt.stageScale(stage));
    }

    /** 큰 펫 그림의 실제 높이(바닥에서 머리 끝까지). 하트와 강조선 자리를 잰다. */
    static int drawnHeight(PetState p, int size) {
        double box = size * PetArt.stageScale(Level.of(p.getExp()).getStage());
        return (int) (box * PetArt.heightRatio(p.getKind(), p.getPose()));
    }

    /** 방 그림 위 큰 펫. 저장된 자세와 장신구로 그린다. */
    private JComponent stage() {
        return new JComponent() {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                g.clip(new RoundRectangle2D.Double(0, 0, w, getHeight(), Theme.IMAGE_RADIUS, Theme.IMAGE_RADIUS));
                if (Images.ROOM != null) {
                    Images.cover(g, Images.ROOM, 0, 0, w, getHeight());
                }
                PetState p = data.activePet();
                int ground = ground(getHeight());
                int size = petSize(w, getHeight());
                BufferedImage rug = Images.item(ItemId.RUG);
                if (rug != null) {
                    Images.fitBottom(g, rug, w / 2 - size * 3 / 5, ground - size / 5, size * 6 / 5, size / 4);
                }
                g.setColor(new Color(0x9A, 0x6A, 0x45, 40));
                g.fillOval(w / 2 - size * 3 / 10, ground - size / 22, size * 3 / 5, size / 11);
                drawStage(g, p.getKind(), p.getPose(), p.getWear(), Level.of(p.getExp()).getStage(), w / 2, ground, size);
                BufferedImage yarn = Images.item(ItemId.YARN_BALL);
                if (yarn != null) {
                    Images.fitBottom(g, yarn, w / 2 - size * 3 / 5, ground - size / 6, size / 5, size / 5);
                }
                int top = ground - drawnHeight(p, size);
                Icons.draw(g, Icons.Kind.HEART, w / 2 - size * 2 / 5, top + size / 8, size / 7, Theme.HEART);
                Icons.draw(g, Icons.Kind.HEART, w / 2 - size * 3 / 10, top + size / 30, size / 10, Theme.HEART);
                Icons.draw(g, Icons.Kind.SPARK, w / 2 + size / 4, top + size / 6, size / 6, Theme.SPARK);
                g.dispose();
            }
        };
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
        stage.repaint();
    }
}