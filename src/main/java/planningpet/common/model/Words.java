package planningpet.common.model;

/** 여러 도메인이 쓰는 글자 규칙(06 16번 "하는 일"의 받침, 조사, 제목 줄임). */
public final class Words {
    private Words() {
    }

    /** 이름 뒤에 붙는 말. 마지막 글자에 받침이 있으면 "이야", 없으면 "야". */
    public static String iya(String name) {
        return hasBatchim(name) ? "이야" : "야";
    }

    /** 마지막 글자를 읽을 때 받침이 있는지. 한글 음절과 숫자(영, 일, 삼, 육, 칠, 팔)만 본다. */
    public static boolean hasBatchim(String word) {
        char last = word.charAt(word.length() - 1);
        if (last >= 0xAC00 && last <= 0xD7A3) {
            return (last - 0xAC00) % 28 != 0;
        }
        return "013678".indexOf(last) >= 0;
    }

    /** 마지막 글자에 받침이 있으면 withBatchim, 없으면 without. */
    public static String josa(String word, String withBatchim, String without) {
        return hasBatchim(word) ? withBatchim : without;
    }

    /** 15자를 넘으면 앞 14자 뒤에 "…". */
    public static String shortTitle(String title) {
        return title.length() > 15 ? title.substring(0, 14) + "…" : title;
    }
}
