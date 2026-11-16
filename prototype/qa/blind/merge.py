"""분석 세션 지시문을 만든다.

쓰기: python3 merge.py heur <out-blind> <heur-merge.md>
      python3 merge.py expect <out-blind> <expect.md> <05-ui-design.md> <expect-map.tsv>
"""
import re
import sys

kind, base, template = sys.argv[1], sys.argv[2], sys.argv[3]
text = open(template, encoding="utf-8").read()
if kind == "heur":
    lists = []
    for h in range(1, 6):
        lists.append(f"### 평가자 {h}\n\n" + open(f"{base}/heur{h}/answer.md", encoding="utf-8").read().strip())
    print(text.replace("{{LISTS}}", "\n\n".join(lists)))
    sys.exit(0)
spec_file, mapping = sys.argv[4], sys.argv[5]
spec, section = {}, None
for line in open(spec_file, encoding="utf-8"):
    if re.match(r"#{2,3} ", line):
        section = line.strip("# \n")
    m = re.match(r"\| (\d+) \| (.*?) \| (.*?) \| (.*?) \| (.*?) \|$", line.strip())
    if m and section:
        spec[(section, m.group(1))] = f"{m.group(2)}: {m.group(4)}. 오류 처리: {m.group(5)}"
preds = {}
for n in range(1, 11):
    for phase in ("b", "b2"):
        try:
            rows = open(f"{base}/p{n}-{phase}/pairs.tsv", encoding="utf-8")
        except FileNotFoundError:
            continue
        for row in rows:
            i, screen, no, op, pred, real = row.rstrip("\n").split("\t")
            if pred and op in ("누르기", "글 쓰고 Enter", "글 쓰고 가까운 버튼 누르기"):
                preds.setdefault((screen, no, op), []).append(f"p{n}: {re.sub(r' [(]확신.*', '', pred)}")
out = []
for line in open(mapping, encoding="utf-8"):
    if line.startswith("#") or not line.strip():
        continue
    section, no, parts = line.rstrip("\n").split("\t")
    out.append(f"### {section} {no}. {spec[(section, no)]}\n")
    for part in parts.split(","):
        screen, num = part.strip().split(":")
        for op in ("누르기", "글 쓰고 Enter", "글 쓰고 가까운 버튼 누르기"):
            for p in preds.get((f"screen-{screen}.png", num, op), []):
                out.append(f"- 그림 {screen} 번호 {num}, {op}: {p}")
    out.append("")
print(text.replace("{{ROWS}}", "\n".join(out)))
