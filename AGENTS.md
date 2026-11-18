# AGENTS.md — Working Guide for AI Agents & Contributors

This document tells any AI agent (or human contributor) how to work inside this
repository **correctly**. Read it fully before making any change.

## 1. What this project is

**Borogame for macOS** is a native Apple Silicon (arm64) port of the Borogame
game-streaming client. Borogame (https://borogame.com) is a game-streaming
service whose Android and Windows clients were built as forks of the open-source
[Moonlight](https://github.com/moonlight-stream) clients. This repository
recreates that client for macOS on the current, maintained upstream
**moonlight-qt** release.

We do **not** vendor upstream into git. Instead:

```
upstream.json          pins the exact moonlight-qt release we track
overlay/               Borogame's own NEW files (copied over upstream at build time)
patches/               small .patch files for the few upstream files we must MODIFY
scripts/               assemble + build + package + install
```

`scripts/prepare-source.sh` assembles a full working source tree in `upstream/`
(clean git clone of the pinned commit + overlay copied on top + patches applied).
That directory is build scratch space and is **git-ignored** — never commit it.

## 2. Golden rules

1. **Never edit files inside `upstream/`.** It is generated. If you need a
   different behavior in an upstream file, either:
   - add a *new* file under `overlay/` (preferred), or
   - regenerate `patches/*.patch` against the pinned commit (see §5).
2. **Every task goes through `TASKS.md`.** When you start work, mark the task
   `in progress`; when done, check it off and add a one-line note of what was
   done. This file is the single source of truth for project state.
3. **One logical change per commit.** Follow Conventional Commits
   (`feat:`, `fix:`, `docs:`, `build:`, `ci:`, `chore:`, `refactor:` …).
   Subject line ≤ 72 chars, imperative mood, no period. Example:
   ```
   feat(boro): add tickets page with WHMCS ticket submission
   ```
4. **All Markdown documentation is written in English.**
5. **The API contract is sacred.** The macOS client must talk to the exact same
   backend endpoints and parameters as the shipped Android/Windows clients.
   The contract is documented in `docs/api-contract.md` and was recovered by
   decompiling the official Android APK (see `reference/android/`). If the
   backend changes, update `docs/api-contract.md` first, then the code.
6. **Licensing.** Upstream moonlight-qt is GPL-3.0-or-later. Any distribution
   of this port must ship source (or a written offer) for the full
   corresponding source: upstream at the pinned commit + this repository.
   Keep the `LICENSE` file intact.
7. **Secrets never enter git.** Code-signing certificates, notarization
   credentials, API tokens: use environment variables or GitHub Actions
   secrets only. `.gitignore` blocks `*.p12`, `.env`, `secrets/`.

## 3. Daily commands

```bash
# Assemble the full source tree (idempotent, safe to re-run)
./scripts/prepare-source.sh

# Build the app (requires Qt 6 + Xcode; see docs/build-macos.md)
./scripts/build-macos.sh

# Package a .dmg from the built app
./scripts/make-dmg.sh

# Install the freshly built app into /Applications on this Mac
./scripts/install-local.sh
```

## 4. Repository layout

| Path                        | Purpose                                            |
|-----------------------------|----------------------------------------------------|
| `upstream.json`             | Pin: repo, tag, commit of moonlight-qt             |
| `overlay/app/boro/`         | C++/Qt service layer (BoroApi, pairing, session)   |
| `overlay/app/gui/`          | Borogame QML pages (Store, Tickets, Login…)        |
| `overlay/app/res/`          | Borogame branding assets (logo, icons, SVG)        |
| `patches/`                  | Minimal patches to upstream files (branding/hooks) |
| `scripts/`                  | prepare / build / package / install scripts        |
| `docs/api-contract.md`      | Backend HTTP contract (recovered from APK)         |
| `docs/build-macos.md`       | Toolchain setup + build + signing                  |
| `reference/android/`        | Decompiled Borogame-specific classes from the APK  |
| `.github/workflows/`        | CI: automatic macOS arm64 builds                   |
| `test/`                     | Manual smoke-test checklist to run on a real Mac   |
| `TASKS.md`                  | Live task board — update it with every change      |

## 5. How to change an upstream file (the patch workflow)

1. `./scripts/prepare-source.sh` → full tree appears in `upstream/`.
2. Edit the files in `upstream/` as needed.
3. Regenerate the patch:
   ```bash
   cd upstream
   git diff > ../patches/0001-borogame-integration.patch
   ```
4. Verify round-trip: `./scripts/prepare-source.sh --verify` must succeed on a
   fresh assembly.
5. Commit the patch file (never the `upstream/` tree itself).

Keep patches **small and surgical** — every upstream file we patch is future
merge friction when we bump the pinned version. Prefer new files.

## 6. Code conventions

- C++/Qt code follows upstream moonlight-qt style (tabs for indentation in
  `.cpp/.h`, same include ordering, `qCWarning`/`qCDebug` categories).
- QML follows upstream indentation (4 spaces).
- Network calls live **only** in `overlay/app/boro/` classes — never put
  `QNetworkAccessManager` calls inside QML or session/streaming code.
- All user-facing strings in Borogame-specific UI start with plain English and
  may be wrapped with `qsTr()`.
- Logging prefix for our modules: `boro`.

## 7. CI / release

- `.github/workflows/build-macos.yml` builds an unsigned (or signed, when
  secrets are present) arm64 `.dmg` on every push to `main` and on tags
  `v*`. Artifacts are downloadable from the Actions run page.
- Tags trigger a GitHub Release draft with the DMG attached.
- Signing identity: `Developer ID Application: Pedram Gholizadeh` (org
  *webiro.ir*). Without secrets, CI produces an ad-hoc signed build that
  macOS Gatekeeper will flag — install with `scripts/install-local.sh`
  (removes quarantine) for local testing.

## 8. Before you finish any session

- [ ] `TASKS.md` updated (status + result notes)
- [ ] `docs/` updated if behavior or the API changed
- [ ] Commits are conventional, atomic, and build-ready
- [ ] `prepare-source.sh --verify` passes
