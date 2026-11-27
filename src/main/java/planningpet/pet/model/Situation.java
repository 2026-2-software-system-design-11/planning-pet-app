package planningpet.pet.model;

/** 펫이 말하는 상황(06 16번 표). 상황마다 PetTalk.say가 받는 인자가 다르다(docs/porting/pet-talk.md). */
public enum Situation {
    /** 첫 실행의 첫 브리핑. 처음 만난 인사다(D-74). */
    FIRST_RUN,
    /** 8일 이상 만에 브리핑(D-23). */
    WELCOME_BACK,
    /** 메인에서 오늘이 아닌 날을 고름. */
    OTHER_DATE
}
