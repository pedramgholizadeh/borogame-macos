#!/usr/bin/env bash
# run-api-smoke.sh — build & run the BoroApi integration test against the
# live Borogame backend. Requires Qt (qmake toolchain on PATH) and an
# assembled source tree is NOT required (test compiles overlay code alone).
#
# Usage: test/run-api-smoke.sh <license-key>
set -euo pipefail
fail() { echo "run-api-smoke: ERROR: $*" 1>&2; exit 1; }

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
KEY="${1:-}"
[ -n "$KEY" ] || fail "usage: run-api-smoke.sh <license-key>"
command -v qmake >/dev/null || fail "qmake not found — install Qt 6 and put it on PATH"
MOC="$(qmake -query QT_HOST_LIBEXECS)/moc"
[ -x "$MOC" ] || fail "moc not found at $MOC"

OUT="$(mktemp -d "${TMPDIR:-/tmp}/boro-smoke.XXXXXX")"
trap 'rm -rf "$OUT"' EXIT

"$MOC" "$ROOT/overlay/app/boro/boroapi.h" -o "$OUT/moc_boroapi.cpp"
clang++ -std=gnu++17 -fPIC \
    -I"$ROOT/overlay/app" -I"$ROOT/overlay/app/boro" \
    $(for m in Core Network Gui; do echo "-I$(qmake -query QT_INSTALL_HEADERS)/Qt$m"; done) \
    -I"$(qmake -query QT_INSTALL_HEADERS)" \
    -o "$OUT/api-smoke" \
    "$ROOT/test/api-smoke.cpp" "$ROOT/overlay/app/boro/boroapi.cpp" "$OUT/moc_boroapi.cpp" \
    -F"$(qmake -query QT_INSTALL_LIBS)" \
    -framework QtCore -framework QtNetwork -framework QtGui

"$OUT/api-smoke" "$KEY"
