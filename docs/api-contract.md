# Borogame Backend API Contract

This document is the **authoritative** description of how Borogame clients
(Android, Windows, macOS) talk to the Borogame service. It was recovered by
decompiling the official Borogame Android client (`borogame.apk`, classes
`com.limelight.{Store,TicketsActivity,ProfileSectionActivity,AutoPair}`,
`com.limelight.preferences.AddComputerManually`) and cross-checking strings in
the shipped Windows client (`Borogame.exe`, a moonlight-qt fork).

The macOS port **must** remain byte-compatible with this contract.
Reference decompiled sources: [`reference/android/`](../reference/android/).

## Base URLs

| Purpose         | URL                                                          |
|-----------------|--------------------------------------------------------------|
| API gateway     | `https://borogame.com/api.php`                               |
| Game pages      | `https://borogame.com/games.php/<Title_underscored>`         |
| Game thumbnails | `https://borogame.com/templates/borogame/dist/images/thumb/<Title_underscored>.jpg` |
| Help / FAQ      | `https://borogame.com/faq.php`                               |

`<Title_underscored>` = game title with spaces replaced by `_`.

## Credential model

The only client credential is the **license key** (a.k.a. `userLicense`).
It is obtained from the Borogame website and validated once through the
`license` endpoint, then persisted locally.

- Android storage: `SharedPreferences("AppPreferences")`, key `"userLicense"`
- macOS storage: `QSettings` (org `Borogame`, app `Borogame`), key `"userLicense"`

## Endpoints

### 1. Validate license + fetch profile — `GET api.php?license=<KEY>`

Used for both validation and profile display. Returns a JSON **object**
(verified against the live backend, Nov 2026):

```json
{
  "validation": "true",
  "fName": "پدرام",
  "lName": "قلی زاده",
  "fullName": "پدرام قلی زاده",
  "email": "user@example.com",
  "phone": "912xxxxxx",
  "nic": "پ‌ق‌ز",
  "cards": "0"
}
```

> ⚠️ **Gotcha:** `validation` arrives as the **string** `"true"` / `"false"`,
> and `cards` is a string too. The Android client survives this because
> `org.json`'s `getBoolean()` coerces `"true"`. In Qt, use
> `value == "true"` (case-insensitive) — a plain `QJsonValue::toBool()`
> returns **false** for the string form.

| Field        | Type    | Meaning                          |
|--------------|---------|----------------------------------|
| `validation` | string  | `"true"` if the license is valid |
| `fName`/`lName` | string | first / last name              |
| `fullName`   | string  | full name                        |
| `email`      | string  | e-mail address                   |
| `phone`      | string  | mobile number                    |
| `nic`        | string  | user nickname (avatar text)      |
| `cards`      | string  | number of credit cards / balance units |

Client behavior (Android `ProfileSectionActivity`):
1. If `validation == "true"` → persist the license, then treat the *same*
   response as the profile payload and display the fields.
2. Otherwise → show *invalid license*.
3. Transport/parse failure → show *validation error*.

### 2. Fetch assigned host — `GET api.php?fetchHost=<KEY>`

Returns a JSON **object**:

```json
{ "ip": "10.10.0.42" }
```

- `"ip" != "0"` → the service has assigned a host. The client immediately adds
  it as a host (manual add, port defaults to `47989`) so the user sees it in
  the PC grid.
- `"ip" == "0"` → no host assigned yet. The client shows a *no playground*
  state whose CTA opens the browser at
  `api.php?sso=<KEY>&path=games.php`.

### 3. Auto-pairing relay — `GET api.php?pinPair=<PIN>&ip=<HOST_IP>`

Sent right before running the standard Moonlight PIN pairing:

1. Client generates the PIN locally (same 4-digit generator as Moonlight).
2. Client fires a **fire-and-forget** GET to
   `api.php?pinPair=<PIN>&ip=<active host IP>` (IP without port).
3. Client immediately starts the standard Moonlight pairing flow with the
   same PIN. The Borogame backend relays the PIN to the designated host so
   the user never types it.

Response body is ignored; HTTP success is logged only.

### 4. Store listing — `GET api.php?store=true`

Returns a JSON **array of arrays**:

```json
[
  ["[Game Title]", "<unused column>", "https://borogame.com/games.php/Game_Title"],
  ...
]
```

- Column 0: title (may contain literal `[`, `]`, `"` — the client strips them)
- Column 2: canonical game page URL
- Thumbnail: derived from the title —
  `templates/borogame/dist/images/thumb/<Title_underscored>.jpg`
- Click action (with license): open browser at
  `api.php?sso=<KEY>&path=games.php/<Title_underscored>`
- Click action (without license): open browser at `games.php/<Title_underscored>`

### 5. Support tickets — `GET api.php?tickets=<KEY>`

Returns a JSON **array of objects** (WHMCS support tickets):

```json
[
  {
    "subject": "Cannot connect",
    "deptname": "پشتیبانی فنی",
    "status": "Open",
    "date": "2025-01-14",
    "url": "viewticket.php&tid=1234"
  }
]
```

Statuses observed: `Open`, `Answered`, `Customer-Reply`, `Closed`.
Departments observed: `پشتیبانی فنی` (technical), `پشتیبانی محصول` (product),
`امور مالی` (finance).

- Empty array → show *no tickets* state.
- Ticket click → browser at `api.php?sso=<KEY>&path=api.php?<ticket.url>`
- New ticket → browser at `api.php?sso=<KEY>&path=submitticket.php`

### 6. Web SSO — `GET api.php?sso=<KEY>&path=<WEB_PATH>`

Not a data API: opening this URL in a browser logs the user into the
Borogame website using their license key and forwards to `path`
(e.g. `games.php/Title`, `submitticket.php`, `faq.php/`).

## Error handling rules

- All requests are plain GET, no auth headers, response is UTF-8 JSON.
- Network failure never crashes the client: show a non-blocking error and
  keep the UI usable.
- Missing/invalid license only gates the *service pages* (store, tickets,
  profile, host fetching) — the underlying Moonlight UI (manual host add,
  settings) stays functional.

## macOS implementation mapping

| Contract element            | macOS implementation                       |
|-----------------------------|--------------------------------------------|
| HTTP client                 | `BoroApi` (`overlay/app/boro/boroapi.cpp`) |
| License persistence         | `QSettings` key `userLicense`              |
| Store model                 | `BoroApi::fetchStore()` → `storeReady`     |
| Tickets model               | `BoroApi::fetchTickets()` → `ticketsReady` |
| Host fetch + add            | `BoroApi::fetchHost()` + `ComputerManager.addNewHostManually()` |
| Auto-pair relay             | `BoroApi::sendAutoPairPin()` hooked in `PcView.qml` |
| SSO / web pages             | `Qt.openUrlExternally()` (default browser) |
