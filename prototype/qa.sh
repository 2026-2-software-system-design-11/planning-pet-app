#!/bin/sh
# 유저플로우를 창 없이 돌리고 docs/qa/에 보고서와 틀린 단계 화면을 만든다.
set -e
cd "$(dirname "$0")"
./build.sh
rm -rf out-qa ../docs/qa/shots
javac -encoding UTF-8 -cp out -d out-qa $(find qa -name '*.java')
java -Djava.awt.headless=true -cp out:out-qa:. planningpet.proto.QaRun ../docs/qa/prototype-flows.md ../docs/qa/shots
