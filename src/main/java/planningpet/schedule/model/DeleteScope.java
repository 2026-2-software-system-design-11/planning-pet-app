package planningpet.schedule.model;

/** 반복 사본을 지울 때의 범위(06 5번, D-42): 이 일정만, 이후 반복 모두. 반복 사본이 아니면 화면은 묻지 않고 ONLY_THIS를 넘긴다. */
public enum DeleteScope {
    ONLY_THIS, THIS_AND_AFTER
}
