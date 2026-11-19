#!/usr/bin/env bash
# prepare-source.sh — assemble the full Borogame macOS source tree.
#
# Assembles: pinned upstream moonlight-qt (build/upstream/) + overlay/ files
# copied on top + patches/*.patch applied. Idempotent: always rebuilds the
# tree from scratch so repeated runs are deterministic.
#
# Usage:
#   scripts/prepare-source.sh            # assemble into build/upstream/
#   scripts/prepare-source.sh --verify   # assemble into a temp dir and exit
set -euo pipefail

fail() { echo "prepare-source: ERROR: $*" 1>&2; exit 1; }

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
VERIFY=0
[ "${1:-}" = "--verify" ] && VERIFY=1

# ---------------------------------------------------------------- upstream pin
read_pin() {
    python3 - "$ROOT/upstream.json" <<'PY'
import json, sys
pin = json.load(open(sys.argv[1]))
print(pin["repo"]); print(pin["ref"]); print(pin["commit"])
PY
}
{ read -r REPO; read -r REF; read -r COMMIT; } < <(read_pin) || fail "cannot parse upstream.json"
[ -n "$REPO" ] && [ -n "$REF" ] && [ -n "$COMMIT" ] || fail "incomplete upstream.json"

# ---------------------------------------------------------------- destination
if [ "$VERIFY" = 1 ]; then
    WORK="$(mktemp -d "${TMPDIR:-/tmp}/borogame-src.XXXXXX")"
    trap 'rm -rf "$WORK"' EXIT
else
    WORK="$ROOT/build"
    rm -rf "$WORK/upstream"
fi
DEST="$WORK/upstream"
mkdir -p "$WORK"

echo "==> Cloning upstream $REPO @ $REF ($COMMIT)"
git clone --depth 1 --branch "$REF" --recursive "$REPO" "$DEST" \
    || fail "git clone failed"
ACTUAL="$(git -C "$DEST" rev-parse HEAD)"
[ "$ACTUAL" = "$COMMIT" ] || fail "upstream commit mismatch: got $ACTUAL, pinned $COMMIT"

echo "==> Copying Borogame overlay"
cp -R "$ROOT/overlay/." "$DEST/"

echo "==> Applying patches"
for p in "$ROOT"/patches/*.patch; do
    [ -e "$p" ] || { echo "    (no patches)"; break; }
    echo "    $(basename "$p")"
    git -C "$DEST" apply --whitespace=nowarn "$p" || fail "patch failed: $p"
done

if [ "$VERIFY" = 1 ]; then
    echo "==> Verify OK: upstream + overlay + patches assemble cleanly"
    git -C "$DEST" diff --stat | tail -3 || true
else
    echo "==> Source tree ready at $DEST"
    echo "    Next: scripts/build-macos.sh"
fi
