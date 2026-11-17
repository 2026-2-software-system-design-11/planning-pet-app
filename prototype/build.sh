#!/bin/sh
# 프로토타입을 컴파일하고 실행 JAR를 만든다. JDK 21이 필요하다.
set -e
cd "$(dirname "$0")"
rm -rf out
javac -encoding UTF-8 -d out $(find src -name '*.java')
jar --create --file planningpet-prototype.jar --main-class planningpet.app.Main -C out . assets
echo "만들었다: prototype/planningpet-prototype.jar"
