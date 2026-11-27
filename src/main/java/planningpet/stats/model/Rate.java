package planningpet.stats.model;

/** 끝낸 수와 셀 일정 수(06 29번 출력). */
public record Rate(int done, int total) {
    /** 반올림한 백분율. 셀 일정이 없으면 0이다. */
    public int percent() {
        return total == 0 ? 0 : Math.round(done * 100f / total);
    }
}
