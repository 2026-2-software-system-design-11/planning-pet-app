"""판정 결과를 실제 동작 줄마다 모은다.

쓰기: python3 tally.py <out-blind> <결과.md> [단계 이름, 기본 b]
줄마다 같음, 다름, 판정 못 함, 예측 없음을 세고, 다름이 많은 줄부터 그 예측 문장을 그대로 붙인다.
"""
import collections
import re
import sys

base, out = sys.argv[1], sys.argv[2]
phase = sys.argv[3] if len(sys.argv) > 3 else "b"
rows = {}
count = collections.defaultdict(collections.Counter)
diff = collections.defaultdict(list)
for n in range(1, 11):
    pairs = {}
    for line in open(f"{base}/p{n}-{phase}/pairs.tsv", encoding="utf-8"):
        i, screen, no, op, pred, real = line.rstrip("\n").split("\t")
        rows[i] = (screen, int(no), op, real)
        pairs[i] = pred
        if not pred:
            count[i]["예측 없음"] += 1
    seen = set()
    for line in open(f"{base}/p{n}-{phase}/judge.md", encoding="utf-8"):
        c = [x.strip() for x in line.strip().strip("|").split("|")]
        if len(c) >= 2 and re.match(r"\d+$", c[0]) and c[0] in pairs and pairs[c[0]] and c[0] not in seen:
            seen.add(c[0])
            verdict = "같음" if c[1].startswith("같") else "다름" if c[1].startswith("다") else "판정 못 함"
            count[c[0]][verdict] += 1
            if verdict == "다름":
                diff[c[0]].append(f"p{n}: {pairs[c[0]]}")
    for i, p in pairs.items():
        if p and i not in seen:
            count[i]["판정 빠짐"] += 1
order = sorted(rows, key=lambda i: (-count[i]["다름"], -count[i]["예측 없음"], int(i)))
total = collections.Counter()
for i in rows:
    total.update(count[i])
w = open(out, "w", encoding="utf-8")
w.write("# 블라인드 검사: 예측과 실제 판정\n\n")
w.write("페르소나 10명의 단계 b 예측을 실제 동작 표(actual.md)와 줄마다 짝지어, 두 문장만 보는 판정 세션이 정했다. ")
w.write("\"예측 없음\"은 그 사람이 그 조작을 해 볼 생각을 하지 않았다는 뜻이다.\n\n")
w.write("모두: " + ", ".join(f"{k} {v}" for k, v in sorted(total.items())) + "\n\n")
w.write("| 그림 | 번호 | 조작 | 실제 | 같음 | 다름 | 판정 못 함 | 예측 없음 |\n|---|---|---|---|---|---|---|---|\n")
for i in order:
    s, no, op, real = rows[i]
    c = count[i]
    w.write(f"| {s} | {no} | {op} | {real} | {c['같음']} | {c['다름']} | {c['판정 못 함']} | {c['예측 없음']} |\n")
w.write("\n## 다름으로 판정된 예측 원문\n\n다름이 많은 줄부터. 줄마다 실제와 그 줄의 예측을 그대로 옮겼다.\n")
for i in order:
    if not diff[i]:
        continue
    s, no, op, real = rows[i]
    w.write(f"\n### {s} {no}번 {op} (다름 {count[i]['다름']})\n\n- 실제: {real}\n")
    for d in diff[i]:
        w.write(f"- {d}\n")
print(dict(total))
