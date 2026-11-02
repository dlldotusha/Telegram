#!/bin/sh
# Тесты ядра NextEngine на обычной JVM.
# Нужны kotlinc (KOTLINC, по умолчанию из PATH) и org.json для JVM (на Android он встроен):
#   JSON_CP=/path/to/json.jar ./nextengine-tests/run.sh
set -e
ROOT=$(cd "$(dirname "$0")/.." && pwd)
KOTLINC=${KOTLINC:-kotlinc}
OUT=$(mktemp -d)
CORE="$ROOT/TMessagesProj/src/main/java/org/nextgram/engine/core"
"$KOTLINC" -nowarn -cp "$JSON_CP" -include-runtime -d "$OUT/tests.jar" "$CORE"/*.kt "$ROOT/nextengine-tests/CoreTest.kt"
java -cp "$OUT/tests.jar:$JSON_CP" CoreTestKt
