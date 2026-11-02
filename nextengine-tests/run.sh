#!/bin/sh
# Тесты ядра кастомизации на обычной JVM.
# Нужен org.json (на Android он встроен). Передайте путь к классам или jar:
#   JSON_CP=/path/to/json.jar ./nextengine-tests/run.sh
set -e
ROOT=$(cd "$(dirname "$0")/.." && pwd)
OUT=$(mktemp -d)
CORE="$ROOT/TMessagesProj/src/main/java/org/nextgram/engine/core"
javac -d "$OUT" -cp "$JSON_CP" "$CORE"/*.java "$ROOT/nextengine-tests/CoreTest.java"
java -cp "$OUT:$JSON_CP" CoreTest
