package planningpet.common.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** 06 16번의 글자 규칙과 docs/porting/day-rules.md 2절의 경계. */
class WordsTest {
    @Test
    void E_WORD_1_nameWithoutBatchim() {
        assertEquals("야", Words.iya("나비"));
    }

    @Test
    void E_WORD_2_nameWithBatchim() {
        assertEquals("이야", Words.iya("콩"));
    }

    @Test
    void E_WORD_3_latinName() {
        assertEquals("야", Words.iya("Max"));
    }

    @Test
    void E_WORD_4_firstSyllable() {
        assertEquals("야", Words.iya("가"));
    }

    @Test
    void E_WORD_5_lastSyllable() {
        assertEquals("이야", Words.iya("힣"));
    }

    @Test
    void E_WORD_6_digitWithBatchim() {
        assertEquals("이야", Words.iya("펫1"));
    }

    @Test
    void E_WORD_7_digitWithoutBatchim() {
        assertEquals("야", Words.iya("펫2"));
    }

    @Test
    void E_WORD_8_jamo() {
        assertEquals("야", Words.iya("ㅋ"));
    }

    @Test
    void E_WORD_9_empty() {
        assertThrows(StringIndexOutOfBoundsException.class, () -> Words.iya(""));
    }

    @Test
    void E_WORD_10_dueWords() {
        assertEquals("이야", Words.josa("내일", "이야", "야"));
        assertEquals("야", Words.josa("모레", "이야", "야"));
        assertEquals("야", Words.josa("3일 뒤", "이야", "야"));
    }

    @Test
    void E_WORD_11_numbersAndLatin() {
        assertEquals("가", Words.josa("레벨 2", "이", "가"));
        assertEquals("이", Words.josa("레벨 10", "이", "가"));
        assertEquals("가", Words.josa("Project", "이", "가"));
    }

    @Test
    void E_WORD_12_rieulBatchim() {
        assertEquals("으로", Words.josa("1일", "으로", "로"));
    }

    private static final String HANGUL_15 = "가나다라마바사아자차카타파하가";
    private static final String SMILE = "\uD83D\uDE00";

    @Test
    void E_WORD_13_fifteen() {
        assertEquals(HANGUL_15, Words.shortTitle(HANGUL_15));
    }

    @Test
    void E_WORD_14_sixteen() {
        assertEquals("가나다라마바사아자차카타파하…", Words.shortTitle(HANGUL_15 + "나"));
    }

    @Test
    void E_WORD_15_talk4Title() {
        assertEquals("2026 하반기 공채 자기…", Words.shortTitle("2026 하반기 공채 자기소개서 1번 문항"));
    }

    @Test
    void E_WORD_16_empty() {
        assertEquals("", Words.shortTitle(""));
    }

    @Test
    void E_WORD_17_emoji() {
        assertEquals(SMILE.repeat(15), Words.shortTitle(SMILE.repeat(15)));
    }

    @Test
    void E_WORD_18_emojiFifteenth() {
        String title = "가".repeat(13) + SMILE + "나";
        assertEquals(title, Words.shortTitle(title));
    }

    @Test
    void E_WORD_19_emojiFourteenth() {
        assertEquals("가".repeat(13) + SMILE + "…", Words.shortTitle("가".repeat(13) + SMILE + "나나"));
    }

    @Test
    void E_WORD_20_joinedEmoji() {
        String man = "\uD83D\uDC68";
        String family = man + "\u200D\uD83D\uDC69\u200D\uD83D\uDC67";
        assertEquals("가".repeat(13) + man + "…", Words.shortTitle("가".repeat(13) + family + "나"));
    }
}
