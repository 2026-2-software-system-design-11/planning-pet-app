package planningpet.schedule.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.LongSupplier;

/** 일정 다루기(06 3번). 앱 데이터의 일정 목록과 넣은 순 번호를 받아 다룬다. */
public final class TaskBook {
    private final List<Task> tasks;
    private final LongSupplier nextSeq;

    /** nextSeq는 앱 데이터의 다음 넣은 순 번호를 하나 꺼내고 늘린다. */
    public TaskBook(List<Task> tasks, LongSupplier nextSeq) {
        this.tasks = new ArrayList<>(tasks);
        this.nextSeq = nextSeq;
    }

    /** 넣은 일정 모두. 바꿀 수 없는 목록이다. */
    public List<Task> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    /** 06 3번. 검사(2번)를 통과한 입력으로 일정을 만든다. 통과하지 않으면 IllegalArgumentException. */
    public List<Task> add(TaskDraft draft) {
        if (!draft.validate().isEmpty()) {
            throw new IllegalArgumentException("검사를 통과하지 않은 입력");
        }
        Task task = make(draft, draft.date());
        tasks.add(task);
        return List.of(task);
    }

    /** 06 데이터 절의 처음 값으로 day의 일정 하나를 만든다. 보상과 끝냄은 없다. */
    private Task make(TaskDraft d, LocalDate day) {
        boolean period = d.kind() == Kind.TASK && d.repeat() == null && d.dueDate() != null;
        // D-68: 기간 할 일의 시각은 목록과 자동 배치로만 정하므로 시각 없이 만든다.
        return new Task(UUID.randomUUID().toString(), nextSeq.getAsLong(), d.kind(), d.title().strip(), day,
                period ? null : d.startTime(), d.durationMinutes(), d.memo(),
                !period && d.startTime() != null && d.nextDay(), d.category(), null, null,
                period ? d.dueDate() : null, period ? day : null, false, null, 0, 0, null, null);
    }
}
