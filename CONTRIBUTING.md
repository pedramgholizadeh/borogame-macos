# Contributing to Borogame for macOS

Thank you for helping build the Borogame macOS client! This document explains
how to set up, code, and submit changes so the project stays clean and
maintainable.

## 1. Prerequisites

- macOS on Apple Silicon (M1 or newer) for local testing
- Xcode 15+ (`xcode-select --install`, full Xcode for the Qt build)
- [Homebrew](https://brew.sh)
- Qt 6 (`brew install qt`), `create-dmg` (`brew install create-dmg`)
- Read [`AGENTS.md`](AGENTS.md) — it defines the working rules for this repo
  and applies to human contributors too.

## 2. Development workflow

```bash
git clone https://github.com/pedramgholizadeh/borogame-macos.git
cd borogame-macos

./scripts/prepare-source.sh   # assemble upstream + overlay + patches
./scripts/build-macos.sh      # build Borogame.app
./scripts/install-local.sh    # install & launch locally
```

The full assembled source tree lives in `upstream/` (git-ignored). **Never
edit it directly for permanent changes** — see AGENTS.md §5 for the patch
workflow.

## 3. Branching & commits

- Branch from `main`; keep PRs small and focused.
- **Conventional Commits** are enforced by convention:
  - `feat:` new feature (`feat(boro): add tickets page`)
  - `fix:` bug fix
  - `docs:` documentation only
  - `build:` build system/scripts
  - `ci:` GitHub Actions
  - `refactor:`, `chore:`, `test:`
- Subject ≤ 72 chars, imperative, no trailing period.

## 4. Code style

- **C++/Qt**: follow moonlight-qt style — tabs, same include order, Qt naming.
  Network code only inside `overlay/app/boro/`.
- **QML**: 4-space indent, follow upstream component patterns.
- **Docs**: English only. Keep `TASKS.md` updated with every change.
- Keep Borogame-specific code **isolated** in `overlay/` and `patches/` —
  reviewers should always be able to tell upstream code from Borogame code.

## 5. Backend compatibility

The client must remain protocol-compatible with the live Borogame backend.
If you touch anything HTTP-related:

1. Read and update [`docs/api-contract.md`](docs/api-contract.md) first.
2. Test against the real endpoints; never invent response fields.
3. Handle failure modes gracefully (network loss, expired license, offline
   host) — the streaming session must never be blocked by service-layer errors.

## 6. Testing

- Minimum bar before opening a PR: `prepare-source.sh --verify` + a successful
  local build + app launches and can reach the login screen.
- Streaming tests need a real Borogame host/pairing — coordinate with the
  maintainers (or use a local Sunshine host) and record results in
  `test/smoke-test.md`.

## 7. Licensing

By contributing you agree your work is released under the repository license
(**GPL-3.0-or-later**). Do not commit code with incompatible licenses, and
never commit proprietary Borogame assets unless you hold the rights (this repo
is maintained by the Borogame team).

## 8. Reporting issues

Use GitHub Issues. Include: macOS version, Mac model (Apple Silicon chip),
build artifact hash, logs (`~/Library/Logs/Borogame/`) and the backend
endpoint that failed, if applicable.
