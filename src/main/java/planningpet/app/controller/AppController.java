package planningpet.app.controller;

import java.io.IOException;
import java.util.function.Consumer;
import planningpet.app.model.AppData;
import planningpet.app.model.DataStore;

/** 앱 하나의 controller. 도메인 controller의 변경을 저장과 함께 감싼다(06 22번, 07). */
public final class AppController {
    private final AppData data;
    private final DataStore store;
    /** 저장 실패 창을 띄우는 view의 함수. 창 내용을 받는다(07 "controller는 창을 모른다"). */
    private final Consumer<String> errors;

    public AppController(AppData data, DataStore store, Consumer<String> errors) {
        this.data = data;
        this.store = store;
        this.errors = errors;
    }

    /**
     * 06 22번. 복사본을 떠 두고 edit를 적용한 뒤 save를 한 번 부른다. 저장에 실패하면 복사본으로 되돌리고 errors에 창 내용을 넘긴다.
     * 성공하든 되돌리든 마지막에 데이터 변경 알림을 한 번 보낸다.
     */
    public void change(String what, Runnable edit) {
        AppData before = data.copy();
        edit.run();
        try {
            store.save(data);
        } catch (IOException e) {
            data.restore(before);
            errors.accept(what + " 저장에 실패해서 바꾸기 전으로 돌렸어요.");
        }
        data.changed();
    }
}
