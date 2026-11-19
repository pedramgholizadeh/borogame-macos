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

- [x] T1.1 Decompile official Android APK with jadx
  - *Result:* 1529 classes decompiled (`analysis/decompiled/`, 3 minor errors).
- [x] T1.2 Extract Borogame-specific classes into `reference/android/`
  - *Result:* `AutoPair`, `Store`, `TicketsActivity`, `ProfileSectionActivity`,
    `AddComputerManually` + provenance README committed.
- [x] T1.3 Document full HTTP contract in `docs/api-contract.md`
  - *Result:* all six endpoints documented with exact params/JSON shapes:
    `license` (validation+profile), `fetchHost` (`{ip}` / `"0"`), `pinPair`
    (`PIN`+`ip` relay), `store` (array-of-arrays), `tickets` (WHMCS objects),
    `sso` (web redirect); thumbnail URL scheme derived from title.
- [x] T1.4 Confirm Windows client uses the same endpoints (payload analysis)
  - *Result:* Windows payload is a moonlight-qt fork with `StoreGrids.qml`,
    `Tickets.qml`, WHMCS `submitticket.php`/`supporttickets.php`,
    `borogame.com/faq.php` — same service layer.

## Phase 2 — Port implementation

- [x] T2.1 `BoroApi` — Qt C++ HTTP client for all backend endpoints
  - *Result:* `overlay/app/boro/boroapi.{h,cpp}`; percent-encoded query building,
    15s transfer timeout, JSON parsing per contract, busy state, signals to QML.
- [x] T2.2 `BoroAuth` — login / SSO / license flows, token storage in QSettings
  - *Result:* folded into `BoroApi::validateLicense` (contract returns profile
    in the same call); license persisted via `QSettings` key `userLicense`
    under org/app `Borogame` (set by the upstream patch in `main.cpp`).
- [x] T2.3 `BoroHostManager` — `fetchHost` → add host → auto-pair via `pinPair`
  - *Result:* `fetchHost()` signal → QML calls `ComputerManager.addNewHostManually`;
    pairing relay `sendAutoPairPin(pin, ip)` hooked into `PcView.qml` right where
    upstream generates the PIN (needs the `activeAddress` role added to ComputerModel).
- [x] T2.4 `BoroStore` — store listing model + game thumbnails
  - *Result:* `BoroStorePage.qml` GridView, `api.php?store=true` parsed as
    array-of-arrays, thumbnail URLs derived from titles (space → underscore).
- [x] T2.5 `BoroTickets` — list + submit tickets (WHMCS)
  - *Result:* `BoroTicketsPage.qml` with status color chips
    (Open/Answered/Customer-Reply/Closed), SSO deep links per contract.
- [x] T2.6 QML: Login page, Store grid page, Tickets page, Profile section
  - *Result:* 4 pages committed; login gate pushed by `main.qml` when not signed in.
- [x] T2.7 QML: wire streaming launch to Borogame hosts
  - *Result:* no custom launch code needed — hosts added through upstream
    ComputerManager flow reuse the standard AppView/streaming path unchanged.
- [x] T2.8 Branding: app name, icons, SVG assets, help URL
  - *Result:* logo + toolbar icons (Material style), `borogame.icns` generated,
    About dialog left upstream for now (low priority, tracked as future work).
- [x] T2.9 macOS app bundle: `Info.plist`, versioning, `Borogame.icns`
  - *Result:* bundle id `com.borogame.Borogame`, display name, icon file
    updated via the integration patch; version stays upstream's `version.txt`.
- [x] T2.10 Upstream patch `0001-borogame-integration.patch` (minimal, surgical)
  - *Result:* 9 files, +131/−18, every anchor asserted exactly-once during
    generation to catch upstream drift loudly.
- [x] T2.11 `prepare-source.sh --verify` passes against pinned upstream
  - *Result:* round-trip verified on fresh assembly (see Phase 3 T3.1).

## Phase 3 — Build & packaging

- [x] T3.1 `scripts/prepare-source.sh` (assemble upstream + overlay + patches)
  - *Result:* deterministic re-assembly, `--verify` mode for CI/pre-commit checks.
- [x] T3.2 `scripts/build-macos.sh` (qmake build, arm64, release)
  - *Result:* reuses upstream prebuilt libs (`setup-deps.py`, cached), arm64 only,
    `disable-discord-rpc`.
- [x] T3.3 `scripts/make-dmg.sh` (app bundle → DMG, optional signing/notarization)
  - *Result:* macdeployqt + dSYM + `create-dmg` (hdiutil fallback) +
    `SIGNING_IDENTITY` / `NOTARY_KEYCHAIN_PROFILE` env support.
