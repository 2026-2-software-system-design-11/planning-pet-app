@echo off
rem 빌드하고 프로토타입 창을 연다. JDK 21이 필요하다.
cd /d "%~dp0"
if exist out rmdir /s /q out
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -d out @sources.txt || exit /b 1
del sources.txt
jar --create --file planningpet-prototype.jar --main-class planningpet.app.Main -C out . assets
java -jar planningpet-prototype.jar %*
