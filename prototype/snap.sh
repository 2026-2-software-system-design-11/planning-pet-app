#!/bin/sh
# 창을 띄우지 않고 모든 화면을 screenshots/에 PNG로 그린다.
set -e
cd "$(dirname "$0")"
./build.sh
rm -rf out-qa
javac -encoding UTF-8 -cp out -d out-qa $(find qa -name '*.java')
java -Djava.awt.headless=true -cp out:out-qa:. planningpet.qa.Snap screenshots
