"""잘라 낸 그림 다듬기. clean.py 그림 [seed:x,y,tol]... [erase:x0,y0,x1,y1]... [yellow:x0,y0,x1,y1]...
seed: 그 점에서 비슷한 색으로 이어진 픽셀을 투명하게(둘러싸인 배경 구멍). erase: 상자 안을 지움. yellow: 상자 안의 노란 픽셀(강조선)을 지움. mint: 상자 안의 민트빛 배경 픽셀(손잡이 구멍)을 지움."""
import sys
from collections import deque
from PIL import Image
p = sys.argv[1]
im = Image.open(p).convert('RGBA'); px = im.load(); w, h = im.size
for a in sys.argv[2:]:
    kind, v = a.split(':'); n = [int(t) for t in v.split(',')]
    if kind == 'seed':
        x0, y0, tol = n; q = deque([(x0, y0)]); seen = set()
        while q:
            x, y = q.popleft()
            if (x, y) in seen: continue
            seen.add((x, y)); r, g, b, al = px[x, y]
            for nx, ny in ((x+1, y), (x-1, y), (x, y+1), (x, y-1)):
                if 0 <= nx < w and 0 <= ny < h and (nx, ny) not in seen:
                    r2, g2, b2, a2 = px[nx, ny]
                    if a2 > 0 and abs(r2-r)+abs(g2-g)+abs(b2-b) <= tol: q.append((nx, ny))
        for x, y in seen: r, g, b, al = px[x, y]; px[x, y] = (r, g, b, 0)
        print('seed', n, len(seen))
    elif kind == 'erase':
        for y in range(n[1], n[3]):
            for x in range(n[0], n[2]): px[x, y] = (0, 0, 0, 0)
    elif kind == 'yellow':
        c = 0
        for y in range(n[1], n[3]):
            for x in range(n[0], n[2]):
                r, g, b, al = px[x, y]
                if al > 0 and r > 190 and g > 140 and b < 150 and r - b > 80: px[x, y] = (0, 0, 0, 0); c += 1
        print('yellow', c)
    elif kind == 'mint':
        for y in range(n[1], n[3]):
            for x in range(n[0], n[2]):
                r, g, b, al = px[x, y]
                if al > 0 and b > r + 8 and g > r + 4: px[x, y] = (r, g, b, 0)
im.save(p)
