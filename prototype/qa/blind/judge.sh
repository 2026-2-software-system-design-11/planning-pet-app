#!/bin/sh
# 페르소나 한 명의 예측과 실제를 판정 세션에 넘긴다. 판정 세션은 도구가 없고 두 문장만 본다.
# 쓰기: qa/blind/judge.sh <페르소나 번호>   (prototype 폴더에서, blind.sh 다음에)
set -e
n=$1
OUT="out-blind/p$n-b"
python3 qa/blind/pairs.py ../docs/qa/blind/actual.md "$OUT/answer.md" "$OUT/pairs.tsv"
python3 - "$OUT/pairs.tsv" qa/blind/judge.md > "$OUT/judge-prompt.md" <<'PY'
import sys
rows = []
for line in open(sys.argv[1], encoding="utf-8"):
    i, screen, no, op, pred, real = line.rstrip("\n").split("\t")
    if pred:
        rows.append(f"- 줄 {i}\n  - 예측: {pred}\n  - 실제: {real}")
print(open(sys.argv[2], encoding="utf-8").read().replace("{{ROWS}}", "\n".join(rows)))
PY
WORK=$(mktemp -d /tmp/blind-judge.XXXX)
(cd "$WORK" && claude -p "$(cat "$OLDPWD/$OUT/judge-prompt.md")" --tools "" --permission-mode default \
    --strict-mcp-config --disable-slash-commands --no-session-persistence \
    --output-format stream-json --verbose < /dev/null) > "$OUT/judge-session.jsonl"
rm -rf "$WORK"
python3 qa/blind/notool.py "$OUT/judge-session.jsonl" "$OUT/judge.md"
