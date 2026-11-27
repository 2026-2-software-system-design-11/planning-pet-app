package planningpet.schedule.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** 06 19번. 시간표 한 날에서 겹친 일정을 나란히 놓을 열. */
public final class TimetableLayout {
    private TimetableLayout() {
    }

    /**
     * 한 날짜의 시각 있는 일정마다 열과 무리의 열 수. 시작 순, 같으면 긴 것, 같으면 넣은 순으로 늘어놓은 순서다. 겹침으로 이어진 일정이
     * 한 무리이고, 각 일정은 앞 일정이 끝난 가장 왼쪽 열에 들어간다. 시각은 앱 하루의 분(D-77)이다.
     */
    public static List<Slot> of(List<Task> dayTasks) {
        List<Task> sorted = dayTasks.stream()
                .sorted(Comparator.comparingInt(Task::startMinute)
                        .thenComparing(Comparator.comparingInt(Task::getDurationMinutes).reversed())
                        .thenComparingLong(Task::getSeq))
                .toList();
        List<Slot> result = new ArrayList<>();
        List<Integer> columnEnds = new ArrayList<>();
        int clusterStart = 0;
        int clusterEnd = -1;
        for (Task t : sorted) {
            if (t.startMinute() >= clusterEnd) {
                close(result, clusterStart, columnEnds.size());
                clusterStart = result.size();
                columnEnds.clear();
            }
            int col = 0;
            while (col < columnEnds.size() && columnEnds.get(col) > t.startMinute()) {
                col++;
            }
            if (col == columnEnds.size()) {
                columnEnds.add(t.endMinute());
            } else {
                columnEnds.set(col, t.endMinute());
            }
            result.add(new Slot(t, col, 0));
            clusterEnd = Math.max(clusterEnd, t.endMinute());
        }
        close(result, clusterStart, columnEnds.size());
        return result;
    }

    /** from부터 끝까지(끝난 무리)의 열 수를 count로 적는다. */
    private static void close(List<Slot> result, int from, int count) {
        for (int i = from; i < result.size(); i++) {
            Slot s = result.get(i);
            result.set(i, new Slot(s.task(), s.column(), count));
        }
    }
}
