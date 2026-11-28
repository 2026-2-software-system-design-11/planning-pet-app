package planningpet.common.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;

/** 공용 누르기 도우미 Input(#520). 글 없는 부품으로 본다. */
class InputTest {
    /** 받은 마우스 사건을 "번호 x,y 횟수 단추 눌린 단추"로 적는 판. */
    private static JPanel heard(List<String> out) {
        JPanel p = new JPanel();
        p.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                out.add(say(e));
            }

            @Override
            public void mousePressed(MouseEvent e) {
                out.add(say(e));
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                out.add(say(e));
            }
        });
        return p;
    }

    private static String say(MouseEvent e) {
        return e.getID() + " " + e.getX() + "," + e.getY() + " " + e.getClickCount() + " " + e.getButton() + " "
                + (e.getModifiersEx() != 0);
    }

    @Test
    void mouseSendsOneEventAtFiveFive() {
        List<String> out = new ArrayList<>();
        JPanel p = heard(out);
        Input.mouse(p, MouseEvent.MOUSE_ENTERED);
        Input.mouse(p, MouseEvent.MOUSE_PRESSED);
        assertEquals(List.of(MouseEvent.MOUSE_ENTERED + " 5,5 0 0 false", MouseEvent.MOUSE_PRESSED + " 5,5 1 1 true"),
                out);
    }

    @Test
    void clickTellsEveryListener() {
        List<String> out = new ArrayList<>();
        JPanel p = heard(out);
        p.addMouseListener(heard(out).getMouseListeners()[0]);
        Input.click(p);
        assertEquals(List.of(MouseEvent.MOUSE_CLICKED + " 5,5 1 1 false", MouseEvent.MOUSE_CLICKED + " 5,5 1 1 false"),
                out);
    }
}
