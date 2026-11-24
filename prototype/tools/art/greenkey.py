"""초록 덤불을 색으로 따낸다. 초록(G-B 큰 픽셀)을 앞으로, 앞에 둘러싸인 구멍(꽃)도 앞으로 둔다. greenkey.py 원본 저장 x0,y0,x1,y1"""
import sys
from collections import deque
from PIL import Image, ImageFilter
src, out, box = sys.argv[1], sys.argv[2], sys.argv[3]
im = Image.open(src).convert('RGBA').crop(tuple(int(v) for v in box.split(',')))
w, h = im.size; px = im.load()
fg = [[(px[x, y][1] - px[x, y][2]) > 18 for x in range(w)] for y in range(h)]
seen = [[False]*w for _ in range(h)]
q = deque([(x, 0) for x in range(w)] + [(w-1, y) for y in range(h)])
while q:
    x, y = q.popleft()
    if seen[y][x] or fg[y][x]: continue
    seen[y][x] = True
    for nx, ny in ((x+1, y), (x-1, y), (x, y+1), (x, y-1)):
        if 0 <= nx < w and 0 <= ny < h: q.append((nx, ny))
mask = Image.new('L', (w, h)); mp = mask.load()
for y in range(h):
    for x in range(w):
        mp[x, y] = 0 if seen[y][x] else 255
mask = mask.filter(ImageFilter.GaussianBlur(1.0))
im.putalpha(mask); im.save(out); print(out, im.size)
