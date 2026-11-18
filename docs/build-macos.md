# Building Borogame for macOS

Everything needed to produce `Borogame.app` / `Borogame.dmg` locally on an
Apple Silicon Mac.

## 1. Toolchain

| Requirement   | Version / source                                        |
|---------------|----------------------------------------------------------|
| macOS         | 13.0 or newer (Apple Silicon M1/M2/M3/M4)                |
| Xcode         | 15 or newer (`xcodebuild -version` to verify)            |
| Qt            | 6.8+ via Homebrew (`brew install qt`) **or** Qt 6.11 via [Qt Online Installer](https://www.qt.io/download-qt-installer) |
| create-dmg    | optional, nicer DMGs (`brew install node && npm i -g create-dmg`) |
| python3       | ships with macOS / Xcode CLT (used by upstream `setup-deps.py`) |

If qmake is not on `PATH` after a Homebrew Qt install:

```bash
export PATH="$(brew --prefix qt)/bin:$PATH"
```

## 2. Build

```bash
# 1) Assemble the full source tree: pinned upstream + overlay + patches
./scripts/prepare-source.sh

# 2) Compile (downloads upstream prebuilt macOS libs on first run)
./scripts/build-macos.sh

# 3) Package
./scripts/make-dmg.sh
# → build/dist/Borogame-<version>.dmg

# 4) Install locally for testing
./scripts/install-local.sh
```

What each script does:

- **prepare-source.sh** — `git clone --depth 1 --recursive` of the pinned
  moonlight-qt release (see `upstream.json`), copies `overlay/` on top,
  applies `patches/*.patch`. Add `--verify` to check the round trip in a
  temporary directory without touching `build/`.
- **build-macos.sh** — runs upstream's `setup-deps.py` (prebuilt SDL2/FFmpeg/
  OpenSSL/libplacebo bundle, cached in `build/upstream/libs/`), then
  `qmake` + `make` for **arm64 only**, producing `build/build-Release/app/Borogame.app`.
- **make-dmg.sh** — `macdeployqt` (bundles Qt runtime), dSYM extraction,
  optional signing/notarization (see below), `create-dmg` (or `hdiutil` fallback).
- **install-local.sh** — copies to `/Applications`, clears the quarantine
  attribute, verifies the signature, launches the app.

## 3. Code signing & notarization

Without a certificate the app is ad-hoc signed: it runs locally
(`install-local.sh`) but Gatekeeper will warn on other Macs.

With a **Developer ID Application** certificate (identity for this project:
`Developer ID Application: Pedram Gholizadeh`, org *webiro.ir*):

```bash
export SIGNING_IDENTITY="Developer ID Application: Pedram Gholizadeh"
./scripts/make-dmg.sh
```

Notarization (requires an app-specific password and one-time keychain
profile setup):

```bash
xcrun notarytool store-credentials borogame-notary \
    --apple-id you@example.com --password APP-SPECIFIC-PASSWORD --team-id TEAMID

export SIGNING_IDENTITY="Developer ID Application: Pedram Gholizadeh"
export NOTARY_KEYCHAIN_PROFILE=borogame-notary
./scripts/make-dmg.sh
```

CI performs the identical steps when these repository secrets exist:
`MACOS_CERT_P12` (base64 of the .p12), `MACOS_CERT_P12_PASSWORD`,
`APPLE_ID`, `APPLE_PASSWORD`, `APPLE_TEAM_ID`.

## 4. Troubleshooting

- **`Missing dependencies`** at qmake time → `setup-deps.py` didn't run;
  delete `build/upstream/libs` and re-run `build-macos.sh`.
- **Qt Quick Controls modules missing in the bundle** → make sure
  `macdeployqt` ran (step 3) — it imports QML from `app/gui`.
- **App crashes on launch after copy** → quarantine attribute; re-run
  `install-local.sh` (it strips `com.apple.quarantine`).
- **Patch fails to apply** → upstream drifted from the pinned commit;
  re-read AGENTS.md §5 and regenerate `patches/0001-borogame-integration.patch`.
