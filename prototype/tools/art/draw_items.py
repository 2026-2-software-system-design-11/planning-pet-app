"""생성 할당량이 막혀 예시에 없는 아이템(러그, 책상, 소파)을 손으로 그린다. 예시 가구와 같은 규칙:
갈색 외곽선 #804C22(생성 가구의 외곽선에서 잰 값), 꿀색 원목 #E9A35A(그림자 #C97F3E, 밝은 면 #F6C688), 크림 #FFF3DF(그림자 #F3DCC0), 복숭아 발자국 #F2A27C,
면마다 아래쪽에 그림자 한 단, 왼쪽 위 작은 흰 하이라이트. 4배로 그려 줄인다. draw-items.py 저장폴더"""
import sys
from PIL import Image, ImageDraw, ImageChops
OUT = (128, 76, 34, 255)
WOOD, WOOD_SH, WOOD_HI = (233, 163, 90, 255), (201, 127, 62, 255), (246, 198, 136, 255)
CREAM, CREAM_SH = (255, 243, 223, 255), (243, 220, 192, 255)
PEACH = (242, 162, 124, 255)
S = 4
LW = 6 * S


def canvas():
    return Image.new('RGBA', (512 * S, 512 * S), (0, 0, 0, 0))


def part(img, kind, box, fill, shade, radius=0, shade_from=0.62, hi=True):
    """한 면: 채우고, 아래쪽을 그림자 색으로, 왼쪽 위에 하이라이트, 외곽선."""
    box = [v * S for v in box]
    mask = Image.new('L', img.size, 0)
    md = ImageDraw.Draw(mask)
    if kind == 'rect':
        md.rounded_rectangle(box, radius * S, fill=255)
    else:
        md.ellipse(box, fill=255)
    layer = Image.new('RGBA', img.size, fill)
    img.paste(layer, (0, 0), mask)
    band = Image.new('L', img.size, 0)
    y0 = box[1] + (box[3] - box[1]) * shade_from
    soft = (box[3] - box[1]) * 0.12
    bd = ImageDraw.Draw(band)
    for i in range(int(soft)):
        bd.line((0, y0 - soft + i, img.width, y0 - soft + i), fill=int(255 * i / soft))
    bd.rectangle((0, y0, img.width, img.height), fill=255)
    img.paste(Image.new('RGBA', img.size, shade), (0, 0), ImageChops.multiply(mask, band))
    d = ImageDraw.Draw(img)
    if hi and kind == 'rect':
        w, h = box[2] - box[0], box[3] - box[1]
        inset = max(radius * S * 0.55, 10 * S)
        hx, hy = box[0] + inset, box[1] + max(radius * S * 0.35, 7 * S)
        d.rounded_rectangle((hx, hy, hx + max(w * 0.14, 8 * S), hy + max(min(h * 0.08, 6 * S), 3 * S)), 3 * S, fill=(255, 255, 255, 170))
    if kind == 'rect':
        d.rounded_rectangle(box, radius * S, outline=OUT, width=LW)
    else:
        d.ellipse(box, outline=OUT, width=LW)


def paw(img, cx, cy, size, squash=1.0, color=PEACH):
    d = ImageDraw.Draw(img)
    cx, cy, size = cx * S, cy * S, size * S
    pw, ph = size * 0.62, size * 0.5 * squash
    d.ellipse((cx - pw / 2, cy - ph * 0.1, cx + pw / 2, cy + ph * 0.9), fill=color)
    for dx, dy in ((-0.42, -0.30), (-0.15, -0.55), (0.15, -0.55), (0.42, -0.30)):
        tx, ty = cx + dx * size, cy + dy * size * squash
        r, rh = size * 0.13, size * 0.16 * squash
        d.ellipse((tx - r, ty - rh, tx + r, ty + rh), fill=color)


def save(img, path):
    img.resize((512, 512), Image.LANCZOS).save(path)
    print(path)


