package planningpet.pet.view;

import planningpet.app.view.App;

import planningpet.app.controller.AppController;
import planningpet.app.model.MockData;
import planningpet.app.model.Store;
import planningpet.common.view.Ui;
import planningpet.pet.controller.PetController;
import planningpet.pet.model.Talk;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/** 반려동물 고르기 창(05, UC-17). 첫 실행 때 메인 창보다 먼저 뜬다. */
public final class PickDialog {
    public final JRadioButton cat = new JRadioButton("고양이", true);
    public final JRadioButton dog = new JRadioButton("강아지");
    public final JTextField name = new JTextField(12);
    final JButton start = new JButton("시작하기");
    public final JPanel panel = new JPanel(new BorderLayout(0, 10));

    public PickDialog() {
        Ui.limit(name, 10);
        ButtonGroup group = new ButtonGroup();
        group.add(cat);
        group.add(dog);
        JPanel animals = new JPanel(new GridLayout(2, 2, 16, 4));
        animals.add(new PetView(MockData.look("CAT"), 2));
        animals.add(new PetView(MockData.look("DOG"), 2));
        animals.add(cat);
        animals.add(dog);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        panel.add(new JLabel("<html><b>함께할 반려동물을 골라 주세요</b></html>"), BorderLayout.NORTH);
        panel.add(animals, BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout(0, 2));
        bottom.add(Ui.flow(new JLabel("이름"), name, start), BorderLayout.NORTH);
        JLabel hint = Ui.hint("비워 두면 나비, 바둑이로 불러요");
        hint.setBorder(BorderFactory.createEmptyBorder(0, 44, 0, 0));
        bottom.add(hint, BorderLayout.SOUTH);
        panel.add(bottom, BorderLayout.SOUTH);
    }

    /** 시작하기 버튼과 같다. 고른 동물과 이름(비우면 나비, 바둑이)을 넣는다. */
    public void apply(Store s) {
        // 첫 실행 창은 앱 창보다 먼저 뜬다. 앱 controller를 이 데이터로 따로 만든다.
        new PetController(new AppController(s, new Talk(s)), s).choose(dog.isSelected(), name.getText());
    }

    /** D-48. 나의 방의 "+": 남은 종류 하나를 이름을 붙여 데려온다. 창 위 문구와 버튼만 다르고 이름 칸은 처음 고르기와 같다. */
    public static void adopt(App app) {
        boolean dog = app.store.pet("DOG") == null;
        if (app.store.pet(dog ? "DOG" : "CAT") != null) {
            return;
        }
        String kind = dog ? "강아지" : "고양이";
        JTextField field = new JTextField(12);
        Ui.limit(field, 10);
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBorder(BorderFactory.createEmptyBorder(10, 12, 6, 12));
        p.add(new JLabel("<html><b>" + kind + "를 데려올까요?</b></html>"), BorderLayout.NORTH);
        p.add(new PetView(MockData.look(dog ? "DOG" : "CAT"), 2), BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout(0, 2));
        bottom.add(Ui.flow(new JLabel("이름"), field), BorderLayout.NORTH);
        bottom.add(Ui.hint("비워 두면 " + (dog ? "바둑이" : "나비") + "로 불러요"), BorderLayout.SOUTH);
        p.add(bottom, BorderLayout.SOUTH);
        int answer = javax.swing.JOptionPane.showOptionDialog(app.root, p, kind + " 데려오기", javax.swing.JOptionPane.DEFAULT_OPTION,
                javax.swing.JOptionPane.PLAIN_MESSAGE, null, new String[] {"취소", "데려오기"}, "데려오기");
        if (answer == 1) {
            app.pet.adopt(dog, field.getText());
        }
    }

    /** 고르면 true. 닫기 버튼으로 닫으면 false이고 앱은 아무것도 저장하지 않고 끝난다. */
    public boolean ask(Store s) {
        JDialog d = new JDialog((java.awt.Frame) null, "플래닝펫 시작", true);
        boolean[] picked = {false};
        start.addActionListener(e -> {
            apply(s);
            picked[0] = true;
            d.dispose();
        });
        d.setContentPane(panel);
        d.getRootPane().setDefaultButton(start);
        d.pack();
        d.setLocationRelativeTo(null);
        d.setVisible(true);
        return picked[0];
    }
}
