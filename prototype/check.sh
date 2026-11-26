#!/bin/sh
# 06 검사 표 가운데 프로토타입이 따르는 줄을 확인한다.
set -e
cd "$(dirname "$0")"
./build.sh
# D-46 검사(grep). 아래 형태만 잡는다. 이름을 바꿔 부르거나 다른 클래스를 거치면 놓친다.
# model: Swing과 AWT를 쓰지 않는다(import와 전체 이름 모두).
# view: 데이터 변경 알림과 펫 말을 보내지 않고, Model 메서드(일정 추가, 삭제, 반복에 넣기, 보상, 출석, 사기, 연속 접속)를 부르거나
#       Model 필드에 대입하거나(=, +=, -=, ++, --) Model 목록을 바꾸지 않는다.
# 알림(changed)은 앱 controller만 보낸다.
fields="calendarFixed|listClickTime|weekStart|petKind|petName|points|exp|streak|attendanceDay|lastOpenDay|start|title|minutes|memo|done|date|rewardPoints|rewardExp|rewardDay|rewardCount|rewardCountDay|talk|talkSeq|now|seriesId|kind|seq|nextSeq|dueDate|startDay|doneDay|repeat|nextDay"
bad=$( (grep -rnE "javax\.swing|java\.awt" src/planningpet/*/model
        grep -rnE "\.(changed|say)\(|book\.(add|delete|applyToSeries|update|setTime)\(|game\.(complete|uncomplete|attend)\(|\.buy\([^,)]*\)|Visits\.|\.($fields) *([-+*/]?=[^=]|\+\+|--)|\.(placed|owned|tasks|sampleIds)\.(add|remove|clear|removeIf|addAll)\(" src/planningpet/*/view | grep -v "this\.[a-zA-Z]* = "
        grep -rn "\.changed()" src | grep -v "src/planningpet/app/controller/AppController.java") || true)
if [ -n "$bad" ]; then
    echo "D-46 어긋남:"
    echo "$bad"
    exit 1
fi
# 05 시각 고치기 창: open()이 spot(T-EDIT-45가 화면 좌표까지 본다)의 결과를 그대로 창 자리로 쓰는지. 창을 띄우지 않는 검사는 창을 만들 수 없어 그 한 줄을 grep으로 본다.
grep -q "d.setLocation(spot(anchor, d.getSize(), app.root, app.root.getLocationOnScreen()));" src/planningpet/schedule/view/TimeEditor.java || { echo "T-EDIT-45 어긋남: TimeEditor.open이 spot을 쓰지 않음"; exit 1; }
# D-98: 칸 아래 글이 늘거나 줄면 창 크기를 다시 맞춘다. 창을 띄우지 않는 검사는 창을 만들 수 없어 그 줄을 grep으로 본다.
for f in TaskDialog TimeEditor; do
    grep -q "relayout = d::pack" src/planningpet/schedule/view/$f.java || { echo "D-98 어긋남: $f.java에 relayout = d::pack 없음"; exit 1; }
done
rm -rf out-check
javac -encoding UTF-8 -cp out -d out-check $(find check -name '*.java')
# 06 커버리지(#259): user-flows 06 커버리지 절의 수, 번호 목록, 번호마다 적은 흐름 단계가 06 검사 표와 흐름 단계 표의 검사 줄 칸에 맞는지.
java -cp out-check planningpet.check.CoverageCheck ../docs/06-functions.md ../docs/user-flows.md
java -Djava.awt.headless=true -cp out:out-check planningpet.check.ProtoCheck
