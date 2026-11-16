#!/bin/sh
# 블라인드 예측 검사(docs/qa 사용성 점검). 명세를 모르는 세션이 화면 그림만 보고 동작을 예측한다.
# 쓰기: ./blind.sh <페르소나 번호...>   예: ./blind.sh 1 2 3
# 결과: out-blind/key.md(번호 열쇠), out-blind/p<번호>-<a|b>/answer.md(예측), session.jsonl(감사용 기록)
set -e
cd "$(dirname "$0")"
./build.sh
rm -rf out-qa
javac -encoding UTF-8 -cp out -d out-qa $(find qa -name '*.java')
mkdir -p out-blind
IMAGES=$(mktemp -d /tmp/blind-images.XXXX)
java -Djava.awt.headless=true -cp out:out-qa:. planningpet.proto.BlindShots "$IMAGES" out-blind/key.md
# 페르소나 다섯 명씩 같이 돌린다.
printf '%s\n' "$@" | xargs -P 5 -I{} qa/blind/run-one.sh {} "$IMAGES"
rm -rf "$IMAGES"
