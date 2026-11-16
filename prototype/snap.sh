#!/bin/sh
# 창을 띄우지 않고 모든 화면을 screenshots/에 PNG로 그린다.
set -e
cd "$(dirname "$0")"
./build.sh
java -Djava.awt.headless=true -cp planningpet-prototype.jar planningpet.proto.Snap screenshots
