"""personas.md에서 한 사람의 사람, 원하는 것, 떠나는 이유, 시점만 꺼내 예측 지시문에 넣는다.
상태와 흐름 줄은 앱의 기능 이름이 들어 있어 넣지 않는다.

쓰기: python3 persona.py <personas.md> <번호> <predict.md>
"""
import re
import sys

src, no, template = sys.argv[1], int(sys.argv[2]), sys.argv[3]
mark = "①②③④⑤⑥⑦⑧⑨⑩"[no - 1]
text = open(src, encoding="utf-8").read()
part = re.search(r"^## " + mark + r" (.+?)$(.*?)(?=^## |\Z)", text, re.M | re.S)
name = re.sub(r"\s*\(.*?\)", "", part.group(1))
keep = [l for l in part.group(2).splitlines()
        if re.match(r"- \*\*(사람|원하는 것|떠나는 이유|시점)\*\*", l)]
who = "## 나는 누구인가\n\n" + name + "\n\n" + "\n".join(keep)
print(open(template, encoding="utf-8").read().replace("{{PERSONA}}", who))
