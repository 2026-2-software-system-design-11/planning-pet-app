#!/bin/sh
# 사용성 점검 2차(#79): 다른 상태 화면 예측(a2, b2), 걸음 단위 점검, 10원칙 평가. 세션은 다섯 개씩 같이 돈다.
# 쓰기: ./usability.sh   결과: out-blind/p<번호>-a2, -b2, -walk, out-blind/heur<번호>
set -e
cd "$(dirname "$0")"
./build.sh
rm -rf out-qa
javac -encoding UTF-8 -cp out -d out-qa $(find qa -name '*.java')
mkdir -p out-blind
IMG=$(mktemp -d /tmp/usability-img.XXXX)
STEPS=$(mktemp -d /tmp/usability-steps.XXXX)
java -Djava.awt.headless=true -cp out:out-qa:. planningpet.qa.BlindShots "$IMG" out-blind/key.md
java -Djava.awt.headless=true -cp out:out-qa:. planningpet.qa.BlindStates "$IMG" out-blind/key2.md
java -Djava.awt.headless=true -cp out:out-qa:. planningpet.qa.QaRun "$STEPS/report.md" "$STEPS/fail" "$STEPS"
for k in 05 06 07 08 09 10 11 13 14 15 16 17 19; do cp "$IMG/screen-$k.png" "$STEPS/dialog-$k.png"; done
cp "$STEPS/steps.tsv" out-blind/steps.tsv
JOBS=out-blind/jobs.txt
: > "$JOBS"
for n in 1 2 3 4 5 6 7 8 9 10; do
    mkdir -p out-blind/p$n-a2 out-blind/p$n-b2 out-blind/p$n-walk
    python3 qa/blind/persona.py ../docs/personas.md $n qa/blind/predict-a2.md > out-blind/p$n-a2/prompt.md
    python3 qa/blind/persona.py ../docs/personas.md $n qa/blind/predict2.md > out-blind/p$n-b2/prompt.md
    python3 qa/blind/persona.py ../docs/personas.md $n qa/blind/walk.md > out-blind/p$n-walk/persona.md
    python3 qa/blind/walkprompt.py out-blind/p$n-walk/persona.md "$STEPS/steps.tsv" "$STEPS" $n > out-blind/p$n-walk/prompt.md
    echo "qa/blind/session.sh out-blind/p$n-a2 out-blind/p$n-a2/prompt.md $IMG/screen-[0-9][0-9].png" >> "$JOBS"
    echo "qa/blind/session.sh out-blind/p$n-b2 out-blind/p$n-b2/prompt.md $IMG/*.png" >> "$JOBS"
    echo "qa/blind/session.sh out-blind/p$n-walk out-blind/p$n-walk/prompt.md $STEPS/UF-$n-*.png $STEPS/dialog-*.png" >> "$JOBS"
done
for h in 1 2 3 4 5; do
    mkdir -p out-blind/heur$h
    cp qa/blind/heuristic.md out-blind/heur$h/prompt.md
    echo "qa/blind/session.sh out-blind/heur$h out-blind/heur$h/prompt.md $IMG/screen-[0-9][0-9].png" >> "$JOBS"
done
xargs -P 5 -I{} sh -c '{}' < "$JOBS"
rm -rf "$IMG" "$STEPS"
