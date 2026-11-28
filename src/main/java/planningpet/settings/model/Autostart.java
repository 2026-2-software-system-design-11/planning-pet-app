package planningpet.settings.model;

import java.io.IOException;

/** 컴퓨터 켤 때 실행(06 25번). 운영체제에 등록하는 구현은 settings.store.OsAutostart다(07). */
public interface Autostart {
    /** 설치 파일로 설치한 앱이라 등록할 수 있는가. 실행 JAR면 false다(D-29). */
    boolean available();

    /** 지금 운영체제에 등록되어 있는가. 부를 때마다 운영체제에서 다시 읽는다. */
    boolean registered();

    /** 켜면 등록하고 끄면 지운다. 운영체제가 거절하면 그 이유를 글로 담은 IOException이다. */
    void set(boolean on) throws IOException;
}
