#!/bin/sh
# 빌드하고 프로토타입 창을 연다.
set -e
cd "$(dirname "$0")"
./build.sh
java -jar planningpet-prototype.jar "$@"
