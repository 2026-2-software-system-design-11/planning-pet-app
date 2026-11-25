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
fields="calendarFixed|petKind|petName|points|exp|streak|attendanceDay|lastOpenDay|start|title|minutes|memo|done|date|rewardPoints|rewardExp|rewardDay|rewardCount|rewardCountDay|talk|talkSeq|now|seriesId|kind|seq|nextSeq|dueDate|startDay|doneDay|repeat|nextDay"
bad=$( (grep -rnE "javax\.swing|java\.awt" src/planningpet/*/model
        grep -rnE "\.(changed|say)\(|book\.(add|delete|applyToSeries|update|setTime)\(|game\.(complete|uncomplete|attend)\(|\.buy\([^,)]*\)|Visits\.|\.($fields) *([-+*/]?=[^=]|\+\+|--)|\.(placed|owned|tasks)\.(add|remove|clear|removeIf|addAll)\(" src/planningpet/*/view | grep -v "this\.[a-zA-Z]* = "
        grep -rn "\.changed()" src | grep -v "src/planningpet/app/controller/AppController.java") || true)
if [ -n "$bad" ]; then
    echo "D-46 어긋남:"
    echo "$bad"
    exit 1
fi
rm -rf out-check
javac -encoding UTF-8 -cp out -d out-check $(find check -name '*.java')
java -Djava.awt.headless=true -cp out:out-check planningpet.check.ProtoCheck
