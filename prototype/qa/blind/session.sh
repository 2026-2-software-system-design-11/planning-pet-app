#!/bin/sh
# 명세를 모르는 세션 하나를 연다. 주어진 파일만 빈 임시 폴더에 담고 도구는 Read뿐이다. 감사를 통과하면 answer.md를 남긴다.
# 쓰기: qa/blind/session.sh <결과 폴더> <지시문 파일> <넣을 파일...>   (prototype 폴더에서)
OUT=$1
PROMPT=$2
shift 2
WORK=$(mktemp -d /tmp/blind-run.XXXX)
cp "$@" "$WORK"/
mkdir -p "$OUT"
(cd "$WORK" && claude -p "$(cat "$OLDPWD/$PROMPT")" --tools Read --permission-mode default \
    --strict-mcp-config --disable-slash-commands --no-session-persistence \
    --output-format stream-json --verbose < /dev/null) > "$OUT/session.jsonl"
if python3 qa/blind/audit.py "$OUT/session.jsonl" "$WORK" "$OUT/answer.md"; then
    echo "$OUT: 감사 통과"
else
    echo "$OUT: 감사 실패, 이 답은 쓰지 않는다"
    mv "$OUT/answer.md" "$OUT/answer.rejected.md"
fi
rm -rf "$WORK"
