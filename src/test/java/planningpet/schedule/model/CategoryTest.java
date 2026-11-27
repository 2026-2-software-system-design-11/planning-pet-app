package planningpet.schedule.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/** docs/porting/task.md 2절의 유형 경계. */
class CategoryTest {
    @Test
    void E_CAT_1_labelsInOrder() {
        List<String> labels = Arrays.stream(Category.values()).map(Category::label).toList();
        assertEquals(List.of("학업", "운동", "프로젝트", "개인/취미", "기타"), labels);
    }
}
