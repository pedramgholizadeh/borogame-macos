#!/usr/bin/env bash
# generate-icns.sh — build Borogame.icns from overlay/app/res/borogame.svg
# using only macOS built-in tools (qlmanage SVG rasterization + iconutil).
set -euo pipefail

fail() { echo "generate-icns: ERROR: $*" 1>&2; exit 1; }

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SRC="$ROOT/overlay/app/res/borogame.svg"
OUT="$ROOT/overlay/app/borogame.icns"

[ -f "$SRC" ] || fail "missing $SRC"

TMP="$(mktemp -d "${TMPDIR:-/tmp}/borogame-icns.XXXXXX")"
trap 'rm -rf "$TMP"' EXIT

echo "==> Rasterizing SVG to PNG set"
qlmanage -t -s 1024 -o "$TMP" "$SRC" >/dev/null 2>&1 \
    || fail "qlmanage failed to rasterize SVG"
PNG="$TMP/borogame.svg.png"
[ -f "$PNG" ] || fail "rasterized PNG not produced"

for size in 16 32 64 128 256 512; do
    half=$((size / 2))
    sips -z "$size" "$size" "$PNG" --out "$TMP/icon_${size}x${size}.png" >/dev/null
    if [ "$half" -ge 16 ]; then
        cp "$TMP/icon_${size}x${size}.png" "$TMP/icon_${half}x${half}@2x.png" 2>/dev/null || true
    fi
done
cp "$PNG" "$TMP/icon_512x512@2x.png"

mkdir "$TMP/Borogame.iconset"
for f in 16 32 64 128 256 512; do
    cp "$TMP/icon_${f}x${f}.png" "$TMP/Borogame.iconset/" 2>/dev/null || true
done
cp "$TMP/icon_512x512@2x.png" "$TMP/Borogame.iconset/" 2>/dev/null || true

echo "==> Generating icns"
iconutil -c icns "$TMP/Borogame.iconset" -o "$OUT" || fail "iconutil failed"
echo "==> Wrote $OUT"
