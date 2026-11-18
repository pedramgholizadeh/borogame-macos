# Manual Smoke Test — Borogame for macOS

Run on a real Apple Silicon Mac after `scripts/install-local.sh`.
Check items as you go; note failures with the app log
(`~/Library/Logs/Borogame/` or Console.app, filter: process "Borogame").

## 1. Launch & gate

- [ ] App launches without Gatekeeper warning (signed build) or after right-click → Open (unsigned)
- [ ] With no stored license, the **Borogame Login** page is shown on top of the PC grid
- [ ] Entering an invalid license shows *Invalid license key.*
- [ ] Entering a valid license shows *License accepted…*, then the assigned host IP is added automatically and the app returns to the PC grid
- [ ] Relaunching with a stored license skips the login page

## 2. Host & pairing

- [ ] Assigned host appears in the PC grid and turns **online**
- [ ] Clicking the unpaired host shows the pairing dialog with a PIN and pairs **without manual PIN entry** (auto-pair relay `pinPair` works)
- [ ] After pairing, the app grid lists the service apps

## 3. Streaming

- [ ] Launching a game starts a stream (video + audio)
- [ ] Keyboard/mouse input works; gamepad input works if attached
- [ ] Quitting the session returns to the app grid
- [ ] Fullscreen toggle (Cmd+F / Ctrl+Cmd+F) behaves

## 4. Service pages

- [ ] Toolbar shows Store / Tickets / Account buttons when signed in
- [ ] **Store** grid loads games with thumbnails from borogame.com
- [ ] Clicking a store game opens the SSO game page in the default browser
- [ ] **Tickets** lists tickets with correct status colors; empty state shows when none
- [ ] *New ticket* opens the WHMCS submit page (SSO)
- [ ] Clicking a ticket opens its WHMCS view page (SSO)
- [ ] **Account** shows nickname avatar, full name, email, cards count
- [ ] *Check my host* re-fetches `fetchHost` without errors
- [ ] *Sign out* clears the license and returns to the login page; Store/Tickets/Account toolbar buttons disappear
- [ ] Help button opens `borogame.com/faq.php`

## 5. Settings & branding

- [ ] About/settings pages open; app title in the menu bar reads **Borogame**
- [ ] Dock icon is the Borogame gamepad logo
- [ ] Settings changes (decoder, resolution, etc.) persist across relaunch
  (stored under org "Borogame", separate from upstream Moonlight settings)
- [ ] No "Moonlight" strings visible in normal user flows

## 6. Regression guard

- [ ] `scripts/prepare-source.sh --verify` passes
- [ ] `Add PC manually` still works for ad-hoc hosts (upstream path intact)
- [ ] App does not crash with network disabled (service pages show error/empty states)
