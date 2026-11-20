#!/bin/sh
# 페르소나 한 명의 블라인드 예측. blind.sh가 부른다.
# 쓰기: qa/blind/run-one.sh <페르소나 번호> <그림 폴더>
# 단계 a(첫인상)는 번호 없는 그림만, 단계 b는 번호 그림까지 본다. 따로 연 세션이라 a의 답은 번호를 모른 채 쓴 것이다.
n=$1
IMAGES=$2
run() {
    phase=$1 template=$2 pattern=$3
    WORK=$(mktemp -d /tmp/blind-run.XXXX)
    cp $pattern "$WORK"/
    OUT="out-blind/p$n-$phase"
    mkdir -p "$OUT"
    python3 qa/blind/persona.py ../docs/ux/personas.md "$n" "$template" > "$OUT/prompt.md"
    # 빈 폴더에서 연다: 저장소 경로, 프로젝트 문맥, MCP, 스킬이 들어가지 않고 도구는 Read뿐이다.
    (cd "$WORK" && claude -p "$(cat "$OLDPWD/$OUT/prompt.md")" --tools Read --permission-mode default \
        --strict-mcp-config --disable-slash-commands --no-session-persistence \
        --output-format stream-json --verbose < /dev/null) > "$OUT/session.jsonl"
    if python3 qa/blind/audit.py "$OUT/session.jsonl" "$WORK" "$OUT/answer.md"; then
        echo "페르소나 $n 단계 $phase: 감사 통과"
    else
        echo "페르소나 $n 단계 $phase: 감사 실패, 이 예측은 쓰지 않는다"
        mv "$OUT/answer.md" "$OUT/answer.rejected.md"
    fi
    rm -rf "$WORK"
}
run a qa/blind/predict-a.md "$IMAGES/screen-[0-9][0-9].png"
run b qa/blind/predict.md "$IMAGES/*.png"
