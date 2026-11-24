"""예시에서 잘라 낸 1단계 펫에 2, 3단계 소품을 덧그린다(D-20: 1단계 목걸이나 목수건, 2단계 민트 스카프, 3단계 스카프와 왕관).
제품 cats.png의 소품 규칙을 따랐다. pet-stages.py 잘라낸폴더 저장폴더"""
import sys
from PIL import Image, ImageDraw
src, out = sys.argv[1], sys.argv[2]
K = 4
LINE = (52, 44, 44, 255)
MINT, MINT_SH, CREAM = (158, 217, 192, 255), (118, 196, 166, 255), (255, 243, 223, 255)
GOLD, GOLD_SH, GEM = (245, 197, 66, 255), (222, 160, 40, 255), (240, 128, 60, 255)
PAD = 16  # 왕관이 들어갈 위 여백


def up(im):
    c = Image.new('RGBA', (im.width, im.height + PAD), (0, 0, 0, 0))
    c.paste(im, (0, PAD))
    return c.resize((c.width * K, c.height * K), Image.LANCZOS)


def down(im):
    return im.resize((im.width // K, im.height // K), Image.LANCZOS)


def P(pts):
    return [(x * K, (y + PAD) * K) for x, y in pts]


def scarf(im, left, right, mid_y, tip, knot):
    d = ImageDraw.Draw(im)
    lx, ly = left
    rx, ry = right
    # 목선을 따라 내려왔다 올라가는 윗변, 아래로 처진 삼각
    top = [(lx + (rx - lx) * t / 10, ly + (ry - ly) * t / 10 + (mid_y - (ly + ry) / 2) * (1 - (2 * t / 10 - 1) ** 2)) for t in range(11)]
    poly = top + [tip]
    d.polygon(P(poly), fill=MINT)
    lower = [(x, y) for x, y in top]
    shade = [(lower[i][0], lower[i][1] + (tip[1] - lower[i][1]) * 0.55) for i in range(len(lower))]
    d.polygon(P(shade + [tip]), fill=MINT_SH)
    d.line(P([(x, y + 2.2) for x, y in top]), fill=CREAM, width=int(1.6 * K))
    d.line(P(poly + [poly[0]]), fill=LINE, width=int(1.4 * K), joint='curve')
    kx, ky = knot
    for dx, dy in ((5, 4), (2, 8)):
        d.polygon(P([(kx, ky), (kx + dx + 3, ky + dy - 2), (kx + dx, ky + dy + 3)]), fill=MINT, outline=LINE, width=int(1.4 * K))
    r = 3.6
    d.ellipse(P([(kx - r, ky - r), (kx + r, ky + r)]), fill=MINT, outline=LINE, width=int(1.5 * K))


def crown(im, cx, cy, w, tilt):
    layer = Image.new('RGBA', im.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)
    h = w * 0.62
    x0, y0 = cx - w / 2, cy - h / 2
    pts = [(x0, y0 + h), (x0, y0 + h * 0.25), (x0 + w * 0.27, y0 + h * 0.55), (x0 + w * 0.5, y0), (x0 + w * 0.73, y0 + h * 0.55), (x0 + w, y0 + h * 0.25), (x0 + w, y0 + h)]
    d.polygon(P(pts), fill=GOLD)
    d.polygon(P([(x0, y0 + h * 0.78), (x0 + w, y0 + h * 0.78), (x0 + w, y0 + h), (x0, y0 + h)]), fill=GOLD_SH)
    d.line(P(pts + [pts[0]]), fill=LINE, width=int(1.5 * K), joint='curve')
    for bx, by in ((x0, y0 + h * 0.25), (x0 + w * 0.5, y0), (x0 + w, y0 + h * 0.25)):
        r = w * 0.09
        d.ellipse(P([(bx - r, by - r), (bx + r, by + r)]), fill=GOLD, outline=LINE, width=int(1.2 * K))
    r = w * 0.1
    d.ellipse(P([(cx - r, y0 + h * 0.62 - r), (cx + r, y0 + h * 0.62 + r)]), fill=GEM, outline=LINE, width=int(1.0 * K))
    pivot = (cx * K, (cy + PAD) * K)
    layer = layer.rotate(tilt, resample=Image.BICUBIC, center=pivot)
    im.alpha_composite(layer)


def recolor_red_to_mint(im):
    px = im.load()
    for y in range(im.height):
        for x in range(im.width):
            r, g, b, a = px[x, y]
            if a > 0 and r > g + 60 and r > b + 60 and 48 <= x <= 128 and 70 <= y <= 104:
                f = min(1.15, r / 232)
                px[x, y] = tuple(min(255, int(c * f)) for c in MINT[:3]) + (a,)


cat = Image.open(f'{src}/cat-1.png').convert('RGBA')
dog = Image.open(f'{src}/dog-1.png').convert('RGBA')
c1 = Image.new('RGBA', (cat.width, cat.height + PAD)); c1.paste(cat, (0, PAD)); c1.save(f'{out}/cat-1.png')
c2 = up(cat)
scarf(c2, (50, 89), (99, 88), 96, (75, 119), (95, 92))
c2s = down(c2); c2s.save(f'{out}/cat-2.png')
c3 = c2.copy()
crown(c3, 84, 22, 24, -14)
down(c3).save(f'{out}/cat-3.png')
d1 = Image.new('RGBA', (dog.width, dog.height + PAD)); d1.paste(dog, (0, PAD)); d1.save(f'{out}/dog-1.png')
d2 = dog.copy(); recolor_red_to_mint(d2)
d2p = Image.new('RGBA', (dog.width, dog.height + PAD)); d2p.paste(d2, (0, PAD)); d2p.save(f'{out}/dog-2.png')
d3 = up(d2)
crown(d3, 108, 8, 26, -12)
down(d3).save(f'{out}/dog-3.png')