- [x] T3.4 `scripts/install-local.sh` (quarantine-free local install for testing)
  - *Result:* /Applications install + `xattr -dr com.apple.quarantine` +
    `codesign --verify` + launch.
- [x] T3.5 Local build verified on this Mac (Apple Silicon, Xcode 27.2)
  - *Result:* full pipeline green locally on an Apple M1: `Borogame.app` built
    (arm64, Qt 6.12 Homebrew), `Borogame-6.2.0.dmg` produced (104 MB), app
    launches, QML loads, BoroApi registers, Metal/HEVC hardware decode OK.
    Fixed en route: git exec-bit on scripts, create-dmg CLI collision
    (Homebrew ships a same-named different tool).

## Phase 4 — CI/CD

- [x] T4.1 GitHub Actions: macOS arm64 build on push/PR (artifact DMG)
  - *Result:* `.github/workflows/build-macos.yml`, macos-26 + Qt 6.11.2
    (same pins as upstream's own macOS CI).
- [x] T4.2 Conditional code signing + notarization via repository secrets
  - *Result:* `MACOS_CERT_P12`(+password) → temp keychain + `SIGNING_IDENTITY`;
    `APPLE_ID`/`APPLE_PASSWORD`/`APPLE_TEAM_ID` → notarytool profile; steps
    self-skip when secrets are absent.
- [x] T4.3 Tag `v*` → GitHub Release with DMG attached
  - *Result:* separate `release` job using `gh release create`, published
    (not draft) since the requested deliverable is a downloadable release.
- [x] T4.4 First automated build green on GitHub
  - *Result:* run #44425748057 (d0f7c2e) passed end-to-end; artifact
    `Borogame-macos-arm64` (74 MB DMG) attached to the run.

## Phase 5 — Release

- [ ] T5.1 Manual smoke test on local Mac per `test/smoke-test.md`
      (needs a real Borogame license + assigned host — service-team task;
      partial coverage from T6.1 live API validation)
- [x] T5.2 Push repo to https://github.com/pedramgholizadeh/borogame-macos.git
  - *Result:* `main` pushed; CI green; artifact DMG downloadable from Actions.
- [~] T5.3 Tag first release `v1.0.0`, publish DMG download link in README
      (signing secrets `MACOS_CERT_P12` etc. would enable a notarized DMG)

## Phase 6 — Live validation & Persian localization

- [x] T6.1 Debug "invalid license" reported with a real panel-issued key
  - *Result:* root cause — backend returns `"validation":"true"` as a STRING
    (org.json coerces it on Android; Qt `toBool()` does not). Fixed in
    `BoroApi::validateLicense` (+ string `cards`). Verified live: key from the
    Borogame panel now validates via `test/run-api-smoke.sh`
    (PASS license + PASS fetchHost). Contract doc updated with real
    response shape (`fName`/`lName`/`phone` fields, string `cards`).
- [x] T6.2 Full Persian (فارسی) localization
  - *Result:* `overlay/app/languages/qml_fa.ts` + compiled `qml_fa.qm`
    (283/283 strings translated, 0 unfinished) covering every user-facing
    string incl. streaming errors and settings; Persian added to the
    language enum (`LANG_FA`), suffix map, and settings dropdown; **default
    language set to Persian** for Borogame users; RTL mirroring enabled for
    fa/ckb/he via `QGuiApplication::setLayoutDirection`; upstream "Moonlight"
    strings localized as "بوروگیم". Wired via
    `patches/0002-borogame-localization.patch` (5 files, +105).
- [~] T6.3 Rebuild + verify Persian UI end-to-end on this Mac
  - *Result:* rebuild with the final patch set in progress.

## Phase 7 — Settings UX & tuned defaults

- [x] T7.1 Polished settings page with one-click Borogame profile
  - *Result:* new `overlay/app/gui/BoroSettingsQuickProfile.qml` — full-width
    accent card at the top of Settings with translated 1080p60 / 10 Mbps /
    Metal chips and an **Apply recommended settings** button that also fixes
    machines with older saved preferences. Hooked into upstream via the
    surgical `patches/0004-settings-profile-card.patch`; RTL anchor mirroring
    added. Persian translations extended to 291/291 strings.
- [x] T7.2 Defaults tuned for Apple M1 + 20 Mbps internet
  - *Result:* `patches/0003-borogame-m1-tuning.patch` — upstream's LAN-oriented
    default bitrate halved (1080p60 → 10 Mbps, 50% link headroom), frame pacing
    on, Metal renderer, borderless/maximized UI defaults. Codec/decoder stay on
    **Auto** so hosts without HEVC/AV1 still work, while M1 uses its hardware
    path automatically. Documented in README.
- [~] T7.3 Local DMG build + visual verification of settings & RTL
- [~] T7.4 Publish the same commit as a GitHub Release (`v1.0.0`)
