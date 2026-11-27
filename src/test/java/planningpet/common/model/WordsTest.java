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
}
