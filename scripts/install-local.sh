#!/usr/bin/env bash
# install-local.sh — install the freshly built Borogame.app on this Mac for
# local testing. Removes the quarantine attribute so Gatekeeper doesn't block
# unsigned/CI builds, then launches the app.
set -euo pipefail

fail() { echo "install-local: ERROR: $*" 1>&2; exit 1; }

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
APP="$(ls -d "$ROOT"/build/build-*/app/Borogame.app 2>/dev/null | head -1 || true)"
[ -n "$APP" ] || fail "Borogame.app not found — run scripts/build-macos.sh first"

echo "==> Installing $(basename "$APP") to /Applications"
rm -rf "/Applications/Borogame.app"
cp -R "$APP" /Applications/
APP="/Applications/Borogame.app"

echo "==> Clearing quarantine attributes"
xattr -dr com.apple.quarantine "$APP" 2>/dev/null || true

echo "==> Verifying code signature"
codesign --verify --deep --strict "$APP" && echo "    signature OK" \
    || echo "    WARNING: signature verification failed (expected for unsigned builds)"

echo "==> Launching Borogame"
open "$APP"
echo "==> Installed and launched. Logs: ~/Library/Logs/Borogame/ or Console.app"