def rug(path):
    img = canvas()
    part(img, 'ellipse', (30, 196, 482, 368), CREAM_SH, CREAM_SH, hi=False)
    part(img, 'ellipse', (30, 180, 482, 350), (251, 227, 196, 255), (247, 214, 178, 255), shade_from=0.7)
    d = ImageDraw.Draw(img)
    d.ellipse([v * S for v in (78, 204, 434, 326)], outline=(232, 185, 142, 255), width=4 * S)
    paw(img, 256, 262, 104, squash=0.5)
    save(img, path)


def desk(path):
    img = canvas()
    # 뒤 다리(조금 어둡고 짧게)
    for x in (112, 376):
        part(img, 'rect', (x, 250, x + 24, 420), WOOD_SH, WOOD_SH, radius=8, hi=False)
    # 앞 다리
    for x in (70, 418):
        part(img, 'rect', (x, 250, x + 28, 466), WOOD, WOOD_SH, radius=10, shade_from=1.0, hi=False)
    # 상판 아래 앞판과 서랍
    part(img, 'rect', (78, 222, 438, 290), WOOD, WOOD_SH, radius=10, shade_from=0.7)
    part(img, 'rect', (196, 234, 320, 280), WOOD_HI, WOOD, radius=8, hi=False)
    d = ImageDraw.Draw(img)
    d.ellipse([v * S for v in (250, 249, 266, 265)], fill=WOOD_SH, outline=OUT, width=3 * S)
    # 상판(윗면이 보이게 두 단)
    part(img, 'rect', (52, 168, 464, 214), WOOD_HI, WOOD_HI, radius=16, hi=False)
    part(img, 'rect', (48, 200, 468, 232), WOOD, WOOD_SH, radius=12, shade_from=0.6)
    # 노트
    part(img, 'rect', (110, 150, 230, 188), CREAM, CREAM_SH, radius=6, shade_from=0.75, hi=False)
    d = ImageDraw.Draw(img)
    for y in (162, 172):
        d.line([v * S for v in (126, y, 214, y)], fill=(232, 205, 170, 255), width=3 * S)
    # 연필꽂이와 연필
    for x, top, col in ((332, 80, (246, 211, 107, 255)), (352, 92, (244, 143, 160, 255)), (368, 70, (111, 168, 220, 255))):
        part(img, 'rect', (x, top, x + 14, 150), col, col, radius=6, hi=False)
    part(img, 'rect', (318, 118, 392, 190), (158, 217, 192, 255), (120, 196, 168, 255), radius=14)
    save(img, path)


def sofa(path):
    img = canvas()
    for x in (82, 404):
        part(img, 'rect', (x, 408, x + 28, 448), WOOD_SH, WOOD_SH, radius=8, hi=False)
    part(img, 'rect', (70, 170, 442, 330), CREAM, CREAM_SH, radius=56, shade_from=0.74)
    d = ImageDraw.Draw(img)
    d.line([v * S for v in (256, 190, 256, 300)], fill=(232, 205, 170, 255), width=4 * S)
    part(img, 'rect', (40, 352, 472, 420), WOOD, WOOD_SH, radius=24, shade_from=0.6)
    part(img, 'rect', (96, 288, 416, 372), CREAM, CREAM_SH, radius=26, shade_from=0.66)
    d = ImageDraw.Draw(img)
    d.line([v * S for v in (256, 296, 256, 366)], fill=(232, 205, 170, 255), width=4 * S)
    for x in (26, 410):
        part(img, 'rect', (x, 262, x + 76, 404), WOOD, WOOD_SH, radius=36, shade_from=0.72)
    # 발자국 쿠션
    part(img, 'rect', (128, 214, 224, 300), CREAM, CREAM_SH, radius=24, shade_from=0.78)
    paw(img, 176, 260, 44, squash=0.95)
    save(img, path)


d = sys.argv[1]
rug(d + '/rug.png')
desk(d + '/desk.png')
sofa(d + '/sofa.png')
