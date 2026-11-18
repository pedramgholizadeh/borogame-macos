#!/usr/bin/env bash
# make-dmg.sh — package a built Borogame.app into a distributable .dmg.
#
# Handles macdeployqt, dSYM extraction, optional code signing (Developer ID)
# and optional notarization. Mirrors upstream moonlight-qt's
# scripts/generate-dmg.sh, adapted for the Borogame port (arm64 only).
#
# Environment:
#   BORO_CONFIG             Debug|Release            (default: Release)
#   SIGNING_IDENTITY        codesign identity        (optional; enables signing)
#   NOTARY_KEYCHAIN_PROFILE keychain profile for notarytool (optional)
#   CI_VERSION              version string for artifact naming (optional)
set -euo pipefail

fail() { echo "make-dmg: ERROR: $*" 1>&2; exit 1; }

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BUILD_CONFIG="${BORO_CONFIG:-Release}"
BUILD_FOLDER="$ROOT/build/build-$BUILD_CONFIG"
INSTALLER_FOLDER="$ROOT/build/dist"
APP="$BUILD_FOLDER/app/Borogame.app"

[ -d "$APP" ] || fail "Borogame.app missing — run scripts/build-macos.sh first"

VERSION="${CI_VERSION:-$(cat "$ROOT/build/upstream/app/version.txt" 2>/dev/null || echo "0.0.0")}"
rm -rf "$INSTALLER_FOLDER"
mkdir -p "$INSTALLER_FOLDER"

echo "==> Saving dSYM"
dsymutil "$APP/Contents/MacOS/Borogame" \
    -o "$INSTALLER_FOLDER/Borogame-$VERSION.dsym" || echo "    dSYM creation failed (non-fatal)"

echo "==> Deploying Qt runtime into app bundle"
macdeployqt "$APP" -qmldir="$ROOT/build/upstream/app/gui" -appstore-compliant || fail "macdeployqt failed"
find "$APP" -name '*.dSYM' -exec rm -rf {} + 2>/dev/null || true

if [ -n "${SIGNING_IDENTITY:-}" ]; then
    echo "==> Signing with identity: $SIGNING_IDENTITY"
    codesign --force --deep --options runtime --timestamp \
        --sign "$SIGNING_IDENTITY" "$APP" || fail "Signing failed"
else
    echo "==> No SIGNING_IDENTITY set — leaving app ad-hoc signed (local test only)"
fi

echo "==> Creating DMG"
if command -v create-dmg >/dev/null; then
    if [ -n "${SIGNING_IDENTITY:-}" ]; then
        create-dmg "$APP" "$INSTALLER_FOLDER" \
            --identity="$SIGNING_IDENTITY" --no-version-in-filename \
            || fail "create-dmg failed"
    else
        create-dmg "$APP" "$INSTALLER_FOLDER" --no-version-in-filename || true
    fi
else
    echo "    create-dmg not found, falling back to hdiutil"
    hdiutil create -volname "Borogame" -srcfolder "$APP" -ov -format UDZO \
        "$INSTALLER_FOLDER/Borogame.dmg" || fail "hdiutil failed"
fi

if [ -n "${NOTARY_KEYCHAIN_PROFILE:-}" ]; then
    echo "==> Submitting to App Notary service"
    xcrun notarytool submit --keychain-profile "$NOTARY_KEYCHAIN_PROFILE" \
        --wait "$INSTALLER_FOLDER/Borogame.dmg" || fail "Notary submission failed"
    echo "==> Stapling notary ticket"
    xcrun stapler staple -v "$INSTALLER_FOLDER/Borogame.dmg" || fail "Stapling failed"
fi

if [ -f "$INSTALLER_FOLDER/Borogame.dmg" ]; then
    mv "$INSTALLER_FOLDER/Borogame.dmg" "$INSTALLER_FOLDER/Borogame-$VERSION.dmg"
fi

echo "==> DMG ready: $INSTALLER_FOLDER/Borogame-$VERSION.dmg"
