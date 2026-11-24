#!/bin/sh
# 예시에서 잘라 내거나 손으로 그린 그림을 다시 만든다(어떻게 만들었는지는 assets/PROMPTS.md).
# 사용: tools/art/make-art.sh <나의 방 예시 png> <저장 폴더>
# 생성 모델로 만든 그림(room.png, items의 cushion, plant, bookshelf, lamp, frame, hanging-plant, rug, yarn, bone)과
# 예시 카드에서 잘라 낸 임시 그림(items/bed.png, items/cat-tower.png)은 이 스크립트가 만들지 않는다.
set -e
EX=$1; OUT=$2; T=$(cd "$(dirname "$0")" && pwd)
mkdir -p "$OUT/items" "$OUT/pets" "$OUT/cut"
python3 "$T/cut.py" "$EX" "$OUT/logo-face.png" 30,68,156,152 18
python3 "$T/cut.py" "$EX" "$OUT/cloud.png" 15,665,165,745 6
python3 "$T/greenkey.py" "$EX" "$OUT/meadow-corner.png" 0,770,182,941
python3 "$T/cut.py" "$EX" "$OUT/shop-bag.png" 212,798,378,912 14
python3 "$T/clean.py" "$OUT/shop-bag.png" mint:45,0,106,38
python3 "$T/cut.py" "$EX" "$OUT/cut/cat-1.png" 1300,215,1450,362 16
python3 "$T/clean.py" "$OUT/cut/cat-1.png" seed:28,95,14
python3 "$T/keep_largest.py" "$OUT/cut/cat-1.png"
python3 "$T/cut.py" "$EX" "$OUT/cut/dog-1.png" 782,416,975,540 14
python3 "$T/keep_largest.py" "$OUT/cut/dog-1.png"
python3 "$T/pet_stages.py" "$OUT/cut" "$OUT/pets"
python3 "$T/draw_items.py" "$OUT/items"
rm -rf "$OUT/cut"
