"""걸음 단위 점검 지시문에 그 페르소나의 흐름과 단계를 넣는다.

쓰기: python3 walkprompt.py <persona.py로 만든 지시문> <steps.tsv> <단계 그림 폴더> <페르소나 번호>
"""
import os
import sys

prompt, steps, shots, n = sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4]
lines, flow = [], None
for row in open(steps, encoding="utf-8"):
    f, no, what = row.rstrip("\n").split("\t")
    if not f.startswith(f"UF-{n}-"):
        continue
    if f != flow:
        lines.append(f"\n### {f}\n")
        flow = f
    pic = f"{f}-{no}.png"
    seen = pic if os.path.exists(os.path.join(shots, pic)) else "그림 없음"
    lines.append(f"- 단계 {no} ({seen}): {what}")
print(open(prompt, encoding="utf-8").read().replace("{{FLOWS}}", "\n".join(lines).strip()))
