#!/bin/sh
set -eu
cd "$(dirname "$0")"
mkdir -p build/classes
find src/main/java -name '*.java' -print > build/sources.txt
javac --release 17 -encoding UTF-8 -Xlint:all -Werror -d build/classes @build/sources.txt
exec java -Dfile.encoding=UTF-8 -cp build/classes cars.Main
