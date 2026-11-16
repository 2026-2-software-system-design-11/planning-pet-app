#!/bin/sh
# 페르소나 한 명의 첫인상 답(단계 a)을 부품 목록과 맞춘다. 맞추는 세션은 도구가 없고 두 글만 본다.
# 쓰기: qa/blind/notice.sh <페르소나 번호>   (prototype 폴더에서, blind.sh 다음에)
set -e
n=$1
OUT="out-blind/p$n-a"
python3 - out-blind/key.md "$OUT/answer.md" qa/blind/notice.md > "$OUT/notice-prompt.md" <<'PY'
import sys
key = open(sys.argv[1], encoding="utf-8").read().split("\n", 3)[3]
answer = open(sys.argv[2], encoding="utf-8").read()
print(open(sys.argv[3], encoding="utf-8").read().replace("{{KEY}}", key).replace("{{ANSWER}}", answer))
PY
WORK=$(mktemp -d /tmp/blind-notice.XXXX)
(cd "$WORK" && claude -p "$(cat "$OLDPWD/$OUT/notice-prompt.md")" --tools "" --permission-mode default \
    --strict-mcp-config --disable-slash-commands --no-session-persistence \
    --output-format stream-json --verbose < /dev/null) > "$OUT/notice-session.jsonl"
rm -rf "$WORK"
python3 qa/blind/notool.py "$OUT/notice-session.jsonl" "$OUT/notice.md"
