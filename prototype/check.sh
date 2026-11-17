#!/bin/sh
# 06 검사 표 가운데 프로토타입이 따르는 줄을 확인한다.
set -e
cd "$(dirname "$0")"
./build.sh
rm -rf out-check
javac -encoding UTF-8 -cp out -d out-check $(find check -name '*.java')
java -Djava.awt.headless=true -cp out:out-check planningpet.check.ProtoCheck
