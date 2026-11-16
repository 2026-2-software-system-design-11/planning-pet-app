"""단계 b의 예측과 실제 동작 표를 (그림, 번호, 조작)으로 짝짓는다.

쓰기: python3 pairs.py <actual.md> <answer.md> <짝 파일.tsv>
짝 파일 한 줄: 줄 id, 그림, 번호, 조작, 예측(없으면 빈칸), 실제. 판정 세션에는 줄 id, 예측, 실제만 간다.
"""
import re
import sys

OPS = [("두 번", "두 번 누르기"), ("오른쪽", "오른쪽 버튼"), ("올리", "마우스 올리기"), ("가까운", "글 쓰고 가까운 버튼 누르기"),
       ("버튼 누르", "글 쓰고 가까운 버튼 누르기"), ("enter", "글 쓰고 Enter"), ("esc", "Esc"), ("누르", "누르기"), ("클릭", "누르기")]


def op_of(text):
    t = text.lower()
    for key, name in OPS:
        if key.lower() in t:
            return name
    return None


def tables(path):
    """그림 이름 제목 아래의 표 줄을 (그림, 칸들)로 돌려준다."""
    screen = None
    for line in open(path, encoding="utf-8"):
        m = re.search(r"screen-(\d\d)", line)
        if line.lstrip().startswith("#") and m:
            screen = "screen-" + m.group(1) + ".png"
            continue
        if screen and line.startswith("|") and not re.match(r"\|\s*-", line):
            cells = [c.strip() for c in line.strip().strip("|").split("|")]
            if cells and re.match(r"\d+$", cells[0]):
                yield screen, cells


actual = {}
for screen, c in tables(sys.argv[1]):
    actual[(screen, int(c[0]), c[1])] = c[2]
pred = {}
for screen, c in tables(sys.argv[2]):
    if len(c) < 6:
        continue
    op = op_of(c[2])
    if op is None:
        continue
    key = (screen, int(c[0]), op)
    pred.setdefault(key, c[3] + (" (확신 " + c[5] + ")" if c[5] else ""))
with open(sys.argv[3], "w", encoding="utf-8") as out:
    for i, (key, result) in enumerate(actual.items(), 1):
        p = pred.get(key, "").replace("\t", " ")
        out.write("\t".join([str(i), key[0], str(key[1]), key[2], p, result]) + "\n")
missing = sum(1 for k in actual if k not in pred)
print(f"짝 {len(actual)}줄, 예측 없음 {missing}줄")
