#!/bin/sh
# 06 검사 표 가운데 프로토타입이 따르는 줄을 확인한다.
set -e
cd "$(dirname "$0")"
./build.sh
# D-46: model은 Swing과 AWT를 쓰지 않는다. view는 데이터 변경 알림, 펫 말, Model 값 대입을 하지 않고 controller를 부른다.
bad=$( (grep -rnE "import (javax\.swing|java\.awt)" src/planningpet/*/model
        grep -rnE "store\.(changed|say|add|delete|applyToSeries)\(|\.(placed|owned|tasks)\.(add|remove)|\.(petKind|petName|points|exp|streak|attendanceDay|lastOpenDay|start|title|minutes|memo|done|date|rewardPoints) *= " src/planningpet/*/view) || true)
if [ -n "$bad" ]; then
    echo "D-46 어긋남:"
    echo "$bad"
    exit 1
fi
rm -rf out-check
javac -encoding UTF-8 -cp out -d out-check $(find check -name '*.java')
java -Djava.awt.headless=true -cp out:out-check planningpet.check.ProtoCheck
