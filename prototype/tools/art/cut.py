"""예시 그림에서 한 부분을 잘라 가장자리와 이어진 배경을 투명하게 한다. cut.py 원본 저장 x0,y0,x1,y1 [허용오차=18]
가장자리 픽셀에서 시작해 이웃 픽셀과 색 차이(R, G, B 차이의 합)가 허용오차 안이면 배경으로 이어 간다. 경계는 한 픽셀 부드럽게 한다."""
import sys
from collections import deque
from PIL import Image, ImageFilter
src, out, box = sys.argv[1], sys.argv[2], sys.argv[3]
tol = int(sys.argv[4]) if len(sys.argv) > 4 else 18
im = Image.open(src).convert('RGBA').crop(tuple(int(v) for v in box.split(',')))
w, h = im.size; px = im.load(); seen = bytearray(w * h)
q = deque([(x, y) for x in range(w) for y in (0, h - 1)] + [(x, y) for y in range(h) for x in (0, w - 1)])
mask = Image.new('L', (w, h), 255); mp = mask.load()
while q:
    x, y = q.popleft()
    if seen[y * w + x]: continue
    seen[y * w + x] = 1; mp[x, y] = 0; r, g, b, a = px[x, y]
    for nx, ny in ((x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1)):
        if 0 <= nx < w and 0 <= ny < h and not seen[ny * w + nx]:
            r2, g2, b2, a2 = px[nx, ny]
            if abs(r2 - r) + abs(g2 - g) + abs(b2 - b) <= tol: q.append((nx, ny))
mask = mask.filter(ImageFilter.MinFilter(3)).filter(ImageFilter.GaussianBlur(0.8))
im.putalpha(Image.composite(im.getchannel('A'), Image.new('L', im.size, 0), mask))
bb = im.getchannel('A').getbbox()
x0, y0, x1, y1 = bb
im = im.crop((max(0, x0 - 2), max(0, y0 - 2), min(w, x1 + 2), min(h, y1 + 2)))
im.save(out); print(out, im.size)
