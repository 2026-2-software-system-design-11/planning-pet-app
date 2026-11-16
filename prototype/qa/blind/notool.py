"""도구 없는 세션의 기록을 감사하고 답을 꺼낸다. 도구가 있었거나 썼으면 실패한다.

쓰기: python3 notool.py <session.jsonl> <답을 쓸 파일>
"""
import json
import sys

bad, text = [], []
for line in open(sys.argv[1], encoding="utf-8"):
    try:
        e = json.loads(line)
    except ValueError:
        continue
    if e.get("type") == "system" and e.get("subtype") == "init" and (e.get("tools") or e.get("mcp_servers")):
        bad.append(f"도구 {e.get('tools')}, MCP {e.get('mcp_servers')}")
    if e.get("type") == "assistant":
        bad += [c["name"] for c in e["message"]["content"] if c.get("type") == "tool_use"]
    if e.get("type") == "result":
        text.append(e.get("result", ""))
open(sys.argv[2], "w", encoding="utf-8").write("\n".join(text))
print("감사", "실패: " + ", ".join(map(str, bad)) if bad else "통과")
sys.exit(1 if bad or not text else 0)
