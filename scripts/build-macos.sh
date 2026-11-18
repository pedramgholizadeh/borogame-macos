#!/usr/bin/env bash
# build-macos.sh — build Borogame.app for Apple Silicon (arm64).
#
# Requires the assembled source tree from scripts/prepare-source.sh and Qt 6.
# Mirrors the official upstream macOS build (setup-deps.py prebuilt libraries,
# qmake, make) but pins the architecture to arm64 only.
#
# Environment:
#   BORO_CONFIG   Debug|Release   (default: Release)
set -euo pipefail

fail() { echo "build-macos: ERROR: $*" 1>&2; exit 1; }

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SRC="$ROOT/build/upstream"
BUILD_CONFIG="${BORO_CONFIG:-Release}"
BUILD_FOLDER="$ROOT/build/build-$BUILD_CONFIG"

[ -d "$SRC" ] || fail "source tree missing. Run scripts/prepare-source.sh first."
command -v qmake >/dev/null || fail "qmake not found. Install Qt 6 (see docs/build-macos.md)."

echo "==> Fetching prebuilt macOS dependencies (upstream setup-deps.py)"
if [ ! -d "$SRC/libs/mac" ]; then
    (cd "$SRC" && python3 setup-deps.py) || fail "setup-deps.py failed"
else
    echo "    libs/mac already present, skipping"
fi

echo "==> Configuring ($BUILD_CONFIG, arm64)"
rm -rf "$BUILD_FOLDER"
mkdir -p "$BUILD_FOLDER"
(
    cd "$BUILD_FOLDER"
    qmake "$SRC/moonlight-qt.pro" \
        QMAKE_APPLE_DEVICE_ARCHS="arm64" \
        CONFIG+="disable-discord-rpc" \
        "CONFIG+=$BUILD_CONFIG" \
        || fail "qmake failed"
)

echo "==> Compiling ($(sysctl -n hw.logicalcpu) cores)"
make -C "$BUILD_FOLDER" -j"$(sysctl -n hw.logicalcpu)" \
    "$(echo "$BUILD_CONFIG" | tr '[:upper:]' '[:lower:]')" || fail "make failed"

APP="$BUILD_FOLDER/app/Borogame.app"
[ -d "$APP" ] || fail "Borogame.app not found at $APP"

echo "==> Build OK: $APP"
echo "    Next: scripts/make-dmg.sh"
