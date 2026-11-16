#!/bin/sh
# 도구 없는 분석 세션 하나를 연다. 지시문만 보고 답한다. notool.py로 감사한다.
# 쓰기: qa/blind/ask.sh <결과 폴더> <지시문 파일>   (prototype 폴더에서)
set -e
OUT=$1
mkdir -p "$OUT"
WORK=$(mktemp -d /tmp/blind-ask.XXXX)
(cd "$WORK" && claude -p "$(cat "$OLDPWD/$2")" --tools "" --permission-mode default \
    --strict-mcp-config --disable-slash-commands --no-session-persistence \
    --output-format stream-json --verbose < /dev/null) > "$OUT/session.jsonl"
rm -rf "$WORK"
python3 qa/blind/notool.py "$OUT/session.jsonl" "$OUT/answer.md"
