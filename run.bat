@echo off
chcp 65001 >nul
cd /d "%~dp0"
if not exist build\classes mkdir build\classes
(for /r src\main\java %%f in (*.java) do @echo "%%f") > build\sources.txt
javac --release 17 -encoding UTF-8 -d build\classes @build\sources.txt
if errorlevel 1 exit /b 1
java -Dfile.encoding=UTF-8 -cp build\classes cars.Main
