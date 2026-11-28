package planningpet.pet.view;

import java.awt.Dimension;
import javax.swing.JRadioButton;
import planningpet.common.view.Theme;
import planningpet.pet.model.PetKind;

/**
 * 반려동물 고르기 창의 동물 카드(05 입력 항목 1, 디자인 시스템 10절 "고르기 창"). 부품은 05대로 `JRadioButton`이고 모양만 카드로 그린다.
 * 흰 바깥 테 안에 종류별 바탕(고양이 차가운 하늘빛, 강아지 따뜻한 크림), 작은 언덕 위의 PetArt 펫, 발자국과 종류 이름, 설명 두 줄.
 * 고른 카드는 민트 테두리와 체크 배지, 펫 옆 강조선이다. 펫의 움직임은 #424가 더한다(docs/porting/pick.md).
 */
public final class PickCard extends JRadioButton {
    /** 1배 크기. 창 폭 800에 두 장과 사이 20이 들어간다. */
    static final int W = 248;
    static final int H = 262;

    public PickCard(String text, PetKind kind, boolean selected, String about) {
        super(text, selected);
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setRolloverEnabled(true);
        setFont(Theme.heavy(22));
        getAccessibleContext().setAccessibleDescription(about.replace("\n", " "));
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(W, H);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }
}
