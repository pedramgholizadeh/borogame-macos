# TASKS.md — Borogame for macOS

> Live task board. Update status as work happens:
> `[ ]` todo · `[~]` in progress · `[x]` done.
> Every completed task gets a short *Result:* note describing what was actually
> delivered (files, decisions, caveats).

---

## Phase 0 — Project foundation

- [x] T0.1 Initialize git repository, branch `main`, `.gitignore`, directory skeleton
  - *Result:* repo scaffolded at `borogame-macos`, layout per AGENTS.md §4.
- [x] T0.2 Write governance docs: `AGENTS.md`, `TASKS.md`, `README.md`, `CONTRIBUTING.md`, `LICENSE`
  - *Result:* GPL-3.0 license adopted (upstream inheritance), full English docs.
- [x] T0.3 Pin upstream moonlight-qt release in `upstream.json`
  - *Result:* pinned `v6.2.0` @ `de2467e4` (latest stable, official macOS support).

## Phase 1 — Recover the Borogame backend contract

- [~] T1.1 Decompile official Android APK with jadx
  - *Result:* decompilation running; custom classes identified:
    `Store`, `TicketsActivity`, `ProfileSectionActivity`, `AutoPair`,
    `PosterContentProvider`.
- [ ] T1.2 Extract Borogame-specific classes into `reference/android/`
- [ ] T1.3 Document full HTTP contract in `docs/api-contract.md`
      (endpoints `api.php?sso|license|pinPair|tickets|fetchHost|store`,
      request params, response shapes, thumbnails, WHMCS ticket endpoints)
- [ ] T1.4 Confirm Windows client uses the same endpoints (payload analysis)
  - *Result:* preliminary analysis done — Windows client is a moonlight-qt fork
    with `StoreGrids.qml`, `Tickets.qml`, `borogame.com/faq.php` help URL,
    WHMCS `submitticket.php`/`supporttickets.php` references.

## Phase 2 — Port implementation

- [ ] T2.1 `BoroApi` — Qt C++ HTTP client for all backend endpoints
- [ ] T2.2 `BoroAuth` — login / SSO / license flows, token storage in QSettings
- [ ] T2.3 `BoroHostManager` — `fetchHost` → add host → auto-pair via `pinPair`
- [ ] T2.4 `BoroStore` — store listing model + game thumbnails cache
- [ ] T2.5 `BoroTickets` — list + submit tickets (WHMCS)
- [ ] T2.6 QML: Login page, Store grid page, Tickets page, Profile section
- [ ] T2.7 QML: wire streaming launch (app view → session) to Borogame hosts
- [ ] T2.8 Branding: app name, icons, SVG assets, About dialog, help URL
- [ ] T2.9 macOS app bundle: `Info.plist`, versioning, `Borogame.icns`
- [ ] T2.10 Upstream patch `0001-borogame-integration.patch` (minimal, surgical)
- [ ] T2.11 `prepare-source.sh --verify` passes against pinned upstream

## Phase 3 — Build & packaging

- [ ] T3.1 `scripts/prepare-source.sh` (assemble upstream + overlay + patches)
- [ ] T3.2 `scripts/build-macos.sh` (qmake/CMake build, arm64, release)
- [ ] T3.3 `scripts/make-dmg.sh` (app bundle → DMG, optional signing/notarization)
- [ ] T3.4 `scripts/install-local.sh` (quarantine-free local install for testing)
- [ ] T3.5 Local build verified on this Mac (Apple Silicon, Xcode 27.2)

## Phase 4 — CI/CD

- [ ] T4.1 GitHub Actions: macOS arm64 build on push/PR (artifact DMG)
- [ ] T4.2 Conditional code signing + notarization via repository secrets
- [ ] T4.3 Tag `v*` → GitHub Release draft with DMG attached
- [ ] T4.4 First automated build green on GitHub

## Phase 5 — Release

- [ ] T5.1 Manual smoke test on local Mac per `test/smoke-test.md`
- [ ] T5.2 Push repo to https://github.com/pedramgholizadeh/borogame-macos.git
- [ ] T5.3 Tag first release `v1.0.0`, publish DMG download link in README
