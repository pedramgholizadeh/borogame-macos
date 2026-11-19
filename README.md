# Borogame for macOS

[![Build macOS](https://github.com/pedramgholizadeh/borogame-macos/actions/workflows/build-macos.yml/badge.svg)](https://github.com/pedramgholizadeh/borogame-macos/actions/workflows/build-macos.yml)

A native **Apple Silicon (M-series) macOS client** for the
[Borogame](https://borogame.com) game-streaming service.

Borogame's existing Windows and Android clients are built on top of the excellent
open-source [Moonlight](https://github.com/moonlight-stream) streaming stack.
This project recreates the same client for macOS by building the **current
maintained upstream moonlight-qt** release (pinned in `upstream.json`) with a
thin, clean **Borogame integration layer** (service API, store, tickets,
login/SSO, auto-pairing) applied on top.

> ℹ️ The original Borogame source code was lost (the old GitHub repository was
> deleted years ago). Everything Borogame-specific in this port was recovered by
> forensic analysis of the shipped Android APK (`classes.dex` decompilation) and
> the Windows installer payload, and is documented in
> [`docs/api-contract.md`](docs/api-contract.md).

## ✨ Features

- Native **arm64** build for Apple Silicon Macs (M1/M2/M3/M4)
- Full Moonlight streaming core (HEVC/AV1, HDR, gamepads, low latency)
- **Borogame service integration**
  - Login with your Borogame license key / SSO token
  - Automatic host discovery via `api.php?fetchHost` — no manual host setup
  - Automatic pairing via `api.php?pinPair` — no PIN typing
  - In-app **Store** grid with game artwork
  - In-app **Support Tickets** (list & submit via the WHMCS backend)
  - Profile section and account info
- Clean-room modernization: based on upstream **moonlight-qt v6.2.0** (newer
  than the original Windows fork, which was built from Sep-2024 dependencies)

## ⚙️ Defaults for M1 + 20 Mbps

The client ships with a profile tuned for an Apple M1 MacBook on a 20 Mbps
connection: **1080p / 60 FPS**, a **10 Mbps adaptive bitrate** (50% link
headroom), frame pacing, the Metal renderer, a maximized window, and automatic
codec/decoder selection so M1 hardware decoding is used whenever the host
supports HEVC. The UI opens in **Persian** with full RTL layout.

The **Settings** page starts with a *Recommended for your Mac* card. Press
**Apply recommended settings** at any time to reapply the profile — useful on
machines that already stored older preferences.

## 🎨 Interface

The app uses a restrained near-black dark theme with a single muted violet
accent, minimal borders and generous spacing. All Persian and Latin text uses
the bundled [Vazirmatn](https://github.com/rastikerdar/vazirmatn) font
(SIL Open Font License), so the UI stays consistent on every Mac without
requiring a system font installation.

## 📥 Download

The published build is
[**v1.0.0**](https://github.com/pedramgholizadeh/borogame-macos/releases/tag/v1.0.0)
(download `Borogame-v1.0.0.dmg`). Every push to `main` also produces a fresh
`.dmg` artifact in the
[Actions](https://github.com/pedramgholizadeh/borogame-macos/actions) tab —
suitable for local testing via `scripts/install-local.sh`.

> **Gatekeeper note:** signed & notarized builds are produced when signing
> secrets are configured in CI. Unsigned builds must be installed with
> `scripts/install-local.sh` (strips the quarantine attribute) or opened via
> right-click → Open.

## 🏗️ Architecture

This repository is an **integration layer**, not a fork snapshot:

```
upstream.json   → pins moonlight-qt v6.2.0 (repo + tag + commit)
overlay/        → Borogame's own new files (C++ service layer, QML pages, assets)
patches/        → minimal surgical patches to upstream files (branding, hooks)
scripts/        → prepare-source.sh assembles the full tree, then build/package
```

Why this way: the streaming engine is huge and actively maintained upstream;
keeping our diff as *new files* + a *small patch* makes upstream version bumps
cheap and keeps the "port" auditable.

See [`AGENTS.md`](AGENTS.md) for the full working rules,
[`docs/build-macos.md`](docs/build-macos.md) for the toolchain, and
[`TASKS.md`](TASKS.md) for current project status.

## 🔨 Build from source (macOS, Apple Silicon)

```bash
# one-time toolchain (see docs/build-macos.md for details)
brew install qt create-dmg

# assemble + build + package
./scripts/prepare-source.sh
./scripts/build-macos.sh
./scripts/make-dmg.sh

# install to /Applications for local testing
./scripts/install-local.sh
```

## 🖥️ CI

GitHub Actions builds an arm64 `.dmg` automatically on every push and PR
([workflow](.github/workflows/build-macos.yml)). Code signing and notarization
are enabled automatically when the following secrets are present:

| Secret                         | Purpose                                  |
|--------------------------------|------------------------------------------|
| `MACOS_CERT_P12`               | Developer ID Application certificate     |
| `MACOS_CERT_P12_PASSWORD`      | Certificate password                     |
| `APPLE_ID` / `APPLE_PASSWORD`  | Notarization (App-specific password)     |
| `APPLE_TEAM_ID`                | Notarization team ID                     |

Identity used: **Developer ID Application: Pedram Gholizadeh** (webiro.ir).

## 📡 Backend API

The macOS client speaks the **exact same backend protocol** as the shipped
Android/Windows clients: `https://borogame.com/api.php` (`sso`, `license`,
`pinPair`, `tickets`, `fetchHost`, `store`), game listing and artwork from
`borogame.com/games.php` + thumbnail CDN, and WHMCS support tickets.
Full contract: [`docs/api-contract.md`](docs/api-contract.md).

## 📄 License

GPL-3.0-or-later — inherited from upstream moonlight-qt, which this port
derives from. If you distribute binaries you must provide the corresponding
source (upstream pinned commit + this repository).

## 🙏 Credits

- [moonlight-stream](https://github.com/moonlight-stream) — Moonlight & moonlight-common-c
- [Borogame](https://borogame.com) — the service this client is built for
- Pedram Gholizadeh ([webiro.ir](https://webiro.ir)) — Borogame macOS port
