"""예측 세션 기록을 감사한다. 허용한 폴더 밖을 읽었거나 읽으려 했으면 실패한다.

쓰기: python3 audit.py <session.jsonl> <허용 폴더> <답을 쓸 파일>
"""
import json
import os
import sys

log, allowed, answer = sys.argv[1], os.path.realpath(sys.argv[2]), sys.argv[3]
problems, text = [], []
for line in open(log, encoding="utf-8"):
    try:
        e = json.loads(line)
    except ValueError:
        continue
    if e.get("type") == "system" and e.get("subtype") == "init":
        if e.get("tools") != ["Read"] or e.get("mcp_servers"):
            problems.append(f"도구 {e.get('tools')}, MCP {e.get('mcp_servers')}")
    if e.get("type") == "assistant":
        for c in e["message"]["content"]:
            if c.get("type") == "tool_use":
                path = os.path.realpath(c["input"].get("file_path", ""))
                if c["name"] != "Read" or not path.startswith(allowed + os.sep):
                    problems.append(f"{c['name']} {c['input']}")
    if e.get("type") == "result":
        text.append(e.get("result", ""))
with open(answer, "w", encoding="utf-8") as f:
    f.write("\n".join(text))
for p in problems:
    print("감사 실패:", p)
sys.exit(1 if problems or not text else 0)
