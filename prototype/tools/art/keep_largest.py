"""불투명 픽셀(알파 40 넘음)의 이어진 덩어리 가운데 가장 큰 것만 남긴다(강조선, 말풍선 조각 지우기). keep-largest.py 그림"""
import sys
from collections import deque
from PIL import Image
p = sys.argv[1]
im = Image.open(p).convert('RGBA'); px = im.load(); w, h = im.size
lab = [[0] * w for _ in range(h)]; sizes = {}; n = 0
for y in range(h):
    for x in range(w):
        if px[x, y][3] > 40 and not lab[y][x]:
            n += 1; q = deque([(x, y)]); lab[y][x] = n; c = 0
            while q:
                a, b = q.popleft(); c += 1
                for dx in (-1, 0, 1):
                    for dy in (-1, 0, 1):
                        u, v = a + dx, b + dy
                        if 0 <= u < w and 0 <= v < h and not lab[v][u] and px[u, v][3] > 40:
                            lab[v][u] = n; q.append((u, v))
            sizes[n] = c
big = max(sizes, key=sizes.get)
# 큰 덩어리 둘레 2픽셀 안의 반투명 가장자리는 남기고 나머지는 지운다
keep = [[False] * w for _ in range(h)]
for y in range(h):
    for x in range(w):
        if lab[y][x] == big:
            for dx in range(-2, 3):
                for dy in range(-2, 3):
                    if 0 <= x + dx < w and 0 <= y + dy < h: keep[y + dy][x + dx] = True
for y in range(h):
    for x in range(w):
        if not keep[y][x]:
            r, g, b, a = px[x, y]; px[x, y] = (r, g, b, 0)
im.save(p); print(p, 'pieces', len(sizes), 'kept', sizes[big])
