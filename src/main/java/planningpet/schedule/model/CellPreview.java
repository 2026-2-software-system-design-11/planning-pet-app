package planningpet.schedule.model;

import java.util.List;

/** 달력 칸 하나의 값(06 26번 출력, D-86): 셀 일의 수, 끝내지 않은 일이 있는지, 그날이 마감인 기간 할 일 제목(넣은 순). */
public record CellPreview(int count, boolean open, List<String> due) {
    /** 칸에 보이는 글 "{수}개". 셀 일 없이 마감만 있으면 빈 글이다(빨간 점만 찍는다, D-94). */
    public String text() {
        return count > 0 ? count + "개" : "";
    }

    /** 셀 일도 마감 제목도 없다. 칸에 수도 빨간 점도 없다. */
    public boolean isEmpty() {
        return count == 0 && due.isEmpty();
    }
}
