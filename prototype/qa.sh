#!/bin/sh
# 유저플로우를 창 없이 돌리고 docs/qa/에 보고서와 틀린 단계 화면을 만든다.
set -e
cd "$(dirname "$0")"
./build.sh
rm -rf out-qa ../docs/qa/shots
javac -encoding UTF-8 -cp out -d out-qa $(find qa -name '*.java')
java -Djava.awt.headless=true -cp out:out-qa:. planningpet.proto.QaRun ../docs/qa/prototype-flows.md ../docs/qa/shots
# 사용성 자동 규칙(입력 보존, Enter와 옆 버튼, 용어, 버튼 자리). 어긋나도 실패로 끝내지 않고 보고서에 적는다.
java -Djava.awt.headless=true -cp out:out-qa:. planningpet.proto.QaRules ../docs/qa/rules.md qa/terms.tsv
